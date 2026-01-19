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

import (
	"sync"
)

// sequenceCounter is a global counter used to generate unique sequence numbers
// for communication messages. It starts at 1000 to avoid confusion with
// low-numbered sequences that might be used for special purposes.
//
// This variable is protected by seqMutex for thread-safe access.
var sequenceCounter uint32 = 1000

// seqMutex is a mutex that protects concurrent access to sequenceCounter.
// It ensures that GenerateUniqueSequenceNumber() can be safely called from
// multiple goroutines without race conditions.
var seqMutex sync.Mutex

// GenerateUniqueSequenceNumber generates a globally unique sequence number
// for communication messages.
//
// Returns:
//   - uint32: Unique sequence number
//
// The function uses thread-safe incrementing counter starting from 1000.
// Sequence numbers are used to track messages and match acknowledgments.
//
// Example:
//   seqNum := common.GenerateUniqueSequenceNumber()
//   // seqNum might be 1001, 1002, 1003, etc.
func GenerateUniqueSequenceNumber() uint32 {
	seqMutex.Lock()
	defer seqMutex.Unlock()
	sequenceCounter++
	return sequenceCounter
}