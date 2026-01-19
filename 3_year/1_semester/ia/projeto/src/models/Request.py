import enum
import datetime


class RequestStatus(enum.Enum):
    """
    Enumeration representing the lifecycle status of a ride request.
    """
    PENDENTE = "pendente"
    ATRIBUIDO = "atribuido"  # A car has been assigned
    EM_CURSO = "em_curso"    # Client is inside the vehicle
    CONCLUIDO = "concluido"
    CANCELADO = "cancelado"


class Request:
    """
    Represents a client's request for transportation.

    :param request_id: Unique identifier for the request.
    :param origin_node_id: ID of the starting node.
    :param destination_node_id: ID of the destination node.
    :param num_passengers: Number of passengers.
    :param requested_time: Timestamp of when the request was made.
    :param priority: Priority level (higher means more urgent).
    :param environmental_preference: True if the client prefers an electric vehicle.
    :param max_wait_time_seconds: Maximum time the client is willing to wait (default 600s).
    """
    def __init__(self,
                 request_id: int,
                 origin_node_id: int,
                 destination_node_id: int,
                 num_passengers: int,
                 requested_time: datetime,
                 priority: int = 1,
                 environmental_preference: bool = False,
                 max_wait_time_seconds: int = 600):  # Default 10 min

        self.request_id = request_id
        self.origin_node_id = origin_node_id
        self.destination_node_id = destination_node_id
        self.num_passengers = num_passengers

        self.requested_time = requested_time  # Quando o pedido foi feito
        self.priority = priority  # Prioridade do pedido (maior = mais urgente)
        self.environmental_preference = environmental_preference  # True se prefere elétrico
        self.max_wait_time = datetime.timedelta(seconds=max_wait_time_seconds)
        self.attempts_without_car = 0

        self.status = RequestStatus.PENDENTE
        self.assigned_car_id: str | None = None
        self.price: float | None = None  # Preço final da viagem

    def __repr__(self):
        return (f"Request({self.request_id}, De: {self.origin_node_id}, Para: {self.destination_node_id}, "
                f"Paxs: {self.num_passengers}, Prio: {self.priority}, "
                f"Status: {self.status.value}, "
                f"Carro: {self.assigned_car_id or 'N/A'})")

    def assign_car(self, car_id: str):
        """
        Assigns a specific car to this request if it is still pending.

        :param car_id: The license plate or ID of the car.
        """
        if self.status == RequestStatus.PENDENTE:
            self.assigned_car_id = car_id
            self.status = RequestStatus.ATRIBUIDO
        else:
            print(f"Erro: Não é possível atribuir carro ao pedido {self.request_id} (Estado: {self.status.value})")

    def calculate_and_set_price(self, route_distance_km: float, car_cost_per_km: float):
        """
        Calculates the final price of the ride based on distance and operational costs.
        Sets the ``self.price`` attribute.

        Formula: Base Fare + (Distance * Car Cost * Profit Margin).
        Ensures a minimum price.

        :param route_distance_km: Total distance of the trip IN KILOMETERS.
        :param car_cost_per_km: Operational cost of the assigned vehicle IN EUROS PER KM.
        """
        BASE_FARE = 3.00
        MINIMUM_PRICE = 3.5
        PROFIT_MARGIN = 2.5

        # route_distance_km já está em km
        variable_cost = route_distance_km * car_cost_per_km * PROFIT_MARGIN
        final_price = BASE_FARE + variable_cost

        if final_price < MINIMUM_PRICE:
            final_price = MINIMUM_PRICE

        self.price = round(final_price, 2)
        print(f"Preço da viagem {self.request_id} definido: {self.price:.2f}€ (Distância: {route_distance_km:.2f}km)")

    def complete(self):
        """
        Marks the request as completed.
        """
        self.status = RequestStatus.CONCLUIDO

    def cancel(self, reason: str = "Limite de tentativas excedido"):
        """
        Cancels the request.

        :param reason: A description of why the request was canceled.
        """
        self.status = RequestStatus.CANCELADO
        print(f"❌ Pedido {self.request_id} cancelado: {reason}")