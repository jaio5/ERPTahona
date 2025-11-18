#!/usr/bin/env python3
import re
import sys

path_orig = 'tahonaOriginal.sql'
path_alter = 'tools/tahona_add_pks_fks.sql'

def parse_create_tables(sql):
    tables = {}
    # find all CREATE TABLE `Name` ( ... ); blocks
    for m in re.finditer(r"CREATE\s+TABLE\s+IF\s+NOT\s+EXISTS\s+`([^`]+)`\s*\((.*?)\);", sql, re.S | re.I):
        name = m.group(1)
        body = m.group(2)
        cols = []
        for line in body.splitlines():
            line = line.strip()
            if not line:
                continue
            # consider lines that start with backtick as column defs
            if line.startswith('`'):
                cm = re.match(r"`([^`]+)`", line)
                if cm:
                    cols.append(cm.group(1))
        tables[name] = set(cols)
    return tables


def analyze_alters(alter_sql, tables):
    issues = []
    results = []
    # split statements by semicolon
    stmts = re.split(r';\s*\n', alter_sql)
    for stmt in stmts:
        s = stmt.strip()
        if not s:
            continue
        if not re.match(r'(?i)^ALTER\s+TABLE', s):
            continue
        # get table name
        tm = re.match(r"(?i)ALTER\s+TABLE\s+`([^`]+)`\s+(.*)$", s, re.S)
        if not tm:
            results.append((s, 'PARSE_FAIL', 'No pude parsear el ALTER TABLE'))
            continue
        table = tm.group(1)
        body = tm.group(2)
        if table not in tables:
            results.append((s, 'MISSING_TABLE', f'Tabla `{table}` no encontrada en CREATE TABLE'))
            continue
        tblcols = tables[table]
        # check MODIFY occurrences
        mods = re.findall(r"(?i)MODIFY\s+`([^`]+)`", s)
        missing_mods = [c for c in mods if c not in tblcols]
        # check ADD PRIMARY KEY (col list)
        pkm = re.search(r"(?i)ADD\s+PRIMARY\s+KEY\s*\(([^)]+)\)", s)
        pkcols = []
        missing_pks = []
        if pkm:
            inside = pkm.group(1)
            pkcols = re.findall(r"`([^`]+)`", inside)
            missing_pks = [c for c in pkcols if c not in tblcols]
        # check ADD INDEX / ADD KEY (...) columns (explicit)
        idx_cols = []
        for im in re.finditer(r"(?i)(?:ADD\s+INDEX|ADD\s+KEY|ADD\s+UNIQUE\s+KEY)\s+[^\(]*\(([^)]+)\)", s):
            inside = im.group(1)
            found = re.findall(r"`([^`]+)`", inside)
            if found:
                idx_cols.extend(found)
        missing_idxs = [c for c in idx_cols if c not in tblcols]
        # check FK: FOREIGN KEY (`col`) REFERENCES `RefTable`(`RefCol`)
        fks = []
        fk_matches = re.finditer(r"(?i)FOREIGN\s+KEY\s*\(([^)]+)\)\s+REFERENCES\s+`([^`]+)`\s*\(([^)]+)\)", s)
        missing_fks = []
        for fk in fk_matches:
            left = re.findall(r"`([^`]+)`", fk.group(1))
            ref_table = fk.group(2)
            ref_cols = re.findall(r"`([^`]+)`", fk.group(3))
            for c in left:
                if c not in tblcols:
                    missing_fks.append((table, c, ref_table))
            if ref_table not in tables:
                missing_fks.append((table, None, ref_table))
            else:
                for rc in ref_cols:
                    if rc not in tables[ref_table]:
                        missing_fks.append((ref_table, rc, table))
        status = 'OK' if not (missing_mods or missing_pks or missing_idxs or missing_fks) else 'ISSUES'
        details = {'missing_mods': missing_mods, 'missing_pks': missing_pks, 'missing_idxs': missing_idxs, 'missing_fks': missing_fks}
        results.append((table, status, details))
    return results


def main():
    try:
        with open(path_orig, 'r', encoding='utf-8') as f:
            orig = f.read()
    except Exception as e:
        print(f'Error leyendo {path_orig}: {e}', file=sys.stderr)
        sys.exit(2)
    try:
        with open(path_alter, 'r', encoding='utf-8') as f:
            alt = f.read()
    except Exception as e:
        print(f'Error leyendo {path_alter}: {e}', file=sys.stderr)
        sys.exit(3)

    tables = parse_create_tables(orig)
    print(f'Parsed {len(tables)} CREATE TABLE blocks from {path_orig}.')
    # list tables
    print('\nTablas encontradas:')
    for t in sorted(tables.keys()):
        print(' -', t, f'({len(tables[t])} columnas)')

    results = analyze_alters(alt, tables)
    print('\nResumen de comprobación de ALTER TABLE en', path_alter)
    any_issues = False
    for table, status, details in results:
        print(f'\nTabla `{table}`: {status}')
        if status == 'OK':
            continue
        any_issues = True
        if details['missing_mods']:
            print('  Columnas en MODIFY no encontradas en CREATE TABLE:', details['missing_mods'])
        if details['missing_pks']:
            print('  Columnas para PK no encontradas en CREATE TABLE:', details['missing_pks'])
        if details['missing_idxs']:
            print('  Columnas referenciadas en índices no encontradas:', details['missing_idxs'])
        if details['missing_fks']:
            print('  Problemas en FK (tabla/col/ref):', details['missing_fks'])

    if not any_issues:
        print('\nComprobación completada: no se detectaron problemas estáticos evidentes entre ALTERs y CREATE TABLEs.')
    else:
        print('\nSe detectaron problemas; revisa las entradas anteriores y corrige el script antes de ejecutarlo en el servidor.')

if __name__ == '__main__':
    main()
