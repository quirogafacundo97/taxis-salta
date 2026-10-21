INSERT INTO tarifas (
    tipo,
    bajada_bandera,
    valor_ficha,
    fecha_desde,
    fecha_hasta
)
VALUES
    ('DIURNA', 980.00, 98.00, '2025-12-22', '2026-09-30'),
    ('NOCTURNA', 1176.00, 118.00, '2025-12-22', '2026-09-30'),
    ('DIURNA', 1176.00, 118.00, '2026-10-01', NULL),
    ('NOCTURNA', 1411.00, 142.00, '2026-10-01', NULL);

INSERT INTO feriados (
    fecha,
    descripcion
)
VALUES (
    '2026-08-17',
    'Feriado de prueba'
);
