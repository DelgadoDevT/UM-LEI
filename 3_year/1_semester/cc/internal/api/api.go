package api

import (
	"encoding/json"
	"log"
	"net/http"
	"sync"
	"time"

	"cc.2526/internal/common"
	"cc.2526/internal/network"
)

// =================================================================
// 🧠 MOTHERSHIP STATE ENGINE
// =================================================================

// MothershipState represents the central shared state of the Mothership application.
// It acts as an in-memory database holding the latest information about the fleet.
//
// 🔒 Concurrency Safety:
// Since this struct is accessed by multiple goroutines (HTTP handlers, UDP listeners,
// and background simulation loops), it utilizes a sync.RWMutex.
//   - 👓 Use .RLock() for read-only operations (multiple readers allowed).
//   - ✍️ Use .Lock() for write operations (exclusive access).
type MothershipState struct {
	mu              sync.RWMutex
	latestTelemetry map[uint8]common.TelemetryData // 📡 Maps a unique RoverID to its most recent TelemetryData packet.
	latestMissions  map[uint8]common.Mission       // 📋 Maps a unique MissionID to its full Mission object (status, progress, etc.).
	missionQueue    map[uint8][]common.Mission     // ⏳ Maps a RoverID to a slice of pending Missions (waiting to be sent).
	simulatedTime   int64                          // 🕒 Stores the internal simulation clock (Unix timestamp), decoupled from real wall-clock time.
}

// NewMothershipState initializes and returns a pointer to a new MothershipState instance.
// It allocates memory for the internal maps to prevent nil pointer panics during runtime.
func NewMothershipState() *MothershipState {
	return &MothershipState{
		latestTelemetry: make(map[uint8]common.TelemetryData),
		latestMissions:  make(map[uint8]common.Mission),
		missionQueue:    make(map[uint8][]common.Mission),
		simulatedTime:   time.Now().Unix(), // The simulation starts synchronized with the current real-world time.
	}
}

// =================================================================
// ⚙️ STATE MANAGEMENT METHODS
// =================================================================

// AddSimulatedTime advances the internal simulation clock by a specific amount of seconds.
// This is used to speed up or control the flow of time within the simulation logic.
func (s *MothershipState) AddSimulatedTime(seconds int64) {
	s.mu.Lock()         // Exclusive lock required: we are modifying the state.
	defer s.mu.Unlock() // Ensure the lock is released even if the function panics (though unlikely here).
	s.simulatedTime += seconds
}

// UpdateTelemetry inserts or updates the telemetry data for a specific rover.
// If data for the rover already exists, it is overwritten with this new packet.
func (s *MothershipState) UpdateTelemetry(data common.TelemetryData) {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.latestTelemetry[data.RoverId] = data
}

// UpdateMission registers a new mission or updates the state of an existing one in the registry.
// This is critical for tracking mission progress (e.g., from 'InProgress' to 'Completed').
func (s *MothershipState) UpdateMission(mission common.Mission) {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.latestMissions[mission.MissionId] = mission
}

// EnqueueMission adds a manually created mission to a specific rover's backlog.
// It creates the queue slice for the rover if it doesn't exist yet.
func (s *MothershipState) EnqueueMission(roverId uint8, mission common.Mission) {
	s.mu.Lock()
	defer s.mu.Unlock()
	if _, exists := s.missionQueue[roverId]; !exists {
		s.missionQueue[roverId] = make([]common.Mission, 0)
	}
	s.missionQueue[roverId] = append(s.missionQueue[roverId], mission)
	log.Printf("📥 Mission '%s' queued for Rover %d", mission.Name, roverId)
}

// DequeueMission retrieves and removes the next mission for a rover, following FIFO (First-In-First-Out) logic.
//
// Returns:
//   - 📦 *common.Mission: Pointer to the mission to be executed.
//   - 🚩 bool: 'true' if a mission was found, 'false' if the queue was empty or the rover had no entries.
func (s *MothershipState) DequeueMission(roverId uint8) (*common.Mission, bool) {
	s.mu.Lock()
	defer s.mu.Unlock()

	queue, exists := s.missionQueue[roverId]
	if !exists || len(queue) == 0 {
		return nil, false // No missions available for this rover.
	}

	// FIFO Logic:
	// 1. Capture the first element (index 0).
	mission := queue[0]
	// 2. Reslice the queue to exclude the first element (effectively removing it).
	s.missionQueue[roverId] = queue[1:]

	return &mission, true
}

// GetMission safely retrieves a copy of a mission object by its unique ID.
// Using RLock allows concurrent reads of missions without blocking other readers.
func (s *MothershipState) GetMission(id uint8) (common.Mission, bool) {
	s.mu.RLock()
	defer s.mu.RUnlock()
	m, exists := s.latestMissions[id]
	return m, exists
}

// getLatestTelemetry returns a filtered list of telemetry data for "active" rovers.
// A rover is considered active if it has sent a heartbeat/data packet within the last 15 seconds.
// This prevents the UI from displaying stale data from rovers that may have disconnected.
func (s *MothershipState) getLatestTelemetry() []common.TelemetryData {
	s.mu.RLock()
	defer s.mu.RUnlock()

	// Logic: Define a cutoff time based on real server time (not simulated time)
	// because network connectivity is a real-world constraint.
	realTimeThreshold := time.Now().Unix() - 15

	telemetryList := make([]common.TelemetryData, 0)
	for _, data := range s.latestTelemetry {
		// Only include rovers seen recently
		if data.Timestamp > realTimeThreshold {
			telemetryList = append(telemetryList, data)
		}
	}
	return telemetryList
}

// getMissionsList returns a flat slice containing all missions currently tracked by the Mothership.
// This slice is used to populate the mission registry table in the API/Frontend.
func (s *MothershipState) getMissionsList() []common.Mission {
	s.mu.RLock()
	defer s.mu.RUnlock()

	// Pre-allocate the slice capacity to avoid unnecessary memory re-allocations during the loop.
	missionList := make([]common.Mission, 0, len(s.latestMissions))
	for _, m := range s.latestMissions {
		missionList = append(missionList, m)
	}
	return missionList
}

// =================================================================
// 📡 API REQUEST HANDLERS
// =================================================================

// HandleGetTelemetry responds to GET requests with a JSON array of active rovers and their sensors.
//
// 📍 Endpoint: /api/telemetry/latest
func (s *MothershipState) HandleGetTelemetry(w http.ResponseWriter, r *http.Request) {
	telemetryData := s.getLatestTelemetry()

	// Set standard headers for JSON response and Cross-Origin Resource Sharing (CORS).
	w.Header().Set("Content-Type", "application/json")
	w.Header().Set("Access-Control-Allow-Origin", "*")

	// Encode the data directly to the ResponseWriter stream.
	if err := json.NewEncoder(w).Encode(telemetryData); err != nil {
		log.Printf("Error serializing telemetry JSON: %v", err)
		http.Error(w, "Error processing data", http.StatusInternalServerError)
	}
}

// HandleGetMissions responds to GET requests with a JSON array of all missions.
//
// 📍 Endpoint: /api/missions
func (s *MothershipState) HandleGetMissions(w http.ResponseWriter, r *http.Request) {
	missions := s.getMissionsList()

	w.Header().Set("Content-Type", "application/json")
	w.Header().Set("Access-Control-Allow-Origin", "*")

	if err := json.NewEncoder(w).Encode(missions); err != nil {
		log.Printf("Error serializing missions JSON: %v", err)
		http.Error(w, "Error processing data", http.StatusInternalServerError)
	}
}

// CreateMissionRequest acts as a Data Transfer Object (DTO) for parsing the incoming JSON payload
// when a user submits a new mission via the Ground Control interface.
type CreateMissionRequest struct {
	RoverId uint8              `json:"rover_id"`
	Name    string             `json:"name"`
	Task    string             `json:"task"`
	Type    common.MissionType `json:"type"`
}

// HandleCreateMission processes POST requests to create and enqueue new missions.
// It validates the request method, parses the JSON body, configures mission parameters
// (like max duration) based on the mission type, and adds it to the queue.
//
// 📍 Endpoint: /api/missions/create
func (s *MothershipState) HandleCreateMission(w http.ResponseWriter, r *http.Request) {
	// 1. Enforce HTTP Method
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	// 2. Decode JSON Body
	var req CreateMissionRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid JSON", http.StatusBadRequest)
		return
	}

	// 3. Construct the Domain Object
	newMission := common.NewMission(0, req.Name, common.MARS, req.Type)
	newMission.RoverId = req.RoverId
	newMission.Tarefa = req.Task

	// 4. Apply Business Logic: Set Max Duration based on Mission Type
	switch req.Type {
	case common.MapeamentoGlobal:
		newMission.DuracaoMaxima = 3600
	case common.EstudoAmbiental:
		newMission.DuracaoMaxima = 1800
	default:
		newMission.DuracaoMaxima = 600
	}

	// 5. Enqueue safely
	s.EnqueueMission(req.RoverId, *newMission)

	// 6. Send Success Response
	w.Header().Set("Content-Type", "application/json")
	w.Header().Set("Access-Control-Allow-Origin", "*")
	w.WriteHeader(http.StatusCreated)
	json.NewEncoder(w).Encode(map[string]string{"status": "queued"})
}

// HandleRoot provides a health-check endpoint that returns general system statistics.
// This is useful for debugging and checking if the API is reachable.
//
// 📍 Endpoint: /
func (s *MothershipState) HandleRoot(w http.ResponseWriter, r *http.Request) {
	if r.URL.Path != "/" {
		http.NotFound(w, r)
		return
	}

	// Acquire Read Lock to calculate stats consistently
	s.mu.RLock()
	activeRovers := s.getLatestTelemetry()
	roverCount := len(activeRovers)
	missionCount := len(s.latestMissions)
	queueCount := 0
	for _, q := range s.missionQueue {
		queueCount += len(q)
	}
	simTime := s.simulatedTime
	s.mu.RUnlock()

	// Construct response map
	response := map[string]interface{}{
		"status":      "Mothership API Online",
		"version":     "1.1",
		"server_time": simTime, // Exposes the synchronized simulation time to clients
		"stats": map[string]int{
			"active_rovers":   roverCount,
			"active_missions": missionCount,
			"queued_missions": queueCount,
		},
		"endpoints": []string{
			"/api/telemetry/latest",
			"/api/missions",
			"POST /api/missions/create",
		},
	}

	w.Header().Set("Content-Type", "application/json")
	w.Header().Set("Access-Control-Allow-Origin", "*")

	json.NewEncoder(w).Encode(response)
}

// StartServer configures the HTTP routes and launches the API server in a background goroutine.
// It listens on the port defined in the network configuration.
func StartServer(state *MothershipState) {
	log.Printf("Observation API (Mothership) registering routes...")

	// Register handlers to specific URL paths
	http.HandleFunc("/api/telemetry/latest", state.HandleGetTelemetry)
	http.HandleFunc("/api/missions", state.HandleGetMissions)
	http.HandleFunc("/api/missions/create", state.HandleCreateMission)
	http.HandleFunc("/", state.HandleRoot)

	log.Printf("Observation API running on %s", network.APIServerPort)

	// Launch server in a goroutine so it doesn't block the caller (Main Mothership process)
	go func() {
		// ListenAndServe blocks forever, so we handle errors if it fails to bind
		if err := http.ListenAndServe("0.0.0.0"+network.APIServerPort, nil); err != nil {
			log.Fatalf("Error starting Observation API: %v", err)
		}
	}()
}
