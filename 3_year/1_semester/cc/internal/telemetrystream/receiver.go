// Package telemetrystream handles real-time telemetry data transmission between rovers and the mothership.
// It provides both sending (TelemetrySender) and receiving (TelemetryReceiver) components for
// bidirectional telemetry communication over TCP connections.
//
// The package ensures reliable data transmission with automatic reconnection capabilities
// and fixed-size packet formatting for consistent network communication.
package telemetrystream

import (
	"log"
	"net"
	"sync"

	"cc.2526/internal/common"
	"cc.2526/internal/network"
	"cc.2526/internal/serialization"
)

// TelemetryReceiver handles incoming telemetry data from multiple rovers on the mothership side.
// It accepts connections from rovers, processes telemetry packets, and distributes data to consumers.
type TelemetryReceiver struct {
	listener   net.Listener              // TCP listener for incoming rover connections
	clients    map[net.Conn]bool         // Active rover connections
	clientsMux sync.Mutex                // Mutex for thread-safe client map access
	running    bool                      // Indicates if the receiver is actively running
	stopChan   chan struct{}             // Channel for graceful shutdown signaling
	DataChan   chan common.TelemetryData // Channel for distributing received telemetry data
}

// NewTelemetryReceiver creates a new telemetry receiver ready to accept rover connections.
// Returns: Configured TelemetryReceiver with initialized data channel
func NewTelemetryReceiver() *TelemetryReceiver {
	return &TelemetryReceiver{
		clients:  make(map[net.Conn]bool),
		running:  false,
		stopChan: make(chan struct{}),
		DataChan: make(chan common.TelemetryData, 100),
	}
}

// Start begins listening for incoming rover connections on the telemetry server port.
// Returns error if the listener cannot be started.
func (tr *TelemetryReceiver) Start() error {
	var err error
	tr.listener, err = net.Listen("tcp", network.TelemetryServerPort)
	if err != nil {
		log.Printf("Failed to start telemetry receiver: %v", err)
		return err
	}

	log.Printf("Telemetry receiver listening on %s", network.TelemetryServerPort)

	tr.running = true
	go tr.acceptLoop()

	return nil
}

// acceptLoop continuously accepts new rover connections and spawns handler goroutines.
// Runs until the receiver is stopped or listener encounters a fatal error.
func (tr *TelemetryReceiver) acceptLoop() {
	for tr.running {
		conn, err := tr.listener.Accept()
		if err != nil {
			select {
			case <-tr.stopChan:
				return
			default:
				log.Printf("Error accepting connection: %v", err)
				continue
			}
		}

		log.Printf("New telemetry client connected: %s", conn.RemoteAddr())

		tr.clientsMux.Lock()
		tr.clients[conn] = true
		tr.clientsMux.Unlock()

		go tr.handleClient(conn)
	}
}

// handleClient processes telemetry data from a single rover connection.
// Reads fixed-size packets, deserializes data, and forwards to the data channel.
// Automatically cleans up connection resources when client disconnects.
func (tr *TelemetryReceiver) handleClient(conn net.Conn) {
	defer func() {
		conn.Close()
		tr.clientsMux.Lock()
		delete(tr.clients, conn)
		tr.clientsMux.Unlock()
		log.Printf("Telemetry client disconnected: %s", conn.RemoteAddr())
	}()

	buffer := make([]byte, network.TelemetryPacketSize)

	for {
		select {
		case <-tr.stopChan:
			return
		default:
			n, err := conn.Read(buffer)
			if err != nil {
				return
			}

			if n != network.TelemetryPacketSize {
				log.Printf("Invalid packet size from %s: expected %d, got %d",
					conn.RemoteAddr(), network.TelemetryPacketSize, n)
				continue
			}

			telemetryData := serialization.DeserializeTelemetryData(buffer)

			select {
			case tr.DataChan <- telemetryData:
			default:
				log.Printf("Telemetry data channel full, dropping packet from rover %d", telemetryData.RoverId)
			}

			log.Printf("Received telemetry from rover %d", telemetryData.RoverId)
		}
	}
}

// Stop gracefully shuts down the telemetry receiver, closing all connections and stopping the listener.
// Also closes the data channel to signal consumers that no more data will be received.
func (tr *TelemetryReceiver) Stop() {
	if tr.running {
		tr.running = false
		close(tr.stopChan)

		if tr.listener != nil {
			tr.listener.Close()
		}

		tr.clientsMux.Lock()
		for conn := range tr.clients {
			conn.Close()
		}
		tr.clients = make(map[net.Conn]bool)
		tr.clientsMux.Unlock()

		close(tr.DataChan)
		log.Println("Telemetry receiver stopped")
	}
}

// GetTelemetryData returns a read-only channel for receiving telemetry data from all connected rovers.
// Consumers should monitor this channel for incoming telemetry packets.
func (tr *TelemetryReceiver) GetTelemetryData() <-chan common.TelemetryData {
	return tr.DataChan
}
