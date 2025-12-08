-- =====================================================
-- SCRIPT DE DATOS INICIALES PARA ERP PANADERÍA TAHONA
-- =====================================================

-- CLIENTES
INSERT INTO clientes (codigo, nombre, cif, direccion, poblacion, codigo_postal, provincia, notas) VALUES
('CLI001', 'Panadería El Buen Pan', 'B12345678', 'Calle Mayor 15', 'Alicante', '03001', 'Alicante', 'Cliente preferente'),
('CLI002', 'Restaurante La Estación', 'B23456789', 'Avenida Constitución 45', 'Alicante', '03002', 'Alicante', 'Pedidos diarios'),
('CLI003', 'Hotel Costa Blanca', 'B34567890', 'Paseo Marítimo 100', 'Benidorm', '03500', 'Alicante', 'Entregas urgentes'),
('CLI004', 'Cafetería Central', 'B45678901', 'Plaza España 8', 'Elche', '03201', 'Alicante', NULL),
('CLI005', 'Supermercado Familia', 'B56789012', 'Calle San Juan 22', 'Alicante', '03003', 'Alicante', 'Pagos mensuales');

-- PROVEEDORES
INSERT INTO proveedores (nombre, cif, telefono, email, direccion, ciudad, provincia, cp, pais) VALUES
('Harinas del Levante S.L.', 'B98765432', '965123456', 'ventas@harinaslevante.es', 'Polígono Industrial Norte 45', 'Alicante', 'Alicante', '03006', 'España'),
('Levaduras Industriales S.A.', 'A87654321', '965234567', 'pedidos@levaduras.es', 'Calle Industria 12', 'Valencia', 'Valencia', '46000', 'España'),
('Ingredientes Profesionales', 'B76543210', '965345678', 'info@ingredientes.com', 'Avenida Europa 78', 'Murcia', 'Murcia', '30001', 'España'),
('Embalajes del Mediterráneo', 'B65432109', '965456789', 'ventas@embalajes.es', 'Calle Envases 5', 'Alicante', 'Alicante', '03010', 'España'),
('Maquinaria Panadera S.L.', 'B54321098', '965567890', 'contacto@maquinaria.es', 'Polígono San Jorge 23', 'Castellón', 'Castellón', '12001', 'España');

-- ALMACENES
INSERT INTO almacenes (codigo, nombre) VALUES
('ALM01', 'Almacén Principal'),
('ALM02', 'Almacén Refrigerado'),
('ALM03', 'Almacén de Materias Primas'),
('ALM04', 'Almacén de Productos Terminados'),
('ALM05', 'Almacén Temporal');

-- ARTÍCULOS
INSERT INTO articulos (codigo, descripcion, familia, unidad, iva, pvp, coste) VALUES
('PAN001', 'Pan de Barra 250g', 'Panadería', 'UND', 10.00, 0.85, 0.45),
('PAN002', 'Baguette Francesa', 'Panadería', 'UND', 10.00, 1.20, 0.65),
('PAN003', 'Pan Integral 500g', 'Panadería', 'UND', 10.00, 1.50, 0.80),
('PAN004', 'Pan de Pueblo 750g', 'Panadería', 'UND', 10.00, 2.20, 1.10),
('BOL001', 'Croissant Mantequilla', 'Bollería', 'UND', 10.00, 1.35, 0.70),
('BOL002', 'Napolitana Chocolate', 'Bollería', 'UND', 10.00, 1.40, 0.75),
('BOL003', 'Ensaimada Grande', 'Bollería', 'UND', 10.00, 3.50, 1.80),
('BOL004', 'Palmera de Chocolate', 'Bollería', 'UND', 10.00, 1.60, 0.85),
('PAST001', 'Tarta de Manzana', 'Pastelería', 'UND', 10.00, 15.00, 7.50),
('PAST002', 'Brownie Individual', 'Pastelería', 'UND', 10.00, 2.50, 1.25),
('PAST003', 'Magdalenas (6 uds)', 'Pastelería', 'PKG', 10.00, 3.80, 1.90),
('MP001', 'Harina de Trigo (25kg)', 'Materias Primas', 'SACO', 10.00, 18.50, 12.00),
('MP002', 'Levadura Fresca (500g)', 'Materias Primas', 'PKG', 10.00, 3.20, 2.10),
('MP003', 'Mantequilla (1kg)', 'Materias Primas', 'KG', 10.00, 8.50, 5.60),
('MP004', 'Azúcar (25kg)', 'Materias Primas', 'SACO', 10.00, 22.00, 15.00);

-- Nota: Las facturas y otros datos relacionados se pueden agregar después
-- por ahora solo insertamos los datos maestros básicos

