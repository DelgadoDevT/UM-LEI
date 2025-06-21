import csv
import json
import os
import psycopg2
import mysql.connector
from dotenv import load_dotenv

load_dotenv()

# ----------- CONFIGURATION -------------
POSTGRES_CONFIG = {
  'host': os.getenv('PG_HOST'),
  'port': int(os.getenv('PG_PORT', 5432)),
  'database': os.getenv('PG_DB'),
  'user': os.getenv('PG_USER'),
  'password': os.getenv('PG_PASSWORD')
}

MYSQL_CONFIG = {
  'host': os.getenv('MYSQL_HOST'),
  'port': int(os.getenv('MYSQL_PORT', 3306)),
  'database': os.getenv('MYSQL_DB'),
  'user': os.getenv('MYSQL_USER'),
  'password': os.getenv('MYSQL_PASSWORD')
}

CSV_DIR = 'populate_output/csv'
JSON_DIR = 'populate_output/json'

TABLES = {
  'stand': ['id_stand', 'nome', 'contacto', 'rua', 'cidade'],
  'cliente': ['id_cliente', 'nome', 'nif', 'nic', 'contacto', 'rua', 'pais', 'cidade'],
  'licencas_cliente': ['id_licenca', 'nome', 'id_cliente'],
  'veiculo': ['id_veiculo', 'marca', 'modelo', 'ano', 'matricula', 'cor', 'tipo', 'quilometragem', 'preco', 'obs', 'id_stand'],
  'funcionario': ['id_funcionario', 'contacto', 'cargo', 'horario', 'nome', 'salario', 'nif', 'nss', 'rua', 'pais', 'cidade', 'id_stand'],
  'licencas_funcionario': ['id_licenca', 'nome', 'id_funcionario'],
  'aluguer': ['id_aluguer', 'data_inicio', 'data_termino', 'estado', 'obs', 'id_cliente', 'id_veiculo', 'id_funcionario'],
  'fatura': ['id_aluguer', 'estado', 'data_emissao', 'valor', 'met_pagamento']
}
# ---------------------------------------


def read_from_postgres(table):
  conn = psycopg2.connect(**POSTGRES_CONFIG, options='-c search_path=jbvmotors')
  cur = conn.cursor()
  cur.execute(f"SELECT * FROM {table}")
  columns = [desc[0] for desc in cur.description]
  rows = [dict(zip(columns, row)) for row in cur.fetchall()]
  cur.close()
  conn.close()
  return rows


def read_from_csv(table):
  path = os.path.join(CSV_DIR, f"{table}.csv")
  with open(path, newline='', encoding='utf-8') as f:
    reader = csv.DictReader(f)
    return [row for row in reader]


def read_from_json(table):
  path = os.path.join(JSON_DIR, f"{table}.json")
  with open(path, encoding='utf-8') as f:
    return json.load(f)


def upsert_mysql(table, data, columns):
  conn = mysql.connector.connect(**MYSQL_CONFIG)
  cur = conn.cursor()

  for row in data:
    placeholders = ', '.join(['%s'] * len(columns))
    updates = ', '.join([f"{col}=VALUES({col})" for col in columns if col != 'id'])
    sql = f"""
      INSERT INTO {table} ({', '.join(columns)})
      VALUES ({placeholders})
      ON DUPLICATE KEY UPDATE {updates}
    """
    cur.execute(sql, [row[col] for col in columns])

  conn.commit()
  cur.close()
  conn.close()


def migrate_from(source: str):
  for table, columns in TABLES.items():
    print(f"Migrating table '{table}' from {source}...")

    if source == 'postgres':
      data = read_from_postgres(table)
    elif source == 'csv':
      data = read_from_csv(table)
    elif source == 'json':
      data = read_from_json(table)
    else:
      raise ValueError(f"Unknown source: {source}")

    upsert_mysql(table, data, columns)
    print(f"Table '{table}' migrated successfully from {source} to MySQL.")

if __name__ == '__main__':
  import sys
  if len(sys.argv) < 2 or sys.argv[1] not in ['postgres', 'csv', 'json', 'all']:
    print("Usage: python migrate.py [postgres|csv|json|all]")
    exit(1)

  mode = sys.argv[1]
  if mode == 'all':
    for src in ['postgres', 'csv', 'json']:
      migrate_from(src)
  else:
    migrate_from(mode)
