-- Payment
CREATE TABLE payment
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id  BIGINT      NOT NULL,
    user_id     BIGINT      NOT NULL,
    payment_key VARCHAR(255),
    status      VARCHAR(30) NOT NULL,
    amount      INT         NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP
);
