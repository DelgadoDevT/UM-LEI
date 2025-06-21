-- PostgreSQL version of the jbvmotors schema creation

-- Drop schema if exists and create schema
DROP SCHEMA IF EXISTS jbvmotors CASCADE;
CREATE SCHEMA jbvmotors;

SET search_path TO jbvmotors;

-- Table Stand
DROP TABLE IF EXISTS Stand CASCADE;

CREATE TABLE Stand (
  id_stand SERIAL PRIMARY KEY,
  nome VARCHAR(40) NOT NULL,
  contacto VARCHAR(31) NOT NULL,
  rua VARCHAR(45) NOT NULL,
  cidade VARCHAR(45) NOT NULL
);

-- Table Cliente
DROP TABLE IF EXISTS Cliente CASCADE;

CREATE TABLE Cliente (
  id_cliente SERIAL PRIMARY KEY,
  nome VARCHAR(80) NOT NULL,
  nif INT NOT NULL,
  nic INT NOT NULL,
  contacto VARCHAR(31) NOT NULL,
  rua VARCHAR(45) NOT NULL,
  pais VARCHAR(45) NOT NULL,
  cidade VARCHAR(45) NOT NULL
);

-- Table Funcionario
DROP TABLE IF EXISTS Funcionario CASCADE;

CREATE TABLE Funcionario (
  id_funcionario SERIAL PRIMARY KEY,
  contacto VARCHAR(31) NOT NULL,
  cargo VARCHAR(20) NOT NULL,
  horario VARCHAR(20) NOT NULL,
  nome VARCHAR(80) NOT NULL,
  salario NUMERIC(10,2) NOT NULL,
  nif INT NOT NULL,
  nss INT NOT NULL,
  rua VARCHAR(45) NOT NULL,
  pais VARCHAR(45) NOT NULL,
  cidade VARCHAR(45) NOT NULL,
  id_stand INT NOT NULL,
  CONSTRAINT fk_id_stand_funcionario FOREIGN KEY (id_stand) REFERENCES Stand (id_stand) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- Table Veiculo
DROP TABLE IF EXISTS Veiculo CASCADE;

CREATE TABLE Veiculo (
  id_veiculo SERIAL PRIMARY KEY,
  marca VARCHAR(30) NOT NULL,
  modelo VARCHAR(120) NOT NULL,
  ano INT NOT NULL,
  matricula CHAR(6) NOT NULL,
  cor VARCHAR(30) NOT NULL,
  tipo VARCHAR(20) NOT NULL,
  quilometragem INT NOT NULL,
  preco NUMERIC(10,2) NOT NULL,
  obs TEXT,
  id_stand INT NOT NULL,
  CONSTRAINT fk_id_stand_veiculo FOREIGN KEY (id_stand) REFERENCES Stand (id_stand) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- Table Aluguer
DROP TABLE IF EXISTS Aluguer CASCADE;

CREATE TABLE Aluguer (
  id_aluguer SERIAL PRIMARY KEY,
  data_inicio TIMESTAMP NOT NULL,
  data_termino TIMESTAMP NOT NULL,
  estado VARCHAR(10) NOT NULL,
  obs TEXT NOT NULL,
  id_cliente INT NOT NULL,
  id_veiculo INT NOT NULL,
  id_funcionario INT NOT NULL,
  CONSTRAINT fk_id_cliente FOREIGN KEY (id_cliente) REFERENCES Cliente (id_cliente) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT fk_id_veiculo FOREIGN KEY (id_veiculo) REFERENCES Veiculo (id_veiculo) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT fk_id_funcionario FOREIGN KEY (id_funcionario) REFERENCES Funcionario (id_funcionario) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- Table Fatura
DROP TABLE IF EXISTS Fatura CASCADE;

CREATE TABLE Fatura (
  id_aluguer INT PRIMARY KEY,
  estado VARCHAR(10) NOT NULL,
  data_emissao TIMESTAMP NOT NULL,
  valor NUMERIC(10,2) NOT NULL,
  met_pagamento VARCHAR(20) NOT NULL,
  CONSTRAINT fk_id_aluguer FOREIGN KEY (id_aluguer) REFERENCES Aluguer (id_aluguer) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- Table Licencas_Cliente
DROP TABLE IF EXISTS Licencas_Cliente CASCADE;

CREATE TABLE Licencas_Cliente (
  id_licenca SERIAL PRIMARY KEY,
  nome VARCHAR(48) NOT NULL,
  id_cliente INT NOT NULL,
  CONSTRAINT fk_id_cliente_licencas FOREIGN KEY (id_cliente) REFERENCES Cliente (id_cliente) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- Table Licencas_Funcionario
DROP TABLE IF EXISTS Licencas_Funcionario CASCADE;

CREATE TABLE Licencas_Funcionario (
  id_licenca SERIAL PRIMARY KEY,
  nome VARCHAR(48) NOT NULL,
  id_funcionario INT NOT NULL,
  CONSTRAINT fk_id_funcionario_licencas FOREIGN KEY (id_funcionario) REFERENCES Funcionario (id_funcionario) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE INDEX idx_aluguer_cliente ON aluguer(id_cliente);
CREATE INDEX idx_aluguer_data_inicio ON aluguer(data_inicio);
CREATE INDEX idx_veiculo_matricula ON veiculo(matricula);
CREATE INDEX idx_cliente_nome ON cliente(nome);
