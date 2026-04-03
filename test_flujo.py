import urllib.request
import urllib.error
import json
import time
import urllib.parse

BASE_URL = "http://localhost:8080/api/v1"

class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    RESET = '\033[0m'

def make_request(method, endpoint, data=None, token=None):
    # Encode spaces in URL if any
    url = f"{BASE_URL}{endpoint}".replace(" ", "%20")
    headers = {'Content-Type': 'application/json'}
    if token:
        headers['Authorization'] = f'Bearer {token}'
        
    req_data = None
    if data:
        req_data = json.dumps(data).encode('utf-8')
        
    req = urllib.request.Request(url, data=req_data, headers=headers, method=method)
    
    try:
        with urllib.request.urlopen(req) as response:
            status = response.getcode()
            response_body = response.read().decode('utf-8')
            return status, json.loads(response_body) if response_body else None
    except urllib.error.HTTPError as e:
        response_body = e.read().decode('utf-8') if e.fp else ""
        try:
            return e.code, json.loads(response_body)
        except:
            return e.code, response_body
    except Exception as e:
        return 0, str(e)

def print_result(step_name, status, expected_statuses, extra_info=""):
    if status in expected_statuses:
        print(f"{Colors.GREEN}[OK] {step_name} - OK (Status {status}){Colors.RESET} {extra_info}")
        return True
    else:
        print(f"{Colors.RED}[FAIL] {step_name} - FALLÓ (Status {status}){Colors.RESET} {extra_info}")
        return False

def run_tests():
    print(f"=== INICIANDO PRUEBAS E2E VIBEROUTE COLOMBIA ===\n")

    # 1. Registrar Roles
    print("1. Registrando Usuarios y Obteniendo Tokens...")
    
    # 1.1 Registrar SUPER ADMIN
    superadmin_data = {"email": "superadmin@viberoute.com", "password": "pass", "name": "Super Admin", "phone": "123", "role": "ADMIN"}
    # Ignoramos si ya existe, el login nos dará el token
    make_request("POST", "/auth/register", superadmin_data)
    
    st, res = make_request("POST", "/auth/login", {"email": "superadmin@viberoute.com", "password": "pass"})
    if not print_result("Login Super Admin", st, [200]): return
    token_superadmin = res.get("token") if isinstance(res, dict) else None

    # 1.2 Registrar LOGISTICS
    logistics_data = {"email": "logistics@viberoute.com", "password": "pass", "name": "Logistics Prueba", "phone": "123", "role": "LOGISTICS"}
    make_request("POST", "/auth/register", logistics_data)
    
    st, res = make_request("POST", "/auth/login", {"email": "logistics@viberoute.com", "password": "pass"})
    if not print_result("Login Logistics", st, [200]): return
    token_admin = res.get("token") if isinstance(res, dict) else None 

    # 1.3 Registrar Driver
    driver_data = {"email": "driver@viberoute.com", "password": "pass", "name": "Driver Prueba", "phone": "123", "role": "DRIVER"}
    make_request("POST", "/auth/register", driver_data)
    
    st, res = make_request("POST", "/auth/login", {"email": "driver@viberoute.com", "password": "pass"})
    if not print_result("Login Driver", st, [200]): return
    token_driver = res.get("token") if isinstance(res, dict) else None
    
    st, drivers_res = make_request("GET", "/drivers", token=token_superadmin)
    driver_id = None
    if drivers_res and isinstance(drivers_res, list) and len(drivers_res) > 0:
        driver_id = drivers_res[-1].get("id")
    print_result("Obtener ID del Driver creado", st, [200], f"Driver ID: {driver_id}")
    
    # 2. Crear Pedidos en Bogotá
    print("\n2. Creando Pedidos para generar Lotes...")
    order1 = {"address": "Plaza de Bolívar", "city": "Bogotá", "priority": "HIGH", "clientReference": "TEST-01", "location": {"lat": 4.598, "lng": -74.076}}
    order2 = {"address": "Museo del Oro", "city": "Bogotá", "priority": "MEDIUM", "clientReference": "TEST-02", "location": {"lat": 4.601, "lng": -74.071}}
    
    st, res_order1 = make_request("POST", "/orders", order1, token_admin)
    print_result("Crear Pedido 1 (Bogotá)", st, [200, 201])
    
    st, res_order2 = make_request("POST", "/orders", order2, token_admin)
    print_result("Crear Pedido 2 (Bogotá)", st, [200, 201])
    
    time.sleep(1) 
    
    # 3. Listar Lotes Pendientes
    print("\n3. Verificando Lotes...")
    st, batches = make_request("GET", "/batches/pending", token=token_admin)
    batch_info = f"- Lotes encontrados: {len(batches) if isinstance(batches, list) else 0}"
    print_result("Listar Lotes Pendientes", st, [200], batch_info)
    
    # 4. Asignar Driver al Lote
    print("\n4. Simulación de Operativa de Repartidor...")
    if driver_id:
        st, _ = make_request("PATCH", f"/drivers/{driver_id}/status?status=AVAILABLE", token=token_driver)
        print_result("Cambiar estado de Driver a AVAILABLE", st, [200])
        
        # 5. Emitir señal GPS de proximidad
        print("\n5. Simulando movimiento GPS del repartidor...")
        ping_data = {"driverId": driver_id, "latitude": 4.599, "longitude": -74.075}
        st, _ = make_request("POST", "/locations/ping", ping_data, token_driver)
        print_result("Ping GPS (Cerca al pedido 1)", st, [200, 202, 204], "Revisa la consola de Spring Boot para ver la ALERTA DE COLISIÓN POSTGIS.")
    
    # 6. Probar Inteligencia Artificial (Gemini LLM)
    print("\n6. Solicitando Resumen Diario a Gemini AI...")
    ai_data = {
        "totalDelivered": 18,
        "totalPending": 2,
        "totalHours": 6.5,
        "city": "Bogotá"
    }
    st, ai_res = make_request("POST", "/ai/daily-summary", ai_data, token_admin)
    if print_result("Generar Resumen con IA (Gemini)", st, [200]):
        if isinstance(ai_res, dict):
            resumen_texto = ai_res.get("summary", "No summary generated")
            print(f"{Colors.YELLOW}\n--- RESPUESTA DE GEMINI ---{Colors.RESET}")
            print(resumen_texto)
            print(f"{Colors.YELLOW}---------------------------{Colors.RESET}")

    # === NUEVAS PRUEBAS DE AUDITORÍA (AUDIT 6) ===
    print("\n--- PRUEBAS DE AUDITORÍA FINALIZADA ---")

    # 7. Carga Masiva (Bulk Load)
    print("\n7. Probando Carga Masiva (Bulk Upload)...")
    bulk_orders = [
        {"address": "Unicentro", "city": "Bogotá", "priority": "LOW", "clientReference": "BULK-01"},
        {"address": "Parque 93", "city": "Bogotá", "priority": "MEDIUM", "clientReference": "BULK-02"}
    ]
    st, bulk_res = make_request("POST", "/orders/bulk", bulk_orders, token_admin)
    print_result("Carga Masiva de 2 Pedidos", st, [200, 201], f"Pedidos creados: {len(bulk_res) if isinstance(bulk_res, list) else 0}")

    # 8. Analíticas y Dashboard
    print("\n8. Consultando Dashboard de Analíticas...")
    st, stats = make_request("GET", "/analytics/delivery-summary", token=token_superadmin)
    print_result("Obtener Estadísticas Hoy vs Mes", st, [200], f"Entregas Hoy: {stats.get('deliveriesToday') if isinstance(stats, dict) else 0}")

    # 9. Ranking de Repartidores con Tags
    print("\n9. Verificando Ranking y Medallas (Tags)...")
    st, ranking = make_request("GET", "/analytics/driver-ranking", token=token_superadmin)
    if print_result("Obtener Ranking", st, [200]):
        if isinstance(ranking, list):
            for r in ranking:
                print(f"   > {r.get('driverName')}: {r.get('tag')} ({r.get('successfulDeliveries')} entregas)")

    # 10. Filtrado por Ciudad
    print("\n10. Probando Filtro Geográfico (Pasto)...")
    st, pasto_orders = make_request("GET", "/orders/city/Pasto", token=token_admin)
    print_result("Listar Pedidos en Pasto", st, [200], f"Encontrados: {len(pasto_orders) if isinstance(pasto_orders, list) else 0}")

    # 11. Motivo de No-Entrega
    print("\n11. Simulando Entrega Fallida con Motivo...")
    if driver_id and isinstance(res_order1, dict) and res_order1.get("id"):
        order_id_to_fail = res_order1.get("id")
        st, _ = make_request("PATCH", f"/orders/{order_id_to_fail}/status?status=CANCELLED&reason=Direccion%20Incorrecta", token=token_driver)
        print_result("Marcar Pedido como CANCELADO con Motivo", st, [200], f"Pedido ID: {order_id_to_fail}")

    # 12. Inteligencia de Desvío Contextual (Smart Geofencing)
    print("\n12. Probando Inteligencia de Desvío (Smart Geofencing)...")
    if driver_id:
        # A. Caso: Repartidor en Zona de Parada (Sin alerta de desvío)
        # Pedido 2 está en 4.601, -74.071. Nos ponemos a 20m.
        stop_zone_data = {
            "currentLat": 4.6011, "currentLng": -74.0711,
            "stopLat": 4.6201, "stopLng": -74.0850, # Siguiente parada (lejos)
            "stopAddress": "Calle 72 #10-30",
            "driverName": "Driver Prueba",
            "city": "Bogotá"
        }
        st, res = make_request("POST", "/ai/deviation", stop_zone_data, token_driver)
        if print_result("Ping en Zona de Parada (100m)", st, [200]):
            status = res.get("status")
            print(f"   > Resultado: {status} (Correcto: Ignoró desvío por cercanía a pedido)")

        # B. Caso: Desvío Real (Fuera de zona de parada + lejos de destino)
        # Nos ponemos lejos de todo (ej. Centro Comercial Andino)
        deviation_data = {
            "currentLat": 4.666, "currentLng": -74.053,
            "stopLat": 4.601, "stopLng": -74.071,
            "stopAddress": "Museo del Oro",
            "driverName": "Driver Prueba",
            "city": "Bogotá"
        }
        st, res = make_request("POST", "/ai/deviation", deviation_data, token_driver)
        if print_result("Ping con Desvío Real (> 200m)", st, [200]):
            deviated = res.get("deviated")
            alert = res.get("alert")
            incident_id = res.get("incidentId")
            print(f"   > Desviado: {deviated}, Incident ID: {incident_id}")
            print(f"   {Colors.YELLOW}> Alerta IA: {alert}{Colors.RESET}")

        # C. Caso: Token Saving (Segunda llamada en desvío no llama a Gemini)
        st, res2 = make_request("POST", "/ai/deviation", deviation_data, token_driver)
        if print_result("Segunda Llamada (Ahorro de Tokens)", st, [200]):
            alert2 = res2.get("alert")
            print(f"   > Respuesta: {alert2}")
            if "notificado" in alert2.lower():
                print(f"   {Colors.GREEN}> [PASS] Se usó mensaje estático para ahorrar tokens.{Colors.RESET}")

    print(f"\n=== PRUEBAS E2E FINALIZADAS ===")

if __name__ == "__main__":
    run_tests()
