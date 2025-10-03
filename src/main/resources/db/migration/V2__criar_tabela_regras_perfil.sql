CREATE TABLE tb_rules (
    id SERIAL PRIMARY KEY,
    category VARCHAR(50),
    risk_classification VARCHAR(50) NOT NULL,
    insured_amount_limit BIGINT NOT NULL
);

CREATE INDEX idx_tb_rules_risk_classification ON tb_rules(risk_classification);

INSERT INTO tb_rules (insured_amount_limit, risk_classification, category) VALUES
(50000000, 'LOW_RISK', 'LIFE'),
(50000000, 'LOW_RISK', 'HOME'),
(35000000, 'LOW_RISK', 'AUTO'),
(25500000, 'LOW_RISK', null),
(25000000, 'HIGH_RISK', 'AUTO'),
(15000000, 'HIGH_RISK', 'HOME'),
(12500000, 'HIGH_RISK', null),
(80000000, 'MEDIUM_RISK', 'LIFE'),
(45000000, 'MEDIUM_RISK', 'AUTO'),
(45000000, 'MEDIUM_RISK', 'HOME'),
(37500000, 'MEDIUM_RISK', null),
(20000000, 'UNCLASSIFIED_RISK', 'LIFE'),
(20000000, 'UNCLASSIFIED_RISK', 'HOME'),
(7500000 , 'UNCLASSIFIED_RISK', 'AUTO'),
(5500000, 'UNCLASSIFIED_RISK', null);