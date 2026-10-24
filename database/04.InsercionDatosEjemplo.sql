/*
Antes de ejecutar, crear el usuario de ejemplo "Aldana123" mediante la aplicacion,
de lo contrario no se encriptará la clave adecuadamente.
*/

//INSERCION DE CUENTAS Y MOVIMIENTOS
DECLARE
    v_cli     NUMBER;
    v_debito  NUMBER;
    v_ahorro  NUMBER;
    v_efec    NUMBER;

    PROCEDURE cuenta(p_nombre VARCHAR2, p_cat VARCHAR2, p_id OUT NUMBER) IS
        v_id_cat NUMBER;
    BEGIN
        SELECT id_categoria INTO v_id_cat 
        FROM categoria_cuentas 
        WHERE categoria = p_cat;

        INSERT INTO cuentas (nombre_cuenta, cod_cliente, id_categoria, saldo)
        VALUES (p_nombre, v_cli, v_id_cat, 0)
        RETURNING id_cuenta INTO p_id;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20001, 'La categoría de cuenta "' || p_cat || '" no existe.');
    END;

    PROCEDURE mov(p_tipo CHAR, p_cuenta NUMBER, p_desc VARCHAR2,
                  p_monto NUMBER, p_dias NUMBER, p_cat VARCHAR2) IS
        v_id NUMBER;
    BEGIN
        INSERT INTO movimientos (tipo, descripcion, monto, fecha, id_cuenta)
        VALUES (p_tipo, p_desc, p_monto, TRUNC(SYSDATE) - p_dias, p_cuenta)
        RETURNING id_movimiento INTO v_id;

        IF p_tipo = 'I' THEN
            INSERT INTO ingresos (id_movimiento, id_categoria)
            SELECT v_id, id_categoria FROM categoria_ingresos WHERE categoria = p_cat;
        ELSE
            INSERT INTO gastos (id_movimiento, id_categoria)
            SELECT v_id, id_categoria FROM categoria_gastos WHERE categoria = p_cat;
        END IF;
    END;
BEGIN
    BEGIN
        SELECT cod_cliente INTO v_cli FROM usuario WHERE usuario = 'Aldana123';
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20002, 'El usuario "Aldana123" no existe en la base de datos.');
    END;

    cuenta('BCP Débito',        'Débito',              v_debito);
    cuenta('Ahorro viaje',      'Ahorro',              v_ahorro);
    cuenta('Billetera',         'Efectivo',            v_efec);

    mov('I', v_debito, 'Sueldo septiembre',   2800, 55, 'Sueldo');
    mov('I', v_debito, 'Sueldo octubre',      2800, 25, 'Sueldo');
    mov('I', v_debito, 'Freelance diseño',     450, 12, 'Propina / Extra');
    mov('I', v_ahorro, 'Depósito mensual',     600, 24, 'Otros');
    mov('I', v_ahorro, 'Depósito mensual',     600, 54, 'Otros');
    mov('I', v_efec,   'Retiro para la semana',200, 10, 'Otros');

    mov('G', v_debito, 'Alquiler cuarto',      900, 50, 'Fijo');
    mov('G', v_debito, 'Alquiler cuarto',      900, 20, 'Fijo');
    mov('G', v_debito, 'Internet',              80, 18, 'Fijo');
    mov('G', v_debito, 'Spotify',               17, 15, 'Suscripciones');
    mov('G', v_debito, 'Netflix',               35, 14, 'Suscripciones');

    mov('G', v_debito, 'Supermercado',         210, 40, 'Comidas');
    mov('G', v_debito, 'Supermercado',         185,  9, 'Comidas');
    mov('G', v_efec,   'Almuerzo',              14,  8, 'Comidas');
    mov('G', v_efec,   'Pasaje',                 6,  7, 'Variable');
    mov('G', v_efec,   'Cine con amigas',       38,  6, 'Ocio');
    mov('G', v_debito, 'Salida a comer',        95,  5, 'Ocio');
    mov('G', v_debito, 'Zapatillas en oferta', 160,  3, 'Compras no planificadas');
    mov('G', v_efec,   'Café',                  12,  2, 'Comidas');
    mov('G', v_efec,   'Taxi',                  22,  1, 'Variable');

    -- Recalculo de saldos
    UPDATE cuentas c
    SET saldo = (SELECT NVL(SUM(CASE m.tipo WHEN 'I' THEN m.monto ELSE -m.monto END), 0)
                 FROM movimientos m WHERE m.id_cuenta = c.id_cuenta)
    WHERE c.cod_cliente = v_cli;

    COMMIT;
END;
/

//INSERCION DE METAS FINANCIERAS Y APORTES
DECLARE
    v_cli      NUMBER;
    v_debito   NUMBER;
    v_ahorro   NUMBER;

    c_activa   CONSTANT NUMBER := 1;
    c_corto    CONSTANT NUMBER := 1;
    c_mediano  CONSTANT NUMBER := 2;
    c_largo    CONSTANT NUMBER := 3;

    v_laptop NUMBER; v_viaje NUMBER; v_fondo NUMBER; v_curso NUMBER; v_celu NUMBER;

    PROCEDURE meta(p_nombre VARCHAR2, p_obj NUMBER, p_dias NUMBER,
                   p_tipo NUMBER, p_id OUT NUMBER) IS
    BEGIN
        INSERT INTO metas (nombre_meta, monto_objetivo, saldo, fecha_limite,
                           id_estado, id_tipo, cod_cliente)
        VALUES (p_nombre, p_obj, 0, TRUNC(SYSDATE) + p_dias, c_activa, p_tipo, v_cli)
        RETURNING id_meta INTO p_id;
    END;

    PROCEDURE aporte(p_meta NUMBER, p_cuenta NUMBER, p_monto NUMBER, p_dias_atras NUMBER) IS
    BEGIN
        INSERT INTO aportes_meta (id_meta, id_cuenta, monto, fecha, tipo)  
        VALUES (p_meta, p_cuenta, p_monto, TRUNC(SYSDATE) - p_dias_atras, 'A');

        UPDATE cuentas SET saldo = saldo - p_monto WHERE id_cuenta = p_cuenta;
    END;
    
BEGIN
    SELECT cod_cliente INTO v_cli FROM usuario WHERE usuario = 'demo';
    SELECT id_cuenta INTO v_debito FROM cuentas
        WHERE cod_cliente = v_cli AND nombre_cuenta = 'BCP Débito';
    SELECT id_cuenta INTO v_ahorro FROM cuentas
        WHERE cod_cliente = v_cli AND nombre_cuenta = 'Ahorro viaje';

    meta('Laptop nueva',        3000, 120, c_mediano, v_laptop);
    meta('Viaje a Cusco',       1500,  90, c_corto,   v_viaje);
    meta('Fondo de emergencia', 5000, 365, c_largo,   v_fondo);
    meta('Curso de inglés',      600,  30, c_corto,   v_curso);
    meta('Celular nuevo',       1800, 200, c_mediano, v_celu);  

    aporte(v_laptop, v_debito, 400, 45);
    aporte(v_laptop, v_debito, 300, 30);
    aporte(v_laptop, v_debito, 300, 15);
    aporte(v_laptop, v_debito, 200,  5);
    aporte(v_viaje, v_ahorro, 600, 40);
    aporte(v_viaje, v_ahorro, 400, 10);
    aporte(v_fondo, v_debito, 600, 50);
    aporte(v_fondo, v_debito, 500, 20);
    aporte(v_curso, v_debito, 300, 20);
    aporte(v_curso, v_debito, 300,  3);

    COMMIT;
END;


DECLARE
    v_cli      NUMBER;
    v_debito   NUMBER;
    v_ahorro   NUMBER;

    c_en_progreso CONSTANT NUMBER := 1;

    v_laptop NUMBER; v_viaje NUMBER; v_fondo NUMBER; v_curso NUMBER; v_celu NUMBER;

        PROCEDURE meta(p_nombre VARCHAR2, p_obj NUMBER, p_dias NUMBER,
                   p_tipo VARCHAR2, p_id OUT NUMBER) IS
        v_tipo NUMBER;
    BEGIN
        SELECT id_tipo INTO v_tipo FROM tipo_metas WHERE tipo = p_tipo;

        INSERT INTO metas (nombre_meta, monto_objetivo, saldo, fecha_limite,
                           id_estado, id_tipo, cod_cliente)
        VALUES (p_nombre, p_obj, 0, TRUNC(SYSDATE) + p_dias,
                c_en_progreso, v_tipo, v_cli)
        RETURNING id_meta INTO p_id;
    END;

    PROCEDURE aporte(p_meta NUMBER, p_cuenta NUMBER, p_monto NUMBER, p_dias_atras NUMBER) IS
    BEGIN
        INSERT INTO aportes_meta (id_meta, id_cuenta, monto, fecha, tipo)
        VALUES (p_meta, p_cuenta, p_monto, TRUNC(SYSDATE) - p_dias_atras, 'A');

        UPDATE cuentas SET saldo = saldo - p_monto WHERE id_cuenta = p_cuenta;
    END;
BEGIN
    SELECT cod_cliente INTO v_cli FROM usuario WHERE usuario = 'Aldana123';
    SELECT id_cuenta INTO v_debito FROM cuentas
        WHERE cod_cliente = v_cli AND nombre_cuenta = 'BCP Débito';
    SELECT id_cuenta INTO v_ahorro FROM cuentas
        WHERE cod_cliente = v_cli AND nombre_cuenta = 'Ahorro viaje';

    meta('Laptop nueva',        3000, 120, 'Adquisicion de bienes', v_laptop);
    meta('Viaje a Cusco',       1500,  90, 'Ahorro',                v_viaje);
    meta('Fondo de emergencia', 5000, 365, 'Ahorro',                v_fondo);
    meta('Curso de inglés',      600,  30, 'Inversion',             v_curso);
    meta('Celular nuevo',       1800, 200, 'Adquisicion de bienes', v_celu);  

    aporte(v_laptop, v_debito, 400, 45);
    aporte(v_laptop, v_debito, 300, 30);
    aporte(v_laptop, v_debito, 300, 15);
    aporte(v_laptop, v_debito, 200,  5);

    aporte(v_viaje, v_ahorro, 600, 40);
    aporte(v_viaje, v_ahorro, 400, 10);

    aporte(v_fondo, v_debito, 600, 50);
    aporte(v_fondo, v_debito, 500, 20);

    aporte(v_curso, v_debito, 300, 20);
    aporte(v_curso, v_debito, 300,  3);

    COMMIT;
END;
/