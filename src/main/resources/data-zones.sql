-- Habilitar PostGIS (por si acaso)
CREATE EXTENSION IF NOT EXISTS postgis;

-- Tabla de zonas de cobertura
CREATE TABLE IF NOT EXISTS coverage_zones (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    geom GEOMETRY(POLYGON, 4326) NOT NULL  -- Polígono en coordenadas GPS
);

-- Limpiar antes de insertar para evitar duplicados en reinicios de Docker
DELETE FROM coverage_zones;

-- Insertar polígono de Bogotá (aproximado)
INSERT INTO coverage_zones (name, city, geom) VALUES
('Bogotá Urbana', 'Bogotá', ST_GeomFromText(
  'POLYGON((-74.22 4.47, -74.22 4.84, -73.98 4.84, -73.98 4.47, -74.22 4.47))', 
  4326
)),
('Medellín Urbana', 'Medellín', ST_GeomFromText(
  'POLYGON((-75.66 6.17, -75.66 6.34, -75.50 6.34, -75.50 6.17, -75.66 6.17))', 
  4326
)),
('Cali Urbana', 'Cali', ST_GeomFromText(
  'POLYGON((-76.60 3.34, -76.60 3.50, -76.47 3.50, -76.47 3.34, -76.60 3.34))', 
  4326
)),
('Pasto Urbana', 'Pasto', ST_GeomFromText(
  'POLYGON((-77.34 1.18, -77.34 1.25, -77.24 1.25, -77.24 1.18, -77.34 1.18))', 
  4326
));
