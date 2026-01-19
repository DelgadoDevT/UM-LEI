package groundcontrol

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/signal"
	"sort"
	"strconv"
	"syscall"
	"time"

	"cc.2526/internal/common"
	"cc.2526/internal/network"
)

// mothershipAPI holds the complete base URL (http://ip:port) of the Mothership's API.
// This is used by all helper functions to fetch data.
var mothershipAPI string

// =================================================================
// 🌍 GROUND CONTROL SERVER
// =================================================================

// Server encapsulates the configuration and dependencies of the Ground Control web server.
// It acts as the bridge between the user (Browser) and the Mothership API.
type Server struct {
	// Future extension: Database connections or authentication services could be stored here.
}

// NewServer factory function creates a new instance of the Server struct.
func NewServer() *Server {
	return &Server{}
}

// Start initializes the HTTP server, configures routes, serves static assets,
// and implements a Graceful Shutdown mechanism to ensure clean termination.
//
// 🚀 Lifecycle:
//  1. Configures static file server.
//  2. Registers application routes.
//  3. Starts listening in a background goroutine.
//  4. Blocks main thread waiting for OS interrupt signals (Ctrl+C).
//  5. Performs graceful shutdown (waiting for active requests) upon signal.
//
// Parameters:
//   - listenPort: The local port where the UI will be accessible (e.g., ":8060").
//   - mothershipIP: The IP address of the Mothership backend to connect to.
//   - groundControlIp: The public/local IP of this machine, used only for console display instructions.
func (s *Server) Start(listenPort string, mothershipIP string, groundControlIp string) {
	// Construct the target API URL
	mothershipAPI = "http://" + mothershipIP + network.APIServerPort

	// 1. Static File Server Configuration
	// This serves CSS, JS, and Font files directly from the ./static directory.
	// `http.StripPrefix` is needed so that the URL "/static/file.css" maps to "./static/file.css".
	fs := http.FileServer(http.Dir("./static"))
	http.Handle("/static/", http.StripPrefix("/static/", fs))

	// 2. Route Registration
	// Connects URL paths to their specific handler functions.
	http.HandleFunc("/", homeHandler)                        // Dashboard View
	http.HandleFunc("/mission/submit", submitMissionHandler) // Form Submission Endpoint

	// 3. Server Configuration
	// We create a custom http.Server struct to allow for manual shutdown later.
	srv := &http.Server{
		Addr:    listenPort,
		Handler: nil, // 'nil' tells it to use the DefaultServeMux (where we registered handlers above).
	}

	// Cosmetic: Print banner and access instructions to the console
	fmt.Println("====================================================")
	fmt.Println("🌍 GROUND CONTROL (Web Interface)")
	fmt.Printf("👉 Access via Browser: http://%s%s\n", groundControlIp, listenPort)
	fmt.Println("----------------------------------------------------")
	fmt.Printf("📡 Data Source: %s\n", mothershipAPI)
	fmt.Println("====================================================")

	// 4. Non-Blocking Start
	// We run ListenAndServe in a separate goroutine. If we ran it in the main thread,
	// the code execution would block here, preventing us from listening for shutdown signals.
	go func() {
		// http.ErrServerClosed is a normal error indicating the server stopped via Shutdown(), so we ignore it.
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("❌ Critical error starting server: %v", err)
		}
	}()

	// 5. Graceful Shutdown Implementation
	// We create a buffered channel to receive OS signals.
	quit := make(chan os.Signal, 1)

	// signal.Notify binds the channel to specific system signals:
	// - SIGINT: Sent by CTRL+C
	// - SIGTERM: Sent by termination commands (like docker stop or kill)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)

	// BLOCKING: The main thread pauses here until a signal is received in the 'quit' channel.
	<-quit
	log.Println("\n🛑 Signal received. Shutting down Ground Control...")

	// 6. Cleanup Phase
	// context.WithTimeout creates a context that expires after 5 seconds.
	// This gives existing requests (like a slow file download) 5 seconds to finish before being forcibly killed.
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel() // Ensure resources are released

	// srv.Shutdown stops accepting new connections and waits for active ones to finish (up to the timeout).
	if err := srv.Shutdown(ctx); err != nil {
		log.Fatalf("❌ Error forcing shutdown: %v", err)
	}

	log.Println("✅ Server shut down and port released successfully.")
}

// =================================================================
// 📊 VIEW MODELS (Data Structures)
// =================================================================

// PageData aggregates all the information required to render the HTML template.
// This separates the raw data from the visual presentation layer (ViewModel pattern).
type PageData struct {
	Title        string
	LastUpdate   string
	Rovers       []RoverDisplay   // Processed list of rovers tailored for display
	Missions     []MissionDisplay // Processed list of missions tailored for display
	Mothership   string           // API URL for client-side JS calls
	RoverCount   int
	MissionCount int
	Message      string // Feedback message (e.g., "Mission Sent successfully")
	TimeFactor   int    // Simulation speed factor
	ServerTime   int64  // Synchronized server time
}

// RoverDisplay is a "Decorator" struct. It wraps raw TelemetryData with
// pre-calculated CSS classes and formatted strings to simplify the HTML template logic.
type RoverDisplay struct {
	common.TelemetryData
	StatusString    string // Human-readable status (e.g., "STANDBY")
	PositionStr     string // Formatted "[X, Y, Z]"
	BatteryClass    string // CSS class: "text-success", "text-warning", etc.
	HealthClass     string // CSS class for health
	UptimeFormatted string // HHh MMm SSs format
}

// MissionDisplay is similar to RoverDisplay but for Mission objects.
// It handles formatting of progress bars, time strings, and state badges.
type MissionDisplay struct {
	MissionId   uint8
	RoverName   string
	Tarefa      string
	Tipo        string
	Estado      string
	Progresso   string // Formatted percentage "XX.X%"
	EstadoClass string // CSS badge class
	Prioridade  uint8
	TimeInfo    string // Elapsed time string or "N/A"
}

// =================================================================
// 🔌 HTTP CLIENTS & HANDLERS
// =================================================================

// getTelemetryData performs an HTTP GET to the Mothership to fetch raw rover data.
// It includes a short timeout (2s) to prevent the UI from hanging if the backend is down.
func getTelemetryData() []common.TelemetryData {
	client := &http.Client{Timeout: 2 * time.Second}
	resp, err := client.Get(mothershipAPI + "/api/telemetry/latest")
	if err != nil {
		return nil // Return nil so the UI can show an "Offline" state
	}
	defer resp.Body.Close()

	var telemetryList []common.TelemetryData
	if err := json.NewDecoder(resp.Body).Decode(&telemetryList); err != nil {
		return nil
	}
	return telemetryList
}

// getMissionsData fetches the full list of missions from the backend.
func getMissionsData() []common.Mission {
	client := &http.Client{Timeout: 2 * time.Second}
	resp, err := client.Get(mothershipAPI + "/api/missions")
	if err != nil {
		return nil
	}
	defer resp.Body.Close()

	var missionList []common.Mission
	if err := json.NewDecoder(resp.Body).Decode(&missionList); err != nil {
		return nil
	}
	return missionList
}

// MothershipMeta defines the structure for the root "/" JSON response.
type MothershipMeta struct {
	Status     string `json:"status"`
	ServerTime int64  `json:"server_time"`
}

// getMothershipMeta fetches server metadata, specifically the 'server_time'
// to synchronize the frontend clock with the simulation.
func getMothershipMeta() MothershipMeta {
	client := &http.Client{Timeout: 2 * time.Second}
	resp, err := client.Get(mothershipAPI + "/")

	// Default fallback values in case the server is unreachable
	meta := MothershipMeta{
		Status:     "Offline",
		ServerTime: time.Now().Unix(),
	}

	if err != nil {
		return meta
	}
	defer resp.Body.Close()

	json.NewDecoder(resp.Body).Decode(&meta)
	return meta
}

// homeHandler serves the main dashboard page.
// It delegates the data gathering to 'prepareAndRender'.
func homeHandler(w http.ResponseWriter, r *http.Request) {
	prepareAndRender(w, "")
}

// submitMissionHandler processes the form submission from the dashboard.
// It supports both standard form POSTs (page reload) and AJAX requests (JSON response).
func submitMissionHandler(w http.ResponseWriter, r *http.Request) {
	// Security check: Only allow POST method
	if r.Method != http.MethodPost {
		http.Redirect(w, r, "/", http.StatusSeeOther)
		return
	}

	// Parse form data (application/x-www-form-urlencoded)
	if err := r.ParseForm(); err != nil {
		if r.Header.Get("Accept") == "application/json" {
			http.Error(w, `{"status":"error", "message":"Form parse error"}`, 400)
			return
		}
		prepareAndRender(w, "Error processing form")
		return
	}

	// Extract values
	roverId, _ := strconv.Atoi(r.FormValue("roverId"))
	name := r.FormValue("missionName")
	task := r.FormValue("task")
	missionType := r.FormValue("type")

	// Construct payload for the Mothership API
	payload := map[string]interface{}{
		"rover_id": roverId,
		"name":     name,
		"task":     task,
		"type":     missionType,
	}

	jsonPayload, _ := json.Marshal(payload)

	// Forward the request to the Mothership
	resp, err := http.Post(mothershipAPI+"/api/missions/create", "application/json", bytes.NewBuffer(jsonPayload))

	msg := ""
	status := "success"

	// Analyze response from Mothership
	if err != nil {
		msg = "❌ UPLINK FAILURE"
		status = "error"
	} else if resp.StatusCode == http.StatusCreated {
		msg = "✅ MISSION PACKAGE SENT"
	} else {
		msg = fmt.Sprintf("⚠️ UPLINK ERROR: %d", resp.StatusCode)
		status = "error"
	}

	if resp != nil {
		resp.Body.Close()
	}

	// Smart Response:
	// If the client explicitly requested JSON (e.g., the JavaScript AJAX call), return JSON.
	if r.Header.Get("Accept") == "application/json" {
		w.Header().Set("Content-Type", "application/json")
		response := map[string]string{
			"status":  status,
			"message": msg,
		}
		json.NewEncoder(w).Encode(response)
		return
	}

	// Otherwise, re-render the HTML page with the status message injected.
	prepareAndRender(w, msg)
}

// prepareAndRender acts as a Controller. It orchestrates fetching data,
// sorting lists, applying view logic (decorators), and triggering the template engine.
func prepareAndRender(w http.ResponseWriter, message string) {
	telemetryList := getTelemetryData()
	missionList := getMissionsData()
	meta := getMothershipMeta() // Sync time

	// 1. Sort lists for visual stability
	// (Prevents items from jumping around when the page refreshes)
	sort.Slice(telemetryList, func(i, j int) bool {
		return telemetryList[i].RoverId < telemetryList[j].RoverId
	})

	// Transform raw telemetry into View Models (RoverDisplay)
	var rovers []RoverDisplay
	for _, t := range telemetryList {
		rovers = append(rovers, RoverDisplay{
			TelemetryData:   t,
			StatusString:    getStatusString(t.Status),
			PositionStr:     fmt.Sprintf("[%d, %d, %d]", t.Position.X, t.Position.Y, t.Position.Z),
			BatteryClass:    getBatteryClass(t.Battery),
			HealthClass:     getHealthClass(t.SystemHealth),
			UptimeFormatted: formatUptime(t.Uptime),
		})
	}

	// 2. Sort Missions
	sort.Slice(missionList, func(i, j int) bool {
		return missionList[i].MissionId < missionList[j].MissionId
	})

	// Transform raw missions into View Models (MissionDisplay)
	var missions []MissionDisplay
	for _, m := range missionList {
		timeInfo := "N/A"
		if m.Estado == "EM_PROGRESSO" {
			timeInfo = "T+00h 00m 00s" // Placeholder, JS will update this
		}

		missions = append(missions, MissionDisplay{
			MissionId:   m.MissionId,
			RoverName:   m.RoverName,
			Tarefa:      m.Tarefa,
			Tipo:        string(m.Type),
			Estado:      m.Estado,
			Progresso:   fmt.Sprintf("%.1f%%", m.Progresso),
			EstadoClass: getMissionStateClass(m.Estado),
			Prioridade:  m.Prioridade,
			TimeInfo:    timeInfo,
		})
	}

	// Assemble final Data Object for the template
	data := PageData{
		Title:        "GROUND CONTROL",
		LastUpdate:   time.Now().Format("15:04:05"),
		Rovers:       rovers,
		Missions:     missions,
		Mothership:   mothershipAPI,
		RoverCount:   len(rovers),
		MissionCount: len(missions),
		Message:      message,
		TimeFactor:   int(network.TimeFactorEqualizer.Seconds()),
		ServerTime:   meta.ServerTime, // Inject server time for JS clock
	}

	RenderTemplate(w, data)
}
