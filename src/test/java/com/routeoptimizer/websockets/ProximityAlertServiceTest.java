package com.routeoptimizer.websockets;

import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.repository.OrderRepository;
import com.routeoptimizer.service.ProximityAlertService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProximityAlertServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ProximityAlertService proximityAlertService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        // Usamos reflection o setters directos si están disponibles
        try {
            var field = Order.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(testOrder, 99L);
        } catch (Exception e) {
        }

        testOrder.setAddress("Calle 72 #10-30");
    }

    @Test
    @SuppressWarnings("null")
    void processLocation_CollisionDetected_SendsAlert() {
        // Arrange
        Long driverId = 1L;
        double latActual = 4.650000;
        double lngActual = -74.050000;

        // Simulamos que PostGIS detectó que este pedido está a < 5 metros
        when(orderRepository.findNearbyOrders(eq(latActual), eq(lngActual), eq(5.0)))
                .thenReturn(List.of(testOrder));

        // Act
        proximityAlertService.processDriverLocation(driverId, latActual, lngActual);

        // Assert
        // Capturamos el Payload que se envió por WebSocket
        ArgumentCaptor<String> canalCaptor = ArgumentCaptor.forClass(String.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor
                .forClass((Class<Map<String, Object>>) (Class<?>) Map.class);

        verify(messagingTemplate, times(1)).convertAndSend(canalCaptor.capture(), payloadCaptor.capture());

        assertEquals("/topic/alerts/driver/1", canalCaptor.getValue(), "El canal WebSocket es incorrecto");

        Map<String, Object> payloadEnviado = payloadCaptor.getValue();
        assertEquals("PACKAGE_COLLISION", payloadEnviado.get("type"), "El tipo de alerta debe ser PACKAGE_COLLISION");
        assertEquals(99L, payloadEnviado.get("orderId"), "El ID del pedido no coincide");
        assertNotNull(payloadEnviado.get("vibrationPattern"), "Debe incluir el patrón de vibración premium");
    }

    @Test
    @SuppressWarnings("null")
    void processLocation_WithoutCollision_DoesNothing() {
        // Arrange
        when(orderRepository.findNearbyOrders(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Collections.emptyList());

        // Act
        proximityAlertService.processDriverLocation(1L, 4.65, -74.05);

        // Assert
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }
}
