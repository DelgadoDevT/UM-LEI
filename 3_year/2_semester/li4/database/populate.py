# -*- coding: utf-8 -*-

import os
import csv
import secrets
from faker import Faker
from datetime import datetime, timedelta
from decimal import Decimal, ROUND_HALF_UP
import random
import argparse

# =======================================================================================
# CONFIGURAÇÃO
# =======================================================================================

fake = Faker('pt_PT')
Faker.seed(42) 
random.seed(42)

# Volumes de Dados Controlados (Total global abaixo de 10.000 linhas)
NUM_PRODUTOS = 100
NUM_CATEGORIAS = 10
NUM_FORNECEDORES = 15
NUM_VENDAS_PER_LOJA = 90

# Identificador estável do dono — partilhado entre central e cada loja para garantir
# que a mesma identidade (dono@taki.pt) é reconhecível em todos os nós.
DONO_ID = "00000000-0000-0000-0000-000000000001"
DONO_NOME = "Manuel Pinto da Costa"
DONO_EMAIL = "dono@taki.pt"

OUTPUT_DIR = os.path.join(os.path.dirname(__file__), '..', 'population_output')
MONEY_COLUMNS = {"preco_custo", "preco_venda", "preco", "subtotal", "imposto", "total", "valor", "troco"}

# =======================================================================================
# DADOS MESTRE
# =======================================================================================

LOJAS_DATA = [
    {"id_loja": 0, "nome": "Taki Sede Central", "telefone": "210000000", "email": "sede@taki.pt", "nif": "500000000", "rua": "Avenida Central", "cidade": "Lisboa", "distrito": "Lisboa"},
    {"id_loja": 1, "nome": "Taki Famalicão", "telefone": "252000111", "email": "famalicao@taki.pt", "nif": "510111222", "rua": "Rua da Estação", "cidade": "Vila Nova de Famalicão", "distrito": "Braga"},
    {"id_loja": 2, "nome": "Taki Felgueiras", "telefone": "255000222", "email": "felgueiras@taki.pt", "nif": "510222333", "rua": "Avenida da República", "cidade": "Felgueiras", "distrito": "Porto"},
    {"id_loja": 3, "nome": "Taki Madeira", "telefone": "291000333", "email": "madeira@taki.pt", "nif": "510333444", "rua": "Avenida do Mar", "cidade": "Funchal", "distrito": "Madeira"},
    {"id_loja": 4, "nome": "Taki Cabeceiras", "telefone": "253000444", "email": "cabeceiras@taki.pt", "nif": "510444555", "rua": "Praça da Liberdade", "cidade": "Cabeceiras de Basto", "distrito": "Braga"},
]

SCHEMA_COLS = {
    "loja": ["id_loja", "nome", "telefone", "email", "nif", "rua", "cidade", "distrito"],
    "categoria": ["id_categoria", "designacao", "estado", "id_categoria_pai"],
    "produto": ["id_produto", "codigo_barras", "nome", "descricao", "preco_custo", "preco_venda", "unidade_medida", "taxa_iva", "estado"],
    "fornecedor": ["id_fornecedor", "nome", "nif", "telefone", "email", "estado"],
    "produto_fornecedor": ["id_produto", "id_fornecedor", "preco_custo", "preferencial"],
    "produto_categoria": ["id_produto", "id_categoria"],
    "funcionario": ["id_funcionario", "nome", "email", "cargo", "password_hash", "estado", "id_loja"],
    "inventario": ["id_inventario", "quantidade", "quantidade_minima", "id_loja", "id_produto"],
    "movimento_inventario": ["id_movimento", "tipo", "quantidade", "data_registo", "motivo", "id_inventario", "id_funcionario"],
    "venda": ["id_venda", "data_hora", "subtotal", "imposto", "total", "estado", "id_loja", "id_funcionario"],
    "linha_venda": ["id_linha_venda", "quantidade", "preco", "imposto", "subtotal", "id_venda", "id_produto"],
    "pagamento": ["id_pagamento", "metodo", "valor", "troco", "id_venda"],
    "fatura": ["id_fatura", "num_fatura", "data_emissao", "nif_cliente", "hash", "hash_control", "id_venda"],
    "encomenda": ["id_encomenda", "data_criacao", "data_entrega", "estado", "id_loja", "id_fornecedor"],
    "linha_encomenda": ["id_linha_encomenda", "quantidade", "preco", "id_encomenda", "id_produto"],
    "devolucao": ["id_devolucao", "data_hora", "valor", "metodo_reembolso", "num_nota_credito", "id_venda", "id_funcionario"],
    "linha_devolucao": ["id_linha_devolucao", "quantidade", "valor", "id_devolucao", "id_linha_venda"],
    "promocao": ["id_promocao", "designacao", "desconto", "data_inicio", "data_fim", "estado", "id_loja"],
    "promocao_produto": ["id_promocao", "id_produto"],
}

GESTOR_LOJA_DATA = {
    1: {"nome": "Tiago Rocha", "email": "tiago.rocha@taki.pt"},
    2: {"nome": "Sofia Alves", "email": "sofia.alves@taki.pt"},
    3: {"nome": "Carolina Jesus", "email": "carolina.jesus@taki.pt"},
    4: {"nome": "David Marques", "email": "david.marques@taki.pt"}
}

OPERADOR_LOJA_DATA = {
    1: {"nome": "Operador Loja 1", "email": "operador1@taki.pt"},
    2: {"nome": "Operador Loja 2", "email": "operador2@taki.pt"},
    3: {"nome": "Operador Loja 3", "email": "operador3@taki.pt"},
    4: {"nome": "Operador Loja 4", "email": "operador4@taki.pt"},
}

# =======================================================================================
# HELPERS
# =======================================================================================

def generate_phone():
    return f"{random.choice([2, 9])}{random.randint(10000000, 99999999)}"

def hash_pass():
    return "$2a$12$46ZZgvXTaEgtx.9JWHV3n.CakhCKJlpUZkzSEiugEecdKZ0pF8suO"

def money(v):
    return float(Decimal(str(v)).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP))

def escape_sql(v, col=None):
    if v is None: return "NULL"
    if isinstance(v, bool): return "TRUE" if v else "FALSE"
    if isinstance(v, int): return str(v)
    if isinstance(v, float):
        if col in MONEY_COLUMNS:
            return f"{money(v):.2f}"
        return str(v)
    return "'" + str(v).replace("'", "''") + "'"

def generate_realistic_datetime(rng=None, max_days_ago=180):
    r = rng or random
    # Distribuição enviesada para os últimos 14 dias (~50% das vendas), sobra distribuída até max_days_ago.
    if r.random() < 0.5:
        days_ago = r.randint(0, 13)
    else:
        days_ago = r.randint(14, max_days_ago)
    date = datetime.now() - timedelta(days=days_ago)
    hour = r.randint(8, 19)
    minute = r.randint(0, 59)
    second = r.randint(0, 59)
    return date.replace(hour=hour, minute=minute, second=second, microsecond=0)


def generate_today_datetime(rng=None, hours_offset=None):
    r = rng or random
    now = datetime.now()
    hour = r.randint(8, max(8, now.hour)) if hours_offset is None else max(8, min(19, now.hour - hours_offset))
    minute = r.randint(0, 59)
    second = r.randint(0, 59)
    return now.replace(hour=hour, minute=minute, second=second, microsecond=0)

# =======================================================================================
# GERADORES
# =======================================================================================

def generate_common_data():
    print("A gerar dados comuns...")
    
    cat_names = ["Bebidas", "Laticínios", "Mercearia", "Higiene", "Limpeza", "Congelados", "Padaria", "Snacks", "Animais", "Frescos"]
    categorias = [{'id_categoria': fake.uuid4(), 'designacao': name, 'estado': 'Ativa', 'id_categoria_pai': None} for name in cat_names]
    
    produtos = []
    prod_cat = []
    prefixes = ["Água", "Leite", "Sumo", "Arroz", "Massa", "Feijão", "Azeite", "Sabonete", "Champô", "Detergente"]
    suffixes = ["Premium", "Económico", "Bio", "XL", "Familiar", "Extra", "Suave", "Forte"]
    
    # Preços de custo realistas em incrementos de 0.05€ (preços comuns em retalho)
    custo_buckets = [0.50, 0.65, 0.85, 0.99, 1.20, 1.49, 1.79, 1.99, 2.49, 2.99,
                     3.49, 3.99, 4.49, 4.99, 5.99, 6.99, 7.99, 8.99, 9.99,
                     11.99, 13.99, 15.99, 17.99, 19.99]
    margens = [1.35, 1.40, 1.45, 1.50, 1.55, 1.60]
    for i in range(NUM_PRODUTOS):
        name = f"{random.choice(prefixes)} {random.choice(suffixes)} {i+1}"
        p_id = fake.uuid4()
        custo = random.choice(custo_buckets)
        preco_venda = money(custo * random.choice(margens))
        produtos.append({
            'id_produto': p_id, 'codigo_barras': fake.unique.ean13(), 'nome': name,
            'descricao': f"Desc {name}", 'preco_custo': custo, 'preco_venda': preco_venda,
            'unidade_medida': 'unidade', 'taxa_iva': random.choice([0.06, 0.13, 0.23]), 'estado': 'Ativo'
        })
        prod_cat.append({'id_produto': p_id, 'id_categoria': random.choice(categorias)['id_categoria']})

    fornecedores = []
    for _ in range(NUM_FORNECEDORES):
        fornecedores.append({
            'id_fornecedor': fake.uuid4(), 'nome': fake.company(), 
            'nif': str(fake.unique.random_int(min=500000000, max=599999999)),
            'telefone': generate_phone(), 'email': fake.unique.company_email(), 'estado': 'Ativo'
        })
    
    prod_forn = []
    for p in produtos:
        f = random.choice(fornecedores)
        prod_forn.append({'id_produto': p['id_produto'], 'id_fornecedor': f['id_fornecedor'], 'preco_custo': p['preco_custo'], 'preferencial': True})

    return {
        'loja': LOJAS_DATA, 'categoria': categorias, 'produto': produtos, 'fornecedor': fornecedores, 
        'produto_fornecedor': prod_forn, 'produto_categoria': prod_cat
    }

def generate_store_specific_data(common_data, target_loja_id, caixa_id):
    print(f"  -> A gerar dados específicos para Loja {target_loja_id}")
    rng = random.Random(1000 + target_loja_id)
    
    funcionarios = [] 
    inventario = []
    alerts = 0
    produtos = common_data['produto']

    if target_loja_id == 1:
        subset = produtos[:70] + produtos[80:90]
    elif target_loja_id == 2:
        subset = produtos[20:95]
    elif target_loja_id == 3:
        subset = produtos[:40] + produtos[60:]
    else:
        subset = rng.sample(produtos, 65)

    # 3 produtos com stock baixo em TODAS as lojas (visíveis no agregado central)
    globally_low_ids = {produtos[0]['id_produto'], produtos[7]['id_produto'], produtos[15]['id_produto']}
    # Garantir que estão presentes no subset
    subset_ids = {p['id_produto'] for p in subset}
    for prod in produtos:
        if prod['id_produto'] in globally_low_ids and prod['id_produto'] not in subset_ids:
            subset.append(prod)
            subset_ids.add(prod['id_produto'])

    # Limites mínimos discretos e realistas (stocks de segurança típicos)
    min_levels = [5, 10, 15, 20, 25]
    for p in subset:
        if p['id_produto'] in globally_low_ids:
            # Alerta global: stock muito baixo em TODAS as lojas (demo do agregado)
            qm = rng.choice([15, 20, 25])
            q = rng.randint(0, 2)
        elif alerts < 7 and rng.random() < 0.14:
            # Alerta local: stock abaixo do mínimo nesta loja
            qm = rng.choice([10, 15, 20])
            q = rng.randint(1, qm - 2)
            alerts += 1
        else:
            qm = rng.choice(min_levels)
            # Stock saudável: pelo menos 2x o mínimo, em incrementos de 5
            base_min = max(qm * 2, 20 + target_loja_id * 5)
            base_max = base_min + 60 + target_loja_id * 10
            q = rng.randrange(base_min, base_max + 1, 5)
        inventario.append({'id_inventario': fake.uuid4(), 'quantidade': float(q), 'quantidade_minima': float(qm), 'id_loja': target_loja_id, 'id_produto': p['id_produto']})

    stock_virtual = {inv['id_inventario']: inv['quantidade'] for inv in inventario}

    vendas, linhas, pags, fats, movs = [], [], [], [], []
    devolucoes, linhas_dev = [], []
    op_id = caixa_id

    num_vendas = NUM_VENDAS_PER_LOJA + (target_loja_id * 16)
    # Garantir presença diária recente: primeiras 8 vendas hoje, próximas 8 ontem.
    forced_today = 8
    forced_yesterday = 8
    for i in range(num_vendas):
        v_id = fake.uuid4()
        if i < forced_today:
            v_dt = generate_today_datetime(rng)
        elif i < forced_today + forced_yesterday:
            base = datetime.now() - timedelta(days=1)
            v_dt = base.replace(hour=rng.randint(8, 19), minute=rng.randint(0, 59), second=rng.randint(0, 59), microsecond=0)
        else:
            v_dt = generate_realistic_datetime(rng)

        # Múltiplas linhas por venda (1-5 produtos distintos), distribuição realista
        num_linhas_alvo = rng.choices([1, 2, 3, 4, 5], weights=[20, 30, 25, 15, 10], k=1)[0]
        invs_disponiveis = [iv for iv in inventario if stock_virtual[iv['id_inventario']] >= 1]
        if not invs_disponiveis:
            continue
        invs_escolhidos = rng.sample(invs_disponiveis, min(num_linhas_alvo, len(invs_disponiveis)))

        venda_subtotal = 0.0
        venda_imposto = 0.0
        venda_total = 0.0
        venda_linhas = []
        primeira_linha_id = None
        primeiro_produto = None
        for inv in invs_escolhidos:
            id_inventario = inv['id_inventario']
            p = next(prod for prod in produtos if prod['id_produto'] == inv['id_produto'])
            quantidade_vendida = rng.randint(1, 5)
            if stock_virtual[id_inventario] < quantidade_vendida:
                quantidade_vendida = int(max(1, stock_virtual[id_inventario]))

            ls = money(p['preco_venda'] * quantidade_vendida)
            li = money(ls * p['taxa_iva'])
            total_linha = money(ls + li)

            id_lv = fake.uuid4()
            if primeira_linha_id is None:
                primeira_linha_id = id_lv
                primeiro_produto = p
            venda_linhas.append({'id_linha_venda': id_lv, 'quantidade': float(quantidade_vendida), 'preco': p['preco_venda'], 'imposto': p['taxa_iva'], 'subtotal': total_linha, 'id_venda': v_id, 'id_produto': p['id_produto']})
            stock_virtual[id_inventario] -= quantidade_vendida

            venda_subtotal += ls
            venda_imposto += li
            venda_total += total_linha

        if venda_linhas:
            venda_subtotal = money(venda_subtotal)
            venda_imposto = money(venda_imposto)
            venda_total = money(venda_total)
            linhas.extend(venda_linhas)

            vendas.append({'id_venda': v_id, 'data_hora': v_dt.isoformat(), 'subtotal': venda_subtotal, 'imposto': venda_imposto, 'total': venda_total, 'estado': 'Concluída', 'id_loja': target_loja_id, 'id_funcionario': op_id})
            # Distribuição realista: cartão ~50%, numerário ~30%, MBWay ~20%
            metodo_pagamento = rng.choices(
                ['Cartão', 'Numerário', 'MBWay'],
                weights=[50, 30, 20],
                k=1
            )[0]
            troco = money(0)
            pags.append({'id_pagamento': fake.uuid4(), 'metodo': metodo_pagamento, 'valor': venda_total, 'troco': troco, 'id_venda': v_id})
            fats.append({'id_fatura': fake.uuid4(), 'num_fatura': f"FT {v_dt.year}/{target_loja_id}{i+1000}", 'data_emissao': v_dt.isoformat(), 'nif_cliente': None, 'hash': 'h', 'hash_control': '1', 'id_venda': v_id})

            # Devoluções: ~6% das vendas (parciais da primeira linha), reembolso método 'Original'
            if rng.random() < 0.06 and primeiro_produto is not None:
                primeira = next(l for l in venda_linhas if l['id_linha_venda'] == primeira_linha_id)
                qtd_dev = rng.randint(1, int(primeira['quantidade']))
                valor_dev = money(primeiro_produto['preco_venda'] * qtd_dev * (1 + primeiro_produto['taxa_iva']))
                id_dev = fake.uuid4()
                dev_dt = v_dt + timedelta(days=rng.randint(0, 5))
                if dev_dt > datetime.now():
                    dev_dt = datetime.now()
                num_nc = f"NC {v_dt.year}/{target_loja_id}{i+5000}"
                devolucoes.append({
                    'id_devolucao': id_dev,
                    'data_hora': dev_dt.isoformat(),
                    'valor': valor_dev,
                    'metodo_reembolso': 'Original',
                    'num_nota_credito': num_nc,
                    'id_venda': v_id,
                    'id_funcionario': op_id,
                })
                linhas_dev.append({
                    'id_linha_devolucao': fake.uuid4(),
                    'quantidade': float(qtd_dev),
                    'valor': valor_dev,
                    'id_devolucao': id_dev,
                    'id_linha_venda': primeira_linha_id,
                })

    encomendas, linhas_encomenda = generate_encomendas(common_data, target_loja_id, rng)
    promocoes, promocao_produto = generate_promocoes(common_data, target_loja_id, rng)

    return {
        'funcionario': funcionarios,
        'inventario': inventario,
        'movimento_inventario': movs,
        'venda': vendas,
        'linha_venda': linhas,
        'pagamento': pags,
        'fatura': fats,
        'devolucao': devolucoes,
        'linha_devolucao': linhas_dev,
        'encomenda': encomendas,
        'linha_encomenda': linhas_encomenda,
        'promocao': promocoes,
        'promocao_produto': promocao_produto,
    }


def generate_encomendas(common_data, target_loja_id, rng):
    fornecedores = common_data['fornecedor']
    produtos = common_data['produto']
    encomendas = []
    linhas = []
    estados = ['Pendente', 'Em Trânsito', 'Entregue', 'Concluída', 'Cancelada']
    num_encomendas = 8 + (target_loja_id * 2)
    for _ in range(num_encomendas):
        id_enc = fake.uuid4()
        forn = rng.choice(fornecedores)
        data_criacao = datetime.now() - timedelta(days=rng.randint(0, 60))
        data_entrega = (data_criacao + timedelta(days=rng.randint(2, 14))).date()
        estado = rng.choice(estados)
        encomendas.append({
            'id_encomenda': id_enc,
            'data_criacao': data_criacao.isoformat(),
            'data_entrega': data_entrega.isoformat(),
            'estado': estado,
            'id_loja': target_loja_id,
            'id_fornecedor': forn['id_fornecedor'],
        })
        # Quantidades de encomenda em múltiplos de 10 (típico em B2B)
        for _ in range(rng.randint(1, 4)):
            p = rng.choice(produtos)
            qtd = rng.choice([10, 20, 30, 50, 75, 100, 150, 200])
            linhas.append({
                'id_linha_encomenda': fake.uuid4(),
                'quantidade': float(qtd),
                'preco': p['preco_custo'],
                'id_encomenda': id_enc,
                'id_produto': p['id_produto'],
            })
    return encomendas, linhas


def generate_promocoes(common_data, target_loja_id, rng):
    produtos = common_data['produto']
    promocoes = []
    promocao_produto = []
    designacoes = ['Desconto Verão', 'Promo Família', 'Black Friday', 'Liquidação', 'Especial Cliente', 'Fim de Semana']
    num_promos = 3
    for i in range(num_promos):
        id_promo = fake.uuid4()
        # Activa: começou ontem, termina daqui a 7-30 dias
        data_inicio = (datetime.now() - timedelta(days=1)).replace(hour=0, minute=0, second=0, microsecond=0)
        data_fim = (datetime.now() + timedelta(days=rng.randint(7, 30))).replace(hour=23, minute=59, second=59, microsecond=0)
        promocoes.append({
            'id_promocao': id_promo,
            'designacao': f"{rng.choice(designacoes)} L{target_loja_id}-{i+1}",
            'desconto': float(rng.choice([5, 10, 15, 20, 25, 30, 40, 50])),
            'data_inicio': data_inicio.isoformat(),
            'data_fim': data_fim.isoformat(),
            'estado': 'Ativa',
            'id_loja': target_loja_id,
        })
        # 3-6 produtos por promoção
        produtos_promo = rng.sample(produtos, min(len(produtos), rng.randint(3, 6)))
        for p in produtos_promo:
            promocao_produto.append({'id_promocao': id_promo, 'id_produto': p['id_produto']})
    return promocoes, promocao_produto

def write_sql_file(filename, all_data):
    sql_dir = os.path.join(OUTPUT_DIR, 'sql')
    os.makedirs(sql_dir, exist_ok=True)
    
    with open(os.path.join(sql_dir, filename), 'w', encoding='utf-8') as f:
        f.write("-- Script de População Gerado --\n")
        for table, rows in all_data.items():
            if not rows:
                continue
            f.write(f"\n-- Tabela: {table}\n")
            cols = SCHEMA_COLS.get(table)
            if not cols:
                print(f"AVISO: Schema não encontrado para a tabela '{table}'. A saltar.")
                continue
            
            for row in rows:
                vals = [escape_sql(row.get(c), c) for c in cols]
                f.write(f"INSERT INTO {table} ({', '.join(cols)}) VALUES ({', '.join(vals)});\n")

def main(target):
    print(f"A gerar dados para o alvo: '{target}'")
    
    common_data = generate_common_data()
    all_data_for_sql = {**common_data}
    
    if target == 'central':
        print("Modo Central: A gerar dados agregados de todas as lojas.")
        gestor_central = {
            'id_funcionario': DONO_ID,
            'nome': DONO_NOME,
            'email': DONO_EMAIL,
            'cargo': 'gestor_central',
            'password_hash': hash_pass(),
            'estado': 'Ativo',
            'id_loja': 0
        }

        all_funcionarios = [gestor_central]
        all_transactional_data = {t: [] for t in [
            'inventario', 'movimento_inventario', 'venda', 'linha_venda', 'pagamento', 'fatura',
            'devolucao', 'linha_devolucao', 'encomenda', 'linha_encomenda', 'promocao', 'promocao_produto'
        ]}

        for loja in LOJAS_DATA:
            loja_id = loja['id_loja']
            
            if loja_id != 0:
                gestor_nome = GESTOR_LOJA_DATA[loja_id]["nome"]
                gestor_email = GESTOR_LOJA_DATA[loja_id]["email"]
                caixa_nome = OPERADOR_LOJA_DATA[loja_id]["nome"]
                caixa_email = OPERADOR_LOJA_DATA[loja_id]["email"]
                
                gestor_id = fake.uuid4()
                caixa_id = fake.uuid4()

                all_funcionarios.append({'id_funcionario': gestor_id, 'nome': gestor_nome, 'email': gestor_email, 'cargo': 'gestor_loja', 'password_hash': hash_pass(), 'estado': 'Ativo', 'id_loja': loja_id})
                all_funcionarios.append({'id_funcionario': caixa_id, 'nome': caixa_nome, 'email': caixa_email, 'cargo': 'operador_caixa', 'password_hash': hash_pass(), 'estado': 'Ativo', 'id_loja': loja_id})
                
                store_data = generate_store_specific_data(common_data, loja_id, caixa_id) 
                for t in all_transactional_data:
                    all_transactional_data[t].extend(store_data.get(t, []))
        
        all_data_for_sql['funcionario'] = all_funcionarios
        all_data_for_sql.update(all_transactional_data)
        
        write_sql_file('populate_central.sql', all_data_for_sql)

    else: 
        try:
            target_id = int(target)
            if not any(l['id_loja'] == target_id for l in LOJAS_DATA):
                raise ValueError
        except (ValueError, TypeError):
            print(f"ERRO: --loja_id '{target}' é inválido. Use um ID de loja (0-4).")
            return
            
        print(f"Modo Local: A gerar dados apenas para a loja {target_id}.")
        
        all_funcionarios = []
        if target_id != 0:
            gestor_nome = GESTOR_LOJA_DATA[target_id]["nome"]
            gestor_email = GESTOR_LOJA_DATA[target_id]["email"]
            caixa_nome = OPERADOR_LOJA_DATA[target_id]["nome"]
            caixa_email = OPERADOR_LOJA_DATA[target_id]["email"]

            gestor_id = fake.uuid4()
            caixa_id = fake.uuid4()

            # Dono presente em cada loja para permitir criar gestores localmente. Usa um
            # ID estável partilhado com a central; como Funcionario não é sincronizado via
            # outbox, não há colisão na base central — e o ID consistente facilita uma
            # eventual reconciliação manual.
            all_funcionarios.append({'id_funcionario': DONO_ID, 'nome': DONO_NOME, 'email': DONO_EMAIL, 'cargo': 'gestor_central', 'password_hash': hash_pass(), 'estado': 'Ativo', 'id_loja': target_id})
            all_funcionarios.append({'id_funcionario': gestor_id, 'nome': gestor_nome, 'email': gestor_email, 'cargo': 'gestor_loja', 'password_hash': hash_pass(), 'estado': 'Ativo', 'id_loja': target_id})
            all_funcionarios.append({'id_funcionario': caixa_id, 'nome': caixa_nome, 'email': caixa_email, 'cargo': 'operador_caixa', 'password_hash': hash_pass(), 'estado': 'Ativo', 'id_loja': target_id})
            
            # ATUALIZAÇÃO CRUCIAL: Adicionar 'funcionario' ANTES das tabelas transacionais
            all_data_for_sql['funcionario'] = all_funcionarios
            
            store_data = generate_store_specific_data(common_data, target_id, caixa_id)
            all_data_for_sql.update({k: v for k, v in store_data.items() if k != 'funcionario'})
        else:
            all_data_for_sql['funcionario'] = all_funcionarios
        
        write_sql_file(f'populate_loja_{target_id}.sql', all_data_for_sql)

    print("PRONTO: Script populate.py finalizado e SQL gerado.")
    print(f"Ficheiro de output: {os.path.join(OUTPUT_DIR, 'sql')}")

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="Script de População para a base de dados Taki.")
    parser.add_argument(
        'target',
        nargs='?',
        help="Atalho opcional para o alvo (e.g., '1' ou 'central')."
    )
    parser.add_argument(
        '--loja_id', 
        type=str, 
        required=False,
        help="ID da loja para gerar dados (e.g., '1', '2') ou 'central' para a base de dados agregada."
    )
    args = parser.parse_args()
    alvo = args.loja_id or args.target
    if not alvo:
        parser.error("the following arguments are required: --loja_id (ou argumento posicional target)")
    main(alvo)
