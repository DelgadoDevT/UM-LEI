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
	"time"

	"cc.2526/internal/common"
	"cc.2526/internal/network"
	"cc.2526/internal/serialization"
)

// TelemetrySender handles telemetry data transmission from rovers to the mothership.
// It maintains a persistent connection, sends periodic telemetry updates, and manages
// automatic reconnection in case of network failures.
type TelemetrySender struct {
	socket   string                   // Server address for telemetry transmission
	conn     net.Conn                 // Active network connection to mothership
	rover    common.TelemetryProvider // Rover instance providing telemetry data
	roverId  uint8                    // Rover identifier for logging and tracking
	running  bool                     // Indicates if the sender is actively running
	stopChan chan struct{}            // Channel for graceful shutdown signaling
}

// NewTelemetrySender creates a new telemetry sender for the specified rover.
// Parameters:
//   - rover: TelemetryProvider instance that generates telemetry data
//   - serverAddr: Mothership server address (host:port) for telemetry transmission
//
// Returns: Configured TelemetrySender ready to start
func NewTelemetrySender(rover common.TelemetryProvider, serverAddr string) *TelemetrySender {
	return &TelemetrySender{
		socket:   serverAddr,
		rover:    rover,
		roverId:  rover.GetTelemetryData().RoverId,
		running:  false,
		stopChan: make(chan struct{}),
	}
}

// Start establishes connection to the telemetry server and begins transmission.
// Returns error if initial connection cannot be established.
func (ts *TelemetrySender) Start() error {
	var err error
	ts.conn, err = net.Dial("tcp", ts.socket)
	if err != nil {
		log.Printf("Rover %d: Failed to connect to telemetry server: %v", ts.roverId, err)
		return err
	}

	log.Printf("Rover %d: TelemetryStream connected to %s", ts.roverId, ts.socket)

	ts.running = true
	go ts.sendLoop()

	return nil
}

// sendLoop is the main transmission loop that sends telemetry at regular intervals.
// Handles both periodic transmission and graceful shutdown signals.
func (ts *TelemetrySender) sendLoop() {
	ticker := time.NewTicker(network.TelemetryInterval)
	defer ticker.Stop()
	defer ts.conn.Close()

	for {
		select {
		case <-ts.stopChan:
			log.Printf("Rover %d: Telemetry sender stopping", ts.roverId)
			return
		case <-ticker.C:
			if err := ts.sendTelemetry(); err != nil {
				log.Printf("Rover %d: Error sending telemetry: %v", ts.roverId, err)
				if ts.reconnect() != nil {
					return
				}
			}
		}
	}
}

// sendTelemetry collects current telemetry data, serializes it, and transmits to mothership.
// Validates packet size before transmission to ensure protocol compliance.
// Returns error if transmission fails or packet size is invalid.
func (ts *TelemetrySender) sendTelemetry() error {
	telemetryData := ts.rover.GetTelemetryData()
	serializedData := serialization.SerializeTelemetryData(telemetryData)

	if len(serializedData) != network.TelemetryPacketSize {
		log.Printf("Rover %d: Invalid telemetry packet size: %d", ts.roverId, len(serializedData))
		return network.ErrInvalidDataLength
	}

	_, err := ts.conn.Write(serializedData)
	if err != nil {
		return err
	}

	return nil
}

// reconnect attempts to re-establish connection to the telemetry server.
// Implements fixed-delay retry with maximum attempt limit.
// Returns error if all reconnection attempts fail.
func (ts *TelemetrySender) reconnect() error {
	if ts.conn != nil {
		ts.conn.Close()
	}

	log.Printf("Rover %d: Attempting to reconnect to telemetry server...", ts.roverId)

	for i := 0; i < network.MaxReconnectTries; i++ {
		select {
		case <-ts.stopChan:
			return network.ErrConnectionFailed
		case <-time.After(network.ReconnectDelay):
			var err error
			ts.conn, err = net.Dial("tcp", ts.socket)
			if err == nil {
				log.Printf("Rover %d: Reconnected to telemetry server", ts.roverId)
				return nil
			}
			log.Printf("Rover %d: Reconnection attempt %d/%d failed: %v",
				ts.roverId, i+1, network.MaxReconnectTries, err)
		}
	}

	log.Printf("Rover %d: Failed to reconnect after %d attempts",
		ts.roverId, network.MaxReconnectTries)
	return network.ErrConnectionFailed
}

// Stop gracefully shuts down the telemetry sender, closing connections and stopping the transmission loop.
func (ts *TelemetrySender) Stop() {
	if ts.running {
		ts.running = false
		close(ts.stopChan)
		if ts.conn != nil {
			ts.conn.Close()
		}
		log.Printf("Rover %d: Telemetry sender stopped", ts.roverId)
	}
}
