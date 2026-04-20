package com.routeoptimizer.service;

import java.util.List;

import com.routeoptimizer.model.Coordinate;

/**
 * Interface for geolocation and mapping services.
 * Abstracts the real implementation (e.g., Google Maps, Mapbox).
 */
public interface MapService {

  /**
   * Converts a text address to coordinates (lat, lng).
   */
  Coordinate geocode(String address, String city);

  /**
   * Calculates the traveling distance between two coordinates.
   */
  Double calculateDistance(Coordinate origin, Coordinate destination);

  /**
   * Suggests corrections for an invalid address.
   */
  List<String> suggestAddress(String text);

  /**
   * Gets the distance matrix (in meters) between a list of points.
   */
  long[][] getDistanceMatrix(List<Coordinate> points);

  /**
   * Gets the detailed directions (polyline) between origin, destination and waypoints.
   */
  String getDirections(Coordinate origin, Coordinate destination, List<Coordinate> waypoints);

  /**
   * Gets a traffic factor (0.0 to 1.0) for a specific city.
   */
  Double getTrafficFactor(String city);
}
