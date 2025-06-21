import argparse
import random
import string
import os
import csv
import json
from faker import Faker
from faker.providers import BaseProvider
from faker_vehicle import VehicleProvider
from collections import OrderedDict
from datetime import datetime

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

class CustomProvider(BaseProvider):
  def phone_number(self):
    prefix = random.choice(['91', '92', '93', '96'])
    number = ''.join(str(random.randint(0, 9)) for _ in range(7))
    return f'{prefix}{number}'

  def license_plate(self):
    def letters(n): return ''.join(random.choices(string.ascii_uppercase, k=n))
    def digits(n): return ''.join(random.choices('0123456789', k=n))
    return f"{letters(2)}{digits(2)}{letters(2)}"
  
  def ssn(self):
    return ''.join(random.choices(string.digits, k=9))

fake = Faker()
fake.add_provider(CustomProvider)
fake.add_provider(VehicleProvider)

allowed_licenca_types = ['AM','A1','A2','A','B','B1','C','C1','D','D1']

stands = []
clientes = []
clientes_licencas = []
veiculos = []
funcionarios = []
funcionarios_licencas = []
alugueres = []
faturas = []

def populate_vars(n_clientes: int, n_licencas_client: int,
  n_veiculos: int, n_funcionarios: int,
  n_licencas_func: int, n_alugueres: int):

  stands.extend([
    (1, 'JBV Motors Braga', fake.phone_number(), 'Rua de S. Vicente', 'Braga'),
    (2, 'JBV Motors Maia', fake.phone_number(), 'Rua do Barreiro', 'Porto'),
    (3, 'JBV Motors Viana', fake.phone_number(), 'Rua do Bispo', 'Viana do Castelo'),
  ])

  lic_id = 1
  for i in range(1, n_clientes + 1):
    cliente = (
      i,
      fake.name(),
      fake.ssn(),
      fake.ssn(),
      fake.phone_number(),
      fake.street_name(),
      fake.country_code(),
      fake.city()
    )
    clientes.append(cliente)

    count = random.randint(1, min(n_licencas_client, len(allowed_licenca_types)))
    for lic in random.sample(allowed_licenca_types, count):
      clientes_licencas.append((lic_id, lic, i))
      lic_id += 1

  for i in range(1, n_veiculos + 1):
    veiculo = (
      i,
      fake.vehicle_make(),
      fake.vehicle_model(),
      random.randint(2000, 2025),
      fake.license_plate().replace(' ', ''),
      fake.color_name(),
      random.choice(['ciclomotor', 'carro', 'moto', 'camiao', 'autocarro']),
      random.randint(0, 200000),
      random.randint(1000, 50000),
      fake.sentence(),
      random.choice([1,2,3])
    )
    veiculos.append(veiculo)

  lic_id = 1
  for i in range(1, n_funcionarios + 1):
    funcionario = (
      i,
      fake.phone_number(),
      random.choice(['vendedor','mecanico','gerente']),
      random.choice(['0-8','8-16','16-24']),
      fake.name(),
      random.randint(1000,5000),
      fake.ssn(),
      fake.ssn(),
      fake.street_name(),
      fake.country_code(),
      fake.city(),
      random.choice([1,2,3])
    )
    funcionarios.append(funcionario)

    count = random.randint(1, min(n_licencas_func, len(allowed_licenca_types)))
    for lic in random.sample(allowed_licenca_types, count):
      funcionarios_licencas.append((lic_id, lic, i))
      lic_id += 1

  veiculos_dict = {v[0]: v for v in veiculos}

  for i in range(1, n_alugueres + 1):
    data_inicio = fake.date_time_this_year(before_now=True)
    data_termino = fake.date_time_between(start_date=data_inicio, end_date='+30d')
    estado = random.choice(['pendente','finalizado','cancelado'])
    aluguer = (
      i,
      data_inicio,
      data_termino,
      estado,
      fake.sentence(),
      random.choice([c[0] for c in clientes]),
      random.choice(list(veiculos_dict.keys())),
      random.choice([f[0] for f in funcionarios])
    )
    alugueres.append(aluguer)

    if estado == 'finalizado':
      dias = max((data_termino - data_inicio).days, 1)
      preco = veiculos_dict[aluguer[6]][8]
      faturas.append((
        i,
        random.choice(['pago','pendente']),
        data_inicio,
        round(preco * dias, 2),
        random.choice(['dinheiro','cartao','transferencia'])
      ))

def write_sql_file(filename: str, dialect: str):
  with open(filename, 'w', encoding='utf-8') as f:
    def w(stmt, vals=None):
      if vals is None:
        f.write(stmt + '\n')
      else:
        f.write(stmt.format(*vals) + '\n')

    if dialect == "mysql":
      w("USE jbvmotors;")
    elif dialect == "postgres":
      w("\\c jbvmotors;")

    for r in stands:
      w('INSERT INTO stand VALUES ({}, \'{}\', \'{}\', \'{}\', \'{}\');', r)
    for r in clientes:
      w('INSERT INTO cliente VALUES ({}, \'{}\', \'{}\', \'{}\', \'{}\', \'{}\', \'{}\', \'{}\');', r)
    for r in clientes_licencas:
      w('INSERT INTO licencas_cliente VALUES ({}, \'{}\', {});', r)
    for r in veiculos:
      w('INSERT INTO veiculo VALUES ({}, \'{}\', \'{}\', {}, \'{}\', \'{}\', \'{}\', {}, {}, \'{}\', {});', r)
    for r in funcionarios:
      w('INSERT INTO funcionario VALUES ({}, \'{}\', \'{}\', \'{}\', \'{}\', {}, \'{}\', \'{}\', \'{}\', \'{}\', \'{}\', {});', r)
    for r in funcionarios_licencas:
      w('INSERT INTO licencas_funcionario VALUES ({}, \'{}\', {});', r)
    for r in alugueres:
      w('INSERT INTO aluguer VALUES ({}, \'{}\', \'{}\', \'{}\', \'{}\', {}, {}, {});', r)
    for r in faturas:
      w('INSERT INTO fatura VALUES ({}, \'{}\', \'{}\', {}, \'{}\');', r)

def write_csv_files(output_dir):
  table_map = OrderedDict([
    ('stand', (TABLES['stand'], stands)),
    ('cliente', (TABLES['cliente'], clientes)),
    ('licencas_cliente', (TABLES['licencas_cliente'], clientes_licencas)),
    ('veiculo', (TABLES['veiculo'], veiculos)),
    ('funcionario', (TABLES['funcionario'], funcionarios)),
    ('licencas_funcionario', (TABLES['licencas_funcionario'], funcionarios_licencas)),
    ('aluguer', (TABLES['aluguer'], alugueres)),
    ('fatura', (TABLES['fatura'], faturas)),
  ])

  os.makedirs(output_dir, exist_ok=True)

  for name, (headers, rows) in table_map.items():
    with open(os.path.join(output_dir, f"{name}.csv"), 'w', newline='', encoding='utf-8') as f:
      writer = csv.writer(f)
      writer.writerow(headers)
      writer.writerows(rows)

def write_json_files(output_dir):
  def serialize_row(headers, row):
    return {
      header: (value.isoformat() if isinstance(value, datetime) else value)
      for header, value in zip(headers, row)
    }

  table_map = OrderedDict([
    ('stand', (TABLES['stand'], stands)),
    ('cliente', (TABLES['cliente'], clientes)),
    ('licencas_cliente', (TABLES['licencas_cliente'], clientes_licencas)),
    ('veiculo', (TABLES['veiculo'], veiculos)),
    ('funcionario', (TABLES['funcionario'], funcionarios)),
    ('licencas_funcionario', (TABLES['licencas_funcionario'], funcionarios_licencas)),
    ('aluguer', (TABLES['aluguer'], alugueres)),
    ('fatura', (TABLES['fatura'], faturas)),
  ])

  os.makedirs(output_dir, exist_ok=True)

  for name, (headers, rows) in table_map.items():
    data = [serialize_row(headers, row) for row in rows]
    with open(os.path.join(output_dir, f"{name}.json"), 'w', encoding='utf-8') as f:
      json.dump(data, f, indent=2, ensure_ascii=False)

def parse_args():
  p = argparse.ArgumentParser(description='Generate fake data SQL, CSV, or JSON')
  p.add_argument('--clientes', type=int, default=20)
  p.add_argument('--licencas-cliente', type=int, default=2)
  p.add_argument('--veiculos', type=int, default=30)
  p.add_argument('--funcionarios', type=int, default=8)
  p.add_argument('--licencas-funcionario', type=int, default=3)
  p.add_argument('--alugueres', type=int, default=100)
  p.add_argument("--seed", type=int, help="Optional random seed for reproducible results.")
  p.add_argument('--mysql', action='store_true', help='Generate MySQL SQL script')
  p.add_argument('--postgres', action='store_true', help='Generate PostgreSQL SQL script')
  p.add_argument('--csv', action='store_true', help='Generate CSV files')
  p.add_argument('--json', action='store_true', help='Generate JSON files')
  p.add_argument('--all', action='store_true', help='Generate all outputs')
  return p.parse_args()

if __name__ == '__main__':
  args = parse_args()

  if args.seed is not None:
    random.seed(args.seed)
    Faker.seed(args.seed)

  populate_vars(
    args.clientes,
    args.licencas_cliente,
    args.veiculos,
    args.funcionarios,
    args.licencas_funcionario,
    args.alugueres
  )

  os.makedirs('populate_output', exist_ok=True)

  if args.all or args.mysql:
    write_sql_file('populate_output/populate_mysql.sql', dialect='mysql')
    print("✅ MySQL SQL script written.")

  if args.all or args.postgres:
    write_sql_file('populate_output/populate_postgres.sql', dialect='postgres')
    print("✅ PostgreSQL SQL script written.")

  if args.all or args.csv:
    write_csv_files('populate_output/csv')
    print("✅ CSV files written.")

  if args.all or args.json:
    write_json_files('populate_output/json')
    print("✅ JSON files written.")
