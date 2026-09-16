-- ============================================================
-- SCHEMA & TABELAS INICIAIS DO EVENTGO
-- ============================================================

CREATE SCHEMA IF NOT EXISTS eventgo;
SET search_path TO eventgo;

-- ------------------------------------------------------------
-- USUARIO
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
    id                BIGSERIAL       PRIMARY KEY,
    nome              VARCHAR(100)    NOT NULL,
    login             VARCHAR(50)     NOT NULL UNIQUE,
    senha_hash        VARCHAR(255)    NOT NULL,
    perfil            VARCHAR(30)     NOT NULL CHECK (perfil IN ('ADMIN', 'OPERADOR_BILHETERIA', 'OPERADOR_PORTARIA')),
    ativo             BOOLEAN         NOT NULL DEFAULT TRUE,
    criado_em         TIMESTAMP       NOT NULL DEFAULT NOW(),
    atualizado_em     TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- ------------------------------------------------------------
-- EVENTO
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS evento (
    id                BIGSERIAL       PRIMARY KEY,
    nome              VARCHAR(200)    NOT NULL,
    descricao         TEXT,
    data_evento       DATE            NOT NULL,
    horario           TIME            NOT NULL,
    local             VARCHAR(200)    NOT NULL,
    capacidade_total  INTEGER         NOT NULL CHECK (capacidade_total > 0),
    situacao          VARCHAR(20)     NOT NULL DEFAULT 'PLANEJADO'
                                      CHECK (situacao IN ('PLANEJADO', 'ABERTO', 'ENCERRADO', 'CANCELADO')),
    criado_em         TIMESTAMP       NOT NULL DEFAULT NOW(),
    atualizado_em     TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- ------------------------------------------------------------
-- SETOR
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS setor (
    id                BIGSERIAL       PRIMARY KEY,
    evento_id         BIGINT          NOT NULL REFERENCES evento(id) ON DELETE CASCADE,
    nome              VARCHAR(100)    NOT NULL,
    capacidade        INTEGER         NOT NULL CHECK (capacidade > 0),
    criado_em         TIMESTAMP       NOT NULL DEFAULT NOW(),
    UNIQUE (evento_id, nome)
);

-- ------------------------------------------------------------
-- TIPO_INGRESSO
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tipo_ingresso (
    id                BIGSERIAL       PRIMARY KEY,
    setor_id          BIGINT          NOT NULL REFERENCES setor(id) ON DELETE CASCADE,
    nome              VARCHAR(100)    NOT NULL,
    categoria         VARCHAR(20)     NOT NULL CHECK (categoria IN ('INTEIRA', 'MEIA', 'CORTESIA')),
    criado_em         TIMESTAMP       NOT NULL DEFAULT NOW(),
    UNIQUE (setor_id, nome)
);

-- ------------------------------------------------------------
-- LOTE
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS lote (
    id                    BIGSERIAL       PRIMARY KEY,
    tipo_ingresso_id      BIGINT          NOT NULL REFERENCES tipo_ingresso(id) ON DELETE CASCADE,
    numero_lote           INTEGER         NOT NULL,
    preco                 DECIMAL(10,2)   NOT NULL CHECK (preco >= 0),
    quantidade_total      INTEGER         NOT NULL CHECK (quantidade_total > 0),
    quantidade_disponivel INTEGER         NOT NULL CHECK (quantidade_disponivel >= 0),
    data_inicio           DATE            NOT NULL,
    data_fim              DATE            NOT NULL,
    ativo                 BOOLEAN         NOT NULL DEFAULT TRUE,
    criado_em             TIMESTAMP       NOT NULL DEFAULT NOW(),
    atualizado_em         TIMESTAMP       NOT NULL DEFAULT NOW(),
    UNIQUE (tipo_ingresso_id, numero_lote),
    CHECK (data_fim >= data_inicio),
    CHECK (quantidade_disponivel <= quantidade_total)
);

-- ------------------------------------------------------------
-- PARTICIPANTE
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS participante (
    id                BIGSERIAL       PRIMARY KEY,
    nome              VARCHAR(150)    NOT NULL,
    cpf               VARCHAR(14)     NOT NULL UNIQUE,
    telefone          VARCHAR(20),
    email             VARCHAR(150),
    criado_em         TIMESTAMP       NOT NULL DEFAULT NOW(),
    atualizado_em     TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- ------------------------------------------------------------
-- VENDA
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS venda (
    id                BIGSERIAL       PRIMARY KEY,
    usuario_id        BIGINT          NOT NULL REFERENCES usuario(id),
    evento_id         BIGINT          NOT NULL REFERENCES evento(id),
    forma_pagamento   VARCHAR(30)     NOT NULL
                                      CHECK (forma_pagamento IN ('DINHEIRO', 'CARTAO_DEBITO', 'CARTAO_CREDITO', 'PIX')),
    valor_total       DECIMAL(12,2)   NOT NULL CHECK (valor_total >= 0),
    status            VARCHAR(20)     NOT NULL DEFAULT 'CONFIRMADA'
                                      CHECK (status IN ('CONFIRMADA', 'CANCELADA')),
    data_venda        TIMESTAMP       NOT NULL DEFAULT NOW(),
    criado_em         TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- ------------------------------------------------------------
-- INGRESSO
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ingresso (
    id                    BIGSERIAL       PRIMARY KEY,
    codigo                UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    venda_id              BIGINT          NOT NULL REFERENCES venda(id) ON DELETE CASCADE,
    lote_id               BIGINT          NOT NULL REFERENCES lote(id),
    participante_id       BIGINT          REFERENCES participante(id),
    preco_pago            DECIMAL(10,2)   NOT NULL CHECK (preco_pago >= 0),
    status                VARCHAR(20)     NOT NULL DEFAULT 'ATIVO'
                                          CHECK (status IN ('ATIVO', 'EMITIDO', 'UTILIZADO', 'CANCELADO')),
    data_checkin          TIMESTAMP,
    motivo_cancelamento   VARCHAR(255),
    cancelado_por         BIGINT          REFERENCES usuario(id),
    data_cancelamento     TIMESTAMP,
    criado_em             TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- ------------------------------------------------------------
-- ÍNDICES
-- ------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_evento_situacao      ON evento(situacao);
CREATE INDEX IF NOT EXISTS idx_evento_data          ON evento(data_evento);
CREATE INDEX IF NOT EXISTS idx_setor_evento         ON setor(evento_id);
CREATE INDEX IF NOT EXISTS idx_tipo_ingresso_setor  ON tipo_ingresso(setor_id);
CREATE INDEX IF NOT EXISTS idx_lote_tipo_ingresso   ON lote(tipo_ingresso_id);
CREATE INDEX IF NOT EXISTS idx_venda_evento         ON venda(evento_id);
CREATE INDEX IF NOT EXISTS idx_venda_usuario        ON venda(usuario_id);
CREATE INDEX IF NOT EXISTS idx_ingresso_venda       ON ingresso(venda_id);
CREATE INDEX IF NOT EXISTS idx_ingresso_lote        ON ingresso(lote_id);
CREATE INDEX IF NOT EXISTS idx_ingresso_codigo      ON ingresso(codigo);
CREATE INDEX IF NOT EXISTS idx_ingresso_status      ON ingresso(status);
CREATE INDEX IF NOT EXISTS idx_participante_cpf     ON participante(cpf);

-- ------------------------------------------------------------
-- SEED: Usuário admin padrão (senha: admin123 -> $2a$10$sXuypSuRniqFM2VMY1qoPukL0.etTqE0I7Vfpa65xcogtiI7qtK8u)
-- ------------------------------------------------------------
INSERT INTO usuario (nome, login, senha_hash, perfil)
SELECT 'Administrador', 'admin', '$2a$10$sXuypSuRniqFM2VMY1qoPukL0.etTqE0I7Vfpa65xcogtiI7qtK8u', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE login = 'admin');
