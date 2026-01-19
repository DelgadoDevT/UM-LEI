import csv

# Importar as classes
from src.models.Graph import Graph
from src.models.Model import Model
from src.models.Node import Node
from src.models.Edge import Edge
from src.models.Car import Car, EngineType

# --- Funções de Carregamento (Parsing) ---

def load_nodes(graph: Graph, nodes_filepath: str):
    print(f"A carregar nós de {nodes_filepath}...")
    try:
        with open(nodes_filepath, mode='r', encoding='utf-8') as f:
            reader = csv.DictReader(f)
            count = 0
            for row in reader:
                try:
                    new_node = Node(
                        idx=int(row['id']),
                        lat=float(row['lat']),
                        lon=float(row['lon']),
                        zone=row['zone'],
                        typen=row['type']
                    )
                    graph.add_node(new_node)
                    count += 1
                except ValueError as e:
                    print(f"Erro ao processar linha de nó: {row} - {e}")
            print(f"-> {count} nós carregados.")

    except FileNotFoundError:
        print(f"ERRO: Ficheiro de nós não encontrado em {nodes_filepath}")
    except Exception as e:
        print(f"ERRO inesperado ao carregar nós: {e}")


def load_edges(graph: Graph, edges_filepath: str):
    print(f"A carregar arestas de {edges_filepath}...")
    try:
        with open(edges_filepath, mode='r', encoding='utf-8') as f:
            reader = csv.DictReader(f)
            count = 0
            for row in reader:
                try:
                    new_edge = Edge(
                        origin=int(row['origin']),
                        destination=int(row['destination']),
                        length=float(row['length']),
                        max_speed=float(row['max_speed']),
                        is_one_way=row['is_one_way'].lower() in ['true', '1'],
                        is_available=row['is_available'].lower() in ['true', '1'],
                        ecology=float(row['ecology'])
                    )
                    graph.add_edge(new_edge)
                    count += 1
                except ValueError as e:
                    print(f"Erro ao processar linha de aresta: {row} - {e}")
            print(f"-> {count} arestas carregadas.")

    except FileNotFoundError:
        print(f"ERRO: Ficheiro de arestas não encontrado em {edges_filepath}")
    except Exception as e:
        print(f"ERRO inesperado ao carregar arestas: {e}")


def load_cars(model: Model, cars_filepath: str):
    """
    Carrega os carros (cars) de um ficheiro CSV para o modelo.
    """
    print(f"A carregar carros de {cars_filepath}...")
    try:
        with open(cars_filepath, mode='r', encoding='utf-8') as f:
            reader = csv.DictReader(f, delimiter=';')
            count = 0
            for row in reader:
                try:
                    engine_type_str = row['engine_type'].upper()
                    engine_type = EngineType[engine_type_str]

                    new_car = Car(
                        plate=row['plate'],
                        model=row['model'],
                        engine_type=engine_type,
                        max_autonomy_km=float(row['max_autonomy_km']),
                        passenger_capacity=int(row['passenger_capacity']),
                        operational_cost_per_km=float(row['operational_cost_per_km']),
                        ecological_cost_per_km=float(row['ecological_cost_per_km']),
                        initial_node_id=int(row['initial_node_id'])
                    )
                    model.add_car(new_car)
                    count += 1
                except KeyError:
                    print(f"Erro: Tipo de motor inválido '{row['engine_type']}'. Use 'ELETRICO' ou 'COMBUSTAO'.")
                except ValueError as e:
                    print(f"Erro ao processar linha de carro: {row} - {e}")
            print(f"-> {count} carros carregados.")

    except FileNotFoundError:
        print(f"ERRO: Ficheiro de carros não encontrado em {cars_filepath}")
    except Exception as e:
        print(f"ERRO inesperado ao carregar carros: {e}")