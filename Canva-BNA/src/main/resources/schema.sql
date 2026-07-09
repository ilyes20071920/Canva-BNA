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
