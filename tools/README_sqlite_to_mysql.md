Conversor SQLite -> MySQL (sencillo)

Objetivo
-------
Convertir un volcado SQLite (.sql) a un script compatible con MySQL y
crear automáticamente la base de datos destino.

Archivos
-------
- tools/sqlite_to_mysql.py : script de conversión (Python 3)

Uso (Windows, cmd.exe)
---------------------
1. Copia tu volcado SQLite (por ejemplo: Gest2007.sqlite.sql) en la carpeta del proyecto o indica su ruta completa.
2. Ejecuta (en cmd.exe):

   python tools\sqlite_to_mysql.py Gest2007.sqlite.sql Gest2007_mysql.sql --dbname=gest2007

3. Revisa el archivo `Gest2007_mysql.sql` para validar tipos y constraints.
4. Importa en MySQL:

   mysql -u root -p < Gest2007_mysql.sql

Notas y limitaciones
--------------------
- No cubre todos los casos de SQL de SQLite (triggers complejos, expresiones específicas, tipos personalizados).
- Revisa manualmente los índices, constraints y triggers. Las funciones y virtual tables no se convertirán.
- Haz backup antes de ejecutar en producción.

Si quieres, puedo ejecutar el script de conversión aquí si subes el archivo `Gest2007.sqlite.sql` al repo o me indicas la ruta local dentro del workspace.
