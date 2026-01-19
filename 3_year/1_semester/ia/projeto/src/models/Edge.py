class Edge:
    """
    Represents a directed connection between two nodes in the graph.
    """
    def __init__(self, origin, destination, name=None, length=0.0, max_speed=0.0, is_one_way=False, is_available=True, ecology=0.5):
        """
        Initializes an Edge instance.

        :param origin: Origin node ID.
        :param destination: Destination node ID.
        :param name: Name of the street or edge.
        :param length: Length in meters.
        :param max_speed: Maximum speed in km/h.
        :param is_one_way: Boolean indicating if it is a one-way street.
        :param is_available: Boolean indicating if the edge is traversable.
        """
        self.origin = origin
        self.destination = destination
        self.name = name if name is not None else f"{origin}->{destination}"
        self.length = length
        self.max_speed = max_speed
        self.is_one_way = is_one_way
        self.is_available = is_available
        self.ecology = ecology
        self.cost = self.calculate_base_cost()

    def __str__(self):
        direction = "→" if self.is_one_way else "↔"
        return f"Edge({self.origin} {direction} {self.destination} - {self.length}m, name={self.name})"

    def __repr__(self):
        return self.__str__()

    def __eq__(self, other):
        if not isinstance(other, Edge):
            return False

        return (self.origin == other.origin and
                self.destination == other.destination and
                self.is_one_way == other.is_one_way)

    def __hash__(self):
        return hash((self.origin, self.destination, self.is_one_way))

    def get_base_travel_time(self):
        """
        Calculates the travel time based on length and max speed.

        :return: Time in seconds or infinity if unavailable.
        :rtype: float
        """
        if not self.is_available:
            return float('inf')

        speed_m_per_s = self.max_speed / 3.6
        return self.length / speed_m_per_s

    def calculate_base_cost(self):
        """
        Calculates the base cost of the edge.

        :return: Cost value.
        :rtype: float
        """
        available_multiplier = 1 if self.is_available else float('inf')
        return self.get_base_travel_time() * available_multiplier