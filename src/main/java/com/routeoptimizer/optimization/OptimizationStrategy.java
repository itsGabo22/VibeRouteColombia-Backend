package com.routeoptimizer.optimization;

import com.routeoptimizer.model.Coordinate;
import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.model.entity.Route;

import java.util.List;

/**
 * Interfaz para definir estrategias de optimización de rutas.
 * Permite cambiar entre diferentes algoritmos (OR-Tools, algoritmos genéticos,
 * etc.)
 * sin afectar al resto del sistema.
 */
public interface OptimizationStrategy {

  /**
   * Reordena una lista de pedidos para encontrar la ruta más eficiente.
   * 
   * @param pedidos       Lista de pedidos a optimizar.
   * @param startLocation Ubicación inicial (ej. el almacén o posición del
   *                      repartidor).
   * @return Un objeto Ruta con el orden optimizado y métricas de distancia.
   */
  Route optimize(List<Order> orders, Coordinate startLocation);
}
