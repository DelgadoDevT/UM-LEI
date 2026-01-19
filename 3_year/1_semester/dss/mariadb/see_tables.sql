-- =============================================================
-- SCRIPT 03: VISUALIZAÇÃO E VERIFICAÇÃO DE DADOS
-- =============================================================
-- Atualizado em: 26/12/2025
-- Queries para validar a estrutura e dados da BD
-- =============================================================

USE cadeiaRestaurantes;

-- =============================================================
-- 1. RESTAURANTES E ESTRUTURA ORGANIZACIONAL
-- =============================================================

SELECT '=== RESTAURANTES ===' AS '';
SELECT id, nome, localizacao
FROM restaurante
ORDER BY id;

SELECT '=== POSTOS DE TRABALHO ===' AS '';
SELECT p.id, p.tipo, p.id_restaurante, r.nome AS restaurante
FROM posto p
LEFT JOIN restaurante r ON p.id_restaurante = r.id
ORDER BY p.id_restaurante, p.tipo;

-- =============================================================
-- 2. FUNCIONÁRIOS
-- =============================================================

SELECT '=== TODOS OS FUNCIONÁRIOS ===' AS '';
SELECT f.id, f.nome, f.nif, p.tipo AS posto, r.nome AS restaurante
FROM funcionario f
LEFT JOIN posto p ON f.id_posto = p.id
LEFT JOIN restaurante r ON p.id_restaurante = r.id
ORDER BY f.id;

SELECT '=== GESTORES ===' AS '';
SELECT g.id, f.nome, f.nif,
       COALESCE(r.nome, 'Gestor Geral') AS restaurante
FROM gestor g
JOIN funcionario f ON g.id = f.id
LEFT JOIN posto p ON f.id_posto = p.id
LEFT JOIN restaurante r ON p.id_restaurante = r.id
ORDER BY g.id;

SELECT '=== COZINHEIROS ===' AS '';
SELECT c.id, f.nome, f.nif, r.nome AS restaurante
FROM cozinheiro c
JOIN funcionario f ON c.id = f.id
LEFT JOIN posto p ON f.id_posto = p.id
LEFT JOIN restaurante r ON p.id_restaurante = r.id
ORDER BY c.id;

SELECT '=== ATENDENTES ===' AS '';
SELECT a.id, f.nome, f.nif, r.nome AS restaurante
FROM atendente a
JOIN funcionario f ON a.id = f.id
LEFT JOIN posto p ON f.id_posto = p.id
LEFT JOIN restaurante r ON p.id_restaurante = r.id
ORDER BY a.id;

-- =============================================================
-- 3. INGREDIENTES E STOCK
-- =============================================================

SELECT '=== INGREDIENTES (CATÁLOGO) ===' AS '';
SELECT nome, CONCAT(preco, '€') AS preco
FROM ingrediente
ORDER BY nome;

SELECT '=== STOCK POR RESTAURANTE ===' AS '';
SELECT r.nome AS restaurante,
       s.nome_ingrediente,
       s.quantidade,
       i.preco AS preco_unitario
FROM stock s
JOIN restaurante r ON s.id_restaurante = r.id
JOIN ingrediente i ON s.nome_ingrediente = i.nome
ORDER BY s.id_restaurante, s.nome_ingrediente;

-- =============================================================
-- 4. INDICADORES DE DESEMPENHO
-- =============================================================

SELECT '=== INDICADORES (ÚLTIMOS 30 DIAS) ===' AS '';
SELECT r.nome AS restaurante,
       i.data,
       CONCAT(i.faturacaoTotal, '€') AS faturacao,
       i.totalPedidos AS pedidos,
       CONCAT(i.tempoMedioPreparacao, ' min') AS tempo_medio
FROM indicador i
JOIN restaurante r ON i.id_restaurante = r.id
WHERE i.data >= CURDATE() - INTERVAL 30 DAY
ORDER BY i.id_restaurante, i.data DESC;

SELECT '=== RESUMO INDICADORES POR RESTAURANTE ===' AS '';
SELECT r.nome AS restaurante,
       COUNT(*) AS total_indicadores,
       CONCAT(SUM(i.faturacaoTotal), '€') AS faturacao_total,
       SUM(i.totalPedidos) AS total_pedidos,
       CONCAT(ROUND(AVG(i.tempoMedioPreparacao), 1), ' min') AS tempo_medio_geral
FROM indicador i
JOIN restaurante r ON i.id_restaurante = r.id
GROUP BY r.id, r.nome
ORDER BY r.id;

-- =============================================================
-- 5. PEDIDOS
-- =============================================================

SELECT '=== PEDIDOS (TODOS) ===' AS '';
SELECT p.id,
       r.nome AS restaurante,
       p.estado,
       p.tipo_consumo,
       CONCAT(p.valor_total, '€') AS valor
FROM pedido p
JOIN restaurante r ON p.id_restaurante = r.id
ORDER BY p.id;

SELECT '=== PEDIDOS POR ESTADO ===' AS '';
SELECT r.nome AS restaurante,
       p.estado,
       COUNT(*) AS quantidade,
       CONCAT(SUM(p.valor_total), '€') AS valor_total
FROM pedido p
JOIN restaurante r ON p.id_restaurante = r.id
GROUP BY r.id, r.nome, p.estado
ORDER BY r.id, p.estado;

-- =============================================================
-- 6. LINHAS DE PEDIDO
-- =============================================================

SELECT '=== LINHAS DE PEDIDO ===' AS '';
SELECT lp.id,
       lp.id_pedido,
       lp.quantidade,
       CONCAT(lp.preco_venda, '€') AS preco,
       lp.tipo_artigo,
       lp.id_artigo,
       LEFT(COALESCE(lp.nota, '-'), 30) AS nota,
       CASE
           WHEN lp.ingredientes_personalizados IS NOT NULL
           THEN 'SIM'
           ELSE 'NÃO'
       END AS personalizado
FROM linha_pedido lp
ORDER BY lp.id_pedido, lp.id;

SELECT '=== LINHAS COM INGREDIENTES PERSONALIZADOS ===' AS '';
SELECT lp.id,
       lp.id_pedido,
       lp.quantidade,
       lp.tipo_artigo,
       lp.id_artigo,
       lp.ingredientes_personalizados
FROM linha_pedido lp
WHERE lp.ingredientes_personalizados IS NOT NULL
ORDER BY lp.id_pedido;

-- =============================================================
-- 7. PAGAMENTOS
-- =============================================================

SELECT '=== PAGAMENTOS ===' AS '';
SELECT pg.id,
       pg.id_pedido,
       CONCAT(pg.valor, '€') AS valor,
       pg.metodo,
       pg.detalhes
FROM pagamento pg
ORDER BY pg.id_pedido;

-- =============================================================
-- 8. MENSAGENS
-- =============================================================

SELECT '=== MENSAGENS (ÚLTIMAS 10) ===' AS '';
SELECT m.id,
       p.tipo AS posto,
       r.nome AS restaurante,
       LEFT(m.texto, 50) AS mensagem,
       m.data_hora
FROM mensagem m
JOIN posto p ON m.id_posto = p.id
LEFT JOIN restaurante r ON p.id_restaurante = r.id
ORDER BY m.data_hora DESC
LIMIT 10;

-- =============================================================
-- 9. PRODUTOS E MENUS
-- =============================================================

SELECT '=== PRODUTOS ===' AS '';
SELECT * FROM produto LIMIT 10;

SELECT '=== MENUS ===' AS '';
SELECT * FROM menu LIMIT 10;

-- =============================================================
-- 10. ESTATÍSTICAS GERAIS
-- =============================================================

SELECT '=== ESTATÍSTICAS GERAIS ===' AS '';

SELECT 'Total Restaurantes' AS metrica, COUNT(*) AS valor FROM restaurante
UNION ALL
SELECT 'Total Funcionários', COUNT(*) FROM funcionario
UNION ALL
SELECT 'Total Gestores', COUNT(*) FROM gestor
UNION ALL
SELECT 'Total Cozinheiros', COUNT(*) FROM cozinheiro
UNION ALL
SELECT 'Total Atendentes', COUNT(*) FROM atendente
UNION ALL
SELECT 'Total Ingredientes', COUNT(*) FROM ingrediente
UNION ALL
SELECT 'Total Pedidos', COUNT(*) FROM pedido
UNION ALL
SELECT 'Pedidos Registados', COUNT(*) FROM pedido WHERE estado = 'REGISTADO'
UNION ALL
SELECT 'Pedidos Em Preparação', COUNT(*) FROM pedido WHERE estado = 'EM_PREPARACAO'
UNION ALL
SELECT 'Pedidos Prontos', COUNT(*) FROM pedido WHERE estado = 'PRONTO'
UNION ALL
SELECT 'Pedidos Entregues', COUNT(*) FROM pedido WHERE estado = 'ENTREGUE';

-- =============================================================
-- FIM DO SCRIPT
-- =============================================================

