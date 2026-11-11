/* 
REPORTES BÁSICOS I1 (4)
1. REP_ESTADISTICA_MOVIMIENTOS: Un resumen estadístico de los ingresos y gastos realizados en la app. 
2. REP_BALANCE_CUENTAS_SALDO: Un cliente puede ver la categoria, ranking por saldo, saldo y un resumen de las cuentas que tiene a su nombre
3. REP_METAS_VENCIMIENTO_30DIAS: Un cliente puede ver las metas en progreso que están proximas a vencer (<=30 días), también visualiza su porcentaje de avance
4. REP_LISTADO_CLIENTES: El administrador puede consultar los datos de los clientes registrados, lo que vendrían a ser nombres, apellidos y la concatenación de ambos.
*/

/*===============================================
    ESPECIFICACION Y CUERPO DE PKG_REP_I1
===============================================*/

CREATE OR REPLACE PACKAGE PKG_REP_I1 AS

  PROCEDURE REP_ESTADISTICA_MOVIMIENTOS_T(
    p_cod_cliente IN  NUMBER,   -- ignorado (global)
    p_desde       IN  DATE,
    p_hasta       IN  DATE,
    p_cursor      OUT SYS_REFCURSOR);

  PROCEDURE REP_ESTADISTICA_MOVIMIENTOS_X(
    p_cod_cliente IN  NUMBER,   -- ignorado (global)
    p_desde       IN  DATE,
    p_hasta       IN  DATE,
    p_texto       OUT CLOB);

  PROCEDURE REP_BALANCE_CUENTAS_SALDO_T(
    p_cod_cliente IN  NUMBER,
    p_desde       IN  DATE,     -- ignorado
    p_hasta       IN  DATE,     -- ignorado
    p_cursor      OUT SYS_REFCURSOR);

  PROCEDURE REP_BALANCE_CUENTAS_SALDO_X(
    p_cod_cliente IN  NUMBER,
    p_desde       IN  DATE,     -- ignorado
    p_hasta       IN  DATE,     -- ignorado
    p_texto       OUT CLOB);

  PROCEDURE REP_METAS_VENCIMIENTO_30DIAS_T(
    p_cod_cliente IN  NUMBER,
    p_desde       IN  DATE,     -- ignorado
    p_hasta       IN  DATE,     -- ignorado
    p_cursor      OUT SYS_REFCURSOR);

  PROCEDURE REP_METAS_VENCIMIENTO_30DIAS_X(
    p_cod_cliente IN  NUMBER,
    p_desde       IN  DATE,     -- ignorado
    p_hasta       IN  DATE,     -- ignorado
    p_texto       OUT CLOB);

  PROCEDURE REP_LISTADO_CLIENTES_T(
    p_cod_cliente IN  NUMBER,   -- ignorado (global)
    p_desde       IN  DATE,     -- ignorado
    p_hasta       IN  DATE,     -- ignorado
    p_cursor      OUT SYS_REFCURSOR);

  PROCEDURE REP_LISTADO_CLIENTES_X(
    p_cod_cliente IN  NUMBER,   -- ignorado (global)
    p_desde       IN  DATE,     -- ignorado
    p_hasta       IN  DATE,     -- ignorado
    p_texto       OUT CLOB);

END PKG_REP_I1;
/

CREATE OR REPLACE PACKAGE BODY PKG_REP_I1 AS

  PROCEDURE REP_ESTADISTICA_MOVIMIENTOS_T(
    p_cod_cliente IN  NUMBER,
    p_desde       IN  DATE,
    p_hasta       IN  DATE,
    p_cursor      OUT SYS_REFCURSOR)
  IS
  BEGIN
    IF p_desde IS NULL OR p_hasta IS NULL OR p_desde > p_hasta THEN
      RAISE_APPLICATION_ERROR(-20001, 'Rango de fechas inválido');
    END IF;

    OPEN p_cursor FOR
      SELECT CASE tipo WHEN 'I' THEN 'Ingreso' ELSE 'Gasto' END AS tipo,
             COUNT(*)                                            AS cantidad,
             COUNT(DISTINCT id_cuenta)                           AS cuentas_involucradas,
             NVL(SUM(monto), 0)                                  AS monto_total,
             NVL(ROUND(AVG(monto), 2), 0)                        AS monto_promedio,
             NVL(ROUND(MEDIAN(monto), 2), 0)                     AS monto_mediana,
             NVL(MIN(monto), 0)                                  AS monto_minimo,
             NVL(MAX(monto), 0)                                  AS monto_maximo,
             NVL(ROUND(STDDEV(monto), 2), 0)                     AS desviacion_estandar,
             ROUND(RATIO_TO_REPORT(SUM(monto)) OVER () * 100, 2) AS pct_del_total,
             MIN(fecha)                                          AS primera_fecha,
             MAX(fecha)                                          AS ultima_fecha
        FROM movimientos
       WHERE fecha BETWEEN p_desde AND p_hasta
       GROUP BY tipo
       ORDER BY tipo;
  END REP_ESTADISTICA_MOVIMIENTOS_T;

  PROCEDURE REP_ESTADISTICA_MOVIMIENTOS_X(
    p_cod_cliente IN  NUMBER,
    p_desde       IN  DATE,
    p_hasta       IN  DATE,
    p_texto       OUT CLOB)
  IS
    CURSOR c_est IS
      SELECT CASE tipo WHEN 'I' THEN 'Ingreso' ELSE 'Gasto' END AS tipo,
             COUNT(*)                                            AS cantidad,
             COUNT(DISTINCT id_cuenta)                           AS cuentas_involucradas,
             NVL(SUM(monto), 0)                                  AS monto_total,
             NVL(ROUND(AVG(monto), 2), 0)                        AS monto_promedio,
             NVL(ROUND(MEDIAN(monto), 2), 0)                     AS monto_mediana,
             NVL(MIN(monto), 0)                                  AS monto_minimo,
             NVL(MAX(monto), 0)                                  AS monto_maximo,
             NVL(ROUND(STDDEV(monto), 2), 0)                     AS desviacion_estandar,
             ROUND(RATIO_TO_REPORT(SUM(monto)) OVER () * 100, 2) AS pct_del_total,
             MIN(fecha)                                          AS primera_fecha,
             MAX(fecha)                                          AS ultima_fecha
        FROM movimientos
       WHERE fecha BETWEEN p_desde AND p_hasta
       GROUP BY tipo
       ORDER BY tipo;

    v_hay_datos BOOLEAN := FALSE;
  BEGIN
    p_texto := pkg_rep_util.separador
            || pkg_rep_util.linea('Estadística general de movimientos', '')
            || pkg_rep_util.linea('Desde', TO_CHAR(p_desde, 'DD/MM/YYYY'))
            || pkg_rep_util.linea('Hasta', TO_CHAR(p_hasta, 'DD/MM/YYYY'))
            || pkg_rep_util.separador;

    FOR r IN c_est LOOP
      v_hay_datos := TRUE;
      p_texto := p_texto
              || pkg_rep_util.linea('Tipo', r.tipo)
              || pkg_rep_util.linea('  Cantidad', TO_CHAR(r.cantidad))
              || pkg_rep_util.linea('  Cuentas involucradas', TO_CHAR(r.cuentas_involucradas))
              || pkg_rep_util.linea('  Monto total', pkg_rep_util.monto(r.monto_total))
              || pkg_rep_util.linea('  Promedio', pkg_rep_util.monto(r.monto_promedio))
              || pkg_rep_util.linea('  Mediana', pkg_rep_util.monto(r.monto_mediana))
              || pkg_rep_util.linea('  Mínimo', pkg_rep_util.monto(r.monto_minimo))
              || pkg_rep_util.linea('  Máximo', pkg_rep_util.monto(r.monto_maximo))
              || pkg_rep_util.linea('  Desv. estándar', pkg_rep_util.monto(r.desviacion_estandar))
              || pkg_rep_util.linea('  % del total', TO_CHAR(r.pct_del_total) || ' %')
              || pkg_rep_util.linea('  Primera fecha', TO_CHAR(r.primera_fecha, 'DD/MM/YYYY'))
              || pkg_rep_util.linea('  Última fecha', TO_CHAR(r.ultima_fecha, 'DD/MM/YYYY'))
              || pkg_rep_util.separador;
    END LOOP;

    IF NOT v_hay_datos THEN
      p_texto := p_texto
              || pkg_rep_util.linea('Sin movimientos en el rango indicado.', '')
              || pkg_rep_util.separador;
    END IF;

    p_texto := p_texto || pkg_rep_util.pie_cliente(p_cod_cliente);  
END REP_ESTADISTICA_MOVIMIENTOS_X;

  PROCEDURE REP_BALANCE_CUENTAS_SALDO_T(
    p_cod_cliente IN  NUMBER,
    p_desde       IN  DATE,
    p_hasta       IN  DATE,
    p_cursor      OUT SYS_REFCURSOR)
  IS
  BEGIN
    OPEN p_cursor FOR
      SELECT id_cuenta                                             AS id_cuenta,
             nombre_cuenta                                         AS nombre_cuenta,
             id_categoria                                          AS id_categoria,
             NVL(saldo, 0)                                         AS saldo,
             COUNT(*) OVER ()                                      AS total_cuentas,
             SUM(NVL(saldo, 0)) OVER ()                            AS saldo_total,
             ROUND(AVG(NVL(saldo, 0)) OVER (), 2)                  AS saldo_promedio,
             NVL(MIN(saldo) OVER (), 0)                            AS saldo_minimo,
             NVL(MAX(saldo) OVER (), 0)                            AS saldo_maximo,
             NVL(ROUND(STDDEV(saldo) OVER (), 2), 0)               AS desviacion_saldos,
             ROUND(RATIO_TO_REPORT(NVL(saldo,0)) OVER () * 100, 2) AS pct_patrimonio,
             RANK() OVER (ORDER BY NVL(saldo, 0) DESC)             AS ranking_saldo
        FROM cuentas
       WHERE cod_cliente = p_cod_cliente
       ORDER BY saldo DESC;
  END REP_BALANCE_CUENTAS_SALDO_T;

  PROCEDURE REP_BALANCE_CUENTAS_SALDO_X(
    p_cod_cliente IN  NUMBER,
    p_desde       IN  DATE,
    p_hasta       IN  DATE,
    p_texto       OUT CLOB)
  IS
    CURSOR c_cuentas IS
      SELECT nombre_cuenta,
             id_categoria,
             NVL(saldo, 0)                            AS saldo,
             RANK() OVER (ORDER BY NVL(saldo,0) DESC) AS ranking_saldo
        FROM cuentas
       WHERE cod_cliente = p_cod_cliente
       ORDER BY saldo DESC;

    v_saldo_total  NUMBER := 0;
    v_cant_cuentas NUMBER := 0;
    v_saldo_prom   NUMBER := 0;
  BEGIN
    p_texto := pkg_rep_util.separador
            || pkg_rep_util.linea('Balance de cuentas por saldo', '')
            || pkg_rep_util.linea('Cliente', TO_CHAR(p_cod_cliente))
            || pkg_rep_util.separador;

    FOR r IN c_cuentas LOOP
      v_cant_cuentas := v_cant_cuentas + 1;
      v_saldo_total  := v_saldo_total + r.saldo;
      p_texto := p_texto
              || pkg_rep_util.linea('Cuenta', r.nombre_cuenta)
              || pkg_rep_util.linea('  Categoría (ID)', TO_CHAR(r.id_categoria))
              || pkg_rep_util.linea('  Ranking', '#' || TO_CHAR(r.ranking_saldo))
              || pkg_rep_util.linea('  Saldo', pkg_rep_util.monto(r.saldo));
    END LOOP;

    IF v_cant_cuentas = 0 THEN
      p_texto := p_texto
              || pkg_rep_util.linea('El cliente no tiene cuentas registradas.', '')
              || pkg_rep_util.separador;
    ELSE
      v_saldo_prom := ROUND(v_saldo_total / v_cant_cuentas, 2);
      p_texto := p_texto
              || pkg_rep_util.separador
              || pkg_rep_util.linea('Total de cuentas', TO_CHAR(v_cant_cuentas))
              || pkg_rep_util.linea('Saldo total', pkg_rep_util.monto(v_saldo_total))
              || pkg_rep_util.linea('Saldo promedio', pkg_rep_util.monto(v_saldo_prom))
              || pkg_rep_util.separador;
    END IF;

    p_texto := p_texto || pkg_rep_util.pie_cliente(p_cod_cliente);  
END REP_BALANCE_CUENTAS_SALDO_X;

  PROCEDURE REP_METAS_VENCIMIENTO_30DIAS_T(
    p_cod_cliente IN  NUMBER,
    p_desde       IN  DATE,
    p_hasta       IN  DATE,
    p_cursor      OUT SYS_REFCURSOR)
  IS
  BEGIN
    OPEN p_cursor FOR
      SELECT id_meta                                                AS id_meta,
             nombre_meta                                            AS nombre_meta,
             id_tipo                                                AS id_tipo,
             id_estado                                              AS id_estado,
             fecha_limite                                           AS fecha_limite,
             monto_objetivo                                         AS monto_objetivo,
             NVL(saldo, 0)                                          AS saldo,
             monto_objetivo - NVL(saldo, 0)                         AS falta_por_ahorrar,
             ROUND(NVL(saldo,0) / NULLIF(monto_objetivo,0) * 100, 2)AS avance_pct,
             fecha_limite - TRUNC(SYSDATE)                          AS dias_restantes,
             ROUND((monto_objetivo - NVL(saldo,0))
                   / NULLIF(fecha_limite - TRUNC(SYSDATE), 0), 2)   AS ahorro_diario_necesario,
             RANK() OVER (ORDER BY fecha_limite ASC)                AS urgencia_ranking,
             ROUND(RATIO_TO_REPORT(NVL(saldo,0)) OVER () * 100, 2)  AS pct_del_ahorro_total,
             COUNT(*) OVER ()                                       AS total_metas_por_vencer
        FROM metas
       WHERE cod_cliente = p_cod_cliente
         AND fecha_limite >  TRUNC(SYSDATE)
         AND fecha_limite <= TRUNC(SYSDATE) + 30
         AND id_estado = 1
       ORDER BY fecha_limite ASC;
  END REP_METAS_VENCIMIENTO_30DIAS_T;

  PROCEDURE REP_METAS_VENCIMIENTO_30DIAS_X(
    p_cod_cliente IN  NUMBER,
    p_desde       IN  DATE,
    p_hasta       IN  DATE,
    p_texto       OUT CLOB)
  IS
    CURSOR c_metas IS
      SELECT nombre_meta,
             id_tipo,
             fecha_limite,
             monto_objetivo,
             NVL(saldo, 0)                                           AS saldo,
             monto_objetivo - NVL(saldo, 0)                          AS falta_por_ahorrar,
             ROUND(NVL(saldo,0) / NULLIF(monto_objetivo,0) * 100, 2) AS avance_pct,
             fecha_limite - TRUNC(SYSDATE)                           AS dias_restantes,
             ROUND((monto_objetivo - NVL(saldo,0))
                   / NULLIF(fecha_limite - TRUNC(SYSDATE), 0), 2)    AS ahorro_diario_necesario,
             RANK() OVER (ORDER BY fecha_limite ASC)                 AS urgencia_ranking
        FROM metas
       WHERE cod_cliente = p_cod_cliente
         AND fecha_limite >  TRUNC(SYSDATE)
         AND fecha_limite <= TRUNC(SYSDATE) + 30
         AND id_estado = 1
       ORDER BY fecha_limite ASC;

    v_hay_datos BOOLEAN := FALSE;
  BEGIN
    p_texto := pkg_rep_util.separador
            || pkg_rep_util.linea('Metas que vencen en 30 días', '')
            || pkg_rep_util.linea('Cliente', TO_CHAR(p_cod_cliente))
            || pkg_rep_util.linea('Fecha de corte', TO_CHAR(SYSDATE, 'DD/MM/YYYY'))
            || pkg_rep_util.separador;

    FOR r IN c_metas LOOP
      v_hay_datos := TRUE;
      p_texto := p_texto
              || pkg_rep_util.linea('#' || TO_CHAR(r.urgencia_ranking),
                                    r.nombre_meta)
              || pkg_rep_util.linea('  Tipo (ID)', TO_CHAR(r.id_tipo))
              || pkg_rep_util.linea('  Vence', TO_CHAR(r.fecha_limite, 'DD/MM/YYYY'))
              || pkg_rep_util.linea('  Días restantes', TO_CHAR(r.dias_restantes))
              || pkg_rep_util.linea('  Objetivo', pkg_rep_util.monto(r.monto_objetivo))
              || pkg_rep_util.linea('  Acumulado', pkg_rep_util.monto(r.saldo))
              || pkg_rep_util.linea('  Falta por ahorrar', pkg_rep_util.monto(r.falta_por_ahorrar))
              || pkg_rep_util.linea('  Ahorro diario necesario', pkg_rep_util.monto(r.ahorro_diario_necesario))
              || pkg_rep_util.linea('  Avance', TO_CHAR(r.avance_pct) || ' %')
              || pkg_rep_util.separador;
    END LOOP;

    IF NOT v_hay_datos THEN
      p_texto := p_texto
              || pkg_rep_util.linea('No hay metas que venzan en los próximos 30 días.', '')
              || pkg_rep_util.separador;
    END IF;

    p_texto := p_texto || pkg_rep_util.pie_cliente(p_cod_cliente);  
END REP_METAS_VENCIMIENTO_30DIAS_X;

  PROCEDURE REP_LISTADO_CLIENTES_T(
    p_cod_cliente IN  NUMBER,
    p_desde       IN  DATE,
    p_hasta       IN  DATE,
    p_cursor      OUT SYS_REFCURSOR)
  IS
  BEGIN
    OPEN p_cursor FOR
      SELECT cod_cliente                                        AS cod_cliente,
             INITCAP(TRIM(nombres || ' ' || ap_paterno || ' ' ||
                          NVL(ap_materno, '')))                 AS nombre_completo,
             UPPER(ap_paterno)                                  AS ap_paterno,
             UPPER(NVL(ap_materno, '-'))                        AS ap_materno,
             INITCAP(nombres)                                   AS nombres
        FROM cliente
       ORDER BY ap_paterno, ap_materno, nombres;
  END REP_LISTADO_CLIENTES_T;

  PROCEDURE REP_LISTADO_CLIENTES_X(
    p_cod_cliente IN  NUMBER,
    p_desde       IN  DATE,
    p_hasta       IN  DATE,
    p_texto       OUT CLOB)
  IS
    CURSOR c_clientes IS
      SELECT cod_cliente,
             INITCAP(TRIM(nombres || ' ' || ap_paterno || ' ' ||
                          NVL(ap_materno, '')))                 AS nombre_completo,
             UPPER(ap_paterno)                                  AS ap_paterno,
             UPPER(NVL(ap_materno, '-'))                        AS ap_materno,
             INITCAP(nombres)                                   AS nombres
        FROM cliente
       ORDER BY ap_paterno, ap_materno, nombres;

    v_cant      NUMBER  := 0;
    v_hay_datos BOOLEAN := FALSE;
  BEGIN
    p_texto := pkg_rep_util.separador
            || pkg_rep_util.linea('Listado de clientes', '')
            || pkg_rep_util.separador;

    FOR r IN c_clientes LOOP
      v_hay_datos := TRUE;
      v_cant := v_cant + 1;
      p_texto := p_texto
              || pkg_rep_util.linea('#' || TO_CHAR(v_cant),
                                    '[' || TO_CHAR(r.cod_cliente) || ']')
              || pkg_rep_util.linea('  Nombre completo', r.nombre_completo)
              || pkg_rep_util.linea('  Ap. paterno', r.ap_paterno)
              || pkg_rep_util.linea('  Ap. materno', r.ap_materno)
              || pkg_rep_util.linea('  Nombres', r.nombres)
              || pkg_rep_util.separador;
    END LOOP;

    IF NOT v_hay_datos THEN
      p_texto := p_texto
              || pkg_rep_util.linea('No hay clientes registrados.', '')
              || pkg_rep_util.separador;
    ELSE
      p_texto := p_texto
              || pkg_rep_util.linea('Total de clientes', TO_CHAR(v_cant))
              || pkg_rep_util.separador;
    END IF;

    p_texto := p_texto || pkg_rep_util.pie_cliente(p_cod_cliente);  
END REP_LISTADO_CLIENTES_X;

END PKG_REP_I1;
/
/*===============================================
            PRUEBA DE REPORTES
===============================================*/
--PRUEBA DE REPORTES _T
SET SERVEROUTPUT ON SIZE UNLIMITED
DECLARE
  v_cur  SYS_REFCURSOR;
  v_sql  INTEGER;
  v_desc DBMS_SQL.DESC_TAB;
  v_cols NUMBER;
  v_val  VARCHAR2(4000);
BEGIN
  -- Indicar reporte a probar
  PKG_REP_I1.REP_LISTADO_CLIENTES_T(null,null,null,v_cur); --ADD_MONTHS(SYSDATE, -1), SYSDATE, v_cur); Cambia segun reporte

  v_sql := DBMS_SQL.TO_CURSOR_NUMBER(v_cur);
  DBMS_SQL.DESCRIBE_COLUMNS(v_sql, v_cols, v_desc);   

  -- Imprimir encabezados
  FOR i IN 1 .. v_cols LOOP
    DBMS_OUTPUT.PUT(RPAD(v_desc(i).col_name, 22));
    DBMS_SQL.DEFINE_COLUMN(v_sql, i, v_val, 4000);
  END LOOP;
  DBMS_OUTPUT.NEW_LINE;
  DBMS_OUTPUT.PUT_LINE(RPAD('-', 22 * v_cols, '-'));

  -- Recorrer los datos
  LOOP
    EXIT WHEN DBMS_SQL.FETCH_ROWS(v_sql) = 0;
    FOR i IN 1 .. v_cols LOOP
      DBMS_SQL.COLUMN_VALUE(v_sql, i, v_val);
      DBMS_OUTPUT.PUT(RPAD(NVL(v_val, '(null)'), 22));
    END LOOP;
    DBMS_OUTPUT.NEW_LINE;
  END LOOP;

  DBMS_SQL.CLOSE_CURSOR(v_sql);
END;
/
--PRUEBA DE REPORTES _X
SET SERVEROUTPUT ON SIZE UNLIMITED
SET LINESIZE 200
DECLARE
  v_clob CLOB;
  v_id   NUMBER := 41;  

  PROCEDURE imprimir(p_titulo IN VARCHAR2, p_clob IN CLOB) IS
  BEGIN
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('============================================================');
    DBMS_OUTPUT.PUT_LINE('>>> ' || p_titulo);
    DBMS_OUTPUT.PUT_LINE('============================================================');
    IF p_clob IS NULL OR DBMS_LOB.GETLENGTH(p_clob) = 0 THEN
      DBMS_OUTPUT.PUT_LINE('(CLOB vacío)');
    ELSE
      FOR i IN 0 .. CEIL(DBMS_LOB.GETLENGTH(p_clob) / 4000) - 1 LOOP
        DBMS_OUTPUT.PUT_LINE(
          DBMS_LOB.SUBSTR(p_clob, 4000, i * 4000 + 1)
        );
      END LOOP;
    END IF;
    DBMS_OUTPUT.PUT_LINE('');
  END imprimir;

BEGIN
  PKG_REP_I1.REP_ESTADISTICA_MOVIMIENTOS_X(
    v_id, DATE '2024-01-01', DATE '2024-12-31', v_clob);
  imprimir('REPORTE — Estadística de movimientos', v_clob);

  PKG_REP_I1.REP_BALANCE_CUENTAS_SALDO_X(
    v_id, NULL, NULL, v_clob);
  imprimir('REPORTE — Balance de cuentas por saldo', v_clob);

  PKG_REP_I1.REP_METAS_VENCIMIENTO_30DIAS_X(
    v_id, NULL, NULL, v_clob);
  imprimir('REPORTE — Metas por vencer en 30 días', v_clob);

  PKG_REP_I1.REP_LISTADO_CLIENTES_X(
    NULL, NULL, NULL, v_clob);
  imprimir('REPORTE — Listado de clientes', v_clob);

EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('!!! ERROR: ' || SQLERRM);
    DBMS_OUTPUT.PUT_LINE(DBMS_UTILITY.FORMAT_ERROR_BACKTRACE);
END;
/
/*===============================================
  INSERCIÓN DE REGISTROS EN LA TABLA REPORTES
===============================================*/
--Se registran los dos reportes (formato Tabla y formato Texto), pero para evitar duplicados en la interfaz, solo uno estará activo.

INSERT INTO reportes (categoria, nombre, descripcion, paquete, procedimiento,
                      usa_fechas, activo, solo_admin, formato)
VALUES ('B', 'Estadística global de movimientos',
        'Resumen de todos los ingresos y gastos registrados en la aplicación: cantidad, promedio y variación de los montos.',
        'PKG_REP_I1', 'REP_ESTADISTICA_MOVIMIENTOS_T', 'S', 'N', 'S', 'T');
        
INSERT INTO reportes (categoria, nombre, descripcion, paquete, procedimiento,
                      usa_fechas, activo, solo_admin, formato)
VALUES ('B', 'Estadística global de movimientos',
        'Resumen de todos los ingresos y gastos registrados en la aplicación: cantidad, promedio y variación de los montos.',
        'PKG_REP_I1', 'REP_ESTADISTICA_MOVIMIENTOS_X', 'S', 'S', 'S', 'X');

COMMIT;

INSERT INTO reportes (categoria, nombre, descripcion, paquete, procedimiento,
                      usa_fechas, activo, solo_admin, formato)
VALUES ('B', 'Balance general de cuentas y saldos',
        'Tus cuentas de mayor a menor saldo, con el total, el promedio y qué porcentaje de tu dinero tiene cada una.',
        'PKG_REP_I1', 'REP_BALANCE_CUENTAS_SALDO_T', 'N', 'N', 'N', 'T');

INSERT INTO reportes (categoria, nombre, descripcion, paquete, procedimiento,
                      usa_fechas, activo, solo_admin, formato)
VALUES ('B', 'Balance general de cuentas y saldos',
        'Tus cuentas de mayor a menor saldo, con el total, el promedio y qué porcentaje de tu dinero tiene cada una.',
        'PKG_REP_I1', 'REP_BALANCE_CUENTAS_SALDO_X', 'N', 'S', 'N', 'X');

COMMIT;

INSERT INTO reportes (categoria, nombre, descripcion, paquete, procedimiento,
                      usa_fechas, activo, solo_admin, formato)
VALUES ('B', 'Proximos vencimientos de metas',
        'Metas que vencen en los próximos 30 días: cuánto llevas, cuánto te falta y cuánto ahorrar por día para llegar a tiempo.',
        'PKG_REP_I1', 'REP_METAS_VENCIMIENTO_30DIAS_T', 'N', 'N', 'N', 'T');

INSERT INTO reportes (categoria, nombre, descripcion, paquete, procedimiento,
                      usa_fechas, activo, solo_admin, formato)
VALUES ('B', 'Proximos vencimientos de metas',
        'Metas que vencen en los próximos 30 días: cuánto llevas, cuánto te falta y cuánto ahorrar por día para llegar a tiempo.',
        'PKG_REP_I1', 'REP_METAS_VENCIMIENTO_30DIAS_X', 'N', 'S', 'N', 'X');

COMMIT;

INSERT INTO reportes (categoria, nombre, descripcion, paquete, procedimiento,
                      usa_fechas, activo, solo_admin, formato)
VALUES ('B', 'Listado de clientes registrados',
        'Lista de clientes registrados con su nombre completo.',
        'PKG_REP_I1', 'REP_LISTADO_CLIENTES_T', 'N', 'N', 'S', 'T');

INSERT INTO reportes (categoria, nombre, descripcion, paquete, procedimiento,
                      usa_fechas, activo, solo_admin, formato)
VALUES ('B', 'Listado de clientes registrados',
        'Lista de clientes registrados con su nombre completo.',
        'PKG_REP_I1', 'REP_LISTADO_CLIENTES_X', 'N', 'S', 'S', 'X');

COMMIT;