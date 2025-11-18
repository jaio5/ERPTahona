#!/usr/bin/env python3
import re
from pathlib import Path

root = Path('')
orig = root / 'tahonaOriginal.sql'
out = root / 'tools' / 'modified_inserts.sql'
out_only = root / 'tools' / 'only_inserts.sql'

# ensure tools dir exists
(out.parent).mkdir(parents=True, exist_ok=True)

sql = orig.read_text(encoding='utf-8')

# parse CREATE TABLE blocks (supports IF NOT EXISTS and plain CREATE TABLE)
creates = {}
for m in re.finditer(r"CREATE\s+TABLE(?:\s+IF\s+NOT\s+EXISTS)?\s+`([^`]+)`\s*\((.*?)\)\s*;", sql, re.S | re.I):
    name = m.group(1)
    body = m.group(2)
    cols = []
    for line in body.splitlines():
        line = line.strip()
        if not line:
            continue
        # stop at constraints/keys
        if re.match(r"^(PRIMARY|UNIQUE|KEY|CONSTRAINT|FOREIGN)\b", line, re.I):
            continue
        # column lines usually start with backtick
        cm = re.match(r"`([^`]+)`", line)
        if cm:
            cols.append(cm.group(1))
    creates[name] = cols

print(f'Parsed {len(creates)} CREATE TABLEs')

# Replace INSERTs without explicit column list with column list when possible.
# This pattern captures from VALUES up to the terminating semicolon (handles multiple value tuples).
insert_pattern = re.compile(r"(INSERT\s+INTO\s+`([^`]+)`\s+VALUES\s*)(.*?;)", re.S | re.I)

new_sql = sql
replacements = []
for m in insert_pattern.finditer(sql):
    full = m.group(0)
    prefix = m.group(1)
    tbl = m.group(2)
    rest = m.group(3)  # includes trailing semicolon
    if tbl in creates and creates[tbl]:
        cols = creates[tbl]
        col_list = ','.join(f'`{c}`' for c in cols)
        new_prefix = f"INSERT INTO `{tbl}` ({col_list}) VALUES "
        new_stmt = new_prefix + rest
        replacements.append((full, new_stmt))

# apply replacements safely
for old, new in replacements:
    new_sql = new_sql.replace(old, new)

# write modified_inserts.sql (full SQL but with INSERTs having column lists when possible)
out.write_text('-- Modified INSERTs with explicit column lists\n-- Generated from tahonaOriginal.sql\n\n' + new_sql, encoding='utf-8')
print(f'Wrote {out}')

# extract only INSERT statements for a clean import file
insert_only_pattern = re.compile(r"INSERT\s+INTO\s+`[^`]+`.*?;", re.S | re.I)
inserts = insert_only_pattern.findall(new_sql)

header = [
    '-- Only INSERT statements extracted from tahonaOriginal.sql',
    'SET NAMES utf8mb4;',
    'SET FOREIGN_KEY_CHECKS=0;'
]
footer = ['SET FOREIGN_KEY_CHECKS=1;']

out_only.write_text('\n'.join(header) + '\n\n' + '\n\n'.join(inserts) + '\n\n' + '\n'.join(footer), encoding='utf-8')
print(f'Wrote {out_only} with {len(inserts)} INSERT statements')
