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

// AssignmentHandler is a function type that handles incoming mission assignments.
// This callback is invoked when the rover receives a mission assignment from the MotherShip.
type AssignmentHandler func(assignment common.MissionAssignment)

// SetAssignmentHandler sets the callback function for handling mission assignments.
//
// Parameters:
//   - handler: Function to call when a mission assignment is received
//
// The handler is called from the ackHandler goroutine when a MISSION_ASSIGNMENT
// message is received and deserialized.
func (mls *MissionLinkSender) SetAssignmentHandler(handler AssignmentHandler) {
	mls.assignmentHandler = handler
}

// MissionLinkSender manages rover-side mission communication with the MotherShip.
// It handles sending mission requests and updates, and receiving mission assignments.
type MissionLinkSender struct {
	// serverAddr is the UDP address of the MotherShip server
	serverAddr        string

    // conn is the UDP connection to the MotherShip
	conn              *net.UDPConn

    // roverId is the unique identifier for this rover
	roverId           uint8

    // RoverName is the human-readable name of the rover
	RoverName         string

    // running indicates if the sender is active
	running           bool

    // stopChan signals goroutines to stop during shutdown
	stopChan          chan struct{}

    // seqNum is the next sequence number to use for outgoing messages
	seqNum            uint32

    // pendingAcks tracks sent messages waiting for acknowledgment
	pendingAcks       map[uint32]time.Time

    // ackMux protects concurrent access to pendingAcks
	ackMux            sync.Mutex

    // assignmentHandler is called when a mission assignment is received
	assignmentHandler AssignmentHandler
}

// NewMissionLinkSender creates a new mission link sender for rover communication.
//
// Parameters:
//   - roverId: Unique identifier for the rover
//   - serverAddr: UDP address of the MotherShip (e.g., "localhost:12345")
//   - roverName: Human-readable name of the rover
//
// Returns:
//   - *MissionLinkSender: A new sender instance ready for use
//
// Example:
//   sender := NewMissionLinkSender(1, "localhost:8080", "RoverAlpha")
func NewMissionLinkSender(roverId uint8, serverAddr string, roverName string) *MissionLinkSender {
	return &MissionLinkSender{
		serverAddr:  serverAddr,
		roverId:     roverId,
		RoverName:   roverName,
		running:     false,
		stopChan:    make(chan struct{}),
		seqNum:      1,
		pendingAcks: make(map[uint32]time.Time),
	}
}

// Start establishes the UDP connection to the MotherShip and starts the acknowledgment handler.
//
// Returns:
//   - error: Any error encountered during connection setup
//
// The method:
//   - Resolves the UDP address
//   - Creates a UDP connection
//   - Starts the acknowledgment handler goroutine
//   - Sets the sender as running
//
// If connection fails, the error is returned and the sender remains stopped.
func (mls *MissionLinkSender) Start() error {
	udpAddr, err := net.ResolveUDPAddr("udp", mls.serverAddr)
	if err != nil {
		return err
	}

	mls.conn, err = net.DialUDP("udp", nil, udpAddr)
	if err != nil {
		return err
	}

	log.Printf("Rover %d: MissionLink UDP connected to %s", mls.roverId, mls.serverAddr)
	mls.running = true

	go mls.ackHandler()

	return nil
}

// ackHandler runs in a goroutine to process incoming messages from the MotherShip.
// It handles two message types:
//   - MISSION_ACK: Removes pending acknowledgments
//   - MISSION_ASSIGNMENT: Invokes the assignment handler callback
//
// The handler uses a timeout on reads to allow graceful shutdown and
// continues running until the sender is stopped.
func (mls *MissionLinkSender) ackHandler() {

	buffer := make([]byte, network.MissionLinkAssignPacketSize)

	for mls.running {
		mls.conn.SetReadDeadline(time.Now().Add(100 * time.Millisecond))
		n, err := mls.conn.Read(buffer)

		if err != nil {
			if netErr, ok := err.(net.Error); ok && netErr.Timeout() {
				continue
			}
			select {
			case <-mls.stopChan:
				return
			default:
				continue
			}
		}

		msgType := common.MessageType(buffer[0])

		switch msgType {
		case common.MISSION_ACK:
			if n >= 15 {
				ack := serialization.DeserializeMissionAck(buffer[:n])
				mls.handleAck(ack)
			}

		case common.MISSION_ASSIGNMENT:
			if n >= 100 {
				assignment := serialization.DeserializeMissionAssignment(buffer[:n])
				if mls.assignmentHandler != nil {
					mls.assignmentHandler(assignment)
				}
				mls.SendMissionAck(assignment.SeqNum, common.ACK_SUCCESS)
			}

		default:
			log.Printf("Rover %d: Unknown message type: %d", mls.roverId, msgType)
		}
	}
}

// handleAck processes an acknowledgment from the MotherShip.
//
// Parameters:
//   - ack: The acknowledgment message containing sequence information
//
// The method removes the corresponding sequence from pendingAcks if it exists,
// indicating successful delivery of the original message.
func (mls *MissionLinkSender) handleAck(ack common.MissionAck) {
	mls.ackMux.Lock()
	defer mls.ackMux.Unlock()

	if _, exists := mls.pendingAcks[ack.AckForSeq]; exists {
		delete(mls.pendingAcks, ack.AckForSeq)
	}
}

// SendMissionRequest sends a mission request to the MotherShip.
// This is typically called when a rover is ready for a new mission.
//
// The method:
//   - Creates a MissionRequest with current rover information
//   - Serializes the request
//   - Sends with retry mechanism
//   - Increments sequence number
func (mls *MissionLinkSender) SendMissionRequest() {
	request := common.MissionRequest{
		Type:      common.REQUEST_MISSION,
		SeqNum:    mls.seqNum,
		Version:   1,
		Timestamp: time.Now().Unix(),
		RoverId:   mls.roverId,
		RoverName: mls.RoverName,
	}

	data := serialization.SerializeMissionRequest(&request)
	mls.sendWithRetry(data, mls.seqNum)
	mls.seqNum++
}

// SendMissionUpdate sends a mission progress update to the MotherShip.
//
// Parameters:
//   - progress: The mission progress information including status and metrics
//
// The method:
//   - Sets message type, sequence, and timestamp
//   - Serializes the progress update
//   - Sends with retry mechanism
//   - Increments sequence number
func (mls *MissionLinkSender) SendMissionUpdate(progress common.MissionProgress) {
	progress.Type = common.MISSION_UPDATE
	progress.SeqNum = mls.seqNum
	progress.Version = 1
	progress.Timestamp = time.Now().Unix()
	progress.RoverId = mls.roverId

	data := serialization.SerializeMissionProgress(&progress)

	mls.sendWithRetry(data, mls.seqNum)
	mls.seqNum++
}

// SendMissionAck sends an acknowledgment to the MotherShip.
//
// Parameters:
//   - ackForSeq: The sequence number being acknowledged
//   - status: The acknowledgment status (success/failure)
//
// This is used to acknowledge receipt of mission assignments and other messages.
func (mls *MissionLinkSender) SendMissionAck(ackForSeq uint32, status common.AckStatus) {
	ack := common.MissionAck{
		Type:      common.MISSION_ACK,
		SeqNum:    mls.seqNum,
		Version:   1,
		Timestamp: time.Now().Unix(),
		AckForSeq: ackForSeq,
		RoverId:   mls.roverId,
		Status:    status,
	}

	data := serialization.SerializeMissionAck(&ack)
	mls.conn.Write(data)
	mls.seqNum++
}

// sendWithRetry sends data with retry logic until acknowledgment is received or max retries exceeded.
//
// Parameters:
//   - data: The serialized message data to send
//   - seqNum: The sequence number of this message
//
// The method:
//   - Adds sequence to pending acknowledgments
//   - Attempts sending up to 3 times
//   - Waits for acknowledgment between attempts
//   - Logs failure if all retries exhausted
//
// Timeout between retries is 2 seconds.
func (mls *MissionLinkSender) sendWithRetry(data []byte, seqNum uint32) {
	maxRetries := 3
	timeout := 2 * time.Second

	mls.ackMux.Lock()
	mls.pendingAcks[seqNum] = time.Now()
	mls.ackMux.Unlock()

	for attempt := 0; attempt < maxRetries; attempt++ {
		_, err := mls.conn.Write(data)
		if err != nil {
			log.Printf("Rover %d: Failed to send message (attempt %d): %v",
				mls.roverId, attempt+1, err)
			time.Sleep(time.Second)
			continue
		}

		if mls.waitForAck(seqNum, timeout) {
			return
		}

		log.Printf("Rover %d: Timeout waiting for ACK (attempt %d), retrying...",
			mls.roverId, attempt+1)
	}

	log.Printf("Rover %d: Giving up on seq %d after %d attempts",
		mls.roverId, seqNum, maxRetries)
}

// waitForAck waits for an acknowledgment for a specific sequence number.
//
// Parameters:
//   - seqNum: The sequence number to wait for
//   - timeout: Maximum time to wait for acknowledgment
//
// Returns:
//   - bool: true if acknowledgment received, false if timeout
//
// The method polls the pendingAcks map every 100ms until the sequence is removed
// (indicating acknowledgment) or timeout expires.
func (mls *MissionLinkSender) waitForAck(seqNum uint32, timeout time.Duration) bool {
	deadline := time.Now().Add(timeout)

	for time.Now().Before(deadline) {
		mls.ackMux.Lock()
		_, exists := mls.pendingAcks[seqNum]
		mls.ackMux.Unlock()

		if !exists {
			return true
		}

		time.Sleep(100 * time.Millisecond)
	}
	return false
}

// Stop gracefully shuts down the mission link sender.
//
// The method:
//   - Sets running to false
//   - Closes the stop channel
//   - Closes the UDP connection
//   - Stops all goroutines
func (mls *MissionLinkSender) Stop() {
	if mls.running {
		mls.running = false
		close(mls.stopChan)
		if mls.conn != nil {
			mls.conn.Close()
		}
	}
}