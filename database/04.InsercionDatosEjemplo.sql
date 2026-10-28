/*
Antes de ejecutar, crear el usuario de ejemplo "demo" mediante la aplicacion,
de lo contrario no se encriptará la clave adecuadamente.
*/

//INSERCION DE CUENTAS Y MOVIMIENTOS (v2)
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
            RAISE_APPLICATION_ERROR(-20001,
                'La categoría de cuenta "' || p_cat || '" no existe.');
    END;

    PROCEDURE mov(p_tipo CHAR, p_cuenta NUMBER, p_desc VARCHAR2,
                  p_monto NUMBER, p_dias NUMBER, p_cat VARCHAR2) IS
        v_id   NUMBER;
        v_cat  NUMBER;
    BEGIN
        INSERT INTO movimientos (tipo, descripcion, monto, fecha, id_cuenta)
        VALUES (p_tipo, p_desc, p_monto, TRUNC(SYSDATE) - p_dias, p_cuenta)
        RETURNING id_movimiento INTO v_id;

        IF p_tipo = 'I' THEN
            SELECT id_categoria INTO v_cat
              FROM categoria_ingresos
             WHERE categoria = p_cat;

            INSERT INTO ingresos (id_movimiento, id_categoria)
            VALUES (v_id, v_cat);
        ELSE
            SELECT id_categoria INTO v_cat
              FROM categoria_gastos
             WHERE categoria = p_cat;

            INSERT INTO gastos (id_movimiento, id_categoria)
            VALUES (v_id, v_cat);
        END IF;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20003,
                'Categoría de ' || CASE p_tipo WHEN 'I' THEN 'ingreso' ELSE 'gasto' END
                || ' "' || p_cat || '" no existe.');
    END;
BEGIN
    BEGIN
        SELECT cod_cliente INTO v_cli FROM usuario WHERE usuario = 'demo';
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20002, 'El usuario "demo" no existe.');
    END;

    -- Cuentas
    cuenta('BCP Débito',   'Débito',   v_debito);
    cuenta('Cuenta de Ahorro Ripley', 'Ahorro',   v_ahorro);
    cuenta('Billetera',    'Efectivo', v_efec);

    -- INGRESOS
    mov('I', v_debito, 'Sueldo septiembre',              2800, 55, 'Sueldo');
    mov('I', v_debito, 'Sueldo octubre',                 2800, 25, 'Sueldo');
    mov('I', v_debito, 'Bono desempeño',                  300, 30, 'Bonos y Comisiones');
    mov('I', v_debito, 'Freelance diseño',                450, 12, 'Ingresos Freelance');
    mov('I', v_debito, 'Devolución impuesto',             120, 45, 'Devoluciones y Reembolsos');
    mov('I', v_ahorro, 'Intereses cuenta ahorro',          18, 20, 'Dividendos e Intereses');
    mov('I', v_ahorro, 'Depósito mensual',                600, 54, 'Otros Ingresos');
    mov('I', v_ahorro, 'Depósito mensual',                600, 24, 'Otros Ingresos');
    mov('I', v_efec,   'Regalo cumpleaños',               100, 35, 'Regalos Recibidos');
    mov('I', v_efec,   'Venta de libros usados',           45, 10, 'Venta de Bienes');

    -- GASTOS FIJOS
    mov('G', v_debito, 'Alquiler cuarto',                 900, 50, 'Gastos de Vivienda');
    mov('G', v_debito, 'Alquiler cuarto',                 900, 20, 'Gastos de Vivienda');
    mov('G', v_debito, 'Internet fibra',                   80, 18, 'Servicios Básicos');
    mov('G', v_debito, 'Luz y agua',                       95, 17, 'Servicios Básicos');
    mov('G', v_debito, 'Seguro salud',                    110, 28, 'Salud');
    mov('G', v_debito, 'Cuota tarjeta crédito',           200, 15, 'Deudas y Préstamos');
    mov('G', v_debito, 'Aporte fondo emergencia',         300, 22, 'Ahorro e Inversión');
    mov('G', v_debito, 'Seguro auto',                      70, 40, 'Seguros');

    -- GASTOS VARIABLES / OCIO / HOGAR
    mov('G', v_debito, 'Supermercado',                    210, 40, 'Alimentación');
    mov('G', v_debito, 'Supermercado',                    185,  9, 'Alimentación');
    mov('G', v_efec,   'Almuerzo',                         14,  8, 'Alimentación');

    mov('G', v_debito, 'Spotify',                          17, 15, 'Suscripciones Digitales');
    mov('G', v_debito, 'Netflix',                          35, 14, 'Suscripciones Digitales');

    mov('G', v_efec,   'Pasaje combi',                      6,  7, 'Transporte');
    mov('G', v_efec,   'Taxi nocturno',                    22,  1, 'Transporte');

    mov('G', v_efec,   'Cine con amigas',                  38,  6, 'Ocio y Entretenimiento');
    mov('G', v_debito, 'Salida a comer',                   95,  5, 'Restaurantes y Cafeterías');
    mov('G', v_efec,   'Café con leche',                   12,  2, 'Restaurantes y Cafeterías');

    mov('G', v_debito, 'Zapatillas en oferta',            160,  3, 'Ropa y Calzado');
    mov('G', v_debito, 'Corte de cabello',                 30, 11, 'Cuidado Personal');
    mov('G', v_debito, 'Alimento perro',                   45, 13, 'Mascotas');
    mov('G', v_debito, 'Foco y cortina',                   40,  4, 'Hogar y Mantenimiento');
    mov('G', v_efec,   'Regalo para mamá',                 80, 16, 'Regalos y Donaciones');
    mov('G', v_debito, 'Pasaje bus a provincia',          120, 33, 'Viajes');
    mov('G', v_efec,   'Propina mesero',                   10,  2, 'Otros Gastos');

    -- Recalculo de saldos
    UPDATE cuentas c
       SET saldo = (SELECT NVL(SUM(CASE m.tipo WHEN 'I' THEN m.monto ELSE -m.monto END), 0)
                      FROM movimientos m
                     WHERE m.id_cuenta = c.id_cuenta)
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
        WHERE cod_cliente = v_cli AND nombre_cuenta = 'Cuenta de Ahorro Ripley';

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
/

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
    SELECT cod_cliente INTO v_cli FROM usuario WHERE usuario = 'demo';
    SELECT id_cuenta INTO v_debito FROM cuentas
        WHERE cod_cliente = v_cli AND nombre_cuenta = 'BCP Débito';
    SELECT id_cuenta INTO v_ahorro FROM cuentas
        WHERE cod_cliente = v_cli AND nombre_cuenta = 'Cuenta de Ahorro Ripley';

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

//INSERCION DE TRANSFERENCIAS
DECLARE
    v_cli     NUMBER;
    v_debito  NUMBER;
    v_ahorro  NUMBER;
    v_efec    NUMBER;

    PROCEDURE transferir(p_og NUMBER, p_des NUMBER, p_monto NUMBER, p_dias NUMBER) IS
    BEGIN
        INSERT INTO transferencias (id_cuenta_og, id_cuenta_des, monto, fecha)
        VALUES (p_og, p_des, p_monto, TRUNC(SYSDATE) - p_dias);

        UPDATE cuentas SET saldo = saldo - p_monto WHERE id_cuenta = p_og;
        UPDATE cuentas SET saldo = saldo + p_monto WHERE id_cuenta = p_des;
    END;
BEGIN
    SELECT cod_cliente INTO v_cli FROM usuario WHERE usuario = 'demo';

    SELECT id_cuenta INTO v_debito FROM cuentas
        WHERE cod_cliente = v_cli AND nombre_cuenta = 'BCP Débito';
    SELECT id_cuenta INTO v_ahorro FROM cuentas
        WHERE cod_cliente = v_cli AND nombre_cuenta = 'Cuenta de Ahorro Ripley';
    SELECT id_cuenta INTO v_efec FROM cuentas
        WHERE cod_cliente = v_cli AND nombre_cuenta = 'Billetera';

    transferir(v_debito, v_efec,   100, 12);   
    transferir(v_debito, v_ahorro, 150,  8);   
    transferir(v_ahorro, v_efec,    50,  4);   
    transferir(v_efec,   v_debito,  30,  1);   

    COMMIT;
END;
/