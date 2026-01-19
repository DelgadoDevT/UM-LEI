from src.models.Graph import Graph
from src.models.Car import Car, CarStatus, EngineType
from src.models.Request import Request, RequestStatus
from src.models.FuelStation import FuelStation
from src.utils.Common import format_time

from typing import Dict, List, Tuple

class Model:
    """
    The central model class that manages the Simulation State.
    It holds references to the Graph, the Fleet (Cars), Requests, and Fuel Stations.
    It is responsible for pathfinding and logic execution.

    :param graph: The graph representing the city map.
    """
    def __init__(self, graph: Graph):
        self.graph = graph
        self.cars: Dict[str, Car] = {}
        self.requests: Dict[int, Request] = {}
        self.fuel_stations: Dict[int, FuelStation] = {}  # Mapeia node_id -> FuelStation

        # Formato: cache[algoritmo][(start_id, end_id)] = (path, cost, distance_km)
        self.routing_cache: Dict[str, Dict[Tuple[int, int], Tuple[List[int], float, float]]] = {
            'a_star': {},
            'uniform_cost': {},
            'bfs': {},
            'dfs': {},
            'weighted_a_star': {},
            "greedy": {}
        }

    def get_fuel_stations_from_graph(self):
        """
        Scans the graph nodes for station types ('gas_station', 'charge_station')
        and initializes FuelStation objects accordingly.
        """
        for node_id, node in self.graph.nodes.items():
            if node.type in ['gas_station', 'charge_station']:
                if node.type == 'charge_station':
                    station_type = EngineType.ELETRICO
                    price = 0.15
                else:
                    station_type = EngineType.COMBUSTAO
                    price = 1.5

                name = f"Estação {node_id}"

                station = FuelStation(
                    node_id=node_id,
                    name=name,
                    station_type=station_type,
                    price_per_unit=price
                )
                self.add_fuel_station(station)

    def add_car(self, car: Car):
        """
        Adds a new car to the fleet.

        :param car: The Car object to add.
        """
        if car.plate not in self.cars:
            self.cars[car.plate] = car
        else:
            print(f"Erro: Carro com matrícula {car.plate} já existe.")

    def add_request(self, request: Request):
        """
        Adds a new client request to the system.

        :param request: The Request object to add.
        """
        if request.request_id not in self.requests:
            self.requests[request.request_id] = request
        else:
            print(f"Erro: Pedido com ID {request.request_id} já existe.")

    def add_fuel_station(self, station: FuelStation):
        """
        Adds a new fuel station to the system.

        :param station: The FuelStation object to add.
        """
        if station.node_id not in self.fuel_stations:
            self.fuel_stations[station.node_id] = station
        else:
            print(f"Erro: Já existe uma estação no nodo {station.node_id}.")

    def _clear_cache(self):
        """
        Clears the internal routing cache. Useful when graph weights change (e.g. weather/traffic updates).
        """
        print("Limpando cache de rotas...")
        self.routing_cache = {
            'a_star': {},
            'uniform_cost': {},
            'bfs': {},
            'dfs': {},
            'greedy': {},
            'weighted_a_star': {}
        }

    def find_route(self, start_node_id: int, end_node_id: int, algorithm: str = 'a_star') -> Tuple[
        List[int], float, float]:
        """
        Finds a route between two nodes using the specified algorithm. Uses caching mechanism.

        :param start_node_id: ID of the starting node.
        :param end_node_id: ID of the destination node.
        :param algorithm: The search algorithm to use ('a_star', 'bfs', 'dfs', 'greedy', etc.).
        :return: A tuple containing (path_list, time_cost, distance_km).
        """
        if algorithm not in self.routing_cache:
            print(f"Algoritmo {algorithm} desconhecido. Usando 'a_star'.")
            algorithm = 'a_star'

        if start_node_id not in self.graph.nodes or end_node_id not in self.graph.nodes:
            print(f"Erro: Nó {start_node_id} ou {end_node_id} não existe no grafo")
            return [], float('inf'), float('inf')

        # 1. Verificar a cache
        cached_result = self.routing_cache[algorithm].get((start_node_id, end_node_id))
        if cached_result:
            return cached_result # cacheHit

        # 2. Se não está na cache, calcular
        path = []
        if algorithm == 'a_star':
            path = self.graph.a_star_search(start_node_id, end_node_id)
        elif algorithm == 'uniform_cost':
            path = self.graph.uniform_cost_search(start_node_id, end_node_id)
        elif algorithm == 'bfs':
            path = self.graph.bfs_search(start_node_id, end_node_id)
        elif algorithm == 'dfs':
            path = self.graph.dfs_search(start_node_id, end_node_id)
        elif algorithm == 'weighted_a_star':
            path = self.graph.weighted_a_star_search(start_node_id, end_node_id)
        elif algorithm == 'greedy':
            path = self.graph.greedy_best_first(start_node_id, end_node_id)
        else:
            # Fallback
            path = self.graph.a_star_search(start_node_id, end_node_id)

        if not path:
            return [], float('inf'), float('inf')

        cost_seconds, distance_km = self.graph.calculate_route_properties(path)

        result = (path, cost_seconds, distance_km)
        self.routing_cache[algorithm][(start_node_id, end_node_id)] = result

        return result

    def find_best_car_for_request(self, request_id: int, eco, algorithm: str = 'a_star'):
        """
        Identifies and assigns the optimal car for a given request.

        When eco=True, first considers ecological impact, then travel time.

        :param request_id: The ID of the request to process.
        :param eco: If True, prioritizes ecological impact over travel time.
        :param algorithm: The pathfinding algorithm to use for distance calculation.
        :return: A tuple (best_car_object, time_to_client, distance_to_client_km).
        """
        request = self.requests.get(request_id)
        if not request or request.status != RequestStatus.PENDENTE:
            return None, float('inf'), float('inf')

        if not hasattr(request, 'attempts_without_car'):
            request.attempts_without_car = 0

        if request.attempts_without_car >= 10:
            request.status = RequestStatus.CANCELADO
            return None, float('inf'), float('inf')

        eligible_cars = [
            car for car in self.cars.values()
            if car.status == CarStatus.DISPONIVEL and
               car.matches_preferences(request.num_passengers) and
               not car.is_on_reserve()
        ]

        if not eligible_cars:
            eligible_cars = [
                car for car in self.cars.values()
                if car.status == CarStatus.DISPONIVEL and
                   car.matches_preferences(request.num_passengers)
            ]

        if not eligible_cars:
            request.attempts_without_car += 1
            if request.attempts_without_car >= 10:
                request.status = RequestStatus.CANCELADO
            return None, float('inf'), float('inf')

        best_car = None
        best_ecological_impact = float('inf')
        best_time = float('inf')
        best_dist = float('inf')
        best_time_with_client = float('inf')
        best_dist_with_client = float('inf')
        best_total_time = float('inf')

        for car in eligible_cars:
            _, time_to_client, dist_to_client = self.find_route(
                car.current_node_id, request.origin_node_id, algorithm
            )

            _, time_with_client, dist_with_client = self.find_route(
                request.origin_node_id, request.destination_node_id, algorithm
            )

            if (time_to_client == float('inf') or time_with_client == float('inf') or
                    dist_to_client == float('inf') or dist_with_client == float('inf')):
                continue

            total_time = time_to_client + time_with_client
            total_distance = dist_to_client + dist_with_client

            if car.current_autonomy_km < total_distance:
                continue

            if eco:
                ecological_impact = car.ecological_cost_per_km * total_distance
                if ecological_impact < best_ecological_impact:
                    best_ecological_impact = ecological_impact
                    best_car = car
                    best_time = time_to_client
                    best_dist = dist_to_client
                    best_time_with_client = time_with_client
                    best_dist_with_client = dist_with_client
                    best_total_time = total_time
            else:
                if total_time < best_total_time:
                    best_total_time = total_time
                    best_car = car
                    best_time = time_to_client
                    best_dist = dist_to_client
                    best_time_with_client = time_with_client
                    best_dist_with_client = dist_with_client

        if not best_car:
            request.attempts_without_car += 1
            if request.attempts_without_car >= 10:
                request.status = RequestStatus.CANCELADO
            return None, float('inf'), float('inf')

        if request.attempts_without_car > 0:
            request.attempts_without_car = 0

        status_msg = "✅ (A Tempo)"
        if best_car.is_on_reserve():
            status_msg += " 🔋(RESERVA)"
        if eco:
            status_msg += " 🌿(ECO+)"

        total_time_seconds = best_time + best_time_with_client

        time_to_client_str = format_time(best_time)
        time_with_client_str = format_time(best_time_with_client)
        total_time_str = format_time(total_time_seconds)

        print(f"🚗 Carro {best_car.plate} {status_msg}")
        print(f"   🧭 Algoritmo: {algorithm.upper()}")
        print(f"   🌿 Modo Eco: {'SIM' if eco else 'NÃO'}")
        print(f"   📏 {best_dist:.1f}km → {best_dist_with_client:.1f}km = {best_dist + best_dist_with_client:.1f}km")
        print(f"   ⏱️  {time_to_client_str} + {time_with_client_str} = {total_time_str}")

        request.assign_car(best_car.plate)
        request.calculate_and_set_price(best_dist_with_client, best_car.operational_cost_per_km)
        best_car.set_status(CarStatus.EM_SERVICO)

        request.attempts_without_car = 0

        return best_car, best_time, best_dist

    def complete_request(self, request_id: int, algorithm: str = 'a_star') -> bool:
        """
        Finalizes a request, moving the car to the destination and updating status.

        :param request_id: The ID of the request to complete.
        :param algorithm: The algorithm used for the route.
        :return: True if successful, False otherwise.
        """
        req = self.requests.get(request_id)
        if not req or not req.assigned_car_id:
            return False

        car = self.cars.get(req.assigned_car_id)
        if not car:
            return False

        _, _, dist_to_client = self.find_route(
            car.current_node_id, req.origin_node_id, algorithm
        )

        _, _, dist_with_client = self.find_route(
            req.origin_node_id, req.destination_node_id, algorithm
        )

        if isinstance(dist_to_client, (list, tuple)):
            dist_to_client = float(dist_to_client[0]) if dist_to_client else float('inf')

        if isinstance(dist_with_client, (list, tuple)):
            dist_with_client = float(dist_with_client[0]) if dist_with_client else float('inf')

        if dist_to_client == float('inf') or dist_with_client == float('inf'):
            return False

        total_km = dist_to_client + dist_with_client
        if not car.drive(total_km):
            return False

        car.update_location(req.destination_node_id)
        car.set_status(CarStatus.DISPONIVEL)
        req.complete()

        return True

    def find_nearest_fuel_station(self, car: Car, algorithm: str = 'a_star'):
        """
        Finds the closest compatible fuel/charging station for a specific car.

        :param car: The car that needs refueling.
        :param algorithm: Pathfinding algorithm.
        :return: Tuple (best_station_obj, path, time, distance).
        """
        compatible_stations = [
            station for station in self.fuel_stations.values()
            if station.station_type == car.engine_type and station.is_available
        ]

        if not compatible_stations:
            return None, [], float('inf'), float('inf')

        best_station = None
        best_path = []
        min_time = float('inf')
        best_dist = float('inf')

        for station in compatible_stations:
            path, time, dist_km = self.find_route(
                car.current_node_id,
                station.node_id,
                algorithm
            )

            if not path:
                continue

            if car.current_autonomy_km < dist_km:
                continue

            if time < min_time:
                min_time = time
                best_dist = dist_km
                best_path = path
                best_station = station

        return best_station, best_path, min_time, best_dist

    def process_low_battery_cars(self, algorithm: str = 'a_star'):
        """
        Iterates through the fleet to check for low-battery cars and automatically
        routes them to the nearest station if critical.

        :param algorithm: Pathfinding algorithm to use for station search.
        """
        for car in self.cars.values():
            if car.status == CarStatus.DISPONIVEL and car.is_on_reserve():
                if not hasattr(car, 'reserve_ticks'):
                    car.reserve_ticks = 0

                car.reserve_ticks += 1

                if car.reserve_ticks >= 3:
                    station, path, time_to, dist = self.find_nearest_fuel_station(car, algorithm)

                    if station:
                        if car.current_autonomy_km >= dist:
                            if car.drive(dist):
                                car.update_location(station.node_id)
                                car.recharge()
                                car.reserve_ticks = 0
                            else:
                                print(f"❌ {car.plate} falhou ao conduzir para estação")
                        else:
                            print(f"🚨 EMERGÊNCIA: {car.plate} sem bateria! Teletransporte para estação")
                            car.update_location(station.node_id)
                            car.recharge()
                            car.reserve_ticks = 0
                    else:
                        print(f"⚠️  Nenhuma estação para {car.plate}")