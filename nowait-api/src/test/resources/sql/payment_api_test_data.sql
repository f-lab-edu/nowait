-- User
INSERT INTO users (id, nickname, created_at, updated_at)
VALUES (1, '정의진', '2024-11-01 00:00:00', '2024-11-01 00:00:00');

-- Place
INSERT INTO place (id, name, description, type, old_address, road_address, created_at, updated_at)
VALUES (1, '모수', '한남동 안성재 셰프의 감각적인 미슐랭 3스타 파인다이닝', 'RESTAURANT', '한남동 738-11',
        '서울 용산구 이태원로55가길 45', '2024-11-01 00:00:00', '2024-11-01 00:00:00');

-- BookingSlot
INSERT INTO booking_slot (id, place_id, table_id, date, time, is_booked, deposit_required,
                          confirm_required, deposit_policy_id, created_at, updated_at)
VALUES (1, 1, 1, '2024-12-25', '18:00:00', false, true, true, 1, '2024-11-01 00:00:00',
        '2024-11-01 00:00:00');

-- Bookin
INSERT INTO booking (id, booking_slot_id, user_id, status, party_size, created_at, updated_at)
VALUES (1, 1, 1, 'PENDING_PAYMENT', '2', '2024-11-02 00:00:00', '2024-11-02 00:00:00');
