package main

import (
	"flag"
	"log"
	"os"
	"os/signal"
	"syscall"

	"cc.2526/internal/common"
	"cc.2526/internal/rover"
)

// main is the entry point for the rover simulation application.
// It initializes a rover controller with command-line parameters and manages the rover's lifecycle.
//
// The application:
// - Parses command-line arguments for rover configuration
// - Initializes the rover on the specified planetary body
// - Starts the controller for mission operations and telemetry
// - Handles graceful shutdown on system signals
//
// Command-line flags:
//
//	-id: Rover unique identifier (default: 1)
//	-name: Rover display name (default: "Explorer")
//	-planet: Target planetary body - "mars" or "venus" (default: "mars")
//	-x: Starting X coordinate on planetary surface (default: 0)
//	-y: Starting Y coordinate on planetary surface (default: 0)
//	-ip: Mothership IP address for communication (default: "localhost")
//
// The rover runs until receiving a termination signal (SIGINT, SIGTERM, SIGTSTP),
// then performs a graceful shutdown.
func main() {
	var (
		roverId      = flag.Uint("id", 1, "Rover ID")
		roverName    = flag.String("name", "Explorer", "Rover name")
		planet       = flag.String("planet", "mars", "Planet (mars, venus)")
		startX       = flag.Int("x", 0, "Starting x coordinate")
		startY       = flag.Int("y", 0, "Starting y coordinate")
		mothershipIP = flag.String("ip", "localhost", "Mothership IP address")
	)
	flag.Parse()

	var planetEnum common.Planet
	switch *planet {
	case "mars":
		planetEnum = common.MARS
	case "venus":
		planetEnum = common.VENUS
	default:
		log.Fatalf("Unknown planet: %s. Available: mars, venus", *planet)
	}

	log.Printf("Initializing rover %s (ID: %d) on %s at position (%d, %d)", *roverName, *roverId, *planet, *startX, *startY)

	controller := rover.NewController(
		uint8(*roverId),
		*roverName,
		planetEnum,
		int32(*startX),
		int32(*startY),
		*mothershipIP,
	)

	if err := controller.Start(); err != nil {
		log.Fatalf("Failed to start rover controller: %v", err)
	}

	// Setup signal handling for graceful shutdown
	sigChan := make(chan os.Signal, 1)
	signal.Notify(sigChan, syscall.SIGINT, syscall.SIGTERM, syscall.SIGTSTP)

	// Wait for shutdown signal
	<-sigChan
	log.Println("Received shutdown signal, stopping rover...")

	// Stop the controller
	controller.Stop()

	log.Println("Rover shutdown complete")
}
