import osmnx as ox
import pandas as pd
import numpy as np
from src.utils.ZoneCalc import calculate_zone
from src.utils.Common import haversine_distance
import os
import requests
import random


def get_real_fuel_stations():
    """
    Fetches real fuel and charging station data from Overpass API for Braga.

    :return: List of dictionaries with station data (lat, lon, type).
    :rtype: list
    """
    overpass_url = "http://overpass-api.de/api/interpreter"

    overpass_query = """
    [out:json];
    area["name"="Braga"]->.searchArea;
    (
      node["amenity"="fuel"](area.searchArea);
      node["amenity"="charging_station"](area.searchArea);
    );
    out body;
    """

    try:
        response = requests.post(overpass_url, data=overpass_query, timeout=60)
        data = response.json()
        stations = []

        for element in data.get('elements', []):
            if 'lat' in element and 'lon' in element:
                station_type = 'gas_station' if element.get('tags', {}).get('amenity') == 'fuel' else 'charge_station'
                stations.append({
                    'lat': element['lat'],
                    'lon': element['lon'],
                    'type': station_type
                })

        return stations

    except Exception as e:
        return []


def find_nearest_node(target_lat, target_lon, graph):
    """
    Finds the nearest graph node ID to a given coordinate.

    :param target_lat: Target latitude.
    :param target_lon: Target longitude.
    :param graph: The networkx graph object.
    :return: Node ID of the nearest node.
    """
    min_distance = float('inf')
    nearest_node_id = None

    for node_id, node_data in graph.nodes(data=True):
        node_lat = node_data['y']
        node_lon = node_data['x']
        distance = haversine_distance(target_lat, target_lon, node_lat, node_lon)

        if distance < min_distance:
            min_distance = distance
            nearest_node_id = node_id

    return nearest_node_id


def parse_max_speed(speed_value):
    """
    Parses and sanitizes max speed values from OSM data.

    :param speed_value: Raw speed value (str, int, or list).
    :return: Parsed speed integer or default 50.
    :rtype: int
    """
    if isinstance(speed_value, list):
        speeds = []
        for item in speed_value:
            try:
                if isinstance(item, str):
                    cleaned = item.lower().replace('km/h', '').replace('kph', '').strip()
                    speeds.append(int(cleaned))
                else:
                    speeds.append(int(item))
            except:
                continue
        return min(speeds) if speeds else 50

    if isinstance(speed_value, (int, float)):
        return int(speed_value)

    if isinstance(speed_value, str):
        try:
            cleaned = speed_value.lower().replace('km/h', '').replace('kph', '').strip()
            return int(cleaned)
        except:
            pass

    return 50


def get_ecological_zones(graph, u, v):
    """
    Calculates an ecological score for a road segment based on OSM tags.
    Higher scores indicate more environmentally friendly routes.

    :param graph: The networkx graph object.
    :param u: Origin node ID.
    :param v: Destination node ID.
    :return: Ecological score between 0.0 and 1.0.
    :rtype: float
    """
    edge_data = graph.get_edge_data(u, v, 0)
    if not edge_data:
        return 0.5

    score = 0.5
    tags = edge_data.get('tags', {}) or edge_data

    highway_type = tags.get('highway', '')
    if highway_type in ['residential', 'living_street', 'pedestrian']:
        score += 0.2
    elif highway_type in ['motorway', 'trunk']:
        score -= 0.3
    elif highway_type in ['primary']:
        score -= 0.2

    if tags.get('cycleway') or tags.get('bicycle'):
        score += 0.2

    maxspeed = tags.get('maxspeed', '')
    if isinstance(maxspeed, str) and '30' in maxspeed:
        score += 0.1
    elif isinstance(maxspeed, str) and '20' in maxspeed:
        score += 0.15

    score += random.uniform(-0.05, 0.05)

    return max(0.0, min(1.0, score))

def export_braga_network():
    """
    Main function to download Braga road network, integrate station data, and export to CSV.

    :return: Tuple containing nodes and edges DataFrames.
    :rtype: tuple
    """
    print("Starting Braga network export...")

    place_name = "Braga, Portugal"
    graph = ox.graph_from_place(place_name, network_type='drive')

    real_stations = get_real_fuel_stations()

    nodes_data = []
    for node_id, node_data in graph.nodes(data=True):
        lat = node_data['y']
        lon = node_data['x']
        zone = calculate_zone(lat, lon)

        node_type = 'station'

        nodes_data.append({
            'id': node_id,
            'lat': lat,
            'lon': lon,
            'zone': zone,
            'type': node_type
        })

    nodes_df = pd.DataFrame(nodes_data)

    station_updates = 0
    for station in real_stations:
        nearest_node_id = find_nearest_node(station['lat'], station['lon'], graph)
        if nearest_node_id:
            nodes_df.loc[nodes_df['id'] == nearest_node_id, 'type'] = station['type']
            station_updates += 1

    edges_data = []
    for u, v, key, edge_data in graph.edges(keys=True, data=True):
        if 'length' not in edge_data:
            node_u = graph.nodes[u]
            node_v = graph.nodes[v]
            edge_data['length'] = haversine_distance(
                node_u['y'], node_u['x'], node_v['y'], node_v['x']
            )

        max_speed = parse_max_speed(edge_data.get('maxspeed'))

        name = edge_data.get('name', f"Edge_{u}_{v}")
        if isinstance(name, list):
            name = name[0]

        is_one_way = False

        ecology_score = get_ecological_zones(graph, u, v)

        edges_data.append({
            'origin': u,
            'destination': v,
            'name': str(name),
            'length': int(edge_data['length']),
            'max_speed': max_speed,
            'is_one_way': is_one_way,
            'is_available': True,
            'ecology': round(ecology_score, 2)
        })

    edges_df = pd.DataFrame(edges_data)

    nodes_df_clean = nodes_df.copy()
    for col in nodes_df_clean.columns:
        if nodes_df_clean[col].dtype == 'object':
            nodes_df_clean[col] = nodes_df_clean[col].astype(str).str.replace(',', ';')

    os.makedirs('../../data', exist_ok=True)
    nodes_df_clean.to_csv('../../data/nodes.csv', index=False, sep=',', encoding='utf-8')
    edges_df.to_csv('../../data/edges.csv', index=False, sep=',', encoding='utf-8')

    gas_count = len(nodes_df[nodes_df['type'] == 'gas_station'])
    charge_count = len(nodes_df[nodes_df['type'] == 'charge_station'])

    print("Export completed")
    print(f"Total nodes: {len(nodes_df)}")
    print(f"Total edges: {len(edges_df)}")
    print(f"Fuel stations: {gas_count}")
    print(f"Charging stations: {charge_count}")

    return nodes_df, edges_df


if __name__ == "__main__":
    export_braga_network()