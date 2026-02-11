-- V2: Insert mock resources (no users)

-- Helper function or subqueries will be used to resolve resource_type_id

INSERT INTO resource (name, description, resource_type_id, location, active) VALUES
-- Meeting Rooms
('Sala 101', 'Sala de reuniones pequeña (4 personas)', (SELECT id FROM resource_type WHERE name = 'ROOM'), 'Piso 1, Ala Norte', TRUE),
('Sala 102', 'Sala de reuniones estándar (8 personas)', (SELECT id FROM resource_type WHERE name = 'ROOM'), 'Piso 1, Ala Norte', TRUE),
('Sala Creativa', 'Sala con pizarras y sofás para brainstorming', (SELECT id FROM resource_type WHERE name = 'ROOM'), 'Piso 2, zona común', TRUE),

-- Conference Rooms
('Auditorio Principal', 'Auditorio para 100 personas con proyector HD', (SELECT id FROM resource_type WHERE name = 'CONFERENCE_ROOM'), 'Planta Baja', TRUE),
('Sala de Juntas A', 'Mesa ovalada para 20 personas, videoconferencia', (SELECT id FROM resource_type WHERE name = 'CONFERENCE_ROOM'), 'Piso 3, Dirección', TRUE),

-- VIP Rooms
('Sala VIP Ejecutiva', 'Sala exclusiva con catering y privacidad', (SELECT id FROM resource_type WHERE name = 'VIP_ROOM'), 'Penthouse', TRUE),

-- Corporate Vehicles
('Toyota Fortuner - ABC-123', 'Camioneta 4x4 asignada a operaciones', (SELECT id FROM resource_type WHERE name = 'CORPORATE_VEHICLE'), 'Parqueadero Sótano 2, Plaza 45', TRUE),
('Van Hyundai - XYZ-789', 'Transporte de equipos y personal (9 pasajeros)', (SELECT id FROM resource_type WHERE name = 'CORPORATE_VEHICLE'), 'Parqueadero Sótano 2, Plaza 46', TRUE),

-- Tech Equipment
('Proyector Portátil Epson', 'Proyector 4K móvil', (SELECT id FROM resource_type WHERE name = 'SHARED_TECH_EQUIPMENT'), 'Depósito TI, Piso 2', TRUE),
('MacBook Pro Prestamo #1', 'Laptop de reemplazo temporal (M2)', (SELECT id FROM resource_type WHERE name = 'SHARED_TECH_EQUIPMENT'), 'Helpdesk TI', TRUE),
('Speaker Jabra 510', 'Altavoz para conferencias portátil', (SELECT id FROM resource_type WHERE name = 'SHARED_TECH_EQUIPMENT'), 'Recepción Piso 2', TRUE),

-- Machines
('Contadora Billetes #1', 'Máquina de alta velocidad Glory GFS-220', (SELECT id FROM resource_type WHERE name = 'BILL_COUNTING_MACHINE'), 'Caja Principal, Sucursal Central', TRUE),
('Contadora Billetes #2', 'Máquina estándar de backoffice', (SELECT id FROM resource_type WHERE name = 'BILL_COUNTING_MACHINE'), 'Tesorería', TRUE);
