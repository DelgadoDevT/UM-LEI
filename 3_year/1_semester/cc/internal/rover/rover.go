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
	"math"
	"math/rand"
	"time"

	"cc.2526/internal/common"
)

// Rover represents a planetary exploration vehicle with sensors, power systems, and mission capabilities.
// It maintains telemetry data, environmental readings, and mission progress.
type Rover struct {
	RoverId            uint8             // Unique identifier for the rover
	Name               string            // Human-readable name of the rover
	Planet             common.Planet     // Planetary body where the rover is operating
	Status             uint8             // Current operational status (IDLE, MAPPING, ERROR, etc.)
	Position           common.Coordinate // Current 3D position coordinates (X, Y, Z)
	Velocity           float32           // Current movement speed in meters per second
	Direction          uint16            // Current heading direction in degrees (0-359)
	Battery            uint8             // Remaining battery capacity (0-100%)
	SolarPower         float32           // Current solar power generation rate in watts
	InternalTemp       float32           // Internal rover temperature in Kelvin
	ExternalTemp       float32           // External environmental temperature in Kelvin
	Humidity           uint8             // Ambient humidity level (0-100%)
	Pressure           float32           // Atmospheric pressure in hPa
	Radiation          float32           // Radiation level in microsieverts per hour
	Weight             float32           // Current weight of the rover in kg
	CollectionCapacity float32           // Maximum sample collection capacity in units
	StorageUsage       uint8             // Current storage usage percentage (0-100%)
	MissionProgress    uint8             // Current mission completion percentage (0-100%)
	MissionStartTime   int64             // Unix timestamp when current mission started
	SystemHealth       uint8             // Overall system health status (0-100%)
	ErrorCode          uint8             // Current error code if status is ERROR
	Uptime             uint64            // Total operational time in seconds
	CreationTime       int64             // Unix timestamp when rover was created
}

// NewRover creates a new rover instance with default values based on planet configuration.
// The rover starts at the specified coordinates with full battery and system health.
func NewRover(id uint8, name string, planet common.Planet, X int32, Y int32) *Rover {
	planetConfig := common.GetPlanetConfig(planet)

	return &Rover{
		RoverId:            id,
		Name:               name,
		Planet:             planet,
		Status:             common.IDLE,
		Position:           common.Coordinate{X: X, Y: Y, Z: 0},
		Velocity:           0.0,
		Direction:          0,
		Battery:            100,
		SolarPower:         0.0,
		InternalTemp:       planetConfig.GetBaseTemperature(),
		ExternalTemp:       planetConfig.GetBaseTemperature(),
		Humidity:           planetConfig.HumidityBase,
		Pressure:           planetConfig.AtmosphericPressure,
		Radiation:          planetConfig.RadiationBase,
		Weight:             150.0,
		CollectionCapacity: 50.0,
		StorageUsage:       0,
		MissionProgress:    0,
		MissionStartTime:   0,
		SystemHealth:       100,
		ErrorCode:          0,
		Uptime:             0,
		CreationTime:       time.Now().Unix(),
	}
}

// GetTelemetryData returns current sensor readings and status for monitoring systems.
// This data is suitable for transmission to mothership by telemetryStream protocol.
func (r *Rover) GetTelemetryData() *common.TelemetryData {
	return &common.TelemetryData{
		RoverId:      r.RoverId,
		Name:         r.Name,
		Status:       r.Status,
		Position:     r.Position,
		Battery:      r.Battery,
		InternalTemp: r.InternalTemp,
		ExternalTemp: r.ExternalTemp,
		Humidity:     r.Humidity,
		Pressure:     r.Pressure,
		Radiation:    r.Radiation,
		Timestamp:    time.Now().Unix(),
		Velocity:     r.Velocity,
		Direction:    r.Direction,
		Uptime:       r.Uptime,
		SystemHealth: r.SystemHealth,
	}
}

// Update processes one simulation tick, updating all rover systems.
// DeltaTime represents the elapsed time in seconds since the last update.
// This method coordinates sensor updates, power management, movement, and health checks.
func (r *Rover) Update(deltaTime float32) {
	planetConfig := common.GetPlanetConfig(r.Planet)
	r.Uptime += uint64(deltaTime)

	r.updateBasicSensors(planetConfig)
	r.updatePowerSystems(planetConfig)

	if (r.Status != common.IDLE && r.Status != common.ERROR) && r.Velocity > 0 {
		r.updatePosition(deltaTime)
	}

	r.updateMissionProgress()
	r.updateSystemHealth(planetConfig)
}

// updateBasicSensors simulates environmental sensor readings with daily cycles and random variations.
// Temperature follows a sine wave based on planetary day length, while humidity,
// pressure, and radiation include random fluctuations, within a constant central value.
func (r *Rover) updateBasicSensors(planetConfig common.PlanetConfig) {
	baseTemp := planetConfig.GetBaseTemperature()
	tempVariation := planetConfig.GetTemperatureVariation()

	// Simulate daily temperature cycle using sine wave
	timeOfDay := float32(r.Uptime%planetConfig.DayLength) / float32(planetConfig.DayLength)
	temperatureWave := float32(math.Sin(float64(timeOfDay * 2 * math.Pi)))

	r.ExternalTemp = baseTemp + (temperatureWave * tempVariation)

	// Internal temp slowly adjusts to external changes
	tempDifference := r.ExternalTemp - r.InternalTemp
	r.InternalTemp += tempDifference * 0.1

	// Random humidity fluctuations
	if rand.Float32() < 0.05 {
		humidityVariation := int32(rand.Intn(11)) - 5
		newHumidity := int32(r.Humidity) + humidityVariation
		if newHumidity < 0 {
			newHumidity = 0
		}
		if newHumidity > 100 {
			newHumidity = 100
		}
		r.Humidity = uint8(newHumidity)
	}

	// Random pressure fluctuations
	if rand.Float32() < 0.03 {
		pressureVariation := (rand.Float32() - 0.5) * planetConfig.AtmosphericPressure * 0.01
		r.Pressure = planetConfig.AtmosphericPressure + pressureVariation
	}

	// Random radiation fluctuations
	radiationVariation := rand.Float32() * planetConfig.RadiationBase * 0.1
	r.Radiation = planetConfig.RadiationBase + radiationVariation
}

// updatePowerSystems manages solar power generation and battery consumption.
// Solar efficiency varies by time of day, and low battery triggers safety modes.
func (r *Rover) updatePowerSystems(planetConfig common.PlanetConfig) {
	// Calculate solar efficiency based on time of day
	simulatedTime := r.CreationTime + int64(r.Uptime)
	secondsInDay := simulatedTime % 86400
	hour := secondsInDay / 3600

	var solarEfficiency float32
	switch {
	case hour >= 6 && hour < 18:
		solarEfficiency = 0.8
	case hour >= 5 && hour < 7, hour >= 17 && hour < 19:
		solarEfficiency = 0.4
	default:
		solarEfficiency = 0.0
	}

	r.SolarPower = planetConfig.SolarConstant * solarEfficiency * 0.15

	powerConsumption := float32(r.calculateBatteryDrain())

	// Charge battery if solar power exceeds consumption
	if r.SolarPower > powerConsumption {
		if r.Battery < 100 {
			r.Battery += uint8(rand.Intn(4) + 1)
			if r.Battery > 100 {
				r.Battery = 100
			}
		}
	} else {
		// Drain battery if consumption exceeds solar power
		if r.Battery > 1 {
			r.Battery -= uint8(rand.Intn(2) + 1)
			if r.Battery < 1 {
				r.Battery = 1
			}
		} else {
			r.Battery = 1
			r.Status = common.ERROR
			r.Velocity = 0
		}
	}

	// Enter idle mode if battery is critically low
	if r.Battery < 20 && r.Status != common.ERROR {
		r.Velocity = 0
		r.Status = common.IDLE
	}
}

// updatePosition moves the rover based on current velocity and direction.
// Includes simulated terrain elevation changes and random direction drift.
func (r *Rover) updatePosition(deltaTime float32) {
	if r.Velocity == 0 {
		return
	}

	// Calculate movement using trigonometry
	directionRad := float64(r.Direction) * math.Pi / 180.0
	distance := r.Velocity * deltaTime
	deltaX := distance * float32(math.Cos(directionRad))
	deltaY := distance * float32(math.Sin(directionRad))

	r.Position.X += int32(deltaX)
	r.Position.Y += int32(deltaY)

	// Simulate terrain elevation changes
	if rand.Float32() < 0.4 {
		terrainVariation := rand.Int31n(5) - 2
		r.Position.Z += int16(terrainVariation)
		if r.Position.Z < 0 {
			r.Position.Z = 0
		}
	}

	// Simulate random direction drift
	if rand.Float32() < 0.3 {
		directionVariation := int16(rand.Intn(21)) - 10
		newDirection := int32(r.Direction) + int32(directionVariation)
		if newDirection < 0 {
			newDirection += 360
		}
		r.Direction = uint16(newDirection % 360)
	}
}

// updateSystemHealth degrades system health based on environmental factors and uptime.
// Radiation, extreme temperatures, and general wear reduce system health over time.
func (r *Rover) updateSystemHealth(planetConfig common.PlanetConfig) {
	// Calculate degradation from various factors
	timeDegradation := float32(r.Uptime) * 0.0000001
	radiationEffect := (r.Radiation / planetConfig.RadiationBase) * 0.05

	tempEffect := float32(0.0)
	optimalTemp := planetConfig.GetBaseTemperature()
	tempDifference := math.Abs(float64(r.InternalTemp - optimalTemp))
	if tempDifference > 50 {
		tempEffect = float32(tempDifference) * 0.0005
	}

	totalDegradation := timeDegradation + radiationEffect + tempEffect

	// Apply health degradation
	if totalDegradation > 0.1 {
		healthReduction := uint8(totalDegradation)
		if r.SystemHealth > healthReduction {
			r.SystemHealth -= healthReduction
		} else {
			r.SystemHealth = 1
		}
	}

	// Enter error state if health is critically low
	if r.SystemHealth < 15 && r.Status != common.ERROR {
		r.Status = common.ERROR
		r.Velocity = 0
	}
}

// StartMission begins a new mission if rover is idle.
// Sets mission type, resets progress, and adjusts velocity for planetary gravity.
// Returns error if rover is not in IDLE state.
func (r *Rover) StartMission(missionType uint8) {
	switch r.Status {
	case common.IDLE:
		r.Status = missionType
		r.MissionProgress = 0
		r.MissionStartTime = time.Now().Unix()

		// Adjust velocity based on planetary gravity
		planetConfig := common.GetPlanetConfig(r.Planet)
		gravityFactor := planetConfig.Gravity / 9.81
		r.Velocity = 0.3 * gravityFactor
	case common.ERROR:
		log.Print("Cannot start mission - system in ERROR state")
	}
}

// calculateBatteryDrain returns power consumption rate based on current activity.
// Different mission types consume power at varying rates.
func (r *Rover) calculateBatteryDrain() uint8 {
	baseDrain := uint8(5)

	switch r.Status {
	case common.IDLE:
		return baseDrain
	case common.ERROR:
		return baseDrain
	case common.MAPPING, common.ENVIRONMENTAL, common.RECONNAISANCE:
		return baseDrain + 25
	case common.DIAGNOSTIC, common.ANALYSIS:
		return baseDrain + 35
	default:
		return baseDrain
	}
}

// updateMissionProgress randomly advances mission completion percentage.
// Progress increases by 3-9% per update when actively on a mission.
func (r *Rover) updateMissionProgress() {
	if r.Status == common.MAPPING || r.Status == common.ENVIRONMENTAL ||
		r.Status == common.RECONNAISANCE || r.Status == common.DIAGNOSTIC ||
		r.Status == common.ANALYSIS {

		randomIncrement := float32(3 + rand.Intn(9)) // 3 to 9
		newProgress := float32(r.MissionProgress) + randomIncrement

		if newProgress >= 100 {
			newProgress = 100
		}

		r.MissionProgress = uint8(newProgress)
	}
}
