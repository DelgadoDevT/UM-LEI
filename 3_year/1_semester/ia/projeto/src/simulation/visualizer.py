import folium
from src.models.Model import Model
from src.models.Car import EngineType
from src.utils.Interface import console


def generate_map(model: Model, output_file="simulation_map.html"):
    """
    Generates an interactive HTML map visualizing the current state of the simulation.
    It plots nodes, edges (roads), stations, and current car positions.

    Optimized for performance by drawing straight lines instead of following exact road curvature,
    enabling near-instant generation.

    :param model: The Model object containing the simulation state.
    :param output_file: The filename for the generated HTML map.
    """
    # Substituído o print inicial por um status spinner do Rich
    with console.status(
            f"[bold cyan]VISUALIZADOR: A gerar mapa interativo (modo rápido) em '{output_file}'...[/bold cyan]",
            spinner="earth"):

        # 1. Configurar o mapa centrado em Braga
        braga_coords = [41.55032, -8.42005]
        m = folium.Map(location=braga_coords, zoom_start=14, tiles="Cartodb Positron")

        # 2. Desenhar Arestas (Estradas conhecidas do Grafo)
        edges_drawn = set()  # Evitar desenhar a mesma estrada duas vezes se sobrepostas

        for node_id, edges in model.graph.edges.items():
            origin = model.graph.nodes.get(node_id)
            if not origin: continue

            for edge in edges:
                dest = model.graph.nodes.get(edge.destination)
                if not dest: continue

                # Cria uma chave única para esta ligação (independente da direção)
                edge_key = tuple(sorted((node_id, edge.destination)))
                if edge_key in edges_drawn:
                    continue
                edges_drawn.add(edge_key)

                # Cor da linha muda se for sentido único ou duplo
                color = "#3388ff" if not edge.is_one_way else "#ff8833"
                weight = 4 if not edge.is_one_way else 3
                opacity = 0.7

                # OTIMIZAÇÃO: Desenhar linha reta entre os pontos (Instantâneo)
                route_coords = [[origin.lat, origin.lon], [dest.lat, dest.lon]]

                folium.PolyLine(
                    locations=route_coords,
                    color=color,
                    weight=weight,
                    opacity=opacity,
                    tooltip=f"Ligação {origin.id} -> {dest.id}"
                ).add_to(m)

        # 3. Desenhar Nodos e Estações
        for node in model.graph.nodes.values():
            station = model.fuel_stations.get(node.id)

            if station:
                # Ícone para Estação
                color = "green" if station.station_type == EngineType.ELETRICO else "red"
                icon_name = "bolt" if station.station_type == EngineType.ELETRICO else "tint"

                is_real = "(Real)" in station.name

                folium.Marker(
                    location=[node.lat, node.lon],
                    popup=f"<b>{station.name}</b><br>Preço: {station.price_per_unit}€",
                    icon=folium.Icon(color=color, icon=icon_name, prefix='fa')
                ).add_to(m)

                if is_real:
                    folium.CircleMarker(
                        location=[node.lat, node.lon],
                        radius=6,
                        color="gold",
                        fill=False
                    ).add_to(m)

            elif node.id == 99:
                # Exemplo de nodo especial (Garagem/Sede) se existir
                folium.Marker(
                    location=[node.lat, node.lon],
                    popup=f"Nodo {node.id}: {node.name}",
                    icon=folium.Icon(color="black", icon="ban", prefix='fa')
                ).add_to(m)
            else:
                # Nodos normais
                folium.CircleMarker(
                    location=[node.lat, node.lon],
                    radius=4,
                    color="#3388ff",
                    fill=True,
                    fill_color="white",
                    fill_opacity=1,
                    popup=f"Nodo {node.id}: {node.name}"
                ).add_to(m)

        # 4. Desenhar Carros (Posição Final)
        for car in model.cars.values():
            car_node = model.graph.nodes.get(car.current_node_id)
            if car_node:
                folium.Marker(
                    location=[car_node.lat, car_node.lon],
                    tooltip=f"Táxi {car.plate}",
                    popup=f"<b>{car.model}</b><br>Autonomia: {car.current_autonomy_km:.1f}km",
                    icon=folium.Icon(color="blue", icon="taxi", prefix='fa')
                ).add_to(m)

        m.save(output_file)

    # Mensagem de sucesso estilizada no final
    console.print(f"[bold green]✔ Mapa guardado com sucesso em '{output_file}'.[/bold green]")