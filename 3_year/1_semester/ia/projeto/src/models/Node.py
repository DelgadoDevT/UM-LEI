from src.utils.Common import haversine_distance

class Node:
    """
    Represents a specific point in the map/graph.
    """
    def __init__(self, idx, lat, lon, zone, typen="station", name: str | None = None):
        """
        Initializes a Node instance.

        :param idx: Unique identifier for the node.
        :param lat: Latitude.
        :param lon: Longitude.
        :param zone: Zone identifier.
        :param typen: Type of the node (e.g., 'station', 'charge_station').
        :param name: Optional name for the node.
        """
        self.id = idx
        self.lat = lat
        self.lon = lon
        self.zone = zone
        self.type = typen
        self.name = name or f"Node {idx}"

    def __str__(self):
        return f"Node({self.id}: {self.name} - {self.type})"

    def __repr__(self):
        return self.__str__()

    def __eq__(self, other):
        if not isinstance(other, Node):
            return False
        return self.id == other.id

    def __hash__(self):
        return hash(self.id)

    def get_optimistic_time_to(self, other_node, max_speed_kmh):
        """
        Calculates the optimistic travel time to another node using Haversine distance.

        :param other_node: Target Node object.
        :param max_speed_kmh: Global maximum speed assumption.
        :return: Minimum time in seconds.
        :rtype: float
        """
        if other_node is None:
            return float('inf')
        if max_speed_kmh is None or max_speed_kmh <= 0:
            return float('inf')
        distance_m = haversine_distance(self.lat, self.lon, other_node.lat, other_node.lon)
        distance_km = distance_m / 1000.0  # Convert meters to kilometers
        min_time_seconds = (distance_km / max_speed_kmh) * 3600
        return min_time_seconds

    def is_charge_station(self):
        """Checks if node is a charging station."""
        return self.type == "charge_station"

    def is_gas_station(self):
        """Checks if node is a gas station."""
        return self.type == "gas_station"

    def is_station(self):
        """Checks if node is a generic station."""
        return self.type == "station"