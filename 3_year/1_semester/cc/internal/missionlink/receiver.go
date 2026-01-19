// Package missionlink provides UDP-based mission communication between MotherShip and rovers.
// It implements a reliable messaging protocol with acknowledgment and retry mechanisms.
//
// The package consists of two main components:
//   - MissionLinkSender: Used by rovers to send mission requests and receive assignments
//   - MissionLinkReceiver: Used by MotherShip to receive requests and send assignments
//
// Communication Protocol:
//   - REQUEST_MISSION: Rover requests a new mission
//   - MISSION_ASSIGNMENT: MotherShip assigns mission to rover
//   - MISSION_UPDATE: Rover sends mission progress updates
//   - MISSION_ACK: Acknowledgment for received messages
//
// The protocol ensures reliable delivery through sequence numbers, acknowledgments,
// and configurable retry mechanisms.
package missionlink

import (
	"cc.2526/internal/common"
	"cc.2526/internal/network"
	"cc.2526/internal/serialization"
	"log"
	"net"
	"sync"
	"time"
)

// MissionLinkReceiver manages MotherShip-side mission communication with rovers.
// It handles receiving mission requests and updates, and sending mission assignments.
type MissionLinkReceiver struct {
    // listener is the UDP socket listening for rover connections
	listener    *net.UDPConn

    // clients tracks connected rover addresses by string representation
	clients     map[string]*net.UDPAddr

    // roverAddrs maps rover IDs to their UDP addresses for direct communication
	roverAddrs  map[uint8]*net.UDPAddr

    // clientsMux protects concurrent access to clients and roverAddrs
	clientsMux  sync.Mutex

    // running indicates if the receiver is active
	running     bool

    // stopChan signals goroutines to stop during shutdown
	stopChan    chan struct{}

    // RequestChan buffers incoming mission requests from rovers
	RequestChan chan common.MissionRequest

    // UpdateChan buffers incoming mission progress updates from rovers
	UpdateChan  chan common.MissionProgress

    // AckChan buffers incoming acknowledgments from rovers
	AckChan     chan common.MissionAck
}

// NewMissionLinkReceiver creates a new mission link receiver for MotherShip communication.
//
// Returns:
//   - *MissionLinkReceiver: A new receiver instance ready for use
//
// The receiver is initialized with:
//   - Empty client and rover address maps
//   - Buffered channels for requests, updates, and acknowledgments
//   - Network port from network.MissionLinkServerPort configuration
func NewMissionLinkReceiver() *MissionLinkReceiver {
	return &MissionLinkReceiver{
		clients:     make(map[string]*net.UDPAddr),
		roverAddrs:  make(map[uint8]*net.UDPAddr),
		running:     false,
		stopChan:    make(chan struct{}),
		RequestChan: make(chan common.MissionRequest, 100),
		UpdateChan:  make(chan common.MissionProgress, 100),
		AckChan:     make(chan common.MissionAck, 100),
	}
}

// Start begins listening for UDP connections from rovers.
//
// Returns:
//   - error: Any error encountered during socket setup
//
// The method:
//   - Resolves the UDP address from configuration
//   - Creates a UDP listener
//   - Starts the receive loop goroutine
//   - Sets the receiver as running
func (mlr *MissionLinkReceiver) Start() error {
	udpAddr, err := net.ResolveUDPAddr("udp", network.MissionLinkServerPort)
	if err != nil {
		return err
	}

	mlr.listener, err = net.ListenUDP("udp", udpAddr)
	if err != nil {
		return err
	}

	log.Printf("MissionLink UDP listening on %s", network.MissionLinkServerPort)
	mlr.running = true
	go mlr.receiveLoop()

	return nil
}

// receiveLoop runs in a goroutine to continuously listen for incoming UDP messages.
// It handles connection errors gracefully and continues running until stopped.
//
// For each received message:
//   - Registers the client address
//   - Spawns a goroutine to handle the message
//   - Uses a 1024-byte buffer for incoming data
func (mlr *MissionLinkReceiver) receiveLoop() {
	buffer := make([]byte, 1024)

	for mlr.running {
		n, addr, err := mlr.listener.ReadFromUDP(buffer)
		if err != nil {
			select {
			case <-mlr.stopChan:
				return
			default:
				log.Printf("Error reading UDP: %v", err)
				continue
			}
		}

		mlr.clientsMux.Lock()
		mlr.clients[addr.String()] = addr
		mlr.clientsMux.Unlock()

		go mlr.handleMessage(buffer[:n], addr)
	}
}

// handleMessage processes an incoming UDP message from a rover.
//
// Parameters:
//   - data: The raw message bytes
//   - addr: The UDP address of the sending rover
//
// The method:
//   - Determines message type from first byte
//   - Deserializes based on type
//   - Routes to appropriate channel
//   - Sends acknowledgment for requests and updates
//
// Supported message types:
//   - REQUEST_MISSION: Routes to RequestChan, sends ACK
//   - MISSION_UPDATE: Routes to UpdateChan, sends ACK
//   - MISSION_ACK: Routes to AckChan
func (mlr *MissionLinkReceiver) handleMessage(data []byte, addr *net.UDPAddr) {
	if len(data) == 0 {
		return
	}

	msgType := common.MessageType(data[0])

	switch msgType {
	case common.REQUEST_MISSION:
		if len(data) >= 35 {
			request := serialization.DeserializeMissionRequest(data)
			mlr.registerRoverAddress(request.RoverId, addr)
			mlr.RequestChan <- request
			mlr.SendMissionAck(addr, request.SeqNum, request.RoverId, common.ACK_SUCCESS)
		}

	case common.MISSION_UPDATE:
		if len(data) >= 22 {
			update := serialization.DeserializeMissionProgress(data)
			mlr.UpdateChan <- update
			mlr.SendMissionAck(addr, update.SeqNum, update.RoverId, common.ACK_SUCCESS)
		}

	case common.MISSION_ACK:
		if len(data) >= 20 {
			ack := serialization.DeserializeMissionAck(data)
			mlr.AckChan <- ack
		}

	default:
		log.Printf("Unknown message type: %d, length: %d bytes", msgType, len(data))
	}
}

// SendMissionAssignment sends a mission assignment to a specific rover.
//
// Parameters:
//   - addr: UDP address of the target rover
//   - assignment: Mission assignment to send
//
// The method:
//   - Sets message type, sequence, and timestamp
//   - Serializes the assignment
//   - Sends with retry mechanism
//   - Uses unique sequence number from common.GenerateUniqueSequenceNumber()
func (mlr *MissionLinkReceiver) SendMissionAssignment(addr *net.UDPAddr, assignment common.MissionAssignment) {
	assignment.Type = common.MISSION_ASSIGNMENT
	assignment.SeqNum = common.GenerateUniqueSequenceNumber()
	assignment.Version = 1
	assignment.Timestamp = time.Now().Unix()

	data := serialization.SerializeMissionAssignment(&assignment)

	mlr.sendWithRetry(data, addr, assignment.SeqNum, assignment.RoverId)
}

// SendMissionAck sends an acknowledgment to a rover.
//
// Parameters:
//   - addr: UDP address of the target rover
//   - ackForSeq: Sequence number being acknowledged
//   - roverId: ID of the rover
//   - status: Acknowledgment status
//
// For ACK_RETRY status, uses retry mechanism. For other statuses, sends once.
func (mlr *MissionLinkReceiver) SendMissionAck(addr *net.UDPAddr, ackForSeq uint32, roverId uint8, status common.AckStatus) {
	ack := common.MissionAck{
		Type:      common.MISSION_ACK,
		SeqNum:    common.GenerateUniqueSequenceNumber(),
		Version:   1,
		Timestamp: time.Now().Unix(),
		AckForSeq: ackForSeq,
		RoverId:   roverId,
		Status:    status,
	}

	data := serialization.SerializeMissionAck(&ack)

	if status == common.ACK_RETRY {
		mlr.sendWithRetry(data, addr, ack.SeqNum, roverId)
	} else {
		mlr.listener.WriteToUDP(data, addr)
	}
}

// sendWithRetry sends data with retry logic until acknowledgment is received.
//
// Parameters:
//   - data: Serialized message data
//   - addr: Target rover UDP address
//   - seqNum: Sequence number of this message
//   - roverId: ID of target rover
//
// The method:
//   - Attempts sending up to 3 times
//   - Waits for acknowledgment between attempts
//   - Logs delivery success or failure
//
// Timeout between retries is 2 seconds.
func (mlr *MissionLinkReceiver) sendWithRetry(data []byte, addr *net.UDPAddr, seqNum uint32, roverId uint8) {
	maxRetries := 3
	timeout := 2 * time.Second

	for attempt := 0; attempt < maxRetries; attempt++ {
		_, err := mlr.listener.WriteToUDP(data, addr)
		if err != nil {
			log.Printf("Failed to send assignment to rover %d (attempt %d): %v",
				roverId, attempt+1, err)
			time.Sleep(time.Second)
			continue
		}

		if mlr.waitForAck(seqNum, roverId, timeout) {
			log.Printf("Assignment delivered to rover %d", roverId)
			return
		}

		log.Printf("Timeout waiting for ACK from rover %d (attempt %d), retrying...",
			roverId, attempt+1)
	}

	log.Printf("Giving up on sending assignment to rover %d after %d attempts",
		roverId, maxRetries)
}

// waitForAck waits for an acknowledgment from a specific rover.
//
// Parameters:
//   - seqNum: Expected sequence number in acknowledgment
//   - roverId: Expected rover ID in acknowledgment
//   - timeout: Maximum time to wait
//
// Returns:
//   - bool: true if matching acknowledgment received, false if timeout
//
// The method monitors the AckChan for matching sequence and rover ID.
func (mlr *MissionLinkReceiver) waitForAck(seqNum uint32, roverId uint8, timeout time.Duration) bool {
	deadline := time.Now().Add(timeout)

	for time.Now().Before(deadline) {
		select {
		case ack := <-mlr.AckChan:
			if ack.AckForSeq == seqNum && ack.RoverId == roverId {
				return true
			}
		case <-time.After(100 * time.Millisecond):
		}
	}
	return false
}

// Stop gracefully shuts down the mission link receiver.
//
// The method:
//   - Sets running to false
//   - Closes the stop channel
//   - Closes the UDP listener
//   - Closes all message channels
//   - Stops all goroutines
func (mlr *MissionLinkReceiver) Stop() {
	if mlr.running {
		mlr.running = false
		close(mlr.stopChan)
		if mlr.listener != nil {
			mlr.listener.Close()
		}
		close(mlr.RequestChan)
		close(mlr.UpdateChan)
		close(mlr.AckChan)
	}
}

// registerRoverAddress registers a rover's UDP address for future communication.
//
// Parameters:
//   - roverId: Unique rover identifier
//   - addr: UDP address of the rover
//
// This is called when a rover sends its first message, allowing the MotherShip
// to send direct responses and assignments.
func (mlr *MissionLinkReceiver) registerRoverAddress(roverId uint8, addr *net.UDPAddr) {
	mlr.clientsMux.Lock()
	defer mlr.clientsMux.Unlock()
	mlr.roverAddrs[roverId] = addr
}

// GetRoverAddress retrieves the UDP address for a specific rover.
//
// Parameters:
//   - roverId: Rover identifier to look up
//
// Returns:
//   - *net.UDPAddr: Registered address, or nil if not found
//
// Used by MotherShip to send mission assignments to specific rovers.
func (mlr *MissionLinkReceiver) GetRoverAddress(roverId uint8) *net.UDPAddr {
	mlr.clientsMux.Lock()
	defer mlr.clientsMux.Unlock()
	return mlr.roverAddrs[roverId]
}