import enum


class EngineType(enum.Enum):
    """
    Enumeration representing the engine type of a vehicle.
    """
    COMBUSTAO = "combustao"
    ELETRICO = "eletrico"


class CarStatus(enum.Enum):
    """
    Enumeration representing the current operational status of a vehicle.
    """
    DISPONIVEL = "disponivel"  # Ready for a new request
    EM_SERVICO = "em_servico"  # En route to client or driving client
    A_CARREGAR = "a_carregar"  # Refueling / Recharging
    INATIVO = "inativo"        # Unavailable (e.g., maintenance)


class Car:
    """
    Represents a vehicle in the fleet, tracking its autonomy, location, and status.

    :param plate: The license plate of the car.
    :param model: The model name of the car.
    :param engine_type: The type of engine (Combustion or Electric).
    :param max_autonomy_km: Maximum distance the car can travel on a full tank/charge.
    :param passenger_capacity: Maximum number of passengers.
    :param operational_cost_per_km: Cost to run the car per kilometer (monetary).
    :param ecological_cost_per_km: Environmental impact cost per kilometer.
    :param initial_node_id: The graph node ID where the car starts.
    """
    def __init__(self,
                 plate: str,
                 model: str,
                 engine_type: EngineType,
                 max_autonomy_km: float,
                 passenger_capacity: int,
                 operational_cost_per_km: float,
                 ecological_cost_per_km: float,
                 initial_node_id: int):

        self.plate = plate
        self.model = model
        self.engine_type = engine_type

        self.max_autonomy_km = max_autonomy_km
        self.current_autonomy_km = max_autonomy_km

        self.passenger_capacity = passenger_capacity

        if isinstance(operational_cost_per_km, (list, tuple)):
            operational_cost_per_km = float(operational_cost_per_km[0]) if operational_cost_per_km else 0.0

        if isinstance(ecological_cost_per_km, (list, tuple)):
            ecological_cost_per_km = float(ecological_cost_per_km[0]) if ecological_cost_per_km else 0.0

        self.operational_cost_per_km = operational_cost_per_km
        self.ecological_cost_per_km = ecological_cost_per_km

        self.current_node_id = initial_node_id
        self.status = CarStatus.DISPONIVEL

        self.reserve_threshold_km = self.max_autonomy_km * 0.30

    def __repr__(self):
        return (f"Car({self.plate} - {self.model} [{self.engine_type.value}], "
                f"Cap: {self.passenger_capacity}, "
                f"Aut: {self.current_autonomy_km:.2f}/{self.max_autonomy_km:.2f}km, "
                f"Node: {self.current_node_id}, "
                f"Status: {self.status.value})")

    def drive(self, distance_km: float) -> bool:
        """
        Simulates driving the car for a specific distance, reducing autonomy.

        :param distance_km: The distance to travel in kilometers.
        :return: True if the car had enough autonomy and moved, False otherwise.
        :rtype: bool
        """
        if self.current_autonomy_km >= distance_km:
            self.current_autonomy_km -= distance_km
            return True
        return False

    def recharge(self):
        """
        Refuels or recharges the vehicle to its maximum autonomy.
        """
        self.current_autonomy_km = self.max_autonomy_km
        print(f"Carro {self.plate} reabastecido. Autonomia: {self.current_autonomy_km:.2f}km")

    def is_on_reserve(self) -> bool:
        """
        Checks if the vehicle is running on reserve fuel/battery (below 30%).

        :return: True if current autonomy is below the reserve threshold.
        :rtype: bool
        """
        return self.current_autonomy_km < self.reserve_threshold_km

    def set_status(self, new_status: CarStatus):
        """
        Updates the operational status of the car.

        :param new_status: The new status to assign (CarStatus enum).
        """
        if isinstance(new_status, CarStatus):
            self.status = new_status
        else:
            print(f"Erro: Estado '{new_status}' inválido.")

    def update_location(self, new_node_id: int):
        """
        Updates the current node location of the car.

        :param new_node_id: The ID of the node where the car is now located.
        """
        self.current_node_id = new_node_id

    def matches_preferences(self, num_passengers: int) -> bool:
        """
        Checks if the car meets the criteria for a specific client request.

        :param num_passengers: Number of passengers required.
        :return: True if the car matches the preferences, False otherwise.
        :rtype: bool
        """
        # Verifica capacidade de passageiros
        if self.passenger_capacity < num_passengers:
            return False

        return True