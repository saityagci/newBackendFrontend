-- Agent table
CREATE TABLE agent (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    description VARCHAR(1000),
    client_id BIGINT NOT NULL,
