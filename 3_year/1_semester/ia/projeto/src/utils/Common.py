import math

def haversine_distance(lat1, lon1, lat2, lon2):
    """
    Calculates the great-circle distance between two points on Earth.

    :param lat1: Latitude of point 1.
    :param lon1: Longitude of point 1.
    :param lat2: Latitude of point 2.
    :param lon2: Longitude of point 2.
    :return: Distance in meters.
    :rtype: float
    """
    r = 6371000
    lat1_rad = math.radians(lat1)
    lon1_rad = math.radians(lon1)
    lat2_rad = math.radians(lat2)
    lon2_rad = math.radians(lon2)

    dlat = lat2_rad - lat1_rad
    dlon = lon2_rad - lon1_rad

    a = math.sin(dlat / 2) ** 2 + math.cos(lat1_rad) * math.cos(lat2_rad) * math.sin(dlon / 2) ** 2
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

    return r * c


def format_time(seconds):
    """
    Formats a time duration given in seconds into a human-readable string.
    Returns strings like '2min30s' or '45s'.

    :param seconds: The time duration in seconds.
    :return: A formatted string representing the duration.
    :rtype: str
    """
    if seconds < 60:
        return f"{int(seconds)}s"
    else:
        mins = int(seconds // 60)
        secs = int(seconds % 60)
        if secs > 0:
            return f"{mins}min{secs}s"
        else:
            return f"{mins}min"