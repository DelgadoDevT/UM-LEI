USE jbvmotors;

# RM1: Obter histórico de faturas global, por cliente. 
SELECT F.*
FROM Fatura AS F
INNER JOIN Aluguer AS A ON F.id_aluguer = A.id_aluguer
INNER JOIN Cliente AS C ON A.id_cliente = C.id_cliente
WHERE C.id_cliente = ?;

# RM2  Obter frota de veículos em cada stand. 
SELECT V.*
FROM Veiculo AS V
INNER JOIN Stand AS S ON V.id_stand = S.id_stand
WHERE S.id_stand = ?;

# RM3  Atualizar frota de veículos em cada stand. 
DELIMITER $$
CREATE PROCEDURE adicionar_veiculo(
    IN p_marca VARCHAR(30),
    IN p_modelo VARCHAR(120),
    IN p_ano INT,
    IN p_matricula CHAR(6),
    IN p_cor VARCHAR(30),
    IN p_tipo VARCHAR(20),
    IN p_quilometragem INT,
    IN p_preco DECIMAL(10,2),
    IN p_obs TEXT,
    IN p_id_stand INT
)
BEGIN
    INSERT INTO veiculo (marca, modelo, ano, matricula, cor, tipo, quilometragem, preco, obs, id_stand)
    VALUES (p_marca, p_modelo, p_ano, p_matricula, p_cor, p_tipo, p_quilometragem, p_preco, p_obs, p_id_stand);
END $$
DELIMITER ;

# RM4: Obter a estado atual de um veículo (em que stand se encontra e/ou está a ser alugado por alguém). 
SELECT fv.id_veiculo, s.nome AS nome_stand,
	CASE
		WHEN a.id_veiculo IS NOT NULL AND a.estado = 'pendente' THEN 'Alugado'
		ELSE 'Não Alugado'
	END AS estado,
	CASE
		WHEN a.id_veiculo IS NOT NULL AND a.estado = 'pendente' THEN a.id_cliente
		ELSE NULL
	END AS id_cliente
FROM veiculo fv
INNER JOIN stand s ON fv.id_stand = s.id_stand
LEFT JOIN aluguer a ON fv.id_veiculo = a.id_veiculo AND a.estado = 'pendente'
WHERE fv.id_veiculo = ?;

# RM5: Obter a modelo marca e matrícula de um determinado veículo. 
SELECT modelo AS Modelo, 
    marca AS Marca, 
    matricula AS Matrícula
    FROM Veiculo
    WHERE id_veiculo = ?;

# RM6: Obter licenças/cartas de condução de um cliente. 
SELECT lc.id_licenca, lc.nome AS tipo_licenca
FROM licencas_cliente lc
WHERE lc.id_cliente = 5;

# RM7: Obter histórico de alugueres global de um veículo, por stand e/ou cliente. 
DELIMITER $$
CREATE PROCEDURE obter_historico_alugueres (IN p_id_veiculo INT, IN p_id_stand INT, IN p_id_cliente INT)
BEGIN
    SELECT 
        v.id_veiculo,
        v.marca,
        v.modelo,
        s.nome AS nome_stand,
        a.id_aluguer,
        a.data_inicio,
        a.data_termino,
        a.estado AS estado_aluguer,
        c.id_cliente,
        c.nome AS nome_cliente
    FROM aluguer a
    JOIN veiculo v ON a.id_veiculo = v.id_veiculo
    LEFT JOIN stand s ON v.id_stand = s.id_stand
    LEFT JOIN cliente c ON a.id_cliente = c.id_cliente
    WHERE v.id_veiculo = p_id_veiculo
      AND (p_id_stand IS NULL OR s.id_stand = p_id_stand)
      AND (p_id_cliente IS NULL OR c.id_cliente = p_id_cliente)
    ORDER BY a.data_inicio;
END $$
DELIMITER ;

# RM8: Procurar veículos por tipo.
SELECT * FROM Veiculo
WHERE tipo = ?;

# RM9: Adicionar licenças/cartas de condução de um cliente. 
DELIMITER $$
CREATE PROCEDURE adicionar_licencas_cliente (IN p_id_cliente INT, IN p_licencas TEXT)
BEGIN
    DECLARE licenca VARCHAR(10);
    DECLARE i INT DEFAULT 1;

    SET p_licencas = TRIM(p_licencas);

    WHILE LENGTH(p_licencas) > 0 DO
        SET licenca = SUBSTRING_INDEX(p_licencas, ',', 1);
        SET p_licencas = SUBSTRING(p_licencas, LENGTH(licenca) + 2);

        INSERT INTO licencas_cliente (nome, id_cliente)
        VALUES (licenca, p_id_cliente);
    END WHILE;

END $$
DELIMITER ;

# RM10: Efetuar atualização do estado do aluguer. 
DELIMITER $$
CREATE PROCEDURE atualizar_estado_aluguer(IN p_id_aluguer INT, IN p_novo_estado VARCHAR(20))
BEGIN
    DECLARE exit handler for sqlexception
        ROLLBACK;

    START TRANSACTION;

    UPDATE aluguer
    SET estado = p_novo_estado
    WHERE id_aluguer = p_id_aluguer;

    COMMIT;
END $$
DELIMITER ;

# RM11: Efetuar marcação de aluguer. 
DELIMITER $$
CREATE PROCEDURE pr_efetuar_marcacao_aluguer (IN p_data_inicio DATETIME, IN p_data_termino DATETIME, IN p_id_cliente INT, IN p_id_veiculo INT, IN p_id_funcionario INT)
BEGIN
    DECLARE conflitos INT DEFAULT 0;
    
    START TRANSACTION;

    SELECT COUNT(*) INTO conflitos
    FROM Aluguer a
    WHERE a.id_veiculo = p_data_inicio
      AND a.estado = 'pendente'
      AND (p_data_inicio < a.data_termino AND p_data_termino > a.data_inicio);

    IF conflitos > 0 THEN
		ROLLBACK;
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'O veículo já está alugado nesse período.';
	ELSEIF NOT fun_verificar_licenca_cliente(p_id_cliente, p_id_veiculo) THEN
		ROLLBACK;
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'O cliente não tem a licença necessária para este veículo.';
    ELSE
        INSERT INTO Aluguer (data_inicio, data_termino, estado, id_cliente, id_veiculo, id_funcionario)
        VALUES (p_data_inicio, p_data_termino, 'pendente', p_id_cliente, p_id_veiculo, p_id_funcionario);
        COMMIT;
    END IF;
END $$
DELIMITER ;

# RM12: Atualizar dados após a devolução de um veículo (quilometragem, observações). 
DELIMITER $$
CREATE PROCEDURE atualizar_aluguer_devolucao(IN p_id_aluguer INT, IN p_quilometragem INT, IN p_observacoes TEXT)
BEGIN
    DECLARE exit handler for sqlexception
        ROLLBACK;

    START TRANSACTION;

    UPDATE aluguer
    SET estado = 'finalizado'
    WHERE id_aluguer = p_id_aluguer;

    UPDATE aluguer
    SET quilometragem = p_quilometragem, obs = p_observacoes
    WHERE id_aluguer = p_id_aluguer;

    COMMIT;
END $$
DELIMITER ;

# RM13: Aplicação automática de taxas por atraso/dano.
DELIMITER $$
CREATE TRIGGER tr_aplicar_taxas_ao_concluir_aluguer
AFTER UPDATE ON Aluguer
FOR EACH ROW
BEGIN
    DECLARE v_dias_atraso INT DEFAULT 0;
    DECLARE v_taxa_atraso DECIMAL(10,2) DEFAULT 10.00;  -- 10€ por dia de atraso
    DECLARE v_taxa_dano DECIMAL(10,2) DEFAULT 50.00;    -- 50€ fixos por dano
    DECLARE v_total_taxas DECIMAL(10,2) DEFAULT 0;

    IF NEW.estado = 'finalizado' AND OLD.estado <> 'finalizado' THEN
        IF NEW.data_termino < NOW() THEN
            SET v_dias_atraso = DATEDIFF(NOW(), NEW.data_termino);
            SET v_total_taxas = v_total_taxas + (v_dias_atraso * v_taxa_atraso);
        END IF;

        IF LOCATE('dano', LOWER(NEW.obs)) > 0 THEN
            SET v_total_taxas = v_total_taxas + v_taxa_dano;
        END IF;

        IF v_total_taxas > 0 THEN
            UPDATE Fatura
            SET valor = valor + v_total_taxas
            WHERE id_aluguer = NEW.id_aluguer;
        END IF;
    END IF;
END$$
DELIMITER ;

# RM14: Verificar se cliente tem a licença necessária para conduzir o veículo reservado. 
DELIMITER $$
CREATE FUNCTION fun_verificar_licenca_cliente(p_id_cliente INT, p_id_veiculo INT)
RETURNS BOOLEAN
DETERMINISTIC
BEGIN
    DECLARE v_tipo_veiculo VARCHAR(20);
    DECLARE v_categoria_licenca VARCHAR(10);
    DECLARE v_licenca_valida BOOLEAN DEFAULT FALSE;
    
    DECLARE cur CURSOR FOR
        SELECT nome
        FROM licencas_cliente
        WHERE id_cliente = p_id_cliente;

    DECLARE CONTINUE HANDLER FOR NOT FOUND
        BEGIN
            CLOSE cur;
            RETURN v_licenca_valida;
        END;

    SELECT tipo INTO v_tipo_veiculo
    FROM Veiculo
    WHERE id_veiculo = p_id_veiculo;

    OPEN cur;

    FETCH cur INTO v_categoria_licenca;

    WHILE v_categoria_licenca IS NOT NULL DO
        IF (v_tipo_veiculo = 'ciclomotor' AND v_categoria_licenca = 'AM') THEN
            SET v_licenca_valida = TRUE;
        ELSEIF (v_tipo_veiculo = 'moto' AND v_categoria_licenca IN ('A', 'A1', 'A2')) THEN
            SET v_licenca_valida = TRUE;
        ELSEIF (v_tipo_veiculo = 'carro' AND v_categoria_licenca IN ('B', 'B1')) THEN
            SET v_licenca_valida = TRUE;
        ELSEIF (v_tipo_veiculo = 'camiao' AND v_categoria_licenca IN ('C', 'C1')) THEN
            SET v_licenca_valida = TRUE;
        ELSEIF (v_tipo_veiculo = 'autocarro' AND v_categoria_licenca IN ('D', 'D1')) THEN
            SET v_licenca_valida = TRUE;
        END IF;

        FETCH cur INTO v_categoria_licenca;
    END WHILE;

    CLOSE cur;

    RETURN FALSE;
END $$
DELIMITER ;

# RM15: Relatórios financeiros detalhados, soma do valor angariado no mês. 
SELECT SUM(f.valor) AS total_ganho, MONTH(f.data_emissao) AS mes, YEAR(f.data_emissao) AS ano
FROM fatura f
WHERE f.estado = 'pago'
  AND MONTH(f.data_emissao) = MONTH(CURRENT_DATE())
  AND YEAR(f.data_emissao) = YEAR(CURRENT_DATE())
GROUP BY mes, ano;

# RM16: Emissão de alerta no caso de veículo não ser entregue até a data do fim de aluguer. 
DELIMITER $$
CREATE EVENT verificar_pagamentos_pendentes
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
DO
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE alug_id INT;
    DECLARE veic_id INT;
    DECLARE termino DATETIME;
    DECLARE cliente_id INT;

    DECLARE cur CURSOR FOR
    SELECT a.id_aluguer, a.id_veiculo, a.data_termino, a.id_cliente
    FROM Aluguer a
    LEFT JOIN Fatura f ON a.id_aluguer = f.id_aluguer
    WHERE a.estado = 'pendente' 
      AND a.data_termino < CURRENT_DATE();

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN cur;

    read_loop: LOOP
        FETCH cur INTO alug_id, veic_id, termino, cliente_id;
        IF done = 1 THEN LEAVE read_loop; END IF;

        UPDATE Fatura
        SET valor = valor + 50.00
        WHERE id_aluguer = alug_id;
    END LOOP;

    CLOSE cur;
END $$
DELIMITER ;

# RM17: Emissão automática de Fatura na data de termino do aluguer
DELIMITER $$
CREATE TRIGGER emitir_fatura_automaticamente
AFTER UPDATE ON aluguer
FOR EACH ROW
BEGIN
	DECLARE valor DECIMAL(10,2);
    IF NEW.estado = 'finalizado' AND OLD.estado != 'finalizado' THEN
        SELECT ROUND(v.preco * DATEDIFF(NEW.data_termino, NEW.data_inicio), 2)
        INTO valor
        FROM veiculo v
        WHERE v.id_veiculo = NEW.id_veiculo;

        INSERT INTO fatura (id_aluguer, estado, data_emissao, valor, met_pagamento)
        VALUES (NEW.id_aluguer, 'pendente', NOW(), valor, 'não definido');
    END IF;
END $$
DELIMITER ;

# RM18: Relatório número alugueres mensais. 
SELECT 
    YEAR(data_inicio) AS ano,
    MONTH(data_inicio) AS mes,
    COUNT(id_aluguer) AS numero_alugueres
FROM aluguer
GROUP BY ano, mes
ORDER BY ano DESC, mes DESC;

# RM19: Obter lista dos 5 clientes que realizaram mais alugueres.
SELECT 
    C.nome AS Nome, 
    COUNT(*) AS NrReservas
FROM Cliente AS C
INNER JOIN Aluguer AS A
    ON C.id_cliente = A.id_cliente
GROUP BY A.id_cliente
ORDER BY NrReservas DESC
LIMIT 5;

# RM20: Obter marca de veículo com maior número de alugueres no final do mês.
SELECT 
    V.marca,
    COUNT(*) AS alugueres_totais
FROM Aluguer AS A
INNER JOIN Veiculo AS V ON
    A.id_veiculo = V.id_veiculo
GROUP BY V.marca
ORDER BY alugueres_totais DESC
LIMIT 1;














