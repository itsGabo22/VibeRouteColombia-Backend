package com.routeoptimizer.service;

import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.repository.OrderRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio encargado de procesar la ubicación en tiempo real del repartidor.
 * Llama a PostGIS (< 5m) y si detecta colisión, dispara una alerta al
 * WebSocket.
 */
@Service
public class ProximityAlertService {

    private static final Logger log = LoggerFactory.getLogger(ProximityAlertService.class);

    // Regla de negocio de Caliche: "Una colisión es igual a estar a 5 metros"
    private static final double COLLISION_RADIUS_METERS = 5.0;

    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate messagingTemplate; // La bazuca de mensajes WebSocket

    public ProximityAlertService(OrderRepository orderRepository, SimpMessagingTemplate messagingTemplate) {
        this.orderRepository = orderRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Recibe la lat/lng de un repartidor y toma acción inmediata.
     */
    public void processDriverLocation(Long driverId, double lat, double lng) {
        // 1. PostGIS detecta los paquetes a menos de 5 metros de esa coordinate
        List<Order> nearbyOrders = orderRepository.findNearbyOrders(lat, lng, COLLISION_RADIUS_METERS);

        if (!nearbyOrders.isEmpty()) {
            // ¡Hubo colisión! Tomamos el paquete más relevante (el primero que nos devuelva
            // PostGIS)
            Order order = nearbyOrders.get(0);

            log.info("💥 ¡COLISIÓN DETECTADA POR POSTGIS! Repartidor {} está a <= 5m del pedido #{}",
                    driverId, order.getId());

            // 2. Preparamos la munición (JSON) para disparar al túnel WebSocket
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "PACKAGE_COLLISION");
            alert.put("message",
                    "📦 ¡Paquete cerca! Estás a menos de " + COLLISION_RADIUS_METERS + " metros de la entrega.");
            alert.put("orderId", order.getId());
            alert.put("address", order.getAddress());
            // Patron de vibración (Milisegundos: vibra 200, pausa 100, vibra 200, pausa
            // 100, vibra 500)
            alert.put("vibrationPattern", new int[] { 200, 100, 200, 100, 500 });

            // 3. Disparamos al canal específico de este repartidor
            String channel = "/topic/alerts/driver/" + driverId;
            messagingTemplate.convertAndSend(channel, alert);

            log.info("📡 Señal de vibración enviada al canal: {}", channel);
        }
    }
}
