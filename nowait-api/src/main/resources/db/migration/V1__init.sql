-- Users
CREATE TABLE users
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    nickname   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP
);

-- Place
CREATE TABLE place
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(255) NOT NULL,
    description  TEXT,
    type         VARCHAR(30)  NOT NULL,
    phone_number VARCHAR(20),
    old_address  VARCHAR(255),
    road_address VARCHAR(255),
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP
);

-- BookingSlot
CREATE TABLE booking_slot
(
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    place_id          BIGINT    NOT NULL,
    table_id          BIGINT    NOT NULL,
    date              DATE      NOT NULL,
    time              TIME      NOT NULL,
    is_booked         BOOLEAN   NOT NULL,
    deposit_required  BOOLEAN   NOT NULL,
    confirm_required  BOOLEAN   NOT NULL,
    deposit_policy_id BIGINT,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP
);

-- Booking
CREATE TABLE booking
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_slot_id BIGINT      NOT NULL,
    user_id         BIGINT      NOT NULL,
    status          VARCHAR(30) NOT NULL,
    party_size      INT,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP
);
