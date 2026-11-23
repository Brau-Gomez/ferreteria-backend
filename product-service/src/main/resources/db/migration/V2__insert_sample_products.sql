-- Ferretería Los Maestros - Product Service Sample Data
-- V2: Insertar productos de ejemplo para el catálogo

-- Herramientas Manuales (88c2ef6f-4c16-4cef-b7ac-ed9ff6747db3)
INSERT INTO products (code, name, description, price, compare_at_price, cost_price, stock, min_stock, max_stock, weight_kg, is_active, is_featured, category_id) VALUES
('HER-001', 'Martillo de garra 16 oz', 'Martillo de acero forjado con mango de fibra de vidrio, 16 onzas', 8990, 10990, 5500, 50, 5, 200, 0.5, true, true, '88c2ef6f-4c16-4cef-b7ac-ed9ff6747db3'),
('HER-002', 'Destornillador Phillips #2 x 100mm', 'Destornillador de punta magnética, mango ergonómico antideslizante', 2490, 2990, 1500, 100, 10, 500, 0.1, true, false, '88c2ef6f-4c16-4cef-b7ac-ed9ff6747db3'),
('HER-003', 'Llave inglesa 10"', 'Llave ajustable de acero al cromo vanadio, apertura máxima 30mm', 12990, 14990, 8000, 30, 3, 100, 0.6, true, false, '88c2ef6f-4c16-4cef-b7ac-ed9ff6747db3'),
('HER-004', 'Alicate universal 8"', 'Alicate de corte lateral y punta, acero templado, mango bimaterial', 7990, 9490, 4800, 40, 5, 150, 0.3, true, false, '88c2ef6f-4c16-4cef-b7ac-ed9ff6747db3'),
('HER-005', 'Juego de llaves Allen 9 piezas', 'Llaves hexagonales 1.5-10mm, acero S2, estuche plástico', 5990, 7490, 3600, 60, 5, 200, 0.2, true, true, '88c2ef6f-4c16-4cef-b7ac-ed9ff6747db3');

-- Herramientas Eléctricas (c5884dae-53fc-41ef-beef-66575573b9b0)
INSERT INTO products (code, name, description, price, compare_at_price, cost_price, stock, min_stock, max_stock, weight_kg, is_active, is_featured, category_id) VALUES
('ELE-001', 'Taladro percutor 650W', 'Taladro con percusión, 2 velocidades, portabrocas 13mm, maletín', 45990, 52990, 28000, 20, 2, 50, 2.1, true, true, 'c5884dae-53fc-41ef-beef-66575573b9b0'),
('ELE-002', 'Amoladora angular 4-1/2" 750W', 'Amoladora con protector ajustable, empuñadura lateral, 11000 RPM', 28990, 33990, 17500, 25, 3, 80, 1.8, true, false, 'c5884dae-53fc-41ef-beef-66575573b9b0'),
('ELE-003', 'Sierra circular 1400W 185mm', 'Sierra con guía láser, corte a 45° y 90°, colector de polvo', 52990, 59990, 32000, 15, 2, 40, 3.5, true, true, 'c5884dae-53fc-41ef-beef-66575573b9b0'),
('ELE-004', 'Atornillador inalámbrico 12V', 'Atornillador 2 velocidades, 21 posiciones de torque, 2 baterías Li-ion', 38990, 44990, 23500, 30, 3, 100, 1.2, true, false, 'c5884dae-53fc-41ef-beef-66575573b9b0'),
('ELE-005', 'Lijadora orbital 280W', 'Lijadora con base de 125mm, control de velocidad, bolsa recolectora', 22990, 26990, 14000, 20, 2, 60, 1.4, true, false, 'c5884dae-53fc-41ef-beef-66575573b9b0');

-- Ferretería General (9f28a5ad-1571-40f9-8a42-97f9a471a6a6)
INSERT INTO products (code, name, description, price, compare_at_price, cost_price, stock, min_stock, max_stock, weight_kg, is_active, is_featured, category_id) VALUES
('FER-001', 'Tornillo madera 4x40mm caja 100u', 'Tornillo rosca completa, cabeza avellanada, zincado', 3990, 4790, 2400, 200, 20, 1000, 0.5, true, false, '9f28a5ad-1571-40f9-8a42-97f9a471a6a6'),
('FER-002', 'Clavo acero 2" x 1kg', 'Clavo de cabeza plana, acero al carbono, para encofrado', 2890, 3490, 1750, 150, 10, 500, 1.0, true, false, '9f28a5ad-1571-40f9-8a42-97f9a471a6a6'),
('FER-003', 'Bisagra de libro 3" acero inox', 'Par de bisagras con rodamiento, acero inoxidable 304, tornillos incluidos', 4990, 5990, 3000, 80, 5, 300, 0.2, true, false, '9f28a5ad-1571-40f9-8a42-97f9a471a6a6'),
('FER-004', 'Cerradura de sobreponer 3 puntos', 'Cerradura de seguridad, 3 puntos de anclaje, 3 llaves, acabado negro', 24990, 29990, 15000, 25, 3, 80, 1.5, true, true, '9f28a5ad-1571-40f9-8a42-97f9a471a6a6'),
('FER-005', 'Cadena galvanizada 4mm x 10m', 'Cadena eslabón largo, zincada, carga de trabajo 250kg', 8990, 10990, 5400, 40, 5, 200, 2.0, true, false, '9f28a5ad-1571-40f9-8a42-97f9a471a6a6');

-- Pintura y Acabados (34b64081-c7fb-4d82-b07d-9c1387bb4d04)
INSERT INTO products (code, name, description, price, compare_at_price, cost_price, stock, min_stock, max_stock, weight_kg, is_active, is_featured, category_id) VALUES
('PIN-001', 'Pintura látex interior 4L blanco', 'Pintura acrílica lavable, rendimiento 12m²/L, bajo olor, secado rápido', 18990, 22990, 11500, 60, 5, 200, 4.5, true, true, '34b64081-c7fb-4d82-b07d-9c1387bb4d04'),
('PIN-002', 'Esmalte sintético 1L negro mate', 'Esmalte alquidálico, alta dureza, para metal y madera, exterior/interior', 6990, 8490, 4200, 80, 8, 300, 1.1, true, false, '34b64081-c7fb-4d82-b07d-9c1387bb4d04'),
('PIN-003', 'Brocha cerda natural 2"', 'Brocha de cerda china, mango madera, para esmaltes y barnices', 2490, 2990, 1500, 100, 10, 500, 0.1, true, false, '34b64081-c7fb-4d82-b07d-9c1387bb4d04'),
('PIN-004', 'Rodillo microfibra 18cm', 'Rodillo de microfibra 10mm, para pintura látex y esmaltes, sin burbujas', 3990, 4790, 2400, 90, 10, 400, 0.15, true, false, '34b64081-c7fb-4d82-b07d-9c1387bb4d04'),
('PIN-005', 'Cinta de enmascarar 48mm x 40m', 'Cinta papel crepado, adhesión media, fácil remoción sin residuos', 2990, 3590, 1800, 120, 12, 500, 0.2, true, false, '34b64081-c7fb-4d82-b07d-9c1387bb4d04');

-- Plomería (3061146f-b10d-438a-ae88-bf3ab91189cb)
INSERT INTO products (code, name, description, price, compare_at_price, cost_price, stock, min_stock, max_stock, weight_kg, is_active, is_featured, category_id) VALUES
('PLO-001', 'Tubería PVC 1/2" x 6m', 'Tubería PVC sanitario, clase 6.3 kg/cm², color gris', 4990, 5990, 3000, 100, 10, 500, 0.8, true, false, '3061146f-b10d-438a-ae88-bf3ab91189cb'),
('PLO-002', 'Grifo lavatorio monomando cromo', 'Grifo monomando cerámico, caño alto, aireador ahorra agua, cromo', 18990, 22990, 11500, 30, 3, 100, 1.2, true, true, '3061146f-b10d-438a-ae88-bf3ab91189cb'),
('PLO-003', 'Válvula de bola 1/2" bronce', 'Válvula bola paso total, rosca hembra, manija mariposa, PN25', 5990, 7190, 3600, 60, 5, 200, 0.3, true, false, '3061146f-b10d-438a-ae88-bf3ab91189cb'),
('PLO-004', 'Conexión PVC codo 90° 1/2"', 'Codo 90° PVC sanitario, soldable, bolsa 10 unidades', 2490, 2990, 1500, 150, 15, 600, 0.1, true, false, '3061146f-b10d-438a-ae88-bf3ab91189cb'),
('PLO-005', 'Sifón lavatorio cromado 1-1/4"', 'Sifón botella cromado, tubo extensible, rosca universal', 7990, 9590, 4800, 40, 5, 150, 0.4, true, false, '3061146f-b10d-438a-ae88-bf3ab91189cb');

-- Electricidad (d8b28a50-330c-40fc-bf2e-90fb84885362)
INSERT INTO products (code, name, description, price, compare_at_price, cost_price, stock, min_stock, max_stock, weight_kg, is_active, is_featured, category_id) VALUES
('ELC-001', 'Cable duplex 2x1.5mm x 100m', 'Cable flexible PVC, 2 conductores 1.5mm², blanco, uso domiciliario', 15990, 18990, 9600, 40, 4, 200, 3.5, true, false, 'd8b28a50-330c-40fc-bf2e-90fb84885362'),
('ELC-002', 'Interruptor simple 10A blanco', 'Interruptor de pared, 10A 250V, mecanismo rápido, caja incluida', 1890, 2290, 1100, 200, 20, 1000, 0.08, true, false, 'd8b28a50-330c-40fc-bf2e-90fb84885362'),
('ELC-003', 'Tomacorriente doble 10A blanco', 'Tomacorriente 2P+T 10A 250V, con tapa protectora, norma IRAM', 2490, 2990, 1500, 180, 15, 800, 0.1, true, false, 'd8b28a50-330c-40fc-bf2e-90fb84885362'),
('ELC-004', 'Breaker termomagnético 20A 2P', 'Breaker bipolar 20A, curva C, riel DIN, poder corte 6kA', 8990, 10790, 5400, 50, 5, 200, 0.2, true, true, 'd8b28a50-330c-40fc-bf2e-90fb84885362'),
('ELC-005', 'Cinta aisladora 19mm x 20m', 'Cinta PVC autoextinguible, 600V, varios colores, norma UL', 1290, 1590, 780, 300, 30, 1500, 0.05, true, false, 'd8b28a50-330c-40fc-bf2e-90fb84885362');

-- Seguridad (428b0d98-c779-4601-b523-96be174da723)
INSERT INTO products (code, name, description, price, compare_at_price, cost_price, stock, min_stock, max_stock, weight_kg, is_active, is_featured, category_id) VALUES
('SEG-001', 'Candado de combinación 4 dígitos', 'Candado latón macizo, arco acero endurecido, 4 dígitos reseteables', 6990, 8390, 4200, 60, 5, 200, 0.3, true, false, '428b0d98-c779-4601-b523-96be174da723'),
('SEG-002', 'Cámara WiFi exterior 1080p', 'Cámara IP 2MP, visión nocturna 30m, detección movimiento, IP66', 34990, 39990, 21000, 25, 2, 80, 0.5, true, true, '428b0d98-c779-4601-b523-96be174da723'),
('SEG-003', 'Alarma sonora 120dB', 'Alarma piezoeléctrica 120dB, 12V, para puertas y ventanas', 4990, 5990, 3000, 80, 8, 300, 0.15, true, false, '428b0d98-c779-4601-b523-96be174da723'),
('SEG-004', 'Extintor ABC 1kg', 'Extintor polvo químico seco 1kg, clase ABC, manómetro, soporte', 12990, 15590, 7800, 40, 5, 150, 1.8, true, false, '428b0d98-c779-4601-b523-96be174da723'),
('SEG-005', 'Chaleco reflectante talla M', 'Chaleco alta visibilidad, bandas reflectantes 5cm, cierre velcro', 4990, 5990, 3000, 100, 10, 500, 0.2, true, false, '428b0d98-c779-4601-b523-96be174da723');

-- Jardinería (53c30c50-d4e0-4e88-afa7-2d16b9a40da6)
INSERT INTO products (code, name, description, price, compare_at_price, cost_price, stock, min_stock, max_stock, weight_kg, is_active, is_featured, category_id) VALUES
('JAR-001', 'Manguera jardín 1/2" x 20m', 'Manguera PVC 3 capas, anti-UV, resistente a 20 bar, verde', 14990, 17990, 9000, 50, 5, 200, 2.5, true, false, '53c30c50-d4e0-4e88-afa7-2d16b9a40da6'),
('JAR-002', 'Maceta plástica 30cm terracota', 'Maceta redonda con plato, polipropileno UV, drenaje inferior', 3990, 4790, 2400, 100, 10, 500, 0.4, true, false, '53c30c50-d4e0-4e88-afa7-2d16b9a40da6'),
('JAR-003', 'Tijera podar bypass 200mm', 'Tijera hoja acero SK5, corte bypass, mango aluminio, bloqueo seguridad', 8990, 10790, 5400, 40, 4, 150, 0.3, true, true, '53c30c50-d4e0-4e88-afa7-2d16b9a40da6'),
('JAR-004', 'Rastrillo metálico 14 dientes', 'Rastrillo acero templado, 14 dientes, mango madera 1.5m', 6990, 8390, 4200, 60, 6, 200, 0.8, true, false, '53c30c50-d4e0-4e88-afa7-2d16b9a40da6'),
('JAR-005', 'Fertilizante NPK 15-15-15 5kg', 'Fertilizante granulado balanceado, nitrógeno-fósforo-potasio, jardín/huerta', 11990, 14390, 7200, 80, 8, 300, 5.0, true, false, '53c30c50-d4e0-4e88-afa7-2d16b9a40da6');

-- Organización (571bef96-ade7-4497-9fa3-8ac0d3f047f6)
INSERT INTO products (code, name, description, price, compare_at_price, cost_price, stock, min_stock, max_stock, weight_kg, is_active, is_featured, category_id) VALUES
('ORG-001', 'Caja organizadora 45L transparente', 'Caja plástica apilable, tapa hermética, asas laterales, 45 litros', 7990, 9590, 4800, 80, 8, 300, 1.2, true, false, '571bef96-ade7-4497-9fa3-8ac0d3f047f6'),
('ORG-002', 'Estante metálico 5 niveles 90cm', 'Estante acero epoxy, 5 entrepaños regulables, carga 175kg/nivel', 34990, 39990, 21000, 20, 2, 60, 12.0, true, true, '571bef96-ade7-4497-9fa3-8ac0d3f047f6'),
('ORG-003', 'Gancho adhesivo 3M 2kg paquete 4u', 'Gancho comando, adhesivo removible, soporta 2kg, blanco, 4 unidades', 3990, 4790, 2400, 150, 15, 600, 0.1, true, false, '571bef96-ade7-4497-9fa3-8ac0d3f047f6'),
('ORG-004', 'Carro de herramientas 3 bandejas', 'Carro metálico 3 bandejas, 4 ruedas giratorias, cajón superior con llave', 28990, 34990, 17500, 15, 2, 50, 8.5, true, false, '571bef96-ade7-4497-9fa3-8ac0d3f047f6'),
('ORG-005', 'Maletín herramientas vacío 18"', 'Maletín plástico rígido, bandeja extraíble, cerradura, asa ergonómica', 12990, 15590, 7800, 30, 3, 100, 2.0, true, false, '571bef96-ade7-4497-9fa3-8ac0d3f047f6');

-- Equipos de Protección (3e1ba677-4d62-461f-b9d1-bc0f86913355)
INSERT INTO products (code, name, description, price, compare_at_price, cost_price, stock, min_stock, max_stock, weight_kg, is_active, is_featured, category_id) VALUES
('EPP-001', 'Casco seguridad ABS blanco', 'Casco industrial ABS, suspensión 4 puntos, barbiquejo, norma ANSI Z89.1', 8990, 10790, 5400, 60, 6, 200, 0.4, true, false, '3e1ba677-4d62-461f-b9d1-bc0f86913355'),
('EPP-002', 'Guantes nitrilo talla L caja 100u', 'Guantes descartables nitrilo, sin polvo, texturados, azul, caja 100 und', 9990, 11990, 6000, 100, 10, 500, 0.6, true, true, '3e1ba677-4d62-461f-b9d1-bc0f86913355'),
('EPP-003', 'Lentes seguridad antirayadura', 'Lentes policarbonato, tratamiento antirayadura/antiempañante, UV400', 4990, 5990, 3000, 120, 12, 500, 0.05, true, false, '3e1ba677-4d62-461f-b9d1-bc0f86913355'),
('EPP-004', 'Botas seguridad punta acero 42', 'Bota cuero flor, punta acero 200J, suela PU bidensidad, resistente hidrocarburos', 32990, 38990, 19800, 25, 3, 80, 1.5, true, false, '3e1ba677-4d62-461f-b9d1-bc0f86913355'),
('EPP-005', 'Arnés seguridad 3 argollas', 'Arnés cuerpo completo, 3 argollas D, hebillas rápido ajuste, talla universal', 24990, 29990, 15000, 30, 3, 100, 1.8, true, true, '3e1ba677-4d62-461f-b9d1-bc0f86913355');