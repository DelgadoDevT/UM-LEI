USE jbvmotors;

-- Inserir stands
INSERT INTO stand (nome, contacto, rua, cidade) 
VALUES 
  ('JBV Motors Braga', '968731802', 'Rua de S. Vicente', 'Braga'),
  ('JBV Motors Maia', '921474153', 'Rua do Barreiro', 'Porto'),
  ('JBV Motors Viana', '932580667', 'Rua do Bispo', 'Viana do Castelo');

-- Inserir clientes
INSERT INTO cliente (nome, nif, nic, contacto, rua, pais, cidade) 
VALUES 
  ('José Silva', '750597179', '920080480', '920080480', 'Rua do Centro', 'Portugal', 'Braga'),
  ('Carlos Costa', '701643871', '919862482', '919862482', 'Avenida da Maia', 'Portugal', 'Maia'),
  ('Sofia Pereira', '099453364', '961818052', '961818052', 'Rua da Liberdade', 'Portugal', 'Viana do Castelo'),
  ('Ricardo Almeida', '940607883', '914474394', '914474394', 'Rua do Comércio', 'Portugal', 'Braga'),
  ('Luís Rocha', '407274737', '911148350', '911148350', 'Avenida da Boavista', 'Portugal', 'Porto');

-- Inserir licenças de cliente
INSERT INTO licencas_cliente (nome, id_cliente) 
VALUES 
  ('A', 1),
  ('B', 2),
  ('C1', 2),
  ('B', 3),
  ('B', 4);

-- Inserir veículos
INSERT INTO veiculo (marca, modelo, ano, matricula, cor, tipo, quilometragem, preco, obs, id_stand) 
VALUES 
  ('Ford', 'LX', 2000, 'UJ87OM', 'Cinza Metalizado', 'moto', 194329, 20, '', 1),
  ('Isuzu', 'Azure', 2025, 'RG33TN', 'Branco Pérola', 'moto', 129778, 25, '', 3),
  ('Nissan', 'Frontier', 2011, 'OA78SY', 'Verde Lima', 'carro', 2135, 30, 'Retrovisor partido', 1),
  ('Smart', '911', 2016, 'DW70IQ', 'Azul Turquesa ', 'carro', 56471, 32, '', 2),
  ('Chevrolet', 'CT6-V', 2024, 'HH66HN', 'Vermelho Coral', 'carro', 16623, 85, '', 3);

-- Inserir funcionários
INSERT INTO funcionario (contacto, cargo, horario, nome, salario, nif, nss, rua, pais, cidade, id_stand) 
VALUES 
  ('920080480', 'Vendedor', '08:00 - 16:00', 'Carlos Silva', 1340.00, '123456789', '987654321', 'Avenida da Liberdade', 'Portugal', 'Guimarães', 1),
  ('919862482', 'Mecânico', '08:00 - 16:00', 'João Pereira', 1200.00, '234567890', '876543210', 'Rua de Santa Catarina', 'Portugal', 'Porto', 2),
  ('961818052', 'Gerente', '16:00 - 24:00', 'Rolando Barros', 2500.00, '345678901', '765432109', 'Avenida dos Aliados', 'Portugal', 'Braga', 1),
  ('940607883', 'Vendedor', '08:00 - 16:00', 'Rui Martins', 1400.00, '456789012', '654321098', 'Rua Conde Vizela', 'Portugal', 'Vila do Conde', 2),
  ('407274737', 'Vendedor', '08:00 - 00:16', 'Laura Santos', 1300.00, '567890123', '543210987', 'Rua da Boavista', 'Portugal', 'Porto', 3);

-- Inserir licenças de funcionário
INSERT INTO licencas_funcionario (nome, id_funcionario) 
VALUES 
  ('A', 1),
  ('B1', 1),
  ('B', 2),
  ('C', 3),
  ('B', 3),
  ('B', 4),
  ('B1', 5);

-- Inserir alugueres
INSERT INTO aluguer (data_inicio, data_termino, estado, obs, id_cliente, id_veiculo, id_funcionario) 
VALUES 
  ('2025-05-01 09:00:00', '2025-05-05 09:00:00', 'finalizado', '', 1, 1, 1),
  ('2025-05-01 09:00:00', '2025-05-03 09:00:00', 'finalizado', '', 2, 2, 2),
  ('2025-05-10 09:00:00', '2025-05-12 09:00:00', 'cancelado', '', 3, 3, 3),
  ('2025-05-12 09:00:00', '2025-05-15 09:00:00', 'cancelado', 'Cliente tentou roubar viatura', 4, 4, 4),
  ('2025-05-20 09:00:00', '2025-05-25 09:00:00', 'pendente', '', 5, 5, 5);

INSERT INTO fatura
VALUES 
  (1, 'pago', '2025-05-05 09:26:35.551794', 100.00, 'dinheiro'),
  (2, 'pendente', '2025-05-04 11:38:04.642048', 75.00, 'transferencia');