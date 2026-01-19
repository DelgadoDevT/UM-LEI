import random
from datetime import datetime

from src.models.Graph import Graph
from src.models.Model import Model
from src.models.Request import Request


# --- Funções de Gestão do Ambiente ---

def update_traffic(graph: Graph, zone: str, multiplier: float):
    """
    Atualiza o multiplicador de trânsito para uma zona específica do grafo.
    """
    print(f"AMBIENTE: Trânsito na zona '{zone}' atualizado para {multiplier}x.")
    graph.set_traffic_multiplier(zone, multiplier)


def update_weather(graph: Graph, zone: str, multiplier: float):
    """
    Atualiza o multiplicador de clima para uma zona específica do grafo.
    """
    print(f"AMBIENTE: Clima na zona '{zone}' atualizado para {multiplier}x.")
    graph.set_weather_multiplier(zone, multiplier)


def update_station_availability(model: Model, station_node_id: int, is_available: bool):
    """
    Simula uma estação de carregamento/abastecimento a ficar offline ou online.
    """
    station = model.fuel_stations.get(station_node_id)
    if station:
        station.is_available = is_available
        status = "DISPONÍVEL" if is_available else "INDISPONÍVEL"
        print(f"AMBIENTE: Estação {station.name} (Nodo {station_node_id}) está agora {status}.")
    else:
        print(f"AMBIENTE: Tentativa de atualizar estação inexistente no nodo {station_node_id}.")


def generate_random_request(model: Model, new_request_id: int) -> Request | None:
    """
    Gera um novo pedido de transporte aleatório.
    """
    if len(model.graph.nodes) < 2:
        print("ERRO: Não é possível gerar pedido. Insuficientes nós no grafo.")
        return None

    origin_id = random.choice(list(model.graph.nodes.keys()))
    destination_id = random.choice(list(model.graph.nodes.keys()))
    while origin_id == destination_id:
        destination_id = random.choice(list(model.graph.nodes.keys()))

    num_passengers = random.randint(1, 4)
    priority = random.choice([1, 1, 1, 2, 5])
    env_pref = random.choice([True, False, False])

    # AUMENTO DO TEMPO DE ESPERA: Entre 10 a 25 minutos (600s a 1500s)
    # Isto ajusta-se melhor à escala do mapa fornecido.
    max_wait = random.randint(600, 1500)

    new_req = Request(
        request_id=new_request_id,
        origin_node_id=origin_id,
        destination_node_id=destination_id,
        num_passengers=num_passengers,
        requested_time=datetime.now(),
        priority=priority,
        environmental_preference=env_pref,
        max_wait_time_seconds=max_wait
    )

    model.add_request(new_req)
    return new_req