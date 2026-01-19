package main

import (
	"cc.2526/internal/ground-control"
	"flag"
	"log"
)

func main() {
	// Definição de flags
	// -port: Porta onde o servidor faz bind (ex: :8060 ou :8061)
	// -mothership: IP onde a Nave-Mãe está (ex: 127.0.0.1 ou 10.0.0.1)
	// -local: IP deste Ground Control para mostrar na consola (cosmético)
	port := flag.String("port", ":8060", "Porta de escuta do Ground Control")
	mothershipIP := flag.String("mothership", "127.0.0.1", "IP da Nave-Mãe")
	localIP := flag.String("local", "localhost", "IP local para display")

	flag.Parse()

	log.Printf("A iniciar Ground Control na porta %s...", *port)

	// Inicializa a estrutura do servidor
	gc := groundcontrol.NewServer()

	// Inicia o servidor passando os 3 argumentos
	gc.Start(*port, *mothershipIP, *localIP)
}
