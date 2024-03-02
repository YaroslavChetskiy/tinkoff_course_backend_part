--liquibase formatted sql

--changeset chetskiy:create-schema
CREATE SCHEMA IF NOT EXISTS scrapper_schema;

--changeset chetskiy:create-table-chat
CREATE TABLE IF NOT EXISTS scrapper_schema.chat (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- type - тип ссылки, например GITHUB_REPO, GITHUB_ISSUES, STACKOVERFLOW_QUESTION

--changeset chetskiy:create-table-link
CREATE TABLE IF NOT EXISTS scrapper_schema.link (
    id BIGSERIAL PRIMARY KEY,
    url VARCHAR(255) NOT NULL,
    type VARCHAR(48),
    last_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- я думаю, что логично будет сделать many-to-many связь
--changeset chetskiy:create-association-table-chat-link
CREATE TABLE IF NOT EXISTS scrapper_schema.chat_link (
    chat_id BIGINT REFERENCES scrapper_schema.chat(id) ON DELETE CASCADE,
    link_id BIGINT REFERENCES scrapper_schema.link(id) ON DELETE CASCADE,
    PRIMARY KEY (chat_id, link_id)
);
