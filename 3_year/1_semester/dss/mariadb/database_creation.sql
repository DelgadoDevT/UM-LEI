-- =============================================================
-- SCRIPT 01: CRIAÇÃO DA BASE DE DADOS E ESTRUTURA
-- =============================================================
-- Atualizado em: 26/12/2025
-- =============================================================

DROP DATABASE IF EXISTS cadeiaRestaurantes;

CREATE DATABASE IF NOT EXISTS cadeiaRestaurantes;

USE cadeiaRestaurantes;

-- =============================================================
-- UTILIZADOR
-- =============================================================

DROP USER IF EXISTS 'funcionario'@'localhost';

CREATE USER IF NOT EXISTS 'funcionario'@'localhost' IDENTIFIED BY '';

GRANT ALL PRIVILEGES ON cadeiaRestaurantes.* TO 'funcionario'@'localhost';

FLUSH PRIVILEGES;

-- =============================================================
-- TABELAS PRINCIPAIS
-- =============================================================

CREATE TABLE IF NOT EXISTS restaurante (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    localizacao VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS posto (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tipo VARCHAR(50) NOT NULL,
    id_restaurante INT,
    FOREIGN KEY (id_restaurante) REFERENCES restaurante(id)
);

CREATE TABLE IF NOT EXISTS mensagem (
    id INT PRIMARY KEY AUTO_INCREMENT,
    id_posto INT NOT NULL,
    texto TEXT NOT NULL,
    data_hora TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (id_posto) REFERENCES posto(id)
);

CREATE TABLE IF NOT EXISTS ingrediente (
    nome VARCHAR(50) NOT NULL PRIMARY KEY,
    preco DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS stock (
    id_restaurante INT,
    nome_ingrediente VARCHAR(50),
    quantidade INT NOT NULL,
    PRIMARY KEY (id_restaurante, nome_ingrediente),
    FOREIGN KEY (id_restaurante) REFERENCES restaurante(id),
    FOREIGN KEY (nome_ingrediente) REFERENCES ingrediente(nome)
);

-- =============================================================
-- FUNCIONÁRIOS
-- =============================================================

CREATE TABLE IF NOT EXISTS funcionario (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    nif INT NOT NULL,
    id_posto INT,
    FOREIGN KEY (id_posto) REFERENCES posto(id)
);

CREATE TABLE IF NOT EXISTS atendente (
    id INT PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES funcionario(id)
);

CREATE TABLE IF NOT EXISTS cozinheiro (
    id INT PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES funcionario(id)
);

CREATE TABLE IF NOT EXISTS gestor (
    id INT PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES funcionario(id)
);

-- =============================================================
-- INDICADORES
-- =============================================================

CREATE TABLE IF NOT EXISTS indicador (
    id INT PRIMARY KEY AUTO_INCREMENT,
    data DATE NOT NULL,
    faturacaoTotal DOUBLE DEFAULT 0,
    totalPedidos INT DEFAULT 0,
    tempoMedioPreparacao INT DEFAULT 0,
    id_restaurante INT NOT NULL,
    FOREIGN KEY (id_restaurante) REFERENCES restaurante(id) ON DELETE CASCADE
);

-- =============================================================
-- PRODUTOS E MENUS
-- =============================================================

CREATE TABLE IF NOT EXISTS produto (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    preco DECIMAL(10,2) NOT NULL,
    tamanho VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS produto_ingredientes (
    id_produto INT NOT NULL,
    nome_ingrediente VARCHAR(50) NOT NULL,
    PRIMARY KEY (id_produto, nome_ingrediente),
    FOREIGN KEY (id_produto) REFERENCES produto(id) ON DELETE CASCADE,
    FOREIGN KEY (nome_ingrediente) REFERENCES ingrediente(nome) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS menu (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    preco DECIMAL(10,2),
    categoria VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS menu_produtos (
    id_menu INT NOT NULL,
    id_produto INT NOT NULL,
    PRIMARY KEY (id_menu, id_produto),
    FOREIGN KEY (id_menu) REFERENCES menu(id) ON DELETE CASCADE,
    FOREIGN KEY (id_produto) REFERENCES produto(id) ON DELETE CASCADE
);

-- =============================================================
-- PEDIDOS (Alinhado com PedidoDAO.java)
-- =============================================================

CREATE TABLE IF NOT EXISTS pedido (
    id INT PRIMARY KEY AUTO_INCREMENT,
    estado VARCHAR(50) NOT NULL,
    tipo_consumo VARCHAR(50) NOT NULL,
    id_restaurante INT NOT NULL,
    valor_total DECIMAL(10,2) DEFAULT 0.0,
    FOREIGN KEY (id_restaurante) REFERENCES restaurante(id)
);

CREATE TABLE IF NOT EXISTS linha_pedido (
    id INT PRIMARY KEY AUTO_INCREMENT,
    id_pedido INT NOT NULL,
    quantidade INT NOT NULL,
    preco_venda DECIMAL(10,2) NOT NULL,
    id_artigo VARCHAR(50) NOT NULL,
    tipo_artigo VARCHAR(20) NOT NULL,
    nota TEXT,
    ingredientes_personalizados TEXT,
    FOREIGN KEY (id_pedido) REFERENCES pedido(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS pagamento (
    id INT PRIMARY KEY AUTO_INCREMENT,
    id_pedido INT NOT NULL UNIQUE,
    valor DECIMAL(10,2) NOT NULL,
    metodo VARCHAR(30) NOT NULL,
    detalhes VARCHAR(255),
    FOREIGN KEY (id_pedido) REFERENCES pedido(id) ON DELETE CASCADE
);

-- =============================================================
-- FIM DO SCRIPT
-- =============================================================

