// Package serialization provides functions for serializing and deserializing
// mission-related data structures to and from byte arrays for network transmission.
package serialization

import (
	"cc.2526/internal/common"
	"cc.2526/internal/network"
)

// SerializeMissionAssignment serializes a MissionAssignment struct into a byte array.
// The serialization follows a specific binary format with fixed-size fields and
// variable-length arrays for coordinates and points of interest.
//
// Parameters:
//   - ma: Pointer to the MissionAssignment to serialize
//
// Returns:
//   - []byte: Serialized byte array with the mission assignment data
func SerializeMissionAssignment(ma *common.MissionAssignment) []byte {
	buffer := make([]byte, 0, network.MissionLinkAssignPacketSize)
	
	buffer = append(buffer, uint8(ma.Type))
    buffer = append(buffer, Uint32ToBytes(ma.SeqNum)...)
    buffer = append(buffer, ma.Version)
    buffer = append(buffer, Int64ToBytes(ma.Timestamp)...)
    buffer = append(buffer, ma.RoverId)
    buffer = append(buffer, StringFixedToBytes(ma.RoverName, 20)...)
    buffer = append(buffer, ma.MissionId)
	buffer = append(buffer, StringFixedToBytes(string(ma.MissionConfig.Type), 20)...)
	buffer = append(buffer, StringFixedToBytes(ma.MissionConfig.Description, 50)...)
	buffer = append(buffer, StringFixedToBytes(ma.Tarefa, 50)...)

	buffer = append(buffer, uint8(len(ma.AreaGeografica)))
	for _, coord := range ma.AreaGeografica {
		buffer = append(buffer, Int32ToBytes(coord.X)...)
		buffer = append(buffer, Int32ToBytes(coord.Y)...)
		buffer = append(buffer, Int16ToBytes(coord.Z)...)
	}
	
	buffer = append(buffer, uint8(len(ma.PontosInteresse)))
	for _, ponto := range ma.PontosInteresse {
		buffer = append(buffer, Int32ToBytes(ponto.X)...)
		buffer = append(buffer, Int32ToBytes(ponto.Y)...)
		buffer = append(buffer, Int16ToBytes(ponto.Z)...)
	}
	
	buffer = append(buffer, Uint32ToBytes(ma.Atualizacoes)...)
	buffer = append(buffer, ma.Prioridade)
	buffer = append(buffer, ma.ToleranciaFalhas)
	buffer = append(buffer, Int64ToBytes(ma.DataInicial)...)
	buffer = append(buffer, Uint32ToBytes(ma.DuracaoMaxima)...)

	return buffer
}

// DeserializeMissionAssignment deserializes a byte array into a MissionAssignment struct.
// This function reconstructs the MissionAssignment from the binary format created by
// SerializeMissionAssignment.
//
// Parameters:
//   - data: Byte array containing the serialized mission assignment data
//
// Returns:
//   - common.MissionAssignment: Deserialized mission assignment struct
func DeserializeMissionAssignment(data []byte) common.MissionAssignment {
	ma := common.MissionAssignment{}
	i := 0

	ma.Type = common.MessageType(data[i])
	i++
	ma.SeqNum = BytesToUint32(data, i)
	i += 4
	ma.Version = data[i]
	i++
	ma.Timestamp = BytesToInt64(data, i)
	i += 8
	ma.RoverId = data[i]
	i++
	ma.RoverName = BytesToStringFixedM(data[i:], 20)
	i += 20
	ma.MissionId = data[i]
	i++
	missionType := common.MissionType(BytesToStringFixedM(data[i:], 20))
	i += 20
	description := BytesToStringFixedM(data[i:], 50)
	i += 50
	ma.MissionConfig = common.GetMissionTypeConfig(missionType)
	ma.MissionConfig.Description = description
	ma.Tarefa = BytesToStringFixedM(data[i:], 50)
	i += 50
	
	numCoords := int(data[i])
	i++
	ma.AreaGeografica = make([]common.Coordinate, numCoords)
	for j := 0; j < numCoords; j++ {
		ma.AreaGeografica[j].X = BytesToInt32(data, i)
		i += 4
		ma.AreaGeografica[j].Y = BytesToInt32(data, i)
		i += 4
		ma.AreaGeografica[j].Z = BytesToInt16(data, i)
		i += 2
	}
	
	numPontos := int(data[i])
	i++
	ma.PontosInteresse = make([]common.Coordinate, numPontos)
	for j := 0; j < numPontos; j++ {
		ma.PontosInteresse[j].X = BytesToInt32(data, i)
		i += 4
		ma.PontosInteresse[j].Y = BytesToInt32(data, i)
		i += 4
		ma.PontosInteresse[j].Z = BytesToInt16(data, i)
		i += 2
	}
	
	ma.Atualizacoes = BytesToUint32(data, i)
	i += 4
	ma.Prioridade = data[i]
	i++
	ma.ToleranciaFalhas = data[i]
	i++
	ma.DataInicial = BytesToInt64(data, i)
	i += 8
	ma.DuracaoMaxima = BytesToUint32(data, i)
	i += 4
	
	return ma
}

// SerializeMissionProgress serializes a MissionProgress struct into a byte array.
// The serialization uses flags to indicate which optional fields are present
// (CurrentPosition, NrImagesCaptured, Diagnostics, NrSamplesCollected).
//
// Parameters:
//   - mp: Pointer to the MissionProgress to serialize
//
// Returns:
//   - []byte: Serialized byte array with the mission progress data
func SerializeMissionProgress(mp *common.MissionProgress) []byte {
	buffer := make([]byte, 0, network.MissionLinkProgressPacketSize)

	buffer = append(buffer, uint8(common.MISSION_UPDATE))
	buffer = append(buffer, Uint32ToBytes(mp.SeqNum)...)
	buffer = append(buffer, mp.Version)
	buffer = append(buffer, Int64ToBytes(mp.Timestamp)...)
	buffer = append(buffer, mp.RoverId)
	buffer = append(buffer, mp.MissionId)  
	buffer = append(buffer, Float32ToBytes(mp.Progress)...)
	buffer = append(buffer, uint8(mp.Status))
	
	flags := byte(0)
	if mp.CurrentPosition != nil {
		flags |= 1 << 0
	}
	if mp.NrImagesCaptured != nil {
		flags |= 1 << 1
	}
	if mp.Diagnostics != nil {
		flags |= 1 << 2
	}
	if mp.NrSamplesCollected != nil {
		flags |= 1 << 3
	}
	buffer = append(buffer, flags)
	
	if mp.CurrentPosition != nil {
		buffer = append(buffer, Int32ToBytes(mp.CurrentPosition.X)...)
		buffer = append(buffer, Int32ToBytes(mp.CurrentPosition.Y)...)
		buffer = append(buffer, Int16ToBytes(mp.CurrentPosition.Z)...)
	}
	if mp.NrImagesCaptured != nil {
		buffer = append(buffer, Uint32ToBytes(*mp.NrImagesCaptured)...)
	}
	if mp.Diagnostics != nil {
		buffer = append(buffer, StringFixedToBytes(*mp.Diagnostics, 100)...)
	}
	if mp.NrSamplesCollected != nil {
		buffer = append(buffer, *mp.NrSamplesCollected)
	}
		
	return buffer
}

// DeserializeMissionProgress deserializes a byte array into a MissionProgress struct.
// This function reconstructs the MissionProgress from the binary format, including
// handling optional fields based on the presence flags.
//
// Parameters:
//   - data: Byte array containing the serialized mission progress data
//
// Returns:
//   - common.MissionProgress: Deserialized mission progress struct
func DeserializeMissionProgress(data []byte) common.MissionProgress {
	mp := common.MissionProgress{}
	i := 0

	mp.Type = common.MessageType(data[i])
	i++
	mp.SeqNum = BytesToUint32(data, i)
	i += 4
	mp.Version = data[i]
	i++
	mp.Timestamp = BytesToInt64(data, i)
	i += 8
	mp.RoverId = data[i]
	i++
	mp.MissionId = data[i]
	i++
	mp.Progress = BytesToFloat32(data, i)
	i += 4
	mp.Status = common.MissionStatus(data[i])
	i++
	flags := data[i]
	i++
	
	if flags&(1<<0) != 0 && i+10 <= len(data) {
		pos := common.Coordinate{}
		pos.X = BytesToInt32(data, i)
		i += 4
		pos.Y = BytesToInt32(data, i)
		i += 4
		pos.Z = BytesToInt16(data, i)
		i += 2
		mp.CurrentPosition = &pos
	}
	
	if flags&(1<<1) != 0 && i+4 <= len(data) {
		images := BytesToUint32(data, i)
		i += 4
		mp.NrImagesCaptured = &images
	}
	
	if flags&(1<<2) != 0 && i+100 <= len(data) {
		diag := BytesToStringFixedM(data[i:], 100)
		i += 100
		mp.Diagnostics = &diag
	}
	
	if flags&(1<<3) != 0 && i < len(data) {
		samples := data[i]
		i++
		mp.NrSamplesCollected = &samples
	}
	
	return mp
}

// SerializeMissionRequest serializes a MissionRequest struct into a byte array.
// This is used for rover mission requests with basic identification information.
//
// Parameters:
//   - mr: Pointer to the MissionRequest to serialize
//
// Returns:
//   - []byte: Serialized byte array with the mission request data
func SerializeMissionRequest(mr *common.MissionRequest) []byte {
    buffer := make([]byte, 0, network.MissionLinkRequestPacketSize)
    
    buffer = append(buffer, uint8(mr.Type))
    buffer = append(buffer, Uint32ToBytes(mr.SeqNum)...)
    buffer = append(buffer, mr.Version)
    buffer = append(buffer, Int64ToBytes(mr.Timestamp)...)
    buffer = append(buffer, mr.RoverId)
    buffer = append(buffer, StringFixedToBytes(mr.RoverName, 20)...)
    
    return buffer
}

// DeserializeMissionRequest deserializes a byte array into a MissionRequest struct.
//
// Parameters:
//   - data: Byte array containing the serialized mission request data
//
// Returns:
//   - common.MissionRequest: Deserialized mission request struct
func DeserializeMissionRequest(data []byte) common.MissionRequest {
    mr := common.MissionRequest{}
    i := 0
    
    mr.Type = common.MessageType(data[i])
    i++
    mr.SeqNum = BytesToUint32(data, i)
    i += 4
    mr.Version = data[i]
    i++
    mr.Timestamp = BytesToInt64(data, i)
    i += 8
    mr.RoverId = data[i]
    i++
	mr.RoverName = BytesToStringFixedM(data[i:], 20)
	i += 20
    
    return mr
}

// SerializeMissionAck serializes a MissionAck struct into a byte array.
// This is used for acknowledgment messages in mission communication.
//
// Parameters:
//   - ma: Pointer to the MissionAck to serialize
//
// Returns:
//   - []byte: Serialized byte array with the mission acknowledgment data
func SerializeMissionAck(ma *common.MissionAck) []byte {
    buffer := make([]byte, 0, network.MissionLinkAckPacketSize)
    
    buffer = append(buffer, uint8(ma.Type))
    buffer = append(buffer, Uint32ToBytes(ma.SeqNum)...)
    buffer = append(buffer, ma.Version)
    buffer = append(buffer, Int64ToBytes(ma.Timestamp)...)
    buffer = append(buffer, Uint32ToBytes(ma.AckForSeq)...)
    buffer = append(buffer, ma.RoverId)
    buffer = append(buffer, uint8(ma.Status))
    
    return buffer
}

// DeserializeMissionAck deserializes a byte array into a MissionAck struct.
//
// Parameters:
//   - data: Byte array containing the serialized mission acknowledgment data
//
// Returns:
//   - common.MissionAck: Deserialized mission acknowledgment struct
func DeserializeMissionAck(data []byte) common.MissionAck {
    ma := common.MissionAck{}
    i := 0
    
    ma.Type = common.MessageType(data[i])
    i++
    ma.SeqNum = BytesToUint32(data, i)
    i += 4
    ma.Version = data[i]
    i++
    ma.Timestamp = BytesToInt64(data, i)
    i += 8
    ma.AckForSeq = BytesToUint32(data, i)
    i += 4
    ma.RoverId = data[i]
    i++
    ma.Status = common.AckStatus(data[i])
    i++
    
    return ma
}