-- Procedure (Efetuar marcação de aluguer - RM11)
DELIMITER $$
CREATE PROCEDURE pr_efetuar_marcacao_aluguer (IN p_data_inicio DATETIME, IN p_data_termino DATETIME, IN p_id_cliente INT, IN p_id_veiculo INT, IN p_id_funcionario INT)
BEGIN
    DECLARE conflitos INT DEFAULT 0;
    
    START TRANSACTION;

    SELECT COUNT(*) INTO conflitos
    FROM Aluguer a
    WHERE a.id_veiculo = p_data_inicio
      AND a.estado = 'pendente'
      AND ((p_data_inicio BETWEEN a.data_inicio AND a.data_termino) OR (p_data_termino BETWEEN a.data_inicio AND a.data_termino));

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

-- Function (Verificar se o cliente tem licença para conduzir o tipo de veículo - RM14).
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

-- Trigger (Aplicação automática de taxas por atraso/dano RM13)
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