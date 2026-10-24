ALTER SESSION SET CONTAINER = "XEPDB1";
CREATE USER financista IDENTIFIED BY financista;
GRANT CONNECT, RESOURCE, UNLIMITED TABLESPACE TO financista;

Create tablespace ts_finanzas
Datafile 'C:\proyectoFinanzas\ts_fin.ora'
    size 200M
    AUTOEXTEND ON NEXT 50M MAXSIZE 2G
Extent management local autoallocate;
