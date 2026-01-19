import math
from src.utils.Common import haversine_distance

center_lat = 41.5500
center_lon = -8.4273
city_radium_km = 3500

center_directions = ["north", "northeast", "east", "southeast", "south", "southwest", "west", "northwest"]
periphery_directions = ["north", "south", "east", "west"]

def calculate_zone(lat, lon):
    """
    Determines the zone of a coordinate relative to the city center.

    :param lat: Latitude.
    :param lon: Longitude.
    :return: String identifying the zone (e.g., 'center_north', 'periphery_west').
    :rtype: str
    """
    distance = haversine_distance(lat, lon, center_lat, center_lon)
    if distance <= city_radium_km:
        direction = get_direction_8(lat, lon)
        return f"center_{direction}"
    else:
        direction = get_direction_4(lat, lon)
        return f"periphery_{direction}"

def get_direction_8(lat, lon):
    """
    Calculates the 8-cardinal direction relative to the center.

    :param lat: Latitude.
    :param lon: Longitude.
    :return: Direction string.
    :rtype: str
    """
    angle = math.degrees(math.atan2(lon - center_lon, lat - center_lat)) % 360
    sector = int((angle + 22.5) / 45) % 8
    return center_directions[sector]

def get_direction_4(lat, lon):
    """
    Calculates the 4-cardinal direction relative to the center.

    :param lat: Latitude.
    :param lon: Longitude.
    :return: Direction string.
    :rtype: str
    """
    angle = math.degrees(math.atan2(lon - center_lon, lat - center_lat)) % 360
    sector = int((angle + 45) / 90) % 4
    return periphery_directions[sector]