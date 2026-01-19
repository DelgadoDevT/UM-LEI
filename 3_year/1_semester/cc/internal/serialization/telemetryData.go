// Package serialization provides functions for serializing and deserializing
// mission-related data structures to and from byte arrays for network transmission.
package serialization

import (
	"cc.2526/internal/common"
	"cc.2526/internal/network"
)

// SerializeTelemetryData converts TelemetryData struct into a byte array for network transmission.
// The serialization follows a fixed-size format to ensure consistent packet structure.
//
// Packet structure (fixed size defined by network.TelemetryPacketSize):
//
//	[0]     RoverId (1 byte)
//	[1-11]  Name (11 bytes, fixed length)
//	[12]    Status (1 byte)
//	[13-16] Position.X (4 bytes)
//	[17-20] Position.Y (4 bytes)
//	[21-22] Position.Z (2 bytes)
//	[23]    Battery (1 byte)
//	[24-27] InternalTemp (4 bytes)
//	[28-31] ExternalTemp (4 bytes)
//	[32]    Humidity (1 byte)
//	[33-36] Pressure (4 bytes)
//	[37-40] Radiation (4 bytes)
//	[41-48] Timestamp (8 bytes)
//	[49-52] Velocity (4 bytes)
//	[53-54] Direction (2 bytes)
//	[55-62] Uptime (8 bytes)
//	[63]    SystemHealth (1 byte)
//
// Returns: Byte slice ready for network transmission
func SerializeTelemetryData(t *common.TelemetryData) []byte {
	buffer := make([]byte, 0, network.TelemetryPacketSize)
	buffer = append(buffer, t.RoverId)
	buffer = append(buffer, StringFixedToBytes(t.Name, 11)...)
	buffer = append(buffer, t.Status)
	buffer = append(buffer, Int32ToBytes(t.Position.X)...)
	buffer = append(buffer, Int32ToBytes(t.Position.Y)...)
	buffer = append(buffer, Int16ToBytes(t.Position.Z)...)
	buffer = append(buffer, t.Battery)
	buffer = append(buffer, Float32ToBytes(t.InternalTemp)...)
	buffer = append(buffer, Float32ToBytes(t.ExternalTemp)...)
	buffer = append(buffer, Uint8ToBytes(t.Humidity)...)
	buffer = append(buffer, Float32ToBytes(t.Pressure)...)
	buffer = append(buffer, Float32ToBytes(t.Radiation)...)
	buffer = append(buffer, Int64ToBytes(t.Timestamp)...)
	buffer = append(buffer, Float32ToBytes(t.Velocity)...)
	buffer = append(buffer, Uint16ToBytes(t.Direction)...)
	buffer = append(buffer, UInt64ToBytes(t.Uptime)...)
	buffer = append(buffer, Uint8ToBytes(t.SystemHealth)...)

	return buffer
}

// DeserializeTelemetryData converts a byte array back into a TelemetryData struct.
// This function reverses the serialization process, reading bytes in the same order
// they were written by SerializeTelemetryData.
//
// Parameters:
//
//	data: Byte array received from network, expected to be network.TelemetryPacketSize bytes
//
// Returns: Reconstructed TelemetryData struct with all field values restored
func DeserializeTelemetryData(data []byte) common.TelemetryData {
	t := common.TelemetryData{}
	i := 0

	t.RoverId = BytesToUint8(data, i)
	i++
	t.Name = BytesToStringFixed(data, 11)
	i += 11
	t.Status = BytesToUint8(data, i)
	i++
	t.Position.X = BytesToInt32(data, i)
	i += 4
	t.Position.Y = BytesToInt32(data, i)
	i += 4
	t.Position.Z = BytesToInt16(data, i)
	i += 2
	t.Battery = BytesToUint8(data, i)
	i++
	t.InternalTemp = BytesToFloat32(data, i)
	i += 4
	t.ExternalTemp = BytesToFloat32(data, i)
	i += 4
	t.Humidity = BytesToUint8(data, i)
	i++
	t.Pressure = BytesToFloat32(data, i)
	i += 4
	t.Radiation = BytesToFloat32(data, i)
	i += 4
	t.Timestamp = BytesToInt64(data, i)
	i += 8
	t.Velocity = BytesToFloat32(data, i)
	i += 4
	t.Direction = BytesToUint16(data, i)
	i += 2
	t.Uptime = BytesToUInt64(data, i)
	i += 8
	t.SystemHealth = BytesToUint8(data, i)
	i++

	return t
}
