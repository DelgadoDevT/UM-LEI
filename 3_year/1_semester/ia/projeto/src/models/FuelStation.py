from src.models.Car import EngineType


class FuelStation:
    """
    Represents a refueling or charging station located at a specific graph node.

    :param node_id: The graph node ID where the station is located.
    :param name: The display name of the station.
    :param station_type: The type of fuel provided (Combustion or Electric).
    :param price_per_unit: Cost per liter or per kWh.
    """
    def __init__(self,
                 node_id: int,
                 name: str,
                 station_type: EngineType,
                 price_per_unit: float):
        self.node_id = node_id
        self.name = name
        self.station_type = station_type  # ELETRICO ou COMBUSTAO

        # Preço por kWh (elétrico) ou por Litro (combustão)
        self.price_per_unit = price_per_unit
        self.is_available = True  # Pode ser usado para simular estações offline

    def __repr__(self):
        return (f"FuelStation({self.name} at Node {self.node_id}, "
                f"Type: {self.station_type.value}, "
                f"Price: {self.price_per_unit:.3f})")