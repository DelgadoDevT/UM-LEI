// Package network provides network configuration constants and error definitions
// for the rover communication system. It defines ports, timing intervals, packet sizes,
// and connection parameters used for telemetry and mission communication between
// rovers and the mothership.
package network

import (
	"errors"
	"time"
)

// Network communication errors
var (
	ErrInvalidDataLength = errors.New("invalid data length") // Returned when packet size doesn't match expected length
	ErrConnectionFailed  = errors.New("connection failed")   // Returned when network connection cannot be established
)

// Server port definitions for different services
const (
	TelemetryServerPort   = ":5001" // Port for telemetry data transmission (rover → mothership)
	MissionLinkServerPort = ":5000" // Port for mission assignment and progress communication
	APIServerPort         = ":8080" // Port for REST API and web interface
)

// Time simulation and synchronization constants
const (
	TimeFactor          = 1200 * time.Second // Acceleration factor for simulation time (1 real second = 1200 sim seconds)
	TimeFactorEqualizer = TimeFactor / 4     // Equalizer to synchronize telemetry data with system clock
)

// Communication timing intervals
const (
	TelemetryInterval        = 4 * time.Second // Interval between rover telemetry transmissions
	ReconnectDelay           = 3 * time.Second // Delay between connection retry attempts
	MaxReconnectTries        = 5               // Maximum number of connection retry attempts
	RoverTelemetryDataUpdate = 4 * time.Second // Internal rover telemetry update interval
)

// Packet size definitions for different message types (in bytes)
const (
	TelemetryPacketSize           = 64  // Size of telemetry data packets
	MissionLinkAssignPacketSize   = 512 // Size of mission assignment packets
	MissionLinkProgressPacketSize = 256 // Size of mission progress update packets
	MissionLinkRequestPacketSize  = 128 // Size of mission request packets
	MissionLinkAckPacketSize      = 32  // Size of acknowledgment packets
)

// Mission communication intervals
const (
	RoverMissionLinkDataUpdate = 8 * time.Second  // Interval for sending mission progress updates
	RoverMissionSolicition     = 16 * time.Second // Interval for requesting new missions when idle
)
