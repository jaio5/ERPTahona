-- V50: amplía el teléfono de empresa para admitir dos números en la cabecera
-- (p. ej. "965 68 73 58 - 620 921 017"), que no caben en 20 caracteres.

ALTER TABLE empresa_config MODIFY COLUMN telefono VARCHAR(40);
