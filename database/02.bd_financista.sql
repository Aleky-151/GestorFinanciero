-- =====================================================
-- GESTOR FINANCIERO - MVP
-- Tablespace: ts_finanzas
-- =====================================================
-- ---------- CATEGORÍAS ----------

CREATE TABLE categoria_cuentas (
    id_categoria  NUMBER GENERATED ALWAYS AS IDENTITY,
    categoria     VARCHAR2(50) NOT NULL,
    CONSTRAINT pk_categoria_cuentas PRIMARY KEY (id_categoria)
        USING INDEX TABLESPACE ts_finanzas,
    CONSTRAINT uq_categoria_cuentas UNIQUE (categoria)
        USING INDEX TABLESPACE ts_finanzas
) TABLESPACE ts_finanzas;

CREATE TABLE categoria_gastos (
    id_categoria  NUMBER GENERATED ALWAYS AS IDENTITY,
    categoria     VARCHAR2(50) NOT NULL,
    CONSTRAINT pk_categoria_gastos PRIMARY KEY (id_categoria)
        USING INDEX TABLESPACE ts_finanzas,
    CONSTRAINT uq_categoria_gastos UNIQUE (categoria)
        USING INDEX TABLESPACE ts_finanzas
) TABLESPACE ts_finanzas;

CREATE TABLE categoria_ingresos (
    id_categoria  NUMBER GENERATED ALWAYS AS IDENTITY,
    categoria     VARCHAR2(50) NOT NULL,
    CONSTRAINT pk_categoria_ingresos PRIMARY KEY (id_categoria)
        USING INDEX TABLESPACE ts_finanzas,
    CONSTRAINT uq_categoria_ingresos UNIQUE (categoria)
        USING INDEX TABLESPACE ts_finanzas
) TABLESPACE ts_finanzas;

-- ---------- CLIENTE Y USUARIO ----------

CREATE TABLE cliente (
    cod_cliente  NUMBER GENERATED ALWAYS AS IDENTITY,
    ap_paterno   VARCHAR2(50) NOT NULL,
    ap_materno   VARCHAR2(50),
    nombres      VARCHAR2(100) NOT NULL,
    CONSTRAINT pk_cliente PRIMARY KEY (cod_cliente)
        USING INDEX TABLESPACE ts_finanzas
) TABLESPACE ts_finanzas;

CREATE TABLE usuario (
    id_usuario   NUMBER GENERATED ALWAYS AS IDENTITY,
    usuario      VARCHAR2(50) NOT NULL,
    clave        VARCHAR2(100) NOT NULL,   
    cod_cliente  NUMBER NOT NULL,
    CONSTRAINT pk_usuario PRIMARY KEY (id_usuario)
        USING INDEX TABLESPACE ts_finanzas,
    CONSTRAINT uq_usuario_usuario UNIQUE (usuario)
        USING INDEX TABLESPACE ts_finanzas,
    CONSTRAINT uq_usuario_cliente UNIQUE (cod_cliente)
        USING INDEX TABLESPACE ts_finanzas,
    CONSTRAINT fk_usuario_cliente FOREIGN KEY (cod_cliente)
        REFERENCES cliente (cod_cliente)
) TABLESPACE ts_finanzas;

-- ---------- CUENTAS ----------

CREATE TABLE cuentas (
    id_cuenta       NUMBER GENERATED ALWAYS AS IDENTITY,
    nombre_cuenta   VARCHAR2(80) NOT NULL,
    cod_cliente     NUMBER NOT NULL,
    id_categoria    NUMBER NOT NULL,
    saldo           NUMBER(12,2) DEFAULT 0 NOT NULL,
    CONSTRAINT pk_cuentas PRIMARY KEY (id_cuenta)
        USING INDEX TABLESPACE ts_finanzas,
    CONSTRAINT fk_cuentas_cliente FOREIGN KEY (cod_cliente)
        REFERENCES cliente (cod_cliente),
    CONSTRAINT fk_cuentas_categoria FOREIGN KEY (id_categoria)
        REFERENCES categoria_cuentas (id_categoria),
    CONSTRAINT ck_cuentas_saldo CHECK (saldo >= 0)
) TABLESPACE ts_finanzas;

CREATE INDEX ix_cuentas_cliente   ON cuentas (cod_cliente)  TABLESPACE ts_finanzas;
CREATE INDEX ix_cuentas_categoria ON cuentas (id_categoria) TABLESPACE ts_finanzas;

-- ---------- MOVIMIENTOS ----------

CREATE TABLE movimientos (
    id_movimiento  NUMBER GENERATED ALWAYS AS IDENTITY,
    tipo           CHAR(1) NOT NULL,
    descripcion    VARCHAR2(200),
    monto          NUMBER(12,2) NOT NULL,
    fecha          DATE DEFAULT SYSDATE NOT NULL,
    id_cuenta      NUMBER NOT NULL,
    CONSTRAINT pk_movimientos PRIMARY KEY (id_movimiento)
        USING INDEX TABLESPACE ts_finanzas,
    CONSTRAINT fk_movimientos_cuenta FOREIGN KEY (id_cuenta)
        REFERENCES cuentas (id_cuenta),
    CONSTRAINT ck_movimientos_tipo  CHECK (tipo IN ('I', 'G')),  
    CONSTRAINT ck_movimientos_monto CHECK (monto > 0)
) TABLESPACE ts_finanzas;

CREATE INDEX ix_movimientos_cuenta ON movimientos (id_cuenta)       TABLESPACE ts_finanzas;
CREATE INDEX ix_movimientos_fecha  ON movimientos (fecha)           TABLESPACE ts_finanzas;

-- ---------- INGRESOS Y GASTOS (detalle del movimiento) ----------

CREATE TABLE ingresos (
    id_movimiento  NUMBER NOT NULL,
    id_categoria   NUMBER NOT NULL,
    CONSTRAINT pk_ingresos PRIMARY KEY (id_movimiento)
        USING INDEX TABLESPACE ts_finanzas,
    CONSTRAINT fk_ingresos_movimiento FOREIGN KEY (id_movimiento)
        REFERENCES movimientos (id_movimiento) ON DELETE CASCADE,
    CONSTRAINT fk_ingresos_categoria FOREIGN KEY (id_categoria)
        REFERENCES categoria_ingresos (id_categoria)
) TABLESPACE ts_finanzas;

CREATE INDEX ix_ingresos_categoria ON ingresos (id_categoria) TABLESPACE ts_finanzas;

CREATE TABLE gastos (
    id_movimiento  NUMBER NOT NULL,
    id_categoria   NUMBER NOT NULL,
    CONSTRAINT pk_gastos PRIMARY KEY (id_movimiento)
        USING INDEX TABLESPACE ts_finanzas,
    CONSTRAINT fk_gastos_movimiento FOREIGN KEY (id_movimiento)
        REFERENCES movimientos (id_movimiento) ON DELETE CASCADE,
    CONSTRAINT fk_gastos_categoria FOREIGN KEY (id_categoria)
        REFERENCES categoria_gastos (id_categoria)
) TABLESPACE ts_finanzas;

CREATE INDEX ix_gastos_categoria ON gastos (id_categoria) TABLESPACE ts_finanzas;

-- ---------- DATOS INICIALES ----------

INSERT INTO categoria_cuentas (categoria) VALUES ('Ahorro');
INSERT INTO categoria_cuentas (categoria) VALUES ('Débito');
INSERT INTO categoria_cuentas (categoria) VALUES ('Efectivo');
INSERT INTO categoria_cuentas (categoria) VALUES ('Fondo de Emergencia');

INSERT INTO categoria_gastos (categoria) VALUES ('Fijo');
INSERT INTO categoria_gastos (categoria) VALUES ('Variable');
INSERT INTO categoria_gastos (categoria) VALUES ('Ocio');
INSERT INTO categoria_gastos (categoria) VALUES ('Comidas');
INSERT INTO categoria_gastos (categoria) VALUES ('Compras no planificadas');
INSERT INTO categoria_gastos (categoria) VALUES ('Suscripciones');

INSERT INTO categoria_ingresos (categoria) VALUES ('Sueldo');
INSERT INTO categoria_ingresos (categoria) VALUES ('Propina / Extra');
INSERT INTO categoria_ingresos (categoria) VALUES ('Otros');

COMMIT;

-- ---------- MENU DINAMICO ------------
CREATE TABLE opciones (
    id_opcion      NUMBER GENERATED ALWAYS AS IDENTITY,
    nombre_opcion  VARCHAR2(60) NOT NULL,
    url_opcion     VARCHAR2(100),            
    icono          VARCHAR2(50),
    id_opcion_ref  NUMBER,                   
    orden          NUMBER(3) DEFAULT 0 NOT NULL,
    activo         VARCHAR2(1) DEFAULT 'S' NOT NULL,
    CONSTRAINT pk_opciones PRIMARY KEY (id_opcion) USING INDEX TABLESPACE ts_finanzas,
    CONSTRAINT fk_opciones_ref FOREIGN KEY (id_opcion_ref) REFERENCES opciones (id_opcion),
    CONSTRAINT ck_opciones_activo CHECK (activo IN ('S', 'N'))
) TABLESPACE ts_finanzas;

CREATE INDEX ix_opciones_ref ON opciones (id_opcion_ref) TABLESPACE ts_finanzas;

INSERT INTO opciones (nombre_opcion, url_opcion, icono, orden) VALUES ('Cuentas', '/cuentas', 'fas fa-piggy-bank', 10);
INSERT INTO opciones (nombre_opcion, url_opcion, icono, orden) VALUES ('Movimientos', '/movimientos', 'fas fa-exchange-alt', 20);
INSERT INTO opciones (nombre_opcion, url_opcion, icono, orden) VALUES ('Metas Financieras', '/metas', 'fas fa-bullseye', 30);

COMMIT;