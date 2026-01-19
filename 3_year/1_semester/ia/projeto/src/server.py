import time
import threading
import os
import sys
import queue
import socket
import pickle
from datetime import datetime
from enum import Enum
from typing import Dict, List

current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)
sys.path.insert(0, parent_dir)

from src.utils.Interface import console
from src.models.Graph import Graph
from src.models.Model import Model
from src.models.Car import Car, CarStatus
from src.models.Request import Request, RequestStatus
from src.simulation.data_loader import load_nodes, load_edges, load_cars
from src.simulation.ambient import update_traffic, update_weather, generate_random_request
from src.simulation.visualizer import generate_map
from src.simulation.report_generator import save_requests_report, save_cars_report

REQUEST_BUFFER = queue.Queue()
ALGORITHM = 'a_star'
ENVIRONMENT_CONDITIONS = {
    "meteorologia": "normal",
    "transito": "normal"
}


class AlgorithmType(Enum):
    """
    Enumeration of available pathfinding algorithms.
    
    This enumeration must exactly match the one defined in the client
    to ensure interoperability.
    """
    DFS = "dfs"
    BFS = "bfs"
    UNIFORM_COST = "uniform_cost"
    GREEDY = "greedy"
    WEIGHTED_A_STAR = "weighted_a_star"
    A_STAR = "a_star"


class TaxiGreenServer:
    """
    Main server of the TaxiGreen system.
    
    Manages the simulation, processes requests, communicates with clients, and
    coordinates all system operations. Implements multi-threaded architecture
    to allow simultaneous communication with multiple clients.
    
    Attributes:
        graph (Graph): Graph representing the road network.
        model (Model): Main system model.
        running (bool): Flag indicating if the server is running.
        simulation_thread (threading.Thread): Simulation thread.
        socket_thread (threading.Thread): Socket server thread.
        request_counter (int): Sequential counter for request IDs.
        car_colors (Dict): Mapping of car IDs to visualization colors.
        route_history (Dict): History of completed routes.
        algorithm (str): Current pathfinding algorithm.
        data_lock (threading.Lock): Lock for data synchronization.
        host (str): Server binding address.
        port (int): Server listening port.
        clients (List): List of connected client threads.
    """

    def __init__(self, host='localhost', port=8888):
        """
        Initialize the TaxiGreen server.
        
        Args:
            host (str, optional): Binding address for the server. Defaults to 'localhost'.
            port (int, optional): Listening port for the server. Defaults to 8888.
        """
        self.graph = Graph()
        self.model = None
        self.running = False
        self.simulation_thread = None
        self.socket_thread = None
        self.request_counter = 1
        self.car_colors = {}
        self.route_history = {}
        self.algorithm = ALGORITHM
        self.data_lock = threading.Lock()
        self.host = host
        self.port = port
        self.clients = []

    def initialize_system(self):
        """
        Initialize the system by loading all necessary data.
        
        Loads nodes, edges, and cars from CSV files in the data directory.
        Initializes the graph and model, and configures colors for car visualization.
        
        Returns:
            bool: True if initialization was successful, False otherwise.
        """
        console.clear()
        console.rule("[bold magenta]Inicializando Sistema TaxiGreen Server[/bold magenta]")

        current_dir = os.path.dirname(os.path.abspath(__file__))
        project_root = os.path.dirname(current_dir)
        data_dir = os.path.join(project_root, 'data')

        try:
            nodes_file = os.path.join(data_dir, "nodes.csv")
            edges_file = os.path.join(data_dir, "edges.csv")
            cars_file = os.path.join(data_dir, "cars.csv")

            with console.status("[bold cyan]A carregar dados do disco...[/bold cyan]", spinner="dots"):
                load_nodes(self.graph, nodes_file)
                load_edges(self.graph, edges_file)
                console.log(f"[green]✔[/green] Grafo carregado: {len(self.graph.nodes)} nodos")

                self.model = Model(self.graph)
                load_cars(self.model, cars_file)
                console.log(
                    f"[green]✔[/green] Modelo carregado: {len(self.model.cars)} carros, {len(self.model.fuel_stations)} estações")
                time.sleep(0.5)

            colors = ['red', 'green', 'blue', 'orange', 'purple', 'brown', 'pink', 'gray', 'cyan', 'magenta']
            for i, car_id in enumerate(self.model.cars.keys()):
                self.car_colors[car_id] = colors[i % len(colors)]

            self.model.get_fuel_stations_from_graph()
            console.print(f"\n[bold green]🚀 Sistema pronto! Frota ativa: {len(self.model.cars)} carros.[/bold green]\n")
            return True

        except Exception as e:
            console.print(f"[bold red]❌ Erro crítico ao inicializar sistema: {e}[/bold red]")
            return False

    def create_manual_request(self, origin: int, destination: int, num_passengers: int = 1,
                              environmental_pref: bool = False) -> str:
        """
        Create a manual request and add it to the processing buffer.
        
        Args:
            origin (int): Origin node ID.
            destination (int): Destination node ID.
            num_passengers (int, optional): Number of passengers. Defaults to 1.
            environmental_pref (bool, optional): Preference for eco mode. Defaults to False.
        
        Returns:
            str: Confirmation or error message.
        """
        if origin == 99 or destination == 99:
            return "De momento não é possível aceder ao nodo requisitado."

        if origin not in self.graph.nodes or destination not in self.graph.nodes:
            return f"Erro: Nodos inválidos. Origem: {origin}, Destino: {destination}"

        request_data = {
            'origin': origin,
            'destination': destination,
            'num_passengers': num_passengers,
            'environmental_pref': environmental_pref,
            'timestamp': datetime.now(),
            'type': 'manual'
        }
        REQUEST_BUFFER.put(request_data)
        return f"Pedido manual criado: {origin} → {destination}"

    def process_request_buffer(self) -> int:
        """
        Process all pending requests in the buffer.
        
        Returns:
            int: Number of processed requests.
        """
        processed = 0
        while not REQUEST_BUFFER.empty():
            try:
                request_data = REQUEST_BUFFER.get_nowait()
                self._process_single_request(request_data)
                REQUEST_BUFFER.task_done()
                processed += 1
            except queue.Empty:
                break
        return processed

    def _process_single_request(self, request_data: Dict):
        """
        Process a single request from the buffer.

        Args:
            request_data (Dict): Request data to process.
        """
        try:
            request = Request(
                request_id=self.request_counter,
                origin_node_id=request_data['origin'],
                destination_node_id=request_data['destination'],
                num_passengers=request_data.get('num_passengers', 1),
                requested_time=datetime.now(),
                priority=1,
                environmental_preference=request_data.get('environmental_pref', False),
                max_wait_time_seconds=600
            )

            self.model.add_request(request)
            console.print(
                f"\n[bold yellow]📝 NOVO PEDIDO MANUAL #{self.request_counter}[/bold yellow]: {request_data['origin']} → {request_data['destination']}")
            console.print(f"   🌿 Modo Eco: {'ATIVO' if request.environmental_preference else 'INATIVO'}")
            self.request_counter += 1

        except Exception as e:
            console.print(f"[bold red]Erro ao processar pedido: {e}[/bold red]")

    def update_environment(self, conditions: Dict):
        """
        Update the simulation's environmental conditions.
        
        Applies multipliers to graph edges based on specified meteorological
        and traffic conditions.
        
        Args:
            conditions (Dict): Dictionary with new environmental conditions.
        """
        global ENVIRONMENT_CONDITIONS
        ENVIRONMENT_CONDITIONS.update(conditions)

        weather_multipliers = {"normal": 1.0, "chuva": 1.3, "neblina": 1.5, "nevoeiro": 1.5, "neve": 1.7, "gelo": 2.0}
        traffic_multipliers = {"normal": 1.0, "lento": 1.5, "muito lento": 2.0}

        weather = conditions.get("weather", conditions.get("meteorologia", "normal"))
        traffic = conditions.get("traffic", conditions.get("transito", "normal"))
        zone = conditions.get("zone", "center_north")

        weather_mult = weather_multipliers.get(weather, 1.0)
        traffic_mult = traffic_multipliers.get(traffic, 1.0)

        update_weather(self.model.graph, zone, weather_mult)
        update_traffic(self.model.graph, zone, traffic_mult)

        self.model._clear_cache()

        console.print(f"\n[bold blue]🌍 AMBIENTE ATUALIZADO[/bold blue]")
        console.print(f"   Meteorologia: {weather} (x{weather_mult})")
        console.print(f"   Trânsito: {traffic} (x{traffic_mult})")
        console.print(f"   Zona: {zone}\n")

    def set_algorithm(self, algorithm: AlgorithmType):
        """
        Change the pathfinding algorithm.
        
        Args:
            algorithm (AlgorithmType): New algorithm to use.
        """
        self.algorithm = algorithm.value
        console.print(f"\n[bold magenta]🔧 ALGORITMO ALTERADO PARA:[/bold magenta] {algorithm.value.upper()}\n")

    def _record_route(self, request: Request, car: Car):
        """
        Record a route in the history.
        
        Args:
            request (Request): The request being processed.
            car (Car): The car assigned to the request.
        """
        route_hash = f"{request.origin_node_id}-{request.destination_node_id}"
        if route_hash not in self.route_history:
            self.route_history[route_hash] = {}

        if car.plate not in self.route_history[route_hash]:
            self.route_history[route_hash][car.plate] = 0

        self.route_history[route_hash][car.plate] += 1

    def get_car_info(self) -> List[Dict]:
        """
        Return information about all cars.
        
        Returns:
            List[Dict]: List of dictionaries with car information.
        """
        info = []
        for car in self.model.cars.values():
            info.append({
                'id': car.plate,
                'model': car.model,
                'engine_type': car.engine_type.value,
                'status': car.status.value,
                'autonomy': f"{car.current_autonomy_km:.1f}/{car.max_autonomy_km:.1f}km",
                'location': car.current_node_id,
                'battery': f"{(car.current_autonomy_km / car.max_autonomy_km * 100):.1f}%",
                'on_reserve': car.is_on_reserve()
            })
        return info

    def get_request_info(self) -> List[Dict]:
        """
        Return information about all requests.
        
        Returns:
            List[Dict]: List of dictionaries with request information.
        """
        info = []
        for req in self.model.requests.values():
            distance_to_client = "N/A"
            distance_with_client = "N/A"
            time_to_client = "N/A"
            time_with_client = "N/A"
            total_distance = "N/A"
            total_time = "N/A"

            try:
                _, time_with_sec, dist_with_km = self.model.find_route(
                    req.origin_node_id,
                    req.destination_node_id,
                    algorithm=self.algorithm
                )

                if dist_with_km != float('inf'):
                    distance_with_client = f"{dist_with_km:.2f}km"
                    total_distance = f"{dist_with_km:.2f}km"

                if time_with_sec != float('inf'):
                    time_with_min = int(time_with_sec // 60)
                    time_with_client = f"{time_with_min}min"
                    total_time = f"{time_with_min}min"

                if req.assigned_car_id and req.assigned_car_id in self.model.cars:
                    car = self.model.cars[req.assigned_car_id]

                    _, time_to_sec, dist_to_km = self.model.find_route(
                        car.current_node_id,
                        req.origin_node_id,
                        algorithm=self.algorithm
                    )

                    if dist_to_km != float('inf'):
                        distance_to_client = f"{dist_to_km:.2f}km"
                        if dist_with_km != float('inf'):
                            total_distance = f"{dist_to_km + dist_with_km:.2f}km"

                    if time_to_sec != float('inf'):
                        time_to_min = int(time_to_sec // 60)
                        time_to_client = f"{time_to_min}min"
                        if time_with_sec != float('inf'):
                            total_time = f"{time_to_min + time_with_min}min"

            except:
                pass

            info.append({
                'id': req.request_id,
                'origin': req.origin_node_id,
                'destination': req.destination_node_id,
                'status': req.status.value,
                'passengers': req.num_passengers,
                'environmental': req.environmental_preference,
                'assigned_car': req.assigned_car_id or "N/A",
                'price': f"{req.price:.2f}€" if req.price else "N/A",
                'distance_to_client': distance_to_client,
                'distance_with_client': distance_with_client,
                'total_distance': total_distance,
                'time_to_client': time_to_client,
                'time_with_client': time_with_client,
                'total_time': total_time,
                'priority': req.priority
            })
        return info

    def get_route_history(self) -> Dict:
        """
        Return the route history.
        
        Returns:
            Dict: Dictionary with route history information.
        """
        route_info = {}
        for route_hash, car_counts in self.route_history.items():
            origin, destination = route_hash.split('-')
            route_info[route_hash] = {
                'origin': origin,
                'destination': destination,
                'cars': car_counts
            }
        return route_info

    def generate_route_map(self) -> str:
        """
        Generate a map with completed routes.
        
        Returns:
            str: Filename of the generated map.
        """
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        filename = f"mapa_rotas_{timestamp}.html"
        generate_map(self.model, filename)
        return filename

    def generate_reports(self) -> str:
        """
        Generate CSV reports.
        
        Returns:
            str: Confirmation message with filenames.
        """
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        save_requests_report(self.model.requests, f"requests_report_{timestamp}.csv")
        save_cars_report(self.model.cars, f"cars_report_{timestamp}.csv")
        return f"Relatórios gerados: requests_report_{timestamp}.csv, cars_report_{timestamp}.csv"

    def get_system_stats(self) -> Dict:
        """
        Return system statistics.
        
        Returns:
            Dict: Dictionary with system statistics.
        """
        total_requests = len(self.model.requests)
        concluidos = sum(1 for r in self.model.requests.values() if r.status == RequestStatus.CONCLUIDO)
        pendentes = sum(1 for r in self.model.requests.values() if r.status == RequestStatus.PENDENTE)

        total_stations = len(self.model.fuel_stations)
        real_stations = sum(1 for s in self.model.fuel_stations.values() if "(Real)" in s.name)

        return {
            'requests': {'total': total_requests, 'concluidos': concluidos, 'pendentes': pendentes},
            'stations': {'total': total_stations, 'api_reais': real_stations},
            'cars': {'total': len(self.model.cars)},
            'algorithm': self.algorithm,
            'environment': ENVIRONMENT_CONDITIONS
        }

    def handle_client_command(self, command: str, data: Dict) -> Dict:
        """
        Process commands from the client.
        
        Args:
            command (str): Command name to execute.
            data (Dict): Additional data for the command.
        
        Returns:
            Dict: Response to the client.
        """
        try:
            if command == 'create_request':
                result = self.create_manual_request(
                    data['origin'], data['destination'],
                    data.get('num_passengers', 1), data.get('environmental_pref', False)
                )
                return {'success': True, 'result': result}

            elif command == 'get_car_info':
                return {'success': True, 'data': self.get_car_info()}

            elif command == 'get_request_info':
                return {'success': True, 'data': self.get_request_info()}

            elif command == 'get_route_history':
                return {'success': True, 'data': self.get_route_history()}

            elif command == 'generate_route_map':
                filename = self.generate_route_map()
                return {'success': True, 'filename': filename}

            elif command == 'generate_reports':
                result = self.generate_reports()
                return {'success': True, 'result': result}

            elif command == 'get_system_stats':
                return {'success': True, 'data': self.get_system_stats()}

            elif command == 'set_algorithm':
                algorithm = AlgorithmType(data['algorithm'])
                self.set_algorithm(algorithm)
                return {'success': True, 'message': f'Algoritmo alterado para {algorithm.value}'}

            elif command == 'update_environment':
                self.update_environment(data['conditions'])
                return {'success': True, 'message': 'Condições ambientais atualizadas'}

            else:
                return {'success': False, 'error': 'Comando desconhecido'}

        except Exception as e:
            return {'success': False, 'error': str(e)}

    def socket_handler(self, client_socket):
        """
        Handle communication with a client.
        
        Args:
            client_socket (socket.socket): Client socket connection.
        """
        try:
            while self.running:
                data = client_socket.recv(1048576)
                if not data:
                    break

                try:
                    message = pickle.loads(data)
                    command = message.get('command')
                    command_data = message.get('data', {})

                    response = self.handle_client_command(command, command_data)
                    client_socket.send(pickle.dumps(response))

                except Exception as e:
                    error_response = {'success': False, 'error': str(e)}
                    client_socket.send(pickle.dumps(error_response))

        except Exception as e:
            console.log(f"[bold red]Erro na comunicação com cliente: {e}[/bold red]")
        finally:
            client_socket.close()

    def start_socket_server(self):
        """Start the socket server."""
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server_socket.bind((self.host, self.port))
        server_socket.listen(5)
        console.log(f"🎯 Servidor de sockets iniciado em [bold cyan]{self.host}:{self.port}[/bold cyan]")

        while self.running:
            try:
                client_socket, addr = server_socket.accept()
                console.log(f"🔗 [green]Cliente conectado:[/green] {addr}")
                client_thread = threading.Thread(target=self.socket_handler, args=(client_socket,))
                client_thread.daemon = True
                client_thread.start()
                self.clients.append(client_thread)
            except Exception as e:
                if self.running:
                    console.log(f"[bold red]Erro ao aceitar cliente: {e}[/bold red]")

    def _simulation_loop(self):
        """
        Main simulation loop.
        
        Executes every 3 seconds and processes:
        - Request buffer
        - Random request generation
        - Pending request assignment
        - Low battery car management
        """
        step_count = 0
        already_shown_cancelled = set()

        while self.running:
            step_count += 1

            console.rule(f"[dim]Passo de Simulação #{step_count}[/dim]")

            try:
                self.process_request_buffer()

                if step_count % 3 == 0:
                    from src.simulation.ambient import generate_random_request
                    new_request = generate_random_request(self.model, self.request_counter)
                    if new_request:
                        console.print(f"\n[bold green]✨ AMBIENTE: Novo pedido gerado[/bold green] -> {new_request}")
                        self.request_counter += 1

                pending = [r for r in self.model.requests.values() if r.status == RequestStatus.PENDENTE]

                cancelled = [r for r in self.model.requests.values()
                             if r.status == RequestStatus.CANCELADO and hasattr(r, 'attempts_without_car')
                             and r.attempts_without_car >= 10]

                for req in cancelled:
                    if req.request_id not in already_shown_cancelled:
                        console.print(
                            f"   ⚠️  [yellow]Pedido {req.request_id} foi cancelado automaticamente após 10 tentativas sem carros[/yellow]")
                        already_shown_cancelled.add(req.request_id)

                for req in pending:
                    console.print(
                        f"   🔄 A processar pedido [bold cyan]{req.request_id}[/bold cyan] ({req.origin_node_id} -> {req.destination_node_id})...")

                    best_car, time_to_client, dist_to_client = self.model.find_best_car_for_request(
                        req.request_id, eco=req.environmental_preference, algorithm=self.algorithm
                    )

                    if best_car:
                        if self.model.complete_request(req.request_id, algorithm=self.algorithm):
                            console.print(
                                f"   ✅ [green]Atribuído![/green] Carro [bold]{best_car.plate}[/bold] vai recolher passageiro.")
                            self._record_route(req, best_car)

                cars_needing_refuel = {
                    c.plate: c.current_autonomy_km
                    for c in self.model.cars.values()
                    if c.is_on_reserve() and c.status == CarStatus.DISPONIVEL
                }

                self.model.process_low_battery_cars(algorithm=self.algorithm)

                for plate, old_autonomy in cars_needing_refuel.items():
                    car = self.model.cars[plate]
                    if car.current_autonomy_km > old_autonomy:
                        icon = "🔋" if car.engine_type.value == 'eletrico' else "⛽"
                        console.print(f"   {icon} [bold green]{car.plate}[/bold green] abasteceu/carregou com sucesso.")

            except Exception as e:
                console.print(f"\n[bold red]Erro no passo de simulação: {e}[/bold red]")

            time.sleep(3)

    def start_server(self):
        """Start the complete server."""
        if self.running:
            console.print("[bold yellow]Servidor já está em execução![/bold yellow]")
            return

        self.running = True

        self.simulation_thread = threading.Thread(target=self._simulation_loop, daemon=True)
        self.simulation_thread.start()

        self.socket_thread = threading.Thread(target=self.start_socket_server, daemon=True)
        self.socket_thread.start()

        console.print("[bold green]🟢 Servidor TaxiGreen iniciado com sucesso![/bold green]")

    def stop_server(self):
        """Stop the server."""
        self.running = False
        console.print("[bold red]🔴 Servidor TaxiGreen parado![/bold red]")


def start_taxigreen_server():
    """
    Function to start the TaxiGreen server.
    
    Returns:
        TaxiGreenServer: The server instance if initialization was successful, None otherwise.
    """
    server = TaxiGreenServer()
    if server.initialize_system():
        server.start_server()
        return server
    else:
        console.print("[bold red]❌ Falha ao inicializar o servidor![/bold red]")
        return None


if __name__ == "__main__":
    """
    Main entry point of the TaxiGreen server.
    
    Starts the server and keeps it running until KeyboardInterrupt.
    """
    server = start_taxigreen_server()

    if server:
        try:
            console.print("\n[italic]Servidor em execução. Pressione Ctrl+C para parar.[/italic]")
            while True:
                time.sleep(1)
        except KeyboardInterrupt:
            print("\n")
            console.print("[bold yellow]A encerrar servidor...[/bold yellow]")
            server.stop_server()