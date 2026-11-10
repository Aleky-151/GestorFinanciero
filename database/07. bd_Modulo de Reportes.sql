/*
=================================  SOBRE LOS REPORTES  ===================================================
-- Los reportes se agrupan en paquetes por integrante, donde se implementa cada procedimiento/funcion usada
   (ej. MOVS_POR_TIPO). 
-- El atributo solo_admin determina si ese reporte solo debería estar disponible para el administrador,
   debido al contenido que manejan como todos los reportes de control generados por auditorias de triggers.
-- De manera preliminar y para propósito académico, estas categorias de reporte se definen con un CHECK
   diferenciando reportes B básico, E específico, C complejo, K control
-- Los reportes pueden activarse o desactivarse
-- El uso de fechas se utiliza en la vista, para ocultar los campos que piden al usuario delimitar
   el periodo de analisis del reporte.
-- Se trabaja con 2 formatos, TABLA(T) y TEXTO(X)
==========================================================================================================
*/

CREATE TABLE reportes (
    id_reporte     NUMBER GENERATED ALWAYS AS IDENTITY,
    categoria      CHAR(1) NOT NULL,         
    nombre         VARCHAR2(100) NOT NULL,
    descripcion    VARCHAR2(300),
    paquete        VARCHAR2(50) NOT NULL,    
    procedimiento  VARCHAR2(50) NOT NULL,     
    usa_fechas     CHAR(1) DEFAULT 'S' NOT NULL,
    activo         CHAR(1) DEFAULT 'S' NOT NULL,
    solo_admin CHAR(1) DEFAULT 'N' NOT NULL,
    formato CHAR(1) DEFAULT 'T' NOT NULL,
    CONSTRAINT pk_reportes PRIMARY KEY (id_reporte),
    CONSTRAINT ck_reportes_cat CHECK (categoria IN ('B','E','C','K')),
    CONSTRAINT ck_reportes_fechas CHECK (usa_fechas IN ('S','N')),
    CONSTRAINT ck_reportes_activo CHECK (activo IN ('S','N')),
    CONSTRAINT uq_reportes_proc UNIQUE (paquete, procedimiento),
    CONSTRAINT ck_reportes_admin CHECK (solo_admin IN ('S','N')),
    CONSTRAINT ck_reportes_formato CHECK (formato IN ('T','X'))
);

-- MENU DINAMICO: REPORTES
INSERT INTO OPCIONES(nombre_opcion, url_opcion, icono, orden)
VALUES ('Reportes','/reportes','fas fa-file-lines', 50);
--Verificar la id de Reportes antes de continuar

INSERT INTO OPCIONES(nombre_opcion, url_opcion, id_opcion_ref, orden)
VALUES ('Reportes basicos','/reportes/basicos', 21, 51);
INSERT INTO OPCIONES(nombre_opcion, url_opcion, id_opcion_ref, orden)
VALUES ('Reportes especificos','/reportes/especificos', 21, 52);
INSERT INTO OPCIONES(nombre_opcion, url_opcion, id_opcion_ref, orden)
VALUES ('Reportes complejos','/reportes/complejos', 21, 53);
INSERT INTO OPCIONES(nombre_opcion, url_opcion, id_opcion_ref, orden)
VALUES ('Reportes de control','/reportes/control', 21, 54);

commit;

-- CREACIÓN DE ROL ADMINISTRADOR PARA REPORTES DE CONTROL
ALTER TABLE USUARIO 
ADD rol VARCHAR2(10) DEFAULT 'CLIENTE' NOT NULL;

ALTER TABLE USUARIO
ADD CONSTRAINT ck_usuario_rol CHECK(rol IN ('CLIENTE', 'ADMIN'));

ALTER TABLE OPCIONES
ADD rol_requerido VARCHAR2(10) NULL;

-- Asignación de los reportes de control a solo el administrador
SELECT * FROM OPCIONES ORDER BY orden ASC;
UPDATE OPCIONES SET rol_requerido='ADMIN' WHERE id_opcion=29;

commit;