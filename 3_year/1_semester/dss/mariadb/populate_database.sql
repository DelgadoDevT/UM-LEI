-- =============================================================
-- SCRIPT 02: POVOAMENTO DA BASE DE DADOS
-- =============================================================
-- Atualizado em: 26/12/2025
-- Dados de teste para demonstração do sistema
-- =============================================================

USE cadeiaRestaurantes;

-- =============================================================
-- 1. LIMPEZA
-- =============================================================

DELETE FROM pagamento;
DELETE FROM linha_pedido;
DELETE FROM pedido;
DELETE FROM mensagem;
DELETE FROM indicador;
DELETE FROM atendente;
DELETE FROM cozinheiro;
DELETE FROM gestor;
DELETE FROM funcionario;
DELETE FROM stock;
DELETE FROM posto;
DELETE FROM restaurante;
DELETE FROM menu_produtos;
DELETE FROM menu;
DELETE FROM produto_ingredientes;
DELETE FROM produto;
DELETE FROM ingrediente;

-- =============================================================
-- 2. RESET AUTO_INCREMENTS
-- =============================================================

ALTER TABLE mensagem AUTO_INCREMENT = 1;
ALTER TABLE indicador AUTO_INCREMENT = 1;
ALTER TABLE funcionario AUTO_INCREMENT = 1;
ALTER TABLE posto AUTO_INCREMENT = 1;
ALTER TABLE restaurante AUTO_INCREMENT = 1;
ALTER TABLE produto AUTO_INCREMENT = 1;
ALTER TABLE menu AUTO_INCREMENT = 1;
ALTER TABLE linha_pedido AUTO_INCREMENT = 1;
ALTER TABLE pedido AUTO_INCREMENT = 1;
ALTER TABLE pagamento AUTO_INCREMENT = 1;

-- =============================================================
-- 3. RESTAURANTES
-- =============================================================

INSERT INTO restaurante (nome, localizacao) VALUES
('Restaurante Centro', 'Centro da cidade'),
('Restaurante Norte', 'Zona Norte'),
('Restaurante Sul', 'Zona Sul');

-- =============================================================
-- 4. POSTOS DE TRABALHO
-- =============================================================

INSERT INTO posto (tipo, id_restaurante) VALUES
('BALCAO', 1),
('COZINHA', 1),
('GESTAO', 1),
('BALCAO', 2),
('COZINHA', 2),
('GESTAO', 2),
('BALCAO', 3),
('COZINHA', 3),
('GESTAO', 3);

-- =============================================================
-- 5. FUNCIONÁRIOS
-- =============================================================

INSERT INTO funcionario (nome, nif, id_posto) VALUES
-- Gestor Geral (sem posto específico)
('Admin Geral', 999999999, NULL),
-- Gestores dos Restaurantes
('João Gestor Centro', 111111111, 3),
('Maria Gestora Norte', 222222222, 6),
('Carlos Gestor Sul', 333333333, 9),
-- Cozinheiros
('António Cozinha Centro', 444444444, 2),
('Beatriz Chef Centro', 555555555, 2),
('Duarte Cozinha Norte', 666666666, 5),
('Eva Chef Norte', 777777777, 5),
('Francisco Cozinha Sul', 888888888, 8),
('Gabriela Chef Sul', 123456789, 8),
-- Atendentes
('Helena Atendente Centro', 234567890, 1),
('Ivo Atendente Centro', 345678901, 1),
('Joana Atendente Norte', 456789012, 4),
('Kiko Atendente Norte', 567890123, 4),
('Luísa Atendente Sul', 678901234, 7),
('Miguel Atendente Sul', 789012345, 7);

-- Tipos de Funcionários
INSERT INTO gestor (id) VALUES (1), (2), (3), (4);
INSERT INTO cozinheiro (id) VALUES (5), (6), (7), (8), (9), (10);
INSERT INTO atendente (id) VALUES (11), (12), (13), (14), (15), (16);

-- =============================================================
-- 6. INGREDIENTES
-- =============================================================

INSERT INTO ingrediente (nome, preco) VALUES
('Tomate', 0.50),
('Cebola', 0.30),
('Alho', 0.20),
('Azeite', 0.50),
('Sal', 0.10),
('Pimenta', 0.15),
('Arroz', 1.00),
('Feijão', 1.20),
('Carne Bovina', 2.00),
('Frango', 1.50),
('Peixe', 2.50),
('Batata', 0.40),
('Cenoura', 0.35),
('Alface', 0.40),
('Queijo', 0.70),
('Pão', 0.50),
('Pão Hambúrguer', 0.80),
('Bacon', 1.00),
('Ovo', 0.60);

-- =============================================================
-- 7. STOCK DOS RESTAURANTES
-- =============================================================

INSERT INTO stock (id_restaurante, nome_ingrediente, quantidade) VALUES
-- Restaurante Centro
(1, 'Tomate', 50),
(1, 'Cebola', 30),
(1, 'Alho', 20),
(1, 'Azeite', 10),
(1, 'Carne Bovina', 25),
(1, 'Frango', 20),
(1, 'Pão Hambúrguer', 100),
(1, 'Alface', 40),
(1, 'Queijo', 30),
(1, 'Bacon', 25),
-- Restaurante Norte
(2, 'Tomate', 40),
(2, 'Cebola', 25),
(2, 'Alho', 15),
(2, 'Azeite', 8),
(2, 'Peixe', 18),
(2, 'Batata', 35),
(2, 'Frango', 15),
(2, 'Queijo', 20),
-- Restaurante Sul
(3, 'Tomate', 35),
(3, 'Cebola', 20),
(3, 'Alho', 12),
(3, 'Azeite', 6),
(3, 'Frango', 15),
(3, 'Queijo', 20),
(3, 'Batata', 30),
(3, 'Arroz', 25);

-- =============================================================
-- 8. INDICADORES DE DESEMPENHO
-- =============================================================

INSERT INTO indicador (data, faturacaoTotal, totalPedidos, tempoMedioPreparacao, id_restaurante) VALUES
-- Restaurante Centro (últimos 30 dias)
(CURDATE() - INTERVAL 30 DAY, 1250.50, 45, 12, 1),
(CURDATE() - INTERVAL 25 DAY, 980.75, 38, 15, 1),
(CURDATE() - INTERVAL 20 DAY, 2100.00, 65, 10, 1),
(CURDATE() - INTERVAL 15 DAY, 1750.25, 52, 14, 1),
(CURDATE() - INTERVAL 10 DAY, 890.30, 32, 16, 1),
(CURDATE() - INTERVAL 5 DAY, 1450.60, 48, 11, 1),
(CURDATE() - INTERVAL 1 DAY, 1650.40, 55, 13, 1),
-- Restaurante Norte (últimos 30 dias)
(CURDATE() - INTERVAL 30 DAY, 980.25, 32, 18, 2),
(CURDATE() - INTERVAL 25 DAY, 750.50, 28, 20, 2),
(CURDATE() - INTERVAL 20 DAY, 1650.75, 52, 15, 2),
(CURDATE() - INTERVAL 15 DAY, 1200.30, 42, 17, 2),
(CURDATE() - INTERVAL 10 DAY, 850.60, 30, 19, 2),
(CURDATE() - INTERVAL 5 DAY, 1100.40, 38, 16, 2),
(CURDATE() - INTERVAL 1 DAY, 1350.20, 45, 14, 2),
-- Restaurante Sul (últimos 30 dias)
(CURDATE() - INTERVAL 30 DAY, 650.75, 25, 22, 3),
(CURDATE() - INTERVAL 25 DAY, 820.30, 30, 19, 3),
(CURDATE() - INTERVAL 20 DAY, 1450.60, 48, 16, 3),
(CURDATE() - INTERVAL 15 DAY, 1100.25, 40, 18, 3),
(CURDATE() - INTERVAL 10 DAY, 720.50, 26, 21, 3),
(CURDATE() - INTERVAL 5 DAY, 950.75, 35, 17, 3),
(CURDATE() - INTERVAL 1 DAY, 1250.40, 42, 15, 3);

-- =============================================================
-- 9. PRODUTOS
-- =============================================================


-- Inserir produtos
INSERT INTO produto (id, nome, preco, tamanho) VALUES
(1, 'Hambúrguer Clássico', 6.50, 'M'),
(2, 'Cheeseburger Duplo', 8.50, 'L'),
(3, 'Bitoque no Prato', 10.00, 'M'),
(4, 'Salada de Frango', 7.50, 'M'),
(5, 'Batatas Fritas', 2.50, 'M'),
(6, 'Refrigerante', 2.00, 'M'),
(7, 'Água 50cl', 1.50, 'M'),
(8, 'Sundae Baunilha', 3.00, 'M');

-- Associar ingredientes aos produtos
INSERT INTO produto_ingredientes (id_produto, nome_ingrediente) VALUES
-- Hambúrguer Clássico (ID: 1)
(1, 'Pão Hambúrguer'),
(1, 'Alface'),
(1, 'Carne Bovina'),
(1, 'Tomate'),
-- Cheeseburger Duplo (ID: 2)
(2, 'Pão Hambúrguer'),
(2, 'Carne Bovina'),
(2, 'Queijo'),
(2, 'Bacon'),
-- Bitoque (ID: 3)
(3, 'Arroz'),
(3, 'Batata'),
(3, 'Carne Bovina'),
(3, 'Ovo'),
-- Salada de Frango (ID: 4)
(4, 'Alface'),
(4, 'Tomate'),
(4, 'Cenoura'),
(4, 'Frango');

-- =============================================================
-- 10. MENUS
-- =============================================================


-- Inserir menus
INSERT INTO menu (id, nome, tipo, preco, categoria) VALUES
(9, 'Menu Estudante', 'PACK', 8.00, NULL),
(10, 'Menu Familiar', 'PACK', 25.00, NULL);

-- Associar produtos aos menus
INSERT INTO menu_produtos (id_menu, id_produto) VALUES
-- Menu Estudante (ID: 9)
(9, 1),  -- Hambúrguer
(9, 5),  -- Batatas
(9, 6),  -- Refrigerante
-- Menu Familiar (ID: 10)
(10, 1), -- Hambúrguer
(10, 2), -- Cheeseburger
(10, 5), -- Batatas
(10, 6); -- Refrigerante

-- =============================================================
-- 11. PEDIDOS DE TESTE
-- =============================================================

-- RESTAURANTE CENTRO (ID: 1)
INSERT INTO pedido (estado, tipo_consumo, id_restaurante, valor_total) VALUES
('REGISTADO', 'LOCAL', 1, 25.50),
('REGISTADO', 'TAKE_AWAY', 1, 12.00),
('EM_PREPARACAO', 'LOCAL', 1, 45.00),
('PRONTO', 'TAKE_AWAY', 1, 18.50),
('PRONTO', 'LOCAL', 1, 32.00),
('ENTREGUE', 'LOCAL', 1, 50.00);

-- RESTAURANTE NORTE (ID: 2)
INSERT INTO pedido (estado, tipo_consumo, id_restaurante, valor_total) VALUES
('REGISTADO', 'LOCAL', 2, 15.00),
('PRONTO', 'TAKE_AWAY', 2, 22.00);

-- RESTAURANTE SUL (ID: 3)
INSERT INTO pedido (estado, tipo_consumo, id_restaurante, valor_total) VALUES
('REGISTADO', 'LOCAL', 3, 10.00);

-- =============================================================
-- 12. MENSAGENS DE TESTE (OPCIONAL)
-- =============================================================

INSERT INTO mensagem (id_posto, texto) VALUES
(2, 'Atenção: Novo fornecedor de ingredientes a partir de segunda-feira'),
(1, 'Lembrete: Verificar stock de embalagens take-away'),
(3, 'Reunião de equipa agendada para sexta às 15h');

-- =============================================================
-- FIM DO SCRIPT
-- =============================================================

