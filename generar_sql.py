import re

print("Iniciando generación del script MySQL mejorado...")

# Leer archivo original
with open('D:/Programación/ERP/tahonaOriginal.sql', 'r', encoding='utf-8') as f:
    content = f.read()

print(f"Archivo original leído: {len(content)} caracteres")

# Crear el nuevo script mejorado
output = []
output.append('-- ================================================================')
output.append('-- SCRIPT MYSQL MEJORADO - Base de Datos Tahona ERP')
output.append('-- Generado automáticamente con optimizaciones para MySQL 8.0+')
output.append('-- Fecha: 2025-11-16')
output.append('-- ================================================================\n')

# Configuración inicial mejorada
output.append('SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;')
output.append('SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;')
output.append('SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE=\'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION\';')
output.append('SET NAMES utf8mb4;')
output.append('SET CHARACTER_SET_CLIENT=utf8mb4;')
output.append('SET CHARACTER_SET_CONNECTION=utf8mb4;')
output.append('SET CHARACTER_SET_RESULTS=utf8mb4;')
output.append('SET COLLATION_CONNECTION=utf8mb4_unicode_ci;')
output.append('SET TIME_ZONE=\'+00:00\';')
output.append('')

# Crear/usar base de datos
output.append('-- Crear y usar base de datos')
output.append('DROP DATABASE IF EXISTS tahona;')
output.append('CREATE DATABASE tahona')
output.append('  CHARACTER SET utf8mb4')
output.append('  COLLATE utf8mb4_unicode_ci;')
output.append('')
output.append('USE tahona;')
output.append('')

# Procesar el contenido original
# Extraer CREATE TABLE y convertir tipos de datos
tables = re.findall(r'CREATE TABLE IF NOT EXISTS.*?;', content, re.DOTALL | re.IGNORECASE)
print(f"Tablas encontradas: {len(tables)}")

for idx, table in enumerate(tables):
    # Mejorar definición de tabla
    improved_table = table

    # Convertir TEXT a VARCHAR(500)
    improved_table = re.sub(r'`(\w+)`\s+TEXT', lambda m: f'`{m.group(1)}` VARCHAR(500)', improved_table)

    # Convertir INTEGER a INT
    improved_table = re.sub(r'\s+INTEGER', ' INT', improved_table)

    # Convertir REAL a DECIMAL(15,2)
    improved_table = re.sub(r'\s+REAL', ' DECIMAL(15,2)', improved_table)

    # Añadir ENGINE y DEFAULT CHARSET
    improved_table = improved_table.rstrip(';').rstrip()
    improved_table += '\n  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;'

    output.append(improved_table)
    output.append('')

    if (idx + 1) % 10 == 0:
        print(f"Procesadas {idx + 1} tablas...")

# Añadir los INSERT
output.append('\n-- ================================================================')
output.append('-- INSERCIÓN DE DATOS')
output.append('-- ================================================================\n')

# Extraer todos los INSERT
inserts = re.findall(r'INSERT INTO.*?;', content, re.DOTALL | re.IGNORECASE)
print(f"Inserts encontrados: {len(inserts)}")

for idx, insert in enumerate(inserts):
    output.append(insert)
    if (idx + 1) % 100 == 0:
        print(f"Procesados {idx + 1} inserts...")

# Restaurar configuración
output.append('\n-- ================================================================')
output.append('-- RESTAURAR CONFIGURACIÓN')
output.append('-- ================================================================\n')
output.append('SET SQL_MODE=@OLD_SQL_MODE;')
output.append('SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;')
output.append('SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;')
output.append('')
output.append('-- Script completado exitosamente')

# Escribir archivo
print("Escribiendo archivo de salida...")
with open('D:/Programación/ERP/tahona_mysql_mejorado.sql', 'w', encoding='utf-8') as f:
    f.write('\n'.join(output))

print('\n✓ Script MySQL mejorado generado exitosamente: tahona_mysql_mejorado.sql')
print(f'✓ Total de tablas procesadas: {len(tables)}')
print(f'✓ Total de inserts procesados: {len(inserts)}')
print('\nEl archivo está listo para ejecutarse en MySQL Workbench.')

