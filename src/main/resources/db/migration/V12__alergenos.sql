-- V12: Alérgenos alimentarios (UE 1169/2011) y corrección registro_sanitario
ALTER TABLE articulos ADD COLUMN IF NOT EXISTS alergenos VARCHAR(500);
ALTER TABLE recetas ADD COLUMN IF NOT EXISTS alergenos VARCHAR(500);

-- Asegurar que empresa_config tiene el campo registro_sanitario (por si acaso)
ALTER TABLE empresa_config MODIFY COLUMN registro_sanitario VARCHAR(100);
