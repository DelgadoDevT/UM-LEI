import csv
import os
from datetime import datetime
from typing import Dict

# Importar as classes necessárias
from src.models.Request import Request, RequestStatus
from src.models.Car import Car

def save_requests_report(requests: Dict[int, Request], output_filename: str):
    """
    Guarda um relatório de todos os pedidos (pendentes, concluídos, etc.) num ficheiro CSV.
    Isto permite a análise de métricas.
    """
    print(f"A guardar relatório de pedidos em {output_filename}...")

    # Define o cabeçalho do CSV
    fieldnames = [
        'request_id',
        'status',
        'origin_node_id',
        'destination_node_id',
        'num_passengers',
        'priority',
        'environmental_preference',
        'max_wait_time_seconds',
        'assigned_car_id',
        'final_price'
    ]

    try:
        with open(output_filename, mode='w', newline='', encoding='utf-8') as f:
            writer = csv.DictWriter(f, fieldnames=fieldnames)
            writer.writeheader()

            count = 0
            for req in requests.values():
                writer.writerow({
                    'request_id': req.request_id,
                    'status': req.status.value,
                    'origin_node_id': req.origin_node_id,
                    'destination_node_id': req.destination_node_id,
                    'num_passengers': req.num_passengers,
                    'priority': req.priority,
                    'environmental_preference': req.environmental_preference,
                    'max_wait_time_seconds': req.max_wait_time.total_seconds(),
                    'assigned_car_id': req.assigned_car_id or 'N/A',
                    'final_price': f"{req.price:.2f}" if req.price is not None else 'N/A'
                })
                count += 1
        print(f"-> Relatório de {count} pedidos guardado.")

    except IOError as e:
        print(f"ERRO: Não foi possível escrever no ficheiro {output_filename}. {e}")
    except Exception as e:
        print(f"ERRO inesperado ao guardar relatório: {e}")


def save_cars_report(cars: Dict[str, Car], output_filename: str):
    """
    Guarda o estado final de todos os carros da frota.
    """
    print(f"A guardar relatório de carros em {output_filename}...")

    fieldnames = [
        'plate',
        'model',
        'engine_type',
        'status',
        'current_node_id',
        'current_autonomy_km',
        'max_autonomy_km'
    ]

    try:
        with open(output_filename, mode='w', newline='', encoding='utf-8') as f:
            writer = csv.DictWriter(f, fieldnames=fieldnames)
            writer.writeheader()

            count = 0
            for car in cars.values():
                writer.writerow({
                    'plate': car.plate,
                    'model': car.model,
                    'engine_type': car.engine_type.value,
                    'status': car.status.value,
                    'current_node_id': car.current_node_id,
                    'current_autonomy_km': f"{car.current_autonomy_km:.2f}",
                    'max_autonomy_km': car.max_autonomy_km
                })
                count += 1
        print(f"-> Relatório de {count} carros guardado.")

    except IOError as e:
        print(f"ERRO: Não foi possível escrever no ficheiro {output_filename}. {e}")