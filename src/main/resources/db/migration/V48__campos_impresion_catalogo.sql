-- V48: evolución de los campos de impresión.
-- Antes: par etiqueta/valor de texto libre, global (EMPRESA) o de un único cliente (CLIENTE).
-- Ahora:
--   * origen: SISTEMA (valor autorrellenado desde empresa/cliente por clave_sistema) o PROPIO (texto libre).
--   * visibilidad: TODOS | EXCEPTO | SOLO, con un conjunto de clientes en campo_impresion_clientes.

ALTER TABLE campos_personalizados
  ADD COLUMN origen VARCHAR(10) NOT NULL DEFAULT 'PROPIO' AFTER id,
  ADD COLUMN clave_sistema VARCHAR(60) NULL AFTER origen,
  ADD COLUMN visibilidad VARCHAR(10) NOT NULL DEFAULT 'TODOS';

-- La etiqueta pasa a ser opcional (los campos de sistema pueden usar la del catálogo).
ALTER TABLE campos_personalizados MODIFY COLUMN etiqueta VARCHAR(100) NULL;

-- Conjunto de clientes al que aplica la visibilidad EXCEPTO/SOLO.
CREATE TABLE IF NOT EXISTS campo_impresion_clientes (
  campo_id BIGINT NOT NULL,
  cliente_id BIGINT NOT NULL,
  PRIMARY KEY (campo_id, cliente_id),
  CONSTRAINT fk_cic_campo FOREIGN KEY (campo_id) REFERENCES campos_personalizados(id) ON DELETE CASCADE,
  CONSTRAINT fk_cic_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Migración de datos: los campos que eran de un cliente concreto pasan a visibilidad SOLO
-- con ese cliente en el conjunto. Los globales quedan en TODOS (valor por defecto).
UPDATE campos_personalizados SET visibilidad = 'SOLO'
  WHERE ambito = 'CLIENTE' AND cliente_id IS NOT NULL;

INSERT INTO campo_impresion_clientes (campo_id, cliente_id)
  SELECT id, cliente_id FROM campos_personalizados
  WHERE ambito = 'CLIENTE' AND cliente_id IS NOT NULL;

-- Eliminar las columnas obsoletas ambito / cliente_id.
ALTER TABLE campos_personalizados DROP FOREIGN KEY fk_campo_cliente;
ALTER TABLE campos_personalizados DROP INDEX idx_campos_cliente;
ALTER TABLE campos_personalizados DROP COLUMN cliente_id;
ALTER TABLE campos_personalizados DROP COLUMN ambito;
