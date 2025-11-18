#!/usr/bin/env python3
"""
Convertidor sencillo de un volcado SQLite (.sql) a un script compatible con MySQL.
No cubre todos los casos posibles pero realiza las transformaciones más comunes:
 - elimina PRAGMA, BEGIN/COMMIT, sqlite_sequence
 - convierte CREATE TABLE a un formato con `IF NOT EXISTS` y añade ENGINE/CHARSET
 - convierte INTEGER PRIMARY KEY AUTOINCREMENT -> INT AUTO_INCREMENT PRIMARY KEY
 - reemplaza " identificadores " por `backticks`
 - convierte BOOLEAN -> TINYINT(1), BLOB -> LONGBLOB
 - mantiene e inserta encabezado para crear la base de datos

Uso:
  python tools\sqlite_to_mysql.py input_sqlite_dump.sql output_mysql.sql --dbname=gest2007

Revisa el SQL resultante antes de ejecutarlo en producción.
"""

import re
import argparse
import sys


def transform_create_table(match):
    name = match.group(1)
    body = match.group(2)
    # Reemplazar dobles comillas por backticks dentro del bloque
    body = body.replace('"', '`')
    # Reemplazar AUTOINCREMENT y variantes
    body = re.sub(r'(?i)integer\s+primary\s+key\s+autoincrement', 'INT AUTO_INCREMENT PRIMARY KEY', body)
    body = re.sub(r'(?i)integer\s+primary\s+key', 'INT PRIMARY KEY', body)
    body = re.sub(r'(?i)autoincrement', 'AUTO_INCREMENT', body)
    # Tipos comunes
    body = re.sub(r'(?i)\bboolean\b', 'TINYINT(1)', body)
    body = re.sub(r'(?i)\bblob\b', 'LONGBLOB', body)
    body = re.sub(r'(?i)\bdouble\b', 'DOUBLE', body)
    body = re.sub(r'(?i)\breal\b', 'DOUBLE', body)
    # Evitar DEFAULT (CURRENT_TIMESTAMP) incompatible: dejar como está en la mayoría de casos
    # Normalizar comillas dobles restantes (identificadores)
    # Construir CREATE TABLE con IF NOT EXISTS y engine/charset
    create_stmt = f"CREATE TABLE IF NOT EXISTS `{name}` ({body}) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;"
    return create_stmt


def main():
    parser = argparse.ArgumentParser(description='Convert SQLite dump (.sql) to MySQL-compatible SQL')
    parser.add_argument('input', help='Archivo .sql de origen (SQLite dump)')
    parser.add_argument('output', help='Archivo .sql de salida (MySQL compatible)')
    parser.add_argument('--dbname', default='gest2007', help='Nombre de la base de datos a crear en el script MySQL')
    args = parser.parse_args()

    try:
        with open(args.input, 'r', encoding='utf-8') as f:
            content = f.read()
    except Exception as e:
        print(f"Error leyendo {args.input}: {e}", file=sys.stderr)
        sys.exit(2)

    # Eliminar líneas/commands de SQLite innecesarios
    # Quitar PRAGMA, sqlite specific statements, comienza y termina transacciones
    content = re.sub(r"(?m)^PRAGMA.*$\n?", "", content)
    content = re.sub(r"(?m)^BEGIN TRANSACTION;\n?", "", content, flags=re.I)
    content = re.sub(r"(?m)^COMMIT;\n?", "", content, flags=re.I)
    content = re.sub(r"(?m)^ANALYZE.*$\n?", "", content, flags=re.I)
    content = re.sub(r"(?m)^VACUUM.*$\n?", "", content, flags=re.I)

    # Eliminar la tabla sqlite_sequence y cualquier INSERT hacia ella
    content = re.sub(r"(?is)CREATE TABLE\s+\"?sqlite_sequence\"?.*?;\n?", "", content)
    content = re.sub(r"(?m)^INSERT INTO\s+\"?sqlite_sequence\"?.*$\n?", "", content)

    # Transformar CREATE TABLE blocks
    # Captura: CREATE TABLE "name" ( ... );
    content = re.sub(r'CREATE\s+TABLE\s+\"?([^\"\s(]+)\"?\s*\((.*?)\);', transform_create_table, content, flags=re.S | re.I)

    # Transformar INSERT INTO "table"("col1","col2") VALUES ... -> INSERT INTO `table` (`col1`,`col2`) VALUES ...
    def repl_insert(m):
        table = m.group(1)
        cols = m.group(2)
        # reemplazar comillas dobles por backticks en las columnas
        cols_back = re.sub(r'\"([^\"]+)\"', r'`\1`', cols)
        return f'INSERT INTO `{table}` ({cols_back}) '

    content = re.sub(r'INSERT\s+INTO\s+\"?([^\"\s(]+)\"?\s*\(([^\)]*?)\)\s*VALUES', repl_insert, content, flags=re.I)
    # Transformar INSERT INTO "table" VALUES (...) -> INSERT INTO `table` VALUES (...)
    content = re.sub(r'INSERT\s+INTO\s+\"?([^\"\s(]+)\"?', lambda m: f'INSERT INTO `{m.group(1)}`', content, flags=re.I)

    # Reemplazar identificadores entre dobles comillas por backticks (con precaución)
    content = re.sub(r'\"([^\"]+)\"', r'`\1`', content)

    # Reemplazar AUTOINCREMENT (si quedó)
    content = re.sub(r'(?i)AUTOINCREMENT', 'AUTO_INCREMENT', content)

    # Tipos y otros arreglos en el resto del contenido
    content = re.sub(r'(?i)\bboolean\b', 'TINYINT(1)', content)
    content = re.sub(r'(?i)\bblob\b', 'LONGBLOB', content)
    content = re.sub(r'(?i)\bdatetime\b', 'DATETIME', content)

    # Quitar doble saltos al comienzo y final
    content = content.strip() + '\n'

    header = []
    header.append('SET NAMES utf8mb4;')
    header.append('SET FOREIGN_KEY_CHECKS=0;')
    header.append('\n')
    header.append(f'DROP DATABASE IF EXISTS `{args.dbname}`;')
    header.append(f'CREATE DATABASE IF NOT EXISTS `{args.dbname}` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;')
    header.append(f'USE `{args.dbname}`;')
    header.append('\n')

    final = "\n".join(header) + content + "\nSET FOREIGN_KEY_CHECKS=1;\n"

    try:
        with open(args.output, 'w', encoding='utf-8') as f:
            f.write(final)
    except Exception as e:
        print(f"Error escribiendo {args.output}: {e}", file=sys.stderr)
        sys.exit(3)

    print(f"Conversión completada. Archivo MySQL escrito en: {args.output}")


if __name__ == '__main__':
    main()

