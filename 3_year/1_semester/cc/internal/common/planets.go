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

// Planet represents a planetary body where rovers can operate.
type Planet string

const (
	VENUS Planet = "venus" // Venus: Extreme temperature, high pressure
	MARS  Planet = "mars"  // Mars: Cold, thin atmosphere, primary exploration target
	EARTH Planet = "earth" // Earth: Reference planet with habitable conditions
)

// PlanetConfig contains physical and environmental properties of a planetary body.
// These parameters affect rover operations, power generation, and system performance.
type PlanetConfig struct {
	Name                Planet  // Planetary body name
	Radius              int32   // Planetary radius in meters
	Gravity             float32 // Surface gravity in m/s²
	AtmosphericPressure float32 // Surface atmospheric pressure in hPa
	TemperatureRange    struct {
		Min float32 // Minimum surface temperature in Kelvin
		Max float32 // Maximum surface temperature in Kelvin
	}
	RadiationBase float32 // Base radiation level in microsieverts per hour
	SolarConstant float32 // Solar irradiance at planet's distance in W/m²
	DayLength     uint64  // Length of planetary day in seconds
	HumidityBase  uint8   // Base humidity level in percent
}

// GetPlanetConfig returns the planetary configuration for the specified planet.
// Provides physical constants and environmental parameters used for rover simulation.
func GetPlanetConfig(planet Planet) PlanetConfig {
	switch planet {
	case VENUS:
		return PlanetConfig{
			Name:                VENUS,
			Radius:              6051800,
			Gravity:             8.87,
			AtmosphericPressure: 9200000.0, // 92 bar
			TemperatureRange: struct{ Min, Max float32 }{
				Min: 230.0,
				Max: 737.0,
			},
			RadiationBase: 287,
			SolarConstant: 2770,
			DayLength:     10087200, // 117 days
			HumidityBase:  0,
		}

	case MARS:
		return PlanetConfig{
			Name:                MARS,
			Radius:              3389500,
			Gravity:             3.71,
			AtmosphericPressure: 6.36,
			TemperatureRange: struct{ Min, Max float32 }{
				Min: 186.0,
				Max: 293.0,
			},
			RadiationBase: 233.0,
			SolarConstant: 586.2,
			DayLength:     88775, // 24h 39m 35s
			HumidityBase:  5,
		}

	default: // EARTH, just for reference
		return PlanetConfig{
			Name:                EARTH,
			Radius:              6371000,
			Gravity:             9.81,
			AtmosphericPressure: 1013.25,
			TemperatureRange: struct{ Min, Max float32 }{
				Min: 223.0,
				Max: 323.0,
			},
			RadiationBase: 2.4,
			SolarConstant: 1361.0,
			DayLength:     86400,
			HumidityBase:  60,
		}
	}
}

// GetBaseTemperature returns the average surface temperature for the planet.
// Used as the baseline temperature for rover environmental simulations.
func (p *PlanetConfig) GetBaseTemperature() float32 {
	return (p.TemperatureRange.Min + p.TemperatureRange.Max) / 2
}

// GetTemperatureVariation returns half the temperature range for the planet.
// Used to simulate daily temperature fluctuations in rover environmental models.
func (p *PlanetConfig) GetTemperatureVariation() float32 {
	return (p.TemperatureRange.Max - p.TemperatureRange.Min) / 2
}
