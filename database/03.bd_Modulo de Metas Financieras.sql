-- =====================================================
-- GESTOR FINANCIERO - MVP
-- Tablespace: ts_finanzas
-- =====================================================
-- ---------- CATEGORÍAS----------
CREATE TABLE tipo_metas(
    id_tipo     NUMBER GENERATED ALWAYS AS IDENTITY,
    tipo        VARCHAR2(50) NOT NULL,
    CONSTRAINT pk_tipo_metas PRIMARY KEY (id_tipo) 
        USING INDEX TABLESPACE ts_finanzas,
    CONSTRAINT uq_tipo_metas UNIQUE (tipo)
        USING INDEX TABLESPACE ts_finanzas
)TABLESPACE ts_finanzas;

CREATE TABLE estado_metas(
    id_estado   NUMBER GENERATED ALWAYS AS IDENTITY,
    estado      VARCHAR2(50) NOT NULL,
    CONSTRAINT pk_estado_metas PRIMARY KEY (id_estado) 
        USING INDEX TABLESPACE ts_finanzas,
    CONSTRAINT uq_estado_metas UNIQUE (estado)
        USING INDEX TABLESPACE ts_finanzas
)TABLESPACE ts_finanzas;

-- ------------ DATOS INICIALES ---------------

INSERT INTO tipo_metas (tipo) VALUES ('Pago de Deudas');
INSERT INTO tipo_metas (tipo) VALUES ('Ahorro');
INSERT INTO tipo_metas (tipo) VALUES ('Inversion');
INSERT INTO tipo_metas (tipo) VALUES ('Adquisicion de bienes');
INSERT INTO estado_metas (estado) VALUES ('EN_PROGRESO');
INSERT INTO estado_metas (estado) VALUES ('COMPLETADA');
INSERT INTO estado_metas (estado) VALUES ('VENCIDA');
INSERT INTO estado_metas (estado) VALUES ('CANCELADA');

COMMIT;
-- ----------- METAS FINANCIERAS ------------

CREATE TABLE metas(
    id_meta         NUMBER GENERATED ALWAYS AS IDENTITY,
    nombre_meta     VARCHAR2(80) NOT NULL,
    monto_objetivo  NUMBER(12,2) NOT NULL,
    saldo           NUMBER(12,2) DEFAULT 0 NOT NULL,
    fecha_limite    DATE NOT NULL,
    cod_cliente     NUMBER NOT NULL,
    id_tipo         NUMBER NOT NULL,
    id_estado       NUMBER NOT NULL,
    CONSTRAINT pk_metas PRIMARY KEY (id_meta) USING INDEX TABLESPACE ts_finanzas,
    CONSTRAINT fk_metas_cliente FOREIGN KEY (cod_cliente) 
        REFERENCES cliente (cod_cliente),
    CONSTRAINT fk_metas_tipo FOREIGN KEY (id_tipo) 
        REFERENCES tipo_metas (id_tipo),
    CONSTRAINT fk_metas_estado FOREIGN KEY (id_estado)
        REFERENCES estado_metas (id_estado),
    CONSTRAINT ck_metas_monto_objetivo CHECK (monto_objetivo>0),
    CONSTRAINT ck_metas_saldo_rango CHECK (saldo >= 0 AND saldo <= monto_objetivo)
)TABLESPACE ts_finanzas;

CREATE INDEX ix_metas_tipo ON metas (id_tipo) TABLESPACE ts_finanzas;
CREATE INDEX ix_metas_estado ON metas (id_estado) TABLESPACE ts_finanzas;
CREATE INDEX ix_metas_cliente ON metas (cod_cliente) TABLESPACE ts_finanzas;

--Trigger de validacion de fecha limite
CREATE OR REPLACE TRIGGER trg_valida_fecha_meta
BEFORE INSERT OR UPDATE ON metas
FOR EACH ROW
BEGIN
    IF INSERTING
       OR :OLD.fecha_limite IS NULL
       OR :NEW.fecha_limite <> :OLD.fecha_limite THEN
        IF :NEW.fecha_limite <= TRUNC(SYSDATE) THEN
            RAISE_APPLICATION_ERROR(-20001,
                'La fecha límite de la meta debe ser mayor al día de hoy.');
        END IF;
    END IF;
END;
/
-- --------------- APORTES -------------------

CREATE TABLE aportes_meta(
    id_aporte       NUMBER GENERATED ALWAYS AS IDENTITY,
    monto           NUMBER(12,2) NOT NULL,
    fecha           DATE DEFAULT SYSDATE NOT NULL,
    id_meta         NUMBER NOT NULL,
    id_cuenta     NUMBER NOT NULL,
    CONSTRAINT pk_aportes_meta PRIMARY KEY (id_aporte) USING INDEX TABLESPACE ts_finanzas,
    CONSTRAINT fk_aportes_meta_meta FOREIGN KEY (id_meta)
        REFERENCES metas (id_meta),
    CONSTRAINT fk_aportes_meta_cuenta FOREIGN KEY (id_cuenta)
        REFERENCES cuentas (id_cuenta),
    CONSTRAINT ck_aportes_meta_monto CHECK (monto>0)
)TABLESPACE ts_finanzas;

CREATE INDEX ix_aportes_meta_meta ON aportes_meta (id_meta) TABLESPACE ts_finanzas;
CREATE INDEX ix_aportes_meta_cuenta ON aportes_meta (id_cuentaOg) TABLESPACE ts_finanzas;

ALTER TABLE aportes_meta ADD tipo CHAR(1) DEFAULT 'A' NOT NULL;
ALTER TABLE aportes_meta ADD CONSTRAINT ck_aportes_tipo CHECK (tipo IN ('A', 'R'));  -- A = aporte, R = retiro/devolución

--Validaciones de aportes
CREATE OR REPLACE TRIGGER trg_valida_fecha_aportes_meta
BEFORE INSERT OR UPDATE ON aportes_meta
FOR EACH ROW
DECLARE
    vf_limite DATE;
BEGIN
    IF :NEW.tipo = 'A' THEN
        SELECT fecha_limite INTO vf_limite
        FROM metas WHERE id_meta = :NEW.id_meta;

        IF :NEW.fecha > vf_limite THEN
            RAISE_APPLICATION_ERROR(-20002,
                'No se pueden registrar aportes después de la fecha límite.');
        END IF;
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN NULL;   
END;

--Actualizacion de saldo
CREATE OR REPLACE TRIGGER trg_actualiza_saldo_meta
AFTER INSERT OR UPDATE OR DELETE ON aportes_meta
FOR EACH ROW
DECLARE
    vid_meta  NUMBER;
    vdelta    NUMBER := 0;
    vobjetivo metas.monto_objetivo%TYPE;
    vsaldo    metas.saldo%TYPE;
BEGIN
    IF INSERTING THEN
        vid_meta := :NEW.id_meta;
        vdelta   := CASE :NEW.tipo WHEN 'A' THEN :NEW.monto ELSE -:NEW.monto END;
    ELSIF UPDATING THEN
        vid_meta := :NEW.id_meta;
        vdelta   := CASE :NEW.tipo WHEN 'A' THEN :NEW.monto ELSE -:NEW.monto END
                   - CASE :OLD.tipo WHEN 'A' THEN :OLD.monto ELSE -:OLD.monto END;
    ELSE
        vid_meta := :OLD.id_meta;
        vdelta   := -(CASE :OLD.tipo WHEN 'A' THEN :OLD.monto ELSE -:OLD.monto END);
    END IF;

    SELECT monto_objetivo, saldo INTO vobjetivo, vsaldo
    FROM metas WHERE id_meta = vid_meta FOR UPDATE;

    IF vsaldo + vdelta > vobjetivo THEN
        RAISE_APPLICATION_ERROR(-20003, 'El aporte excede el monto objetivo restante.');
    END IF;

    UPDATE metas
    SET saldo = saldo + vdelta,
        id_estado = CASE
                      WHEN id_estado IN (1, 2) THEN
                          CASE WHEN saldo + vdelta >= monto_objetivo THEN 2 ELSE 1 END
                      ELSE id_estado         
                    END
    WHERE id_meta = vid_meta;
END;
