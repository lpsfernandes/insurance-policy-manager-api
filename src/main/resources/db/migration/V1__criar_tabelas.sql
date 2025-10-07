-- Criação da tabela de apolices
CREATE TABLE tb_policies (
    id VARCHAR PRIMARY KEY,
    client_id VARCHAR NOT NULL,
    product_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    risk_classification VARCHAR(50),
    sales_channel VARCHAR(50),
    payment_method VARCHAR(50) NOT NULL,
    insured_amount BIGINT NOT NULL,
    monthly_premium BIGINT NOT NULL,
    processing_status VARCHAR(50) NOT NULL,
    payment_date TIMESTAMP,
    subscription_date TIMESTAMP,
    reason VARCHAR,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at TIMESTAMP,
    status VARCHAR NOT NULL
);

CREATE INDEX idx_tb_policies_client_id ON tb_policies(client_id);
CREATE INDEX idx_tb_policies_status ON tb_policies(status);
CREATE INDEX idx_tb_policies_created_at ON tb_policies(created_at);
CREATE INDEX idx_tb_policies_status_created_at ON tb_policies(status, created_at);
CREATE INDEX idx_tb_policies_processing_status ON tb_policies(processing_status);

-- Criação da tabela de assistencias
CREATE TABLE tb_assistances (
    id SERIAL PRIMARY KEY,
    policy_id VARCHAR NOT NULL,
    assistance VARCHAR NOT NULL,
    CONSTRAINT fk_tb_assistances_policy
        FOREIGN KEY (policy_id)
        REFERENCES tb_policies(id)
        ON DELETE CASCADE
);

-- Criação da tabela de coberturas
CREATE TABLE tb_coverage (
    id SERIAL PRIMARY KEY,
    policy_id VARCHAR NOT NULL,
    type_coverage VARCHAR NOT NULL,
    insured_amount BIGINT NOT NULL,
    CONSTRAINT fk_tb_coverage_policy
        FOREIGN KEY (policy_id)
        REFERENCES tb_policies(id)
        ON DELETE CASCADE
);

-- Criação da tabela de historico de status
CREATE TABLE tb_status_history (
    id SERIAL PRIMARY KEY,
    policy_id VARCHAR NOT NULL,
    status VARCHAR NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tb_status_history_policy
        FOREIGN KEY (policy_id)
        REFERENCES tb_policies(id)
        ON DELETE CASCADE
);

-- Criação da tabela de historico de eventos
CREATE TABLE tb_outbox_event (
    id SERIAL PRIMARY KEY,
    event_json VARCHAR NOT NULL,
    max_processing_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tb_outbox_event_created_at ON tb_outbox_event(created_at);

-- Criação da tabela de resultado da analise de risco
CREATE TABLE tb_risk_analysis (
    id SERIAL PRIMARY KEY,
    order_id VARCHAR NOT NULL,
    policy_id VARCHAR NOT NULL,
    client_id VARCHAR NOT NULL,
    classification VARCHAR NOT NULL,
    analyzed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tb_risk_analysis_policy
        FOREIGN KEY (policy_id)
        REFERENCES tb_policies(id)
        ON DELETE CASCADE
);


-- Criação da tabela de ocorrencias
CREATE TABLE tb_occurrences (
    id SERIAL PRIMARY KEY,
    occurrence_id VARCHAR NOT NULL,
    risk_analysis_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    type VARCHAR NOT NULL,
    description VARCHAR NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tb_risk_analysis_occurrences
        FOREIGN KEY (risk_analysis_id)
        REFERENCES tb_risk_analysis(id)
        ON DELETE CASCADE
);
