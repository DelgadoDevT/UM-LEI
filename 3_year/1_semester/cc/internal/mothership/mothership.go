// Package mothership implements the central control unit (MotherShip) for the CC-2526 rover system.
// The MotherShip manages rover missions, coordinates communications, and monitors system telemetry.
//
// Key Responsibilities:
//   - Managing rover connections and mission assignments
//   - Processing mission requests and generating appropriate missions
//   - Tracking mission progress and status
//   - Receiving and processing telemetry data
//   - Providing mission queuing and scheduling
//   - Maintaining mission history and state
//
// The MotherShip operates as a concurrent system with multiple goroutines handling
// different aspects of rover management and communication.
package mothership

import (
	"log"
	"math/rand"
	"sync"
	"time"

	"cc.2526/internal/api"
	"cc.2526/internal/common"
	"cc.2526/internal/missionlink"
	"cc.2526/internal/network"
	"cc.2526/internal/rover"
	"cc.2526/internal/telemetrystream"
)

// NaveMae represents the MotherShip control unit.
// It manages all aspects of rover mission control and communication.
type NaveMae struct {
	// Nome is the name identifier for this MotherShip instance
	Nome              string

	// Rovers contains the registered rover instances managed by this MotherShip
	Rovers            map[uint8]*rover.Rover

	// MissoesHistorico maintains a history of all missions processed by this MotherShip
	MissoesHistorico  map[uint8]*common.Mission

	// MissionTypes contains the available mission types that can be assigned to rovers
	MissionTypes      []common.MissionType

	// activeMissions tracks currently active missions (roverId -> missionId mapping)
	// A value of 0 indicates the rover has no active mission
	activeMissions    map[uint8]uint8

	// activeMissionsMu protects concurrent access to activeMissions
	activeMissionsMu  sync.Mutex

	// mothershipState holds the current state of the MotherShip for API exposure
	mothershipState   *api.MothershipState

	// missionReceiver handles incoming mission-related communications
	missionReceiver   *missionlink.MissionLinkReceiver

	// telemetryReceiver handles incoming telemetry data from rovers
	telemetryReceiver *telemetrystream.TelemetryReceiver

	// running indicates whether the MotherShip is currently operational
	running           bool

	// stopChan is used to signal goroutines to stop during shutdown
	stopChan          chan struct{}

	// wg manages goroutine lifecycle during shutdown
	wg                sync.WaitGroup

	// missionCounter provides unique mission IDs
	missionCounter    uint8

	// missionCounterMu protects concurrent access to missionCounter
	missionCounterMu  sync.Mutex
}

// NewNaveMae creates and initializes a new MotherShip instance.
//
// Parameters:
//   - nome: The name identifier for this MotherShip
//
// Returns:
//   - *NaveMae: A new MotherShip instance ready to start
//
// The MotherShip is initialized with default mission types and empty state containers.
func NewNaveMae(nome string) *NaveMae {
	return &NaveMae{
		Nome:             nome,
		Rovers:           make(map[uint8]*rover.Rover),
		MissoesHistorico: make(map[uint8]*common.Mission),
		activeMissions:   make(map[uint8]uint8),
		mothershipState:  api.NewMothershipState(),
		missionCounter:   0,
		MissionTypes: []common.MissionType{
			common.MapeamentoGlobal,
			common.EstudoAmbiental,
			common.ReconFotografico,
			common.DiagnosticoTecnico,
			common.AmostragemAmbiental,
		},
		running:  false,
		stopChan: make(chan struct{}),
	}
}

// Start initializes and starts all MotherShip subsystems.
// This includes:
//   - Starting the API server for external control
//   - Initializing mission link receiver for rover communications
//   - Starting telemetry receiver for rover data
//   - Launching goroutines for:
//     - Handling mission requests
//     - Processing mission updates
//     - Handling telemetry data
//     - Running time simulation
//
// The method blocks until all subsystems are started, then returns control.
// If any subsystem fails to start, the method logs a fatal error and terminates.
func (nm *NaveMae) Start() {
	api.StartServer(nm.mothershipState)

	nm.missionReceiver = missionlink.NewMissionLinkReceiver()
	if err := nm.missionReceiver.Start(); err != nil {
		log.Fatalf("Failed to start MissionLink receiver: %v", err)
	}

	nm.telemetryReceiver = telemetrystream.NewTelemetryReceiver()
	if err := nm.telemetryReceiver.Start(); err != nil {
		nm.missionReceiver.Stop()
		log.Fatalf("Failed to start Telemetry receiver: %v", err)
	}

	log.Println("Mothership ready - waiting for rover connections...")

	nm.running = true

	nm.wg.Add(4)
	go nm.handleMissionRequests()
	go nm.handleMissionUpdates()
	go nm.handleTelemetryData()
	go nm.runTimeSimulation()
}

// Stop gracefully shuts down the MotherShip and all its subsystems.
// This includes:
//   - Stopping the mission link receiver
//   - Stopping the telemetry receiver
//   - Signaling all goroutines to stop
//   - Waiting for all goroutines to complete
//   - Closing communication channels
//
// The method ensures clean shutdown and resource cleanup.
func (nm *NaveMae) Stop() {
	if nm.running {
		nm.running = false

		if nm.missionReceiver != nil {
			nm.missionReceiver.Stop()
		}
		if nm.telemetryReceiver != nil {
			nm.telemetryReceiver.Stop()
		}

		close(nm.stopChan)

		nm.wg.Wait()
	}
}

// runTimeSimulation manages the simulated time progression for the MotherShip.
// This goroutine increments the MotherShip's simulated time by network.TimeFactorEqualizer
// seconds every real-world second.
//
// The simulation runs until the stopChan is closed, at which point it performs
// clean shutdown and returns.
func (nm *NaveMae) runTimeSimulation() {
	defer nm.wg.Done()

	ticker := time.NewTicker(1 * time.Second)
	defer ticker.Stop()

	timeIncrement := int64(network.TimeFactorEqualizer.Seconds())

	log.Printf("Mothership Time Simulation Started (Factor: %ds per second)", timeIncrement)

	for {
		select {
		case <-nm.stopChan:
			log.Println("Mothership Time Simulation Stopped")
			return
		case <-ticker.C:
			nm.mothershipState.AddSimulatedTime(timeIncrement)
		}
	}
}

// handleMissionRequests processes incoming mission requests from rovers.
// This goroutine listens on the mission receiver's RequestChan and processes
// each request as it arrives.
//
// Requests are processed by:
//   - Checking if rover has active mission and cleaning up if needed
//   - Looking for queued manual missions for the rover
//   - Generating new missions if no queued missions exist
//   - Creating and sending mission assignments
//
// The goroutine runs until the MotherShip is stopped or the channel closes.
func (nm *NaveMae) handleMissionRequests() {
	defer nm.wg.Done()

	for nm.running {
		select {
		case <-nm.stopChan:
			return
		case request, ok := <-nm.missionReceiver.RequestChan:
			if !ok {
				return
			}
			nm.processMissionRequest(request)
		}
	}
}

// generateMissionID generates a unique mission identifier.
// The ID is thread-safe and wraps around after reaching maximum uint8 value.
//
// Returns:
//   - uint8: A unique mission ID
func (nm *NaveMae) generateMissionID() uint8 {
	nm.missionCounterMu.Lock()
	defer nm.missionCounterMu.Unlock()

	nm.missionCounter++
	if nm.missionCounter == 0 {
		nm.missionCounter = 1
	}

	return nm.missionCounter
}

// processMissionRequest handles an individual mission request from a rover.
//
// Parameters:
//   - request: The mission request containing rover identification
//
// The method:
//   - Clears any existing mission for the rover
//   - Checks for queued manual missions
//   - Generates a new mission if no queued mission exists
//   - Updates mission state and active missions tracking
//   - Sends mission assignment to the rover
func (nm *NaveMae) processMissionRequest(request common.MissionRequest) {
	log.Printf("Received mission request from rover %d (%s)", request.RoverId, request.RoverName)

	nm.activeMissionsMu.Lock()
	defer nm.activeMissionsMu.Unlock()

	if currentMissionId, exists := nm.activeMissions[request.RoverId]; exists {
		log.Printf("Clearing previous mission state %d for rover %d", currentMissionId, request.RoverId)
		delete(nm.activeMissions, request.RoverId)

		if mission, exists := nm.mothershipState.GetMission(currentMissionId); exists && mission.Estado == "EM_PROGRESSO" {
			mission.Estado = "FALHADA"
			nm.mothershipState.UpdateMission(mission)
		}
	}

	var mission *common.Mission

	queuedMission, found := nm.mothershipState.DequeueMission(request.RoverId)
	if found {
		log.Printf("Found manual mission in queue for Rover %d", request.RoverId)
		mission = queuedMission
		mission.MissionId = nm.generateMissionID()
		if len(mission.AreaGeografica) == 0 {
			mission.AreaGeografica = []common.Coordinate{{X: 0, Y: 0, Z: 0}, {X: 50, Y: 50, Z: 0}}
		}
	} else {
		log.Printf("No manual mission, generating NEW mission for Rover %d", request.RoverId)
		mission = nm.createSampleMission(request.RoverId, request.RoverName)
	}

	assignment := nm.createMissionAssignment(request, mission)

	mission.DataInicial = time.Now().Unix()
	mission.Estado = "EM_PROGRESSO"
	nm.mothershipState.UpdateMission(*mission)

	nm.activeMissions[request.RoverId] = mission.MissionId

	if addr := nm.missionReceiver.GetRoverAddress(request.RoverId); addr != nil {
		nm.missionReceiver.SendMissionAssignment(addr, assignment)
		log.Printf("Sent NEW mission assignment %d to rover %d", mission.MissionId, request.RoverId)
	}
}

// handleMissionUpdates processes mission progress updates from rovers.
// This goroutine listens on the mission receiver's UpdateChan and processes
// each update as it arrives.
//
// Updates are processed by:
//   - Updating mission progress in the state
//   - Handling mission status changes (completed/failed)
//   - Logging position updates when available
//
// The goroutine runs until the MotherShip is stopped or the channel closes.
func (nm *NaveMae) handleMissionUpdates() {
	defer nm.wg.Done()

	for nm.running {
		select {
		case <-nm.stopChan:
			return
		case update, ok := <-nm.missionReceiver.UpdateChan:
			if !ok {
				return
			}
			nm.processMissionUpdate(update)
		}
	}
}

// processMissionUpdate handles an individual mission progress update from a rover.
//
// Parameters:
//   - update: The mission progress update containing status and progress information
//
// The method:
//   - Updates the mission's progress percentage
//   - Changes mission state based on status (IN_PROGRESS, COMPLETED, FAILED)
//   - Completes the mission if status is COMPLETED or FAILED
//   - Logs position information if available in the update
func (nm *NaveMae) processMissionUpdate(update common.MissionProgress) {
	log.Printf("Mission update from rover %d - Mission: %d, Progress: %.1f%%, Status: %v",
		update.RoverId, update.MissionId, update.Progress, update.Status)

	if mission, exists := nm.mothershipState.GetMission(update.MissionId); exists {
		mission.Progresso = update.Progress

		switch update.Status {
		case common.IN_PROGRESS:
			mission.Estado = "EM_PROGRESSO"
		case common.COMPLETED:
			mission.Estado = "CONCLUIDA"
			nm.completeMission(update.RoverId, update.MissionId)
		case common.FAILED:
			mission.Estado = "FALHADA"
			nm.completeMission(update.RoverId, update.MissionId)
		}

		nm.mothershipState.UpdateMission(mission)
	}

	switch update.Status {
	case common.COMPLETED, common.FAILED:
		nm.completeMission(update.RoverId, update.MissionId)
	}

	if update.CurrentPosition != nil {
		log.Printf("Position: (%d, %d, %d)", update.CurrentPosition.X, update.CurrentPosition.Y, update.CurrentPosition.Z)
	}
}

// handleTelemetryData processes incoming telemetry data from rovers.
// This goroutine listens on the telemetry receiver's channel and processes
// each telemetry packet as it arrives.
//
// Telemetry data is forwarded to the MotherShip state for storage and API exposure.
//
// The goroutine runs until the MotherShip is stopped or the channel closes.
func (nm *NaveMae) handleTelemetryData() {
	defer nm.wg.Done()

	for nm.running {
		select {
		case <-nm.stopChan:
			return
		case telemetry, ok := <-nm.telemetryReceiver.GetTelemetryData():
			if !ok {
				return
			}
			nm.processTelemetry(telemetry)
		}
	}
}

// processTelemetry handles individual telemetry data packets from rovers.
//
// Parameters:
//   - telemetry: The telemetry data containing rover system information
//
// The method updates the MotherShip state with the latest telemetry information.
func (nm *NaveMae) processTelemetry(telemetry common.TelemetryData) {
	nm.mothershipState.UpdateTelemetry(telemetry)
}

// createSampleMission generates a sample mission for a rover when no specific
// mission is queued. The mission type is randomly selected from available types.
//
// Parameters:
//   - roverId: The ID of the requesting rover
//   - roverName: The name of the requesting rover
//
// Returns:
//   - *common.Mission: A fully configured mission with appropriate parameters
//     based on the selected mission type
//
// Mission types include:
//   - MapeamentoGlobal: Large area mapping with patrol pattern
//   - EstudoAmbiental: Environmental measurements in defined area
//   - ReconFotografico: Photographic reconnaissance mission
//   - DiagnosticoTecnico: System diagnostic check
//   - AmostragemAmbiental: Environmental sample collection
func (nm *NaveMae) createSampleMission(roverId uint8, roverName string) *common.Mission {
	randomType := nm.MissionTypes[rand.Intn(len(nm.MissionTypes))]

	mission := common.NewMission(nm.generateMissionID(), "Exploracao Automatica", common.MARS, randomType)

	mission.RoverId = roverId
	mission.RoverName = roverName

	switch randomType {

	case common.MapeamentoGlobal:
		mission.Tarefa = "Patrulha de Rotina"
		mission.AreaGeografica = []common.Coordinate{
			{X: 0, Y: 0, Z: 0},
			{X: 100, Y: 0, Z: 0},
			{X: 100, Y: 100, Z: 0},
			{X: 0, Y: 100, Z: 0},
		}
		mission.PontosInteresse = []common.Coordinate{
			{X: 25, Y: 25, Z: 0},
			{X: 75, Y: 75, Z: 0},
		}

	case common.EstudoAmbiental:
		mission.Tarefa = "Medicoes Atmosfericas"
		mission.AreaGeografica = []common.Coordinate{
			{X: 10, Y: 10, Z: 0},
			{X: 20, Y: 20, Z: 0},
		}

	case common.ReconFotografico:
		mission.Tarefa = "Captura fotografica"
		mission.AreaGeografica = []common.Coordinate{
			{X: 50, Y: 50, Z: 0},
			{X: 60, Y: 60, Z: 0},
		}

	case common.DiagnosticoTecnico:
		mission.Tarefa = "Diagnostico completo"
		mission.AreaGeografica = []common.Coordinate{
			{X: 0, Y: 0, Z: 0},
		}

	case common.AmostragemAmbiental:
		mission.Tarefa = "Recolha de amostras"
		mission.AreaGeografica = []common.Coordinate{
			{X: 30, Y: 30, Z: 0},
			{X: 35, Y: 35, Z: 0},
		}
	}

	mission.DuracaoMaxima = 3600
	mission.Atualizacoes = 5
	mission.Prioridade = 3
	mission.ToleranciaFalhas = 15

	return mission
}

// createMissionAssignment creates a mission assignment structure from a mission
// request and a mission definition.
//
// Parameters:
//   - request: The original mission request from the rover
//   - mission: The mission to be assigned
//
// Returns:
//   - common.MissionAssignment: A complete mission assignment ready for transmission
//     to the rover, including all mission parameters and metadata
func (nm *NaveMae) createMissionAssignment(request common.MissionRequest, mission *common.Mission) common.MissionAssignment {
	return common.MissionAssignment{
		RoverId:          request.RoverId,
		RoverName:        request.RoverName,
		MissionId:        mission.MissionId,
		MissionConfig:    common.GetMissionTypeConfig(mission.Type),
		Tarefa:           mission.Tarefa,
		AreaGeografica:   mission.AreaGeografica,
		PontosInteresse:  mission.PontosInteresse,
		Atualizacoes:     mission.Atualizacoes,
		Prioridade:       mission.Prioridade,
		ToleranciaFalhas: mission.ToleranciaFalhas,
		DataInicial:      time.Now().Unix(),
		DuracaoMaxima:    mission.DuracaoMaxima,
		SeqNum:           common.GenerateUniqueSequenceNumber(),
		Version:          1,
	}
}

// completeMission marks a specific mission as completed and removes it from
// active missions tracking for the rover.
//
// Parameters:
//   - roverId: The ID of the rover that completed the mission
//   - missionId: The ID of the completed mission
//
// The method only removes the mission if it is the currently active mission
// for the rover, preventing interference with new mission assignments.
func (nm *NaveMae) completeMission(roverId uint8, missionId uint8) {
	nm.activeMissionsMu.Lock()
	defer nm.activeMissionsMu.Unlock()

	if currentMissionId, exists := nm.activeMissions[roverId]; exists && currentMissionId == missionId {
		delete(nm.activeMissions, roverId)
		log.Printf("Mission %d completed for rover %d, now available for new missions", missionId, roverId)
	}
}