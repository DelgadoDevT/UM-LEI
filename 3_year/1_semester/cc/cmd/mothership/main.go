package main

import (
	"cc.2526/internal/mothership"
	"log"
	"os"
	"os/signal"
	"syscall"
	"flag"
)

// main is the entry point for the MotherShip application.
// It performs the following steps:
// 1. Parses command-line flags to configure the MotherShip
// 2. Creates a new MotherShip instance with the specified name
// 3. Starts the MotherShip and its subsystems
// 4. Waits for a termination signal
// 5. Performs a graceful shutdown when a signal is received
//
// The application handles SIGINT (Ctrl+C), SIGTERM (system termination),
// and SIGTSTP (Ctrl+Z) signals for controlled shutdown.
func main() {
	var (
		name = flag.String("name", "Alpha", "MotherShip Name")
	)
	flag.Parse()

	naveMae := mothership.NewNaveMae(*name)

	log.Printf("=== STARTING MOTHERSHIP %s ===\n", *name)
	naveMae.Start()
	
	sigChan := make(chan os.Signal, 1)
	signal.Notify(sigChan, syscall.SIGINT, syscall.SIGTERM, syscall.SIGTSTP)

	<-sigChan
	log.Printf("=== SHUTTING DOWN MOTHERSHIP %s ===\n", *name)
	
	naveMae.Stop()
	log.Println("Mothership shutdown complete")
}