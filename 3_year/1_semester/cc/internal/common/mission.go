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

import "errors"

// Mission represents a complete mission definition with all parameters.
type Mission struct {
    // MissionId - Unique identifier for the mission
	MissionId        uint8

    // Name - Human-readable mission name
	Name             string

    // Planet - Target planet for the mission
	Planet           Planet

    // Type - Mission type
	Type             MissionType

    // Tarefa - Specific task description
	Tarefa           string

    // AreaGeografica - Geographical area coordinates
	AreaGeografica   []Coordinate

    // DuracaoMaxima - Maximum allowed duration in seconds
	DuracaoMaxima    uint32

    // Atualizacoes - Expected number of progress updates
	Atualizacoes     uint32

    // Estado - Current mission state (PLANEADA, EM_PROGRESSO, etc.)
	Estado           string

    // Prioridade - Priority level (1-5)
	Prioridade       uint8

    // ToleranciaFalhas - Fault tolerance percentage
	ToleranciaFalhas uint8

    // PontosInteresse - Points of interest coordinates
	PontosInteresse  []Coordinate

    // DataInicial - Start timestamp (0 if not started)
	DataInicial      int64

    // RoverId - Assigned rover identifier (0 if unassigned)
	RoverId          uint8

    // RoverName - Assigned rover name
	RoverName        string

    // Progresso - Completion percentage (0.0-100.0)
	Progresso        float32
}

// NewMission creates a new mission with sensible defaults.
//
// Parameters:
//   - id: Unique mission identifier
//   - name: Human-readable mission name
//   - planet: Target planet (currently only MARS)
//   - mtype: Mission type
//
// Returns:
//   - *Mission: New mission with initialized fields
//
// Default values:
//   - Tarefa: Empty string
//   - AreaGeografica: Empty slice
//   - DuracaoMaxima: 0
//   - Atualizacoes: 60 (updates per hour)
//   - Estado: "PLANEADA" (planned)
//   - Prioridade: 3 (medium)
//   - ToleranciaFalhas: 10
//   - PontosInteresse: Empty slice
//   - DataInicial: 0
//   - RoverId: 0 (unassigned)
//   - RoverName: Empty string
//   - Progresso: 0.0
func NewMission(id uint8, name string, planet Planet, mtype MissionType) *Mission {
	return &Mission{
		MissionId:        id,
		Name:             name,
		Planet:           planet,
		Type:             mtype,
		Tarefa:           "",
		AreaGeografica:   []Coordinate{},
		DuracaoMaxima:    0,
		Atualizacoes:     60,
		Estado:           "PLANEADA",
		Prioridade:       3,
		ToleranciaFalhas: 10,
		PontosInteresse:  []Coordinate{},
		DataInicial:      0,
		RoverId:          0,
		RoverName:        "",
		Progresso:        0.0,
	}
}

// UpdateFrom updates this mission's fields from another mission.
//
// Parameters:
//   - other: Mission to copy fields from
//
// Returns:
//   - error: nil if successful, error if missions are nil
//
// This method performs a deep copy of slice fields (AreaGeografica,
// PontosInteresse) to avoid shared references. MissionId is NOT copied
// to maintain unique identity.
func (m *Mission) UpdateFrom(other *Mission) error {
	if m == nil || other == nil {
		return errors.New("nil mission")
	}
	m.Name = other.Name
	m.Type = other.Type
	m.Tarefa = other.Tarefa
	m.AreaGeografica = append([]Coordinate(nil), other.AreaGeografica...)
	m.DuracaoMaxima = other.DuracaoMaxima
	m.Atualizacoes = other.Atualizacoes
	m.Estado = other.Estado
	m.Prioridade = other.Prioridade
	m.ToleranciaFalhas = other.ToleranciaFalhas
	m.PontosInteresse = append([]Coordinate(nil), other.PontosInteresse...)
	m.DataInicial = other.DataInicial
	m.RoverId = other.RoverId
	m.RoverName = other.RoverName
	m.Progresso = other.Progresso
	return nil
}