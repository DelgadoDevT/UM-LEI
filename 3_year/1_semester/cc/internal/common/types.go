// Package common defines shared types, constants, and utilities used throughout
// the CC-2526 rover system. It provides the core data structures for mission
// management, communication, and system state representation.
//
// The package is organized into logical groups:
//   - Mission types and configurations
//   - Communication message structures
//   - Telemetry and system data
//   - Coordinate and geometry types
//   - Utility functions
//
// All components of the system (MotherShip, rovers, API) share these common
// definitions to ensure type safety and interoperability.
package common

// TelemetryProvider is an interface for components that can provide telemetry data.
type TelemetryProvider interface {
    // GetTelemetryData returns the current telemetry data
	GetTelemetryData() *TelemetryData
}

// Coordinate represents a 3D position in the rover's coordinate system.
// Used for mission areas, points of interest, and rover positions.
type Coordinate struct {
    // X - coordinate (meters)
	X int32

    // Y - coordinate (meters)
	Y int32

    // Z - Depth coordinate (meters)
	Z int16
}

// TelemetryData contains comprehensive system status information from a rover.
type TelemetryData struct {
    // RoverId - Unique identifier of the rover
	RoverId      uint8

    // Name - Human-readable name of the rover
	Name 		 string

    // Status - Current operational status (see status constants)
	Status       uint8

    // Position - Current 3D position of the rover
	Position     Coordinate

    // Battery - Remaining battery percentage (0-100)
	Battery      uint8

    // InternalTemp - Internal temperature in Celsius
	InternalTemp float32

    // ExternalTemp - External environmental temperature in Celsius
	ExternalTemp float32

    // Humidity - Environmental humidity percentage (0-100)
	Humidity	 uint8

    // Pressure - Atmospheric pressure in hPa
	Pressure     float32

    // Radiation - Radiation level in microsieverts per hour
	Radiation    float32

    // Timestamp - Unix timestamp when telemetry was recorded
	Timestamp    int64

    // Velocity - Current speed in meters per second
	Velocity     float32

    // Direction - Current heading in degrees (0-359)
	Direction    uint16

    // Uptime - System uptime in seconds
	Uptime       uint64

    // SystemHealth - Overall system health percentage (0-100)
	SystemHealth uint8
}

// MessageType identifies the type of communication message.
type MessageType int

// Message type constants for communication protocol.
const (
    // REQUEST_MISSION - Rover requests a new mission
    REQUEST_MISSION  MessageType = iota

    // MISSION_ASSIGNMENT - MotherShip assigns a mission to rover
    MISSION_ASSIGNMENT

    // MISSION_UPDATE - Rover sends mission progress update
    MISSION_UPDATE

    // MISSION_ACK - Acknowledgment for received messages
    MISSION_ACK
)

// MissionStatus represents the current state of a mission.
type MissionStatus int

// Mission status constants for mission lifecycle.
const (
    // PLANNED - Mission is planned but not yet started
    PLANNED MissionStatus = iota

    // IN_PROGRESS - Mission is currently in progress
    IN_PROGRESS

    // COMPLETED - Mission completed successfully
    COMPLETED

    // FAILED - Mission failed to complete
    FAILED
)

// AckStatus represents the acknowledgment status for communications.
type AckStatus int

// Acknowledgment status constants for communication reliability.
const (
    // ACK_SUCCESS - Message received and processed successfully
    ACK_SUCCESS AckStatus = iota

    // ACK_ERROR - Error processing message
    ACK_ERROR

    // ACK_RETRY - Request retransmission of message
    ACK_RETRY

    // ACK_INVALID - Message format invalid
    ACK_INVALID
)

// String returns the string representation of an AckStatus.
// Implements the Stringer interface for AckStatus.
func (a AckStatus) String() string {
    switch a {
    case ACK_SUCCESS:
        return "SUCCESS"
    case ACK_ERROR:
        return "ERROR" 
    case ACK_INVALID:
        return "INVALID"
    default:
        return "UNKNOWN"
    }
}

// MissionAssignment contains all information needed to assign a mission to a rover.
type MissionAssignment struct {
    // Type - Message type (always MISSION_ASSIGNMENT)
    Type              MessageType

    // SeqNum - Unique sequence number for this message
    SeqNum            uint32

    // Version - Protocol version
    Version           uint8

    // Timestamp - Creation timestamp
    Timestamp         int64

    // RoverId - Target rover identifier
    RoverId           uint8

    // RoverName - Target rover name
    RoverName         string

    // MissionId - Unique mission identifier
    MissionId         uint8

    // MissionConfig - Mission type configuration
    MissionConfig     MissionTypeConfig

    // Tarefa - Mission task description
    Tarefa            string

    // AreaGeografica - Geographical area for the mission
    AreaGeografica    []Coordinate

    // PontosInteresse - Points of interest within the area
    PontosInteresse   []Coordinate

    // Atualizacoes - Number of progress updates expected
    Atualizacoes      uint32

    // Prioridade - Mission priority (1-5, 5 highest)
    Prioridade        uint8

    // ToleranciaFalhas - Fault tolerance percentage (0-100)
    ToleranciaFalhas  uint8

    // DataInicial - Mission start timestamp
    DataInicial       int64

    // DuracaoMaxima - Maximum mission duration in seconds
    DuracaoMaxima     uint32
}

// MissionProgress contains progress updates from an active mission.
type MissionProgress struct {
    // Type - Message type (always MISSION_UPDATE)
    Type               MessageType

    // SeqNum - Unique sequence number for this update
    SeqNum             uint32

    // Version - Protocol version
    Version            uint8

    // Timestamp - Update creation timestamp
    Timestamp          int64

    // RoverId - Source rover identifier
    RoverId            uint8

    // MissionId - Mission identifier being updated
    MissionId          uint8

    // Progress - Completion percentage (0.0-100.0)
    Progress           float32

    // Status - Current mission status
    Status             MissionStatus

    // CurrentPosition - Optional current rover position
    CurrentPosition    *Coordinate

    // NrImagesCaptured - Optional number of images captured
    NrImagesCaptured   *uint32

    // Diagnostics - Optional diagnostic information
    Diagnostics        *string

    // NrSamplesCollected - Optional number of samples collected
    NrSamplesCollected *uint8
}

// MissionRequest is sent by rovers to request a new mission.
type MissionRequest struct {
    // Type - Message type (always REQUEST_MISSION)
    Type         MessageType

    // SeqNum - Unique sequence number for this request
    SeqNum       uint32

    // Version - Protocol version
    Version      uint8

    // Timestamp - Request creation timestamp
    Timestamp    int64

    // RoverId - Requesting rover identifier
    RoverId      uint8

    // RoverName - Requesting rover name
    RoverName    string
}

// MissionAck acknowledges receipt of a message.
type MissionAck struct {
    // Type - Message type (always MISSION_ACK)
    Type      MessageType

    // SeqNum - Unique sequence number for this acknowledgment
    SeqNum    uint32

    // Version - Protocol version
    Version   uint8

    // Timestamp - Acknowledgment creation timestamp
    Timestamp int64

    // AckForSeq - Sequence number being acknowledged
    AckForSeq uint32

    // RoverId - Rover identifier
    RoverId   uint8

    // Status - Acknowledgment status
    Status    AckStatus
}

// AtmosphericComposition contains detailed atmospheric measurements.
type AtmosphericComposition struct {
    // CO2 - Carbon dioxide concentration (ppm)
	CO2  float32

    // O2 - Oxygen concentration (ppm)
	O2   float32

    // CO - Carbon monoxide concentration (ppm)
	CO   float32

    // NO2 - Nitrogen dioxide concentration (ppm)
	NO2  float32

    // SO2 - Sulfur dioxide concentration (ppm)
	SO2  float32

    // O3 - Ozone concentration (ppm)
	O3   float32

    // CH4 - Methane concentration (ppm)
	CH4  float32

    // H2O - Water vapor concentration (ppm)
	H2O  float32

    // Dust - Particulate matter concentration (μg/m³)
	Dust float32
}

// Rover operational status constants.
const (
    // IDLE - Rover is idle and ready for mission
	IDLE = iota

    // MAPPING - Rover is performing mapping mission
	MAPPING

    // ENVIRONMENTAL - Rover is performing environmental study
	ENVIRONMENTAL

    // RECONNAISANCE - Rover is performing reconnaissance
	RECONNAISANCE

    // DIAGNOSTIC - Rover is performing diagnostic check
	DIAGNOSTIC

    // ANALYSIS - Rover is analyzing samples/data
	ANALYSIS

    // ERROR - Rover is in error state
	ERROR
)