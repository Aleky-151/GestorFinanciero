--==================================================
-- INSERCION DE CATEGORIAS PARA GASTOS E INGRESOS
--==================================================
ALTER TABLE categoria_gastos
ADD tipo VARCHAR2(50) CONSTRAINT ck_tipo_catgastos CHECK(tipo in ('FIJA','VARIABLE'));

ALTER TABLE categoria_ingresos
ADD tipo VARCHAR2(50) CONSTRAINT ck_tipo_catingresos CHECK(tipo in ('FIJA','VARIABLE'));

SELECT * FROM categoria_gastos;
SELECT * FROM categoria_ingresos;

UPDATE categoria_gastos SET tipo = 'FIJA'     WHERE categoria = 'Gastos de vivienda';
UPDATE categoria_gastos SET tipo = 'VARIABLE' WHERE categoria = 'Ocio';
UPDATE categoria_gastos SET tipo = 'VARIABLE' WHERE categoria = 'Comidas';
UPDATE categoria_gastos SET tipo = 'VARIABLE' WHERE categoria = 'Compras no planificadas';
UPDATE categoria_gastos SET tipo = 'VARIABLE' WHERE categoria = 'Suscripciones';

COMMIT;

INSERT INTO categoria_gastos (categoria, tipo) VALUES ('Servicios Básicos',        'FIJA');
INSERT INTO categoria_gastos (categoria, tipo) VALUES ('Alimentación',             'FIJA');
INSERT INTO categoria_gastos (categoria, tipo) VALUES ('Transporte',               'FIJA');
INSERT INTO categoria_gastos (categoria, tipo) VALUES ('Salud',                    'FIJA');
INSERT INTO categoria_gastos (categoria, tipo) VALUES ('Educación',                'FIJA');
INSERT INTO categoria_gastos (categoria, tipo) VALUES ('Seguros',                  'FIJA');
INSERT INTO categoria_gastos (categoria, tipo) VALUES ('Deudas y Préstamos',       'FIJA');
INSERT INTO categoria_gastos (categoria, tipo) VALUES ('Ahorro e Inversión',       'FIJA');
INSERT INTO categoria_gastos (categoria, tipo) VALUES ('Impuestos',                'VARIABLE');
INSERT INTO categoria_gastos (categoria, tipo) VALUES ('Restaurantes y Cafeterías','VARIABLE');
INSERT INTO categoria_gastos (categoria, tipo) VALUES ('Ropa y Calzado',           'VARIABLE');
INSERT INTO categoria_gastos (categoria, tipo) VALUES ('Cuidado Personal',         'VARIABLE');
INSERT INTO categoria_gastos (categoria, tipo) VALUES ('Mascotas',                 'VARIABLE');
INSERT INTO categoria_gastos (categoria, tipo) VALUES ('Hogar y Mantenimiento',    'VARIABLE');
INSERT INTO categoria_gastos (categoria, tipo) VALUES ('Regalos y Donaciones',     'VARIABLE');
INSERT INTO categoria_gastos (categoria, tipo) VALUES ('Viajes',                   'VARIABLE');
INSERT INTO categoria_gastos (categoria, tipo) VALUES ('Suscripciones Digitales',  'VARIABLE');
INSERT INTO categoria_gastos (categoria, tipo) VALUES ('Otros Gastos',             'VARIABLE');
COMMIT;

UPDATE categoria_ingresos SET tipo = 'FIJA'     WHERE categoria = 'Sueldo';
UPDATE categoria_ingresos SET tipo = 'VARIABLE' WHERE categoria = 'Propina / Extra';
UPDATE categoria_ingresos SET tipo = 'VARIABLE' WHERE categoria = 'Otros';

INSERT INTO categoria_ingresos (categoria, tipo) VALUES ('Bonos y Comisiones',       'VARIABLE');
INSERT INTO categoria_ingresos (categoria, tipo) VALUES ('Ingresos Freelance',       'VARIABLE');
INSERT INTO categoria_ingresos (categoria, tipo) VALUES ('Negocio Propio',           'VARIABLE');
INSERT INTO categoria_ingresos (categoria, tipo) VALUES ('Rentas',                   'VARIABLE');
INSERT INTO categoria_ingresos (categoria, tipo) VALUES ('Dividendos e Intereses',   'VARIABLE');
INSERT INTO categoria_ingresos (categoria, tipo) VALUES ('Devoluciones y Reembolsos','VARIABLE');
INSERT INTO categoria_ingresos (categoria, tipo) VALUES ('Venta de Bienes',          'VARIABLE');
INSERT INTO categoria_ingresos (categoria, tipo) VALUES ('Regalos Recibidos',        'VARIABLE');
INSERT INTO categoria_ingresos (categoria, tipo) VALUES ('Ayudas o Subsidios',       'VARIABLE');
COMMIT;
