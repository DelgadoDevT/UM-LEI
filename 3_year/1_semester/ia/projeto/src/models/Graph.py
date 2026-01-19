import heapq
from queue import Queue
from src.models.Edge import Edge

class Graph:
    """
    Represents the graph structure of the road network, managing nodes, edges, and navigation logic.
    """
    def __init__(self):
        self.nodes = {}
        self.edges = {}
        self.max_speed_global = 0.0
        self.weather_multipliers = {}
        self.traffic_multipliers = {}

        self.beta = 0.2

        # Heuristic cache for performance optimization
        self._heuristic_cache = {}

    def add_node(self, new_node):
        """
        Adds a new node to the graph.

        :param new_node: The Node object to be added.
        """
        if new_node.id not in self.nodes:
            self.nodes[new_node.id] = new_node
            self.edges[new_node.id] = []

    def add_edge(self, new_edge):
        """
        Adds a new edge to the graph. If the edge is not one-way, a reverse edge is also created.

        :param new_edge: The Edge object to be added.
        """
        if new_edge.origin in self.edges:
            self.edges[new_edge.origin].append(new_edge)

            if self.max_speed_global < new_edge.max_speed:
                self.max_speed_global = new_edge.max_speed

        if not new_edge.is_one_way:
            reversed_edge = Edge(new_edge.destination, new_edge.origin, None, new_edge.length, new_edge.max_speed, False, new_edge.is_available)
            self.edges[new_edge.destination].append(reversed_edge)

            if self.max_speed_global < reversed_edge.max_speed:
                self.max_speed_global = reversed_edge.max_speed

    def heuristic(self, node1_id, node2_id):
        """
        Calculates the heuristic (optimistic travel time) between two nodes.
        Uses caching for performance optimization.

        For admissibility, this must NEVER overestimate the actual cost.
        We use straight-line distance at max speed, with minimum ecology penalty.

        :param node1_id: The ID of the start node.
        :param node2_id: The ID of the goal node.
        :return: Estimated time in seconds or infinity if nodes are invalid.
        :rtype: float
        """
        if node1_id not in self.nodes or node2_id not in self.nodes:
            return float('inf')

        # Check cache first
        cache_key = (node1_id, node2_id)
        if cache_key in self._heuristic_cache:
            return self._heuristic_cache[cache_key]

        node1 = self.nodes[node1_id]
        node2 = self.nodes[node2_id]

        # Get the optimistic time (straight-line distance at max speed)
        optimistic_time = node1.get_optimistic_time_to(node2, self.max_speed_global)

        # Account for the minimum ecology multiplier (best case: ecology = 1.0)
        # min_ecology_multiplier = 1 + beta * (1 - 1.0) = 1.0
        # So no adjustment needed for minimum case
        # Weather and traffic are assumed to be 1.0 in the best case

        # Cache the result
        self._heuristic_cache[cache_key] = optimistic_time

        return optimistic_time

    def clear_heuristic_cache(self):
        """
        Clears the heuristic cache. Should be called when graph structure changes
        or when max_speed_global is updated.
        """
        self._heuristic_cache = {}

    def set_weather_multiplier(self, zone, multiplier):
        """
        Sets the weather cost multiplier for a specific zone.

        :param zone: The zone identifier.
        :param multiplier: The multiplier value (1.0 is neutral).
        """
        self.weather_multipliers[zone] = multiplier

    def set_traffic_multiplier(self, zone, multiplier):
        """
        Sets the traffic cost multiplier for a specific zone.

        :param zone: The zone identifier.
        :param multiplier: The multiplier value (1.0 is neutral).
        """
        self.traffic_multipliers[zone] = multiplier

    def get_zone_multipliers(self, edge):
        """
        Retrieves the weather and traffic multipliers for the zone of the edge's origin.

        :param edge: The edge to check.
        :return: A tuple containing (weather_multiplier, traffic_multiplier).
        :rtype: tuple
        """
        origin_node = self.nodes.get(edge.origin)
        if not origin_node:
            return 1.0, 1.0

        zone = origin_node.zone

        weather_multiplier = self.weather_multipliers.get(zone, 1.0)
        traffic_multiplier = self.traffic_multipliers.get(zone, 1.0)

        return weather_multiplier, traffic_multiplier

    def get_edge_cost(self, edge):
        """
        Calculates the dynamic cost of traversing an edge.

        :param edge: The edge to evaluate.
        :return: The total calculated cost.
        :rtype: float
        """
        if not edge.is_available:
            return float('inf')

        weather_multiplier, traffic_multiplier = self.get_zone_multipliers(edge)
        base_cost = edge.cost * weather_multiplier * traffic_multiplier
        ecology_multiplier = 1 + self.beta * (1 - edge.ecology)

        return base_cost * ecology_multiplier

    def path_to_edges(self, path):
        """
        Converts a list of node IDs into a list of Edge objects.

        :param path: List of node IDs representing the route.
        :return: List of Edge objects connecting the nodes.
        :rtype: list
        """
        edges = []
        for i in range(len(path) - 1):
            current_node = path[i]
            next_node = path[i + 1]

            for edge in self.edges.get(current_node, []):
                if edge.destination == next_node:
                    edges.append(edge)
                    break
            else:
                raise ValueError(f"No edge found from {current_node} to {next_node}")

        return edges

    def calculate_route_cost(self, route_edges):
        """
        Calculates the total dynamic cost of a given route including traffic and weather multipliers per edge.

        :param route_edges: List of Edge objects in the route.
        :return: Total dynamic cost including traffic and weather.
        :rtype: float
        """
        if not route_edges:
            return 0

        total_cost = 0
        for edge in route_edges:
            if not edge.is_available:
                return float('inf')

            weather_multiplier, traffic_multiplier = self.get_zone_multipliers(edge)
            edge_cost = edge.cost * weather_multiplier * traffic_multiplier
            total_cost += edge_cost

        return total_cost

    def calculate_route_properties(self, node_path):
        """
        Calculates the total time and distance for a given path of nodes.
        Includes traffic and weather multipliers but NOT ecology factor.

        :param node_path: List of node IDs representing the path.
        :return: A tuple containing (total_time_in_seconds, total_distance_in_km).
        """
        if not node_path or len(node_path) < 2:
            return 0.0, 0.0

        total_time_seconds = 0.0
        total_distance_m = 0.0

        route_edges = self.path_to_edges(node_path)

        for edge in route_edges:
            if not edge.is_available:
                return float('inf'), float('inf')

            weather_multiplier, traffic_multiplier = self.get_zone_multipliers(edge)
            edge_time = edge.cost * weather_multiplier * traffic_multiplier

            total_time_seconds += float(edge_time)
            total_distance_m += float(edge.length)

        return float(total_time_seconds), float(total_distance_m / 1000.0)

    def dfs_search(self, start_id, goal_id):
        """
        Performs a Depth-First Search (DFS) to find a path.

        :param start_id: Start node ID.
        :param goal_id: Goal node ID.
        :return: List of node IDs representing the path.
        :rtype: list
        """
        if start_id not in self.nodes or goal_id not in self.nodes:
            return []

        stack = [(start_id, [start_id])]
        visited = set()

        while stack:
            current, path = stack.pop()

            if current == goal_id:
                return path

            if current in visited:
                continue

            visited.add(current)

            for edge in self.edges.get(current, []):
                if edge.is_available:
                    neighbor = edge.destination
                    if neighbor not in visited:
                        stack.append((neighbor, path + [neighbor]))

        return []

    def bfs_search(self, start_id, goal_id):
        """
        Performs a Breadth-First Search (BFS) to find the shortest path in terms of hops.

        :param start_id: Start node ID.
        :param goal_id: Goal node ID.
        :return: List of node IDs representing the path.
        :rtype: list
        """
        if start_id not in self.nodes or goal_id not in self.nodes:
            return []

        visited = set()
        queue = Queue()
        queue.put((start_id, [start_id]))
        visited.add(start_id)

        while not queue.empty():
            current, path = queue.get()

            if current == goal_id:
                return path

            for edge in self.edges.get(current, []):
                if edge.is_available:
                    neighbor = edge.destination
                    if neighbor not in visited:
                        visited.add(neighbor)
                        queue.put((neighbor, path + [neighbor]))

        return []

    def uniform_cost_search(self, start_id, goal_id):
        """
        Performs Uniform Cost Search (Dijkstra) considering dynamic edge costs.

        :param start_id: Start node ID.
        :param goal_id: Goal node ID.
        :return: List of node IDs representing the path.
        :rtype: list
        """
        if start_id not in self.nodes or goal_id not in self.nodes:
            return []

        counter = 0  # For tie-breaking
        queue = []
        heapq.heappush(queue, (0, counter, start_id, [start_id], 0))

        best_known_cost = {start_id: 0}
        expanded = set()

        while queue:
            current_cost, _, current_node, current_path, path_cost = heapq.heappop(queue)

            if current_node in expanded:
                continue

            # Skip if we've found a better path to this node after this was added to queue
            if path_cost > best_known_cost.get(current_node, float('inf')):
                continue

            if current_node == goal_id:
                return current_path

            expanded.add(current_node)

            for edge in self.edges.get(current_node, []):
                if not edge.is_available:
                    continue

                neighbor = edge.destination

                if neighbor in expanded:
                    continue

                edge_cost = self.get_edge_cost(edge)
                new_cost = path_cost + edge_cost

                if neighbor not in best_known_cost or new_cost < best_known_cost[neighbor]:
                    best_known_cost[neighbor] = new_cost
                    new_path = current_path + [neighbor]
                    counter += 1
                    heapq.heappush(queue, (new_cost, counter, neighbor, new_path, new_cost))

        return []

    def greedy_best_first(self, start_id, goal_id):
        """
        Performs Greedy Best-First Search using the heuristic.

        :param start_id: Start node ID.
        :param goal_id: Goal node ID.
        :return: List of node IDs representing the path.
        :rtype: list
        """
        if start_id not in self.nodes or goal_id not in self.nodes:
            return []

        queue = []
        heapq.heappush(queue, (self.heuristic(start_id, goal_id), start_id, [start_id]))
        visited = set()

        while queue:
            _, current, path = heapq.heappop(queue)

            if current == goal_id:
                return path

            if current in visited:
                continue
            visited.add(current)

            for edge in self.edges.get(current, []):
                if edge.is_available:
                    neighbor = edge.destination
                    if neighbor not in visited:
                        h = self.heuristic(neighbor, goal_id)
                        heapq.heappush(queue, (h, neighbor, path + [neighbor]))

        return []

    def weighted_a_star_search(self, start_id, goal_id, weight = 1.5):
        """
        Performs Weighted A* Search.

        :param start_id: Start node ID.
        :param goal_id: Goal node ID.
        :param weight: Heuristic weight multiplier (epsilon).
        :return: List of node IDs representing the path.
        :rtype: list
        """
        if start_id not in self.nodes or goal_id not in self.nodes:
            return []

        counter = 0  # For tie-breaking
        queue = []
        g_start = 0
        h_start = self.heuristic(start_id, goal_id)
        f_start = g_start + weight * h_start
        heapq.heappush(queue, (f_start, counter, start_id, [start_id], g_start))

        g_costs = {start_id: g_start}
        expanded = set()

        while queue:
            f_current, _, current_node, current_path, path_g_cost = heapq.heappop(queue)

            # Skip if we've already expanded this node
            if current_node in expanded:
                continue

            # Skip if we've found a better path to this node after this was added to queue
            if path_g_cost > g_costs.get(current_node, float('inf')):
                continue

            if current_node == goal_id:
                return current_path

            expanded.add(current_node)

            for edge in self.edges.get(current_node, []):
                if not edge.is_available:
                    continue

                neighbor = edge.destination

                if neighbor in expanded:
                    continue

                edge_cost = self.get_edge_cost(edge)
                g_new = path_g_cost + edge_cost

                if neighbor not in g_costs or g_new < g_costs[neighbor]:
                    g_costs[neighbor] = g_new
                    h_new = self.heuristic(neighbor, goal_id)
                    f_new = g_new + weight * h_new
                    new_path = current_path + [neighbor]
                    counter += 1
                    heapq.heappush(queue, (f_new, counter, neighbor, new_path, g_new))

        return []

    def a_star_search(self, start_id, goal_id):
        """
        Performs standard A* Search (optimal, weight=1.0).

        This is a dedicated implementation for efficiency, avoiding the overhead
        of calling weighted_a_star_search with weight=1.0.

        :param start_id: Start node ID.
        :param goal_id: Goal node ID.
        :return: List of node IDs representing the path.
        :rtype: list
        """
        if start_id not in self.nodes or goal_id not in self.nodes:
            return []

        counter = 0  # For tie-breaking
        queue = []
        g_start = 0
        h_start = self.heuristic(start_id, goal_id)
        f_start = g_start + h_start
        heapq.heappush(queue, (f_start, counter, start_id, [start_id], g_start))

        g_costs = {start_id: g_start}
        expanded = set()

        while queue:
            f_current, _, current_node, current_path, path_g_cost = heapq.heappop(queue)

            # Skip if we've already expanded this node
            if current_node in expanded:
                continue

            # Skip if we've found a better path to this node after this was added to queue
            if path_g_cost > g_costs.get(current_node, float('inf')):
                continue

            if current_node == goal_id:
                return current_path

            expanded.add(current_node)

            for edge in self.edges.get(current_node, []):
                if not edge.is_available:
                    continue

                neighbor = edge.destination

                if neighbor in expanded:
                    continue

                edge_cost = self.get_edge_cost(edge)
                g_new = path_g_cost + edge_cost

                if neighbor not in g_costs or g_new < g_costs[neighbor]:
                    g_costs[neighbor] = g_new
                    h_new = self.heuristic(neighbor, goal_id)
                    f_new = g_new + h_new  # weight=1.0 (standard A*)
                    new_path = current_path + [neighbor]
                    counter += 1
                    heapq.heappush(queue, (f_new, counter, neighbor, new_path, g_new))

        return []
