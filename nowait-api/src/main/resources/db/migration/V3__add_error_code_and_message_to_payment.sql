ALTER TABLE payment
    ADD COLUMN error_code    VARCHAR(255),
    ADD COLUMN error_message VARCHAR(255);
