USE jbvmotors;
# Seleciona todos os veículos disponíveis para aluguer
CREATE VIEW carros_disponiveis AS
SELECT * 
FROM Veiculo
WHERE id_veiculo NOT IN (
    SELECT id_veiculo
    FROM Aluguer
    WHERE estado = 'pendente'
);

# Seleciona clientes que tem faturas por pagar, e valor que total que devem
CREATE VIEW pagamentos_pendentes AS
SELECT  C.id_cliente,
    C.nome,
    C.contacto,
    CONCAT(C.pais, ', ', C.cidade, ', ', C.rua) AS morada,
    SUM(F.valor) AS valor
FROM cliente AS C
INNER JOIN Aluguer AS A ON A.id_cliente = C.id_cliente
INNER JOIN Fatura AS F ON F.id_aluguer = A.id_aluguer
WHERE F.estado = 'pendente'
GROUP BY C.id_cliente, C.nome, C.contacto, C.pais, C.cidade, C.rua
ORDER BY C.nome;

# Selecionar os clientes que ainda não devolveram os carros dentro do prazo de aluguer
CREATE VIEW alugueres_atrasados AS
SELECT C.id_cliente,
    C.nome,
    C.contacto,
    CONCAT(C.pais, ', ', C.cidade, ', ', C.rua) AS morada,
    DATEDIFF(CURRENT_DATE, A.data_termino) AS dias_de_atraso
FROM Aluguer AS A
INNER JOIN Cliente AS C ON A.id_cliente = C.id_cliente
WHERE A.data_termino < CURRENT_DATE
    AND A.estado = 'pendente'
ORDER BY DATEDIFF(CURRENT_DATE, A.data_termino) DESC;

SELECT * FROM alugueres_atrasados;
SELECT * FROM carros_disponiveis;
SELECT * FROM pagamentos_pendentes;