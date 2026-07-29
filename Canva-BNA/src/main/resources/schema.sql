-- ============================================================
-- Canva-BNA Database Schema
-- Compatible with MySQL 8.0+ and PostgreSQL 14+
-- ============================================================

-- Create database (MySQL only — PostgreSQL databases are created via CLI)
CREATE DATABASE IF NOT EXISTS canva_bna
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE canva_bna;

-- ============================================================
-- Table: users
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id         BIGINT          NOT NULL AUTO_INCREMENT,
    matricule  INT             NOT NULL,
    password   VARCHAR(255)    NOT NULL,
    structure  VARCHAR(255)    NOT NULL,
    role       VARCHAR(50)     NOT NULL,
    enabled    TINYINT(1)      NOT NULL DEFAULT 1,

    CONSTRAINT pk_users            PRIMARY KEY (id),
    CONSTRAINT uq_users_matricule  UNIQUE (matricule),
    CONSTRAINT chk_users_role      CHECK (role IN ('ROLE_ADMIN','ROLE_PRISE_EN_CHARGE','ROLE_CHEF_DIVISION'))
);

-- ============================================================
-- Seed data — initial admin account
-- Password: admin123  (BCrypt, strength 10)
-- ============================================================
INSERT INTO users (matricule, password, structure, role, enabled)
VALUES (
    10001,
    '$2a$10$N.zmdr9zkEp0QNVvIGsVrOoSr4F85hGGUbG/OIUP8y8sdS4x2IhCi',
    'Direction Générale',
    'ROLE_ADMIN',
    1
);

-- Additional seed users for testing
INSERT INTO users (matricule, password, structure, role, enabled)
VALUES (
    20002,
    '$2a$10$N.zmdr9zkEp0QNVvIGsVrOoSr4F85hGGUbG/OIUP8y8sdS4x2IhCi',  -- password: admin123
    'Service Prise en Charge',
    'ROLE_PRISE_EN_CHARGE',
    1
);

INSERT INTO users (matricule, password, structure, role, enabled)
VALUES (
    30003,
    '$2a$10$N.zmdr9zkEp0QNVvIGsVrOoSr4F85hGGUbG/OIUP8y8sdS4x2IhCi',  -- password: admin123
    'Division Technique',
    'ROLE_CHEF_DIVISION',
    1
);

-- ============================================================
-- Table: structures (reference table for organizational units)
-- ============================================================
CREATE TABLE IF NOT EXISTS structures (
    id    BIGINT       NOT NULL AUTO_INCREMENT,
    nom   VARCHAR(255) NOT NULL,
    code  VARCHAR(100) NOT NULL,

    CONSTRAINT pk_structures       PRIMARY KEY (id),
    CONSTRAINT uq_structures_code  UNIQUE (code)
);

-- ============================================================
-- Table: clients (fiche signalétique entreprise)
-- ============================================================
CREATE TABLE IF NOT EXISTS clients (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    identifiant           VARCHAR(50)   NOT NULL,
    groupe                VARCHAR(255)  NOT NULL,
    relation              VARCHAR(255)  NOT NULL,
    activite              VARCHAR(255),
    segment               VARCHAR(100),
    siege_social          VARCHAR(500),
    secteur               VARCHAR(255),
    date_entree_relation  DATE,
    forme_juridique       VARCHAR(255),
    capital_social        DECIMAL(15,3),
    agence                VARCHAR(100),
    direction_regionale   VARCHAR(255),
    structure_id          BIGINT,

    CONSTRAINT pk_clients              PRIMARY KEY (id),
    CONSTRAINT uq_clients_identifiant  UNIQUE (identifiant),
    CONSTRAINT fk_clients_structure    FOREIGN KEY (structure_id) REFERENCES structures(id)
);

-- ============================================================
-- Table: actionnaires (shareholders / associates)
-- ============================================================
CREATE TABLE IF NOT EXISTS actionnaires (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    nom                 VARCHAR(255)  NOT NULL,
    nombre_actions      INT           NOT NULL,
    montant             DECIMAL(15,3),
    pourcentage_actions DOUBLE,
    client_id           BIGINT        NOT NULL,

    CONSTRAINT pk_actionnaires           PRIMARY KEY (id),
    CONSTRAINT fk_actionnaires_client    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);

-- ============================================================
-- Table: comptes (bank accounts)
-- ============================================================
CREATE TABLE IF NOT EXISTS comptes (
    code_guichet   VARCHAR(10)  NOT NULL,
    code_produit   VARCHAR(10)  NOT NULL,
    num_compte     VARCHAR(20)  NOT NULL,
    agence         VARCHAR(100),
    client_id      BIGINT       NOT NULL,

    CONSTRAINT pk_comptes              PRIMARY KEY (code_guichet, code_produit, num_compte),
    CONSTRAINT fk_comptes_client       FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);

-- ============================================================
-- Table: mandataires (authorized signatories on accounts)
-- ============================================================
CREATE TABLE IF NOT EXISTS mandataires (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    num_mandat     VARCHAR(50),
    num_demande    VARCHAR(50),
    type_mandat    VARCHAR(100),
    agence         VARCHAR(100),
    mandant        VARCHAR(255),
    date_creation  DATE,
    date_debut     DATE,
    date_fin       DATE,
    compte_code_guichet VARCHAR(10),
    compte_code_produit VARCHAR(10),
    compte_num_compte   VARCHAR(20),

    CONSTRAINT pk_mandataires          PRIMARY KEY (id),
    CONSTRAINT fk_mandataires_compte   FOREIGN KEY (compte_code_guichet, compte_code_produit, compte_num_compte) 
        REFERENCES comptes(code_guichet, code_produit, num_compte) ON DELETE CASCADE
);

-- ============================================================
-- Seed data — Business module test data
-- Based on real screenshots (BOUDOKHANE MOKHTAR / ILYES MHLHLI)
-- ============================================================

-- Structure de référence
INSERT INTO structures (nom, code) VALUES ('Direction Régionale Tunis I', 'DIR_REG_TUNIS_1');
INSERT INTO structures (nom, code) VALUES ('Direction Régionale Tunis II', 'DIR_REG_TUNIS_2');
INSERT INTO structures (nom, code) VALUES ('Direction Centrale', 'DIR_CENTRALE');

-- Client entreprise : BOUDOKHANE MOKHTAR / ILYES MHLHLI
INSERT INTO clients (identifiant, groupe, relation, activite, segment, siege_social, secteur,
                     date_entree_relation, forme_juridique, capital_social, agence, direction_regionale, structure_id)
VALUES (
    '30032002',
    'ILYESMHESPRIT',
    'STE ENT. ILYES MHLHLI',
    'Développement Informatique',
    'Corporates',
    'BLOC 11 MOUROUJ 6',
    'Sociétés non financières',
    '2008-10-20',
    'Societe A Responsabilite Limitee',
    1920.000,
    '146',
    'DIR.REG. TUNIS I',
    1
);

-- Compte bancaire pour le client
INSERT INTO comptes (code_guichet, code_produit, num_compte, agence, client_id) 
VALUES ('123', 'IM02', '192019', '146', 1);

-- Mandataire sur le compte
INSERT INTO mandataires (num_mandat, num_demande, type_mandat, agence, mandant,
                         date_creation, date_debut, date_fin, compte_code_guichet, compte_code_produit, compte_num_compte)
VALUES (
    '30032002',
    '10-202200157',
    'General',
    '146',
    'ILYES MHLHLI',
    '2022-01-17',
    '2023-11-29',
    '2026-11-29',
    '123',
    'IM02',
    '192019'
);

-- Second client for testing
INSERT INTO clients (identifiant, groupe, relation, activite, segment, siege_social, secteur,
                     date_entree_relation, forme_juridique, capital_social, agence, direction_regionale, structure_id)
VALUES (
    '1010202K',
    'ILYES MH TRADING',
    'STE ILYES MH TRADING',
    'Commerce international',
    'Corporates',
    'BLOC 11 MOUROUJ 6',
    'Sociétés non financières',
    '2015-03-15',
    'Societe A Responsabilite Limitee',
    150000.000,
    '120',
    'DIR.REG. TUNIS II',
    2
);

-- Actionnaires pour le second client
INSERT INTO actionnaires (nom, nombre_actions, montant, pourcentage_actions, client_id)
VALUES ('ILYES MHLHLI', 1500, 75000.000, 50.0, 2);

INSERT INTO actionnaires (nom, nombre_actions, montant, pourcentage_actions, client_id)
VALUES ('ALI ALI MHLHLI', 1500, 75000.000, 50.0, 2);

-- Comptes pour le second client
INSERT INTO comptes (code_guichet, code_produit, num_compte, agence, client_id) 
VALUES ('120', 'DT20', '001234', '120', 2);
INSERT INTO comptes (code_guichet, code_produit, num_compte, agence, client_id) 
VALUES ('120', 'DV20', '005678', '120', 2);

-- ============================================================
-- PostgreSQL equivalent (comment out MySQL block above and
-- uncomment the block below when using PostgreSQL)
-- ============================================================
/*
CREATE TABLE IF NOT EXISTS users (
    id        BIGSERIAL       PRIMARY KEY,
    matricule INTEGER         NOT NULL UNIQUE,
    password  VARCHAR(255)    NOT NULL,
    structure VARCHAR(255)    NOT NULL,
    role      VARCHAR(50)     NOT NULL
                              CHECK (role IN ('ROLE_ADMIN','ROLE_PRISE_EN_CHARGE','ROLE_CHEF_DIVISION')),
    enabled   BOOLEAN         NOT NULL DEFAULT TRUE
);
*/
