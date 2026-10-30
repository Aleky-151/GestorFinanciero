CREATE OR REPLACE PROCEDURE sp_vencer_metas IS
    v_hoy DATE := TRUNC(CAST(SYSTIMESTAMP AT TIME ZONE 'America/Lima' AS DATE));
BEGIN
    UPDATE metas
    SET id_estado = 3
    WHERE id_estado = 1
      AND fecha_limite < v_hoy;
END;
/
BEGIN
    DBMS_SCHEDULER.CREATE_JOB(
        job_name        => 'JOB_VENCER_METAS',
        job_type        => 'PLSQL_BLOCK',
        job_action      => 'BEGIN sp_vencer_metas; COMMIT; END;',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=DAILY;BYHOUR=5;BYMINUTE=5;BYSECOND=0',
        enabled         => TRUE,
        comments        => 'Marca como VENCIDA las metas en progreso con fecha límite pasada');
END;
/

BEGIN DBMS_SCHEDULER.RUN_JOB('JOB_VENCER_METAS'); END;
/
   DROP TRIGGER trg_actualiza_saldo_meta;
   DROP TRIGGER trg_valida_fecha_aportes_meta;
   DROP TRIGGER trg_valida_fecha_meta;

commit;