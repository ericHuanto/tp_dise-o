#!/bin/bash
#Crea los 3 esquemas y un usuario por microservicio con acceso solo a su propio esquema.
set -eo pipefail

mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" <<SQL
CREATE DATABASE IF NOT EXISTS donaciones         CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS logistica          CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS donatrack_usuarios CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'donaciones_app'@'%' IDENTIFIED BY '${DONACIONES_DB_PASSWORD}';
CREATE USER IF NOT EXISTS 'logistica_app'@'%'  IDENTIFIED BY '${LOGISTICA_DB_PASSWORD}';
CREATE USER IF NOT EXISTS 'bff_app'@'%'        IDENTIFIED BY '${BFF_DB_PASSWORD}';

GRANT ALL PRIVILEGES ON donaciones.*         TO 'donaciones_app'@'%';
GRANT ALL PRIVILEGES ON logistica.*          TO 'logistica_app'@'%';
GRANT ALL PRIVILEGES ON donatrack_usuarios.* TO 'bff_app'@'%';

FLUSH PRIVILEGES;
SQL

echo "[init.sh] Esquemas y usuarios de DonaTrack creados."
