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

// MissionType represents the type of mission a rover can perform.
// Each mission type has specific configuration parameters and behaviors.
type MissionType string

// Mission type constants used throughout the system.
const (
    // MapeamentoGlobal - Global topographic mapping of terrain
	MapeamentoGlobal           MissionType = "map_global"

    // EstudoAmbiental - Comprehensive environmental study of planetary conditions
	EstudoAmbiental            MissionType = "estudo_ambient"

    // ReconFotografico - High-resolution photographic reconnaissance
	ReconFotografico           MissionType = "recon_foto"

    // DiagnosticoTecnico - Technical diagnostics of rover systems
	DiagnosticoTecnico         MissionType = "diagn_tecnico"

    // AmostragemAmbiental - Environmental sample collection for analysis
	AmostragemAmbiental        MissionType = "amostra_ambient"
)


// MissionTypeConfig contains configuration for a specific mission type.
// It includes type metadata, description, and type-specific configuration.
type MissionTypeConfig struct {
    // Type identifies the mission type
	Type               MissionType

    // Description provides a human-readable mission description
	Description        string

    // Config contains type-specific configuration parameters
	Config             interface{}
}

// NavigationMode defines how a rover navigates during mapping missions.
type NavigationMode int

// Navigation mode constants for mapping missions.
const (
    // SPIRAL - Navigate in a spiral pattern from center outward
	SPIRAL NavigationMode = iota

    // RANDOM - Navigate randomly within the area
	RANDOM

    // EXHAUSTIVE_COVERAGE - Systematic coverage of entire area
	EXHAUSTIVE_COVERAGE
)

// String returns the string representation of a NavigationMode.
// Implements the Stringer interface for NavigationMode.
func (n NavigationMode) String() string {
    switch n {
    case SPIRAL:
        return "SPIRAL"
    case RANDOM:
        return "RANDOM"
    case EXHAUSTIVE_COVERAGE:
        return "EXHAUSTIVE_COVERAGE"
    default:
        return "RANDOM"
    }
}

// SampleGenre categorizes the type of environmental samples collected.
type SampleGenre int

// Sample genre constants for environmental sampling.
const (
    // BIOLOGICAL - Biological samples (microorganisms, organic material)
	BIOLOGICAL SampleGenre = iota

    // GEOLOGICAL - Geological samples (rocks, soil, minerals)
	GEOLOGICAL

    // FLUID - Fluid samples (water, liquid compounds)
	FLUID
)

// String returns the string representation of a SampleGenre.
// Implements the Stringer interface for SampleGenre.
func (s SampleGenre) String() string {
    switch s {
    case BIOLOGICAL:
        return "BIOLOGICAL"
    case GEOLOGICAL:
        return "GEOLOGICAL"
    case FLUID:
        return "FLUID"
    default:
        return "GEOLOGICAL"
    }
}

// DiagnosticResult represents the outcome of technical diagnostic missions.
type DiagnosticResult string

// Diagnostic result constants for technical diagnostics.
const (
    // DiagnosticSuccess - All systems functioning correctly
    DiagnosticSuccess DiagnosticResult = "SUCCESS"

    // DiagnosticFail - One or more systems have failures
    DiagnosticFail    DiagnosticResult = "FAIL"

    // DiagnosticUnclear - Diagnostic results are ambiguous
    DiagnosticUnclear DiagnosticResult = "UNCLEAR"
)

// String returns the string representation of a DiagnosticResult.
// Implements the Stringer interface for DiagnosticResult.
func (d DiagnosticResult) String() string {
	switch d {
	case DiagnosticSuccess:
		return "SUCCESS"
	case DiagnosticFail:
		return "FAIL"
	case DiagnosticUnclear:
		return "UNCLEAR"
	default:
		return "SUCCESS"
	}
}

// MappingGlobalConfig contains configuration for global mapping missions.
type MappingGlobalConfig struct {
        // NavigationMode determines the pattern used for area coverage
	NavigationMode NavigationMode
}

// EnvironmentalStudyConfig contains configuration for environmental study missions.
type EnvironmentalStudyConfig struct {
    // Humidity - Target humidity percentage (0-100)
	Humidity        uint8

    // Temperature - Target temperature in Celsius
	Temperature     float32

    // Pressure - Target atmospheric pressure in hPa
	Pressure        float32

    // Radiation - Radiation level in Sieverts per hour
	Radiation       float32

    // AirQuality - Atmospheric composition measurements
	AirQuality      AtmosphericComposition

    // CaptureInterval - Time between measurements in seconds
	CaptureInterval uint32
}

// PhotoReconConfig contains configuration for photographic reconnaissance missions.
type PhotoReconConfig struct {
    // MaxImages - Maximum number of images to capture
	MaxImages        uint32

    // CaptureInterval - Time between photo captures in seconds
	CaptureInterval  uint32

    // ResolutionWidth - Image width in pixels
	ResolutionWidth  uint32

    // ResolutionHeight - Image height in pixels
	ResolutionHeight uint32
}

// TechDiagnosticConfig contains configuration for technical diagnostic missions.
type TechDiagnosticConfig struct {
    // ComponentsToCheck - List of system components to diagnose
    ComponentsToCheck []string

    // Result - Expected or actual diagnostic outcome
    Result            DiagnosticResult
}

// EnvironmentalSamplingConfig contains configuration for environmental sampling missions.
type EnvironmentalSamplingConfig struct {
    // SampleGenre - Type of samples to collect
	SampleGenre    SampleGenre

    // SampleCount - Number of samples to collect
	SampleCount    uint8
}

// GetMissionTypeConfig returns the default configuration for a given mission type.
//
// Parameters:
//   - missionType: The mission type to get configuration for
//
// Returns:
//   - MissionTypeConfig: Default configuration including type-specific parameters
//
// The function provides sensible defaults for each mission type. If an unknown
// mission type is provided, it returns the configuration for MapeamentoGlobal.
//
// Example:
//   config := GetMissionTypeConfig(common.MapeamentoGlobal)
func GetMissionTypeConfig(missionType MissionType) MissionTypeConfig {
    switch missionType {
    case MapeamentoGlobal:
        return MissionTypeConfig{
            Type:        MapeamentoGlobal,
            Description: "Mapeamento topografico global do terreno",
            Config: &MappingGlobalConfig{
                NavigationMode: SPIRAL,
            },
        }

    case EstudoAmbiental:
        return MissionTypeConfig{
            Type:        EstudoAmbiental,
            Description: "Estudo ambiental completo das condicoes planetarias",
            Config: &EnvironmentalStudyConfig{
                Humidity:        50,
                Temperature:     15.0,
                Pressure:        1013.25,
                Radiation:       0.1,
                AirQuality:      AtmosphericComposition{},
                CaptureInterval: 60,
            },
        }

    case ReconFotografico:
        return MissionTypeConfig{
            Type:        ReconFotografico,
            Description: "Reconhecimento fotografico de alta resolucao",
            Config: &PhotoReconConfig{
                MaxImages:        100,
                CaptureInterval:  10,
                ResolutionWidth:  1920,
                ResolutionHeight: 1080,
            },
        }

    case DiagnosticoTecnico:
        return MissionTypeConfig{
            Type:        DiagnosticoTecnico,
            Description: "Diagnostico tecnico dos sistemas do rover",
            Config: &TechDiagnosticConfig{
                ComponentsToCheck: []string{"power", "comm", "sensors"},
                Result: 		  DiagnosticSuccess,
            },
        }

    case AmostragemAmbiental:
        return MissionTypeConfig{
            Type:        AmostragemAmbiental,
            Description: "Coleta de amostras ambientais para analise",
            Config: &EnvironmentalSamplingConfig{
                SampleGenre: GEOLOGICAL,
                SampleCount: 5,
            },
        }

    default:
        return MissionTypeConfig{
            Type:        MapeamentoGlobal,
            Description: "Missao padrao de mapeamento",
            Config: &MappingGlobalConfig{
                NavigationMode: SPIRAL,
            },
        }
    }
}