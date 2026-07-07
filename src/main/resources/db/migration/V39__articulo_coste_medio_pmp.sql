-- V39: coste medio ponderado (PMP) por artículo para valoración de existencias
ALTER TABLE articulos ADD COLUMN coste_medio DECIMAL(10,4) NULL;
