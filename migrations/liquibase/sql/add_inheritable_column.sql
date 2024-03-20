--changeset chetskiy:add-answer-count-column
ALTER TABLE scrapper_schema.link
ADD COLUMN answer_count INT;
