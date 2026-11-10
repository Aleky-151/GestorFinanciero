CREATE OR REPLACE PACKAGE pkg_rep_util IS
  FUNCTION linea(p_etiqueta IN VARCHAR2, p_valor IN VARCHAR2) RETURN VARCHAR2;
  FUNCTION separador RETURN VARCHAR2;
  FUNCTION monto(p_valor IN NUMBER) RETURN VARCHAR2;
  FUNCTION pie_cliente(p_cod_cliente IN NUMBER) RETURN VARCHAR2;
END pkg_rep_util;
/

CREATE OR REPLACE PACKAGE BODY pkg_rep_util IS
    FUNCTION linea(p_etiqueta IN VARCHAR2, p_valor IN VARCHAR2) RETURN VARCHAR2 IS
    BEGIN
      RETURN CASE WHEN LENGTH(p_etiqueta) >= 28
                  THEN p_etiqueta || ' '
                  ELSE RPAD(p_etiqueta, 28)
             END || p_valor || CHR(10);
    END linea;

  FUNCTION separador RETURN VARCHAR2 IS
  BEGIN
    RETURN RPAD('-', 60, '-') || CHR(10);
  END separador;

  FUNCTION monto(p_valor IN NUMBER) RETURN VARCHAR2 IS
  BEGIN
    RETURN TRIM(TO_CHAR(NVL(p_valor, 0), 'L999,999.99'));
  END monto;
    
  FUNCTION pie_cliente(p_cod_cliente IN NUMBER) RETURN VARCHAR2 IS
  v_nombre VARCHAR2(250) := 'Administración';
    BEGIN
      IF p_cod_cliente IS NOT NULL THEN
        BEGIN
          SELECT INITCAP(TRIM(nombres || ' ' || ap_paterno || ' ' || ap_materno))
            INTO v_nombre
            FROM cliente
           WHERE cod_cliente = p_cod_cliente;
        EXCEPTION WHEN NO_DATA_FOUND THEN NULL;
        END;
      END IF;
      RETURN 'Reporte generado por ' || v_nombre || CHR(10)
          || 'Fecha: ' || TO_CHAR(SYSTIMESTAMP AT TIME ZONE 'America/Lima',
                                  'DD/MM/YYYY HH24:MI') || CHR(10);
    END pie_cliente;
  
END pkg_rep_util;
/