// Package rover provides a simulation of planetary exploration vehicles and their control systems.
//
// It includes:
// - Rover: Models vehicle behavior including movement, power management, environmental sensing, and mission execution
// - Controller: Manages rover operations, mission assignments, and communication with the mothership
//
// The package supports various planetary bodies and mission types.
package rover

import (
	"log"
	"time"

	"cc.2526/internal/common"
	"cc.2526/internal/missionlink"
	"cc.2526/internal/network"
	"cc.2526/internal/telemetrystream"
)

// Controller manages rover operations, mission coordination, and communication with the mothership.
// It handles telemetry transmission, mission assignments, and real-time control of the rover.
type Controller struct {
	rover           *Rover                           // The rover instance being controlled
	telemetrySender *telemetrystream.TelemetrySender // Handles real-time telemetry data transmission to mothership
	missionSender   *missionlink.MissionLinkSender   // Manages mission assignment and progress communication
	running         bool                             // Indicates if the controller is actively operating
	stopChan        chan struct{}                    // Channel for graceful shutdown signaling
	updateInterval  time.Duration                    // Time interval between rover system updates
	currentMission  *common.MissionAssignment        // Currently assigned mission, nil if no active mission
}

// NewController creates a new controller for managing a rover instance.
func NewController(roverId uint8, name string, planet common.Planet, startX, startY int32, mothershipIP string) *Controller {
	rover := NewRover(roverId, name, planet, startX, startY)
	telemetrySender := telemetrystream.NewTelemetrySender(rover, mothershipIP+network.TelemetryServerPort)

	missionSender := missionlink.NewMissionLinkSender(roverId, mothershipIP+network.MissionLinkServerPort, name)

	controller := &Controller{
		rover:           rover,
		telemetrySender: telemetrySender,
		missionSender:   missionSender,
		running:         false,
		stopChan:        make(chan struct{}),
		updateInterval:  network.RoverTelemetryDataUpdate,
		currentMission:  nil,
	}

	missionSender.SetAssignmentHandler(controller.handleMissionAssignment)

	return controller
}

// Start begins the rover controller and establishes connection to mothership.
func (c *Controller) Start() error {
	log.Printf("Starting rover controller for %s (ID: %d)", c.rover.Name, c.rover.RoverId)

	if err := c.missionSender.Start(); err != nil {
		log.Printf("Failed to start MissionLink sender: %v", err)
		return err
	}

	if err := c.telemetrySender.Start(); err != nil {
		log.Printf("Failed to start telemetry sender: %v", err)
		return err
	}

	c.running = true
	go c.mainLoop()

	log.Printf("Rover controller started successfully")
	return nil
}

// Stop safely shuts down the rover controller and all active operations.
func (c *Controller) Stop() {
	if c.running {
		c.running = false
		close(c.stopChan)
		c.telemetrySender.Stop()
		c.missionSender.Stop()
		log.Printf("Rover controller stopped")
	}
}

// mainLoop is the main control loop that handles rover updates and mission management.
func (c *Controller) mainLoop() {
	ticker := time.NewTicker(c.updateInterval)
	missionRequestTicker := time.NewTicker(network.RoverMissionSolicition)
	missionUpdateTicker := time.NewTicker(network.RoverMissionLinkDataUpdate)

	defer ticker.Stop()
	defer missionRequestTicker.Stop()
	defer missionUpdateTicker.Stop()

	for {
		select {
		case <-c.stopChan:
			return
		case <-ticker.C:
			c.update()
		case <-missionRequestTicker.C:
			if c.rover.Status == common.IDLE && c.currentMission == nil {
				c.missionSender.SendMissionRequest()
			}

		case <-missionUpdateTicker.C:
			if c.currentMission != nil && c.rover.Status != common.IDLE && c.rover.Status != common.ERROR {
				c.sendMissionProgressUpdate()
			}
		}
	}
}

// update processes one simulation tick for the controlled rover.
func (c *Controller) update() {
	deltaTime := float32(network.TimeFactor.Seconds())
	c.rover.Update(deltaTime)

	if c.currentMission != nil && c.rover.MissionProgress >= 100 {
		c.sendMissionCompletion()
	}
}

// handleMissionAssignment processes incoming mission assignments from the mothership.
func (c *Controller) handleMissionAssignment(assignment common.MissionAssignment) {
	log.Printf("Rover %d received mission assignment %d", c.rover.RoverId, assignment.MissionId)

	c.currentMission = &assignment
	c.rover.StartMission(getStatusFromMissionType(assignment.MissionConfig.Type))
	log.Printf("Starting NEW Mission - Type: %s", assignment.MissionConfig.Type)
}

// sendMissionProgressUpdate sends mission progress updates to the mothership.
func (c *Controller) sendMissionProgressUpdate() {
	if c.currentMission == nil {
		return
	}

	var images *uint32
	var samples *uint8
	var diagnostics *string

	switch c.currentMission.MissionConfig.Type {
	case common.ReconFotografico:
		imgCount := uint32(c.rover.MissionProgress * 3)
		images = &imgCount
	case common.AmostragemAmbiental:
		sampleCount := uint8(c.rover.MissionProgress / 10)
		samples = &sampleCount
	case common.DiagnosticoTecnico:
		diag := "Systems diagnostic in progress"
		diagnostics = &diag
	}

	missionProgress := common.MissionProgress{
		RoverId:            c.rover.RoverId,
		MissionId:          c.currentMission.MissionId,
		Progress:           float32(c.rover.MissionProgress),
		Status:             common.IN_PROGRESS,
		CurrentPosition:    &c.rover.Position,
		NrImagesCaptured:   images,
		Diagnostics:        diagnostics,
		NrSamplesCollected: samples,
	}

	c.missionSender.SendMissionUpdate(missionProgress)
}

// sendMissionCompletion sends mission completion notification to the mothership.
func (c *Controller) sendMissionCompletion() {
	if c.currentMission == nil {
		return
	}

	missionProgress := common.MissionProgress{
		RoverId:         c.rover.RoverId,
		MissionId:       c.currentMission.MissionId,
		Progress:        100.0,
		Status:          common.COMPLETED,
		CurrentPosition: &c.rover.Position,
	}

	c.missionSender.SendMissionUpdate(missionProgress)
	log.Printf("Mission %d completed!", c.currentMission.MissionId)

	c.rover.Status = common.IDLE
	c.rover.MissionProgress = 0
	c.rover.MissionStartTime = 0
	c.rover.Velocity = 0

	c.currentMission = nil
}

// getStatusFromMissionType converts mission types to rover status codes.
func getStatusFromMissionType(missionType common.MissionType) uint8 {
	switch missionType {
	case common.MapeamentoGlobal:
		return common.MAPPING
	case common.EstudoAmbiental:
		return common.ENVIRONMENTAL
	case common.ReconFotografico:
		return common.RECONNAISANCE
	case common.DiagnosticoTecnico:
		return common.DIAGNOSTIC
	case common.AmostragemAmbiental:
		return common.ANALYSIS
	default:
		return common.IDLE
	}
}
