// Package serialization provides functions for serializing and deserializing
// mission-related data structures to and from byte arrays for network transmission.
package serialization

import "math"

// =================================================================
// 💾 WRITER FUNCTIONS (Little-Endian)
// =================================================================
// These functions convert Go primitive types into byte slices suitable for network transmission.
// The system uses Little-Endian byte order (least significant byte first).
//
// Example of Little-Endian (for uint16 0x1234):
// [0x34] [0x12]
//  Low    High

// Uint8ToBytes converts a single byte. It's a trivial wrapper for consistency.
func Uint8ToBytes(v uint8) []byte {
	return []byte{v}
}

// Uint16ToBytes splits a 16-bit integer into 2 bytes.
// Example: Value 0x1234 (4660)
// Byte 0: 0x34 (Low byte)
// Byte 1: 0x12 (High byte)
func Uint16ToBytes(v uint16) []byte {
	return []byte{byte(v),
		byte(v >> 8)}
}

// Int16ToBytes casts the signed int to uint16 and reuses the existing logic.
// The bit representation is preserved (Two's Complement).
func Int16ToBytes(v int16) []byte {
	return Uint16ToBytes(uint16(v))
}

// Uint32ToBytes splits a 32-bit integer into 4 bytes.
// It uses bitwise shifting to extract each 8-bit segment.
func Uint32ToBytes(v uint32) []byte {
	return []byte{
		byte(v),       // 0-7 bits
		byte(v >> 8),  // 8-15 bits
		byte(v >> 16), // 16-23 bits
		byte(v >> 24), // 24-31 bits
	}
}

func Int32ToBytes(v int32) []byte {
	return Uint32ToBytes(uint32(v))
}

// Float32ToBytes converts a float to its IEEE 754 binary representation (uint32)
// and then serializes it as bytes.
func Float32ToBytes(f float32) []byte {
	bits := math.Float32bits(f)
	return []byte{
		byte(bits),
		byte(bits >> 8),
		byte(bits >> 16),
		byte(bits >> 24),
	}
}

// UInt64ToBytes splits a 64-bit integer into 8 bytes.
func UInt64ToBytes(v uint64) []byte {
	return []byte{
		byte(v),
		byte(v >> 8),
		byte(v >> 16),
		byte(v >> 24),
		byte(v >> 32),
		byte(v >> 40),
		byte(v >> 48),
		byte(v >> 56),
	}
}

func Int64ToBytes(v int64) []byte {
	return UInt64ToBytes(uint64(v))
}

// StringFixedToBytes converts a string to a fixed-size byte slice.
// Protocol Requirement: Strings must occupy a constant number of bytes.
// - If the string is longer than 'size', it is truncated.
// - If shorter, it is padded with spaces (' ') to fill the remaining buffer.
func StringFixedToBytes(s string, size int) []byte {
	b := make([]byte, size)
	copyLength := len(s)
	if copyLength > size {
		copyLength = size
	}
	copy(b, s[:copyLength])

	// Pad remainder with spaces
	for i := copyLength; i < size; i++ {
		b[i] = ' '
	}

	return b
}

// =================================================================
// 📖 READER FUNCTIONS (Little-Endian)
// =================================================================
// These functions reconstruct Go types from a raw byte slice starting at a given 'offset'.

func BytesToUint8(data []byte, offset int) uint8 {
	return data[offset]
}

// BytesToUint16 reconstructs a uint16 from 2 bytes.
// It uses bitwise OR (|) to combine the bytes after shifting the high byte.
func BytesToUint16(data []byte, offset int) uint16 {
	return uint16(data[offset]) |
		uint16(data[offset+1])<<8
}

func BytesToInt16(data []byte, offset int) int16 {
	return int16(BytesToUint16(data, offset))
}

// BytesToUint32 reconstructs a uint32 from 4 bytes.
// Note: Each byte must be cast to uint32 BEFORE shifting to avoid overflow.
func BytesToUint32(data []byte, offset int) uint32 {
	return uint32(data[offset]) |
		uint32(data[offset+1])<<8 |
		uint32(data[offset+2])<<16 |
		uint32(data[offset+3])<<24
}

func BytesToInt32(data []byte, offset int) int32 {
	return int32(BytesToUint32(data, offset))
}

// BytesToFloat32 reads 4 bytes as a uint32 and interprets them as IEEE 754 float bits.
func BytesToFloat32(data []byte, offset int) float32 {
	bits := uint32(data[offset]) |
		uint32(data[offset+1])<<8 |
		uint32(data[offset+2])<<16 |
		uint32(data[offset+3])<<24
	return math.Float32frombits(bits)
}

func BytesToUInt64(data []byte, offset int) uint64 {
	return uint64(data[offset]) |
		uint64(data[offset+1])<<8 |
		uint64(data[offset+2])<<16 |
		uint64(data[offset+3])<<24 |
		uint64(data[offset+4])<<32 |
		uint64(data[offset+5])<<40 |
		uint64(data[offset+6])<<48 |
		uint64(data[offset+7])<<56
}

func BytesToInt64(data []byte, offset int) int64 {
	return int64(BytesToUInt64(data, offset))
}

// BytesToStringFixed reads a fixed-size string from the buffer.
// It iterates backwards from the end of the allocation to remove trailing spaces (trimming).
func BytesToStringFixed(data []byte, size int) string {
	if len(data) < size {
		panic("data too short for fixed string")
	}

	strBytes := data[:size]

	// Trim padding spaces from the right
	end := size
	for i := size - 1; i >= 0; i-- {
		if strBytes[i] != ' ' {
			end = i + 1
			break
		}
	}

	return string(strBytes[:end])
}

// BytesToStringFixedM is a "Safe Mode" variant of BytesToStringFixed.
// It handles cases where the input buffer is smaller than expected by creating a padded copy first.
// This prevents "index out of range" panics at the cost of slight memory allocation overhead.
func BytesToStringFixedM(data []byte, size int) string {
	if len(data) < size {
		if len(data) == 0 {
			return ""
		}
		// Create a temporary padded buffer
		padded := make([]byte, size)
		copy(padded, data)
		for i := len(data); i < size; i++ {
			padded[i] = ' '
		}
		data = padded
	}

	strBytes := data[:size]

	end := size
	for i := size - 1; i >= 0; i-- {
		if strBytes[i] != ' ' {
			end = i + 1
			break
		}
	}

	return string(strBytes[:end])
}
