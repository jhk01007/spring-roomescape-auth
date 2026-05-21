-- 조회 기간: 2026-04-29 ~ 2026-05-05
-- 기대 순위:
-- Theme 1: 기간 내 예약 10개
-- Theme 2: 기간 내 예약 9개
-- Theme 3: 기간 내 예약 8개
-- Theme 4: 기간 내 예약 7개
-- Theme 5: 기간 내 예약 6개
-- Theme 6: 기간 내 예약 5개
-- Theme 7: 기간 내 예약 4개
-- Theme 8: 기간 내 예약 3개
-- Theme 9: 기간 내 예약 2개
-- Theme 10: 기간 내 예약 1개
-- Theme 11: 기간 밖 예약만 있음
-- Theme 12: 예약 없음

MERGE INTO store KEY(id)
VALUES (1);

INSERT INTO theme (id, store_id, name, description, thumbnail)
VALUES (1, 1, 'Theme 1', 'Popular theme rank 1', 'https://example.com/theme-1.png'),
       (2, 1, 'Theme 2', 'Popular theme rank 2', 'https://example.com/theme-2.png'),
       (3, 1, 'Theme 3', 'Popular theme rank 3', 'https://example.com/theme-3.png'),
       (4, 1, 'Theme 4', 'Popular theme rank 4', 'https://example.com/theme-4.png'),
       (5, 1, 'Theme 5', 'Popular theme rank 5', 'https://example.com/theme-5.png'),
       (6, 1, 'Theme 6', 'Popular theme rank 6', 'https://example.com/theme-6.png'),
       (7, 1, 'Theme 7', 'Popular theme rank 7', 'https://example.com/theme-7.png'),
       (8, 1, 'Theme 8', 'Popular theme rank 8', 'https://example.com/theme-8.png'),
       (9, 1, 'Theme 9', 'Popular theme rank 9', 'https://example.com/theme-9.png'),
       (10, 1, 'Theme 10', 'Popular theme rank 10', 'https://example.com/theme-10.png'),
       (11, 1, 'Theme 11', 'Out of range reservations only', 'https://example.com/theme-11.png'),
       (12, 1, 'Theme 12', 'No reservations', 'https://example.com/theme-12.png');

INSERT INTO reservation_time (id, store_id, start_at)
VALUES (1, 1, '10:00:00'),
       (2, 1, '12:00:00'),
       (3, 1, '14:00:00'),
       (4, 1, '16:00:00'),
       (5, 1, '18:00:00'),
       (6, 1, '20:00:00');

INSERT INTO member (id, nickname, login_id, password, role)
VALUES        (1, 'guest1', 'login1', 'password1', 'USER'),
       (2, 'guest2', 'login2', 'password1', 'USER'),
       (3, 'guest3', 'login3', 'password1', 'USER'),
       (4, 'guest4', 'login4', 'password1', 'USER'),
       (5, 'guest5', 'login5', 'password1', 'USER'),
       (6, 'guest6', 'login6', 'password1', 'USER'),
       (7, 'guest7', 'login7', 'password1', 'USER'),
       (8, 'guest8', 'login8', 'password1', 'USER'),
       (9, 'guest9', 'login9', 'password1', 'USER'),
       (10, 'guest10', 'login10', 'password1', 'USER'),
       (11, 'guest11', 'login11', 'password1', 'USER'),
       (12, 'guest12', 'login12', 'password1', 'USER'),
       (13, 'guest13', 'login13', 'password1', 'USER'),
       (14, 'guest14', 'login14', 'password1', 'USER'),
       (15, 'guest15', 'login15', 'password1', 'USER'),
       (16, 'guest16', 'login16', 'password1', 'USER'),
       (17, 'guest17', 'login17', 'password1', 'USER'),
       (18, 'guest18', 'login18', 'password1', 'USER'),
       (19, 'guest19', 'login19', 'password1', 'USER'),
       (20, 'guest20', 'login20', 'password1', 'USER'),
       (21, 'guest21', 'login21', 'password1', 'USER'),
       (22, 'guest22', 'login22', 'password1', 'USER'),
       (23, 'guest23', 'login23', 'password1', 'USER'),
       (24, 'guest24', 'login24', 'password1', 'USER'),
       (25, 'guest25', 'login25', 'password1', 'USER'),
       (26, 'guest26', 'login26', 'password1', 'USER'),
       (27, 'guest27', 'login27', 'password1', 'USER'),
       (28, 'guest28', 'login28', 'password1', 'USER'),
       (29, 'guest29', 'login29', 'password1', 'USER'),
       (30, 'guest30', 'login30', 'password1', 'USER'),
       (31, 'guest31', 'login31', 'password1', 'USER'),
       (32, 'guest32', 'login32', 'password1', 'USER'),
       (33, 'guest33', 'login33', 'password1', 'USER'),
       (34, 'guest34', 'login34', 'password1', 'USER'),
       (35, 'guest35', 'login35', 'password1', 'USER'),
       (36, 'guest36', 'login36', 'password1', 'USER'),
       (37, 'guest37', 'login37', 'password1', 'USER'),
       (38, 'guest38', 'login38', 'password1', 'USER'),
       (39, 'guest39', 'login39', 'password1', 'USER'),
       (40, 'guest40', 'login40', 'password1', 'USER'),
       (41, 'guest41', 'login41', 'password1', 'USER'),
       (42, 'guest42', 'login42', 'password1', 'USER'),
       (43, 'guest43', 'login43', 'password1', 'USER'),
       (44, 'guest44', 'login44', 'password1', 'USER'),
       (45, 'guest45', 'login45', 'password1', 'USER'),
       (46, 'guest46', 'login46', 'password1', 'USER'),
       (47, 'guest47', 'login47', 'password1', 'USER'),
       (48, 'guest48', 'login48', 'password1', 'USER'),
       (49, 'guest49', 'login49', 'password1', 'USER'),
       (50, 'guest50', 'login50', 'password1', 'USER'),
       (51, 'guest51', 'login51', 'password1', 'USER'),
       (52, 'guest52', 'login52', 'password1', 'USER'),
       (53, 'guest53', 'login53', 'password1', 'USER'),
       (54, 'guest54', 'login54', 'password1', 'USER'),
       (55, 'guest55', 'login55', 'password1', 'USER'),
       (56, 'guest56', 'login56', 'password1', 'USER'),
       (57, 'guest57', 'login57', 'password1', 'USER'),
       (58, 'guest58', 'login58', 'password1', 'USER');


-- Theme 1: 기간 내 예약 10개
INSERT INTO reservation (id, guest_id, date, time_id, theme_id)
VALUES (1, 1, '2026-04-29', 1, 1),
       (2, 2, '2026-04-29', 2, 1),
       (3, 3, '2026-04-30', 1, 1),
       (4, 4, '2026-04-30', 2, 1),
       (5, 5, '2026-05-01', 1, 1),
       (6, 6, '2026-05-01', 2, 1),
       (7, 7, '2026-05-02', 1, 1),
       (8, 8, '2026-05-03', 1, 1),
       (9, 9, '2026-05-04', 1, 1),
       (10, 10, '2026-05-05', 1, 1);

-- Theme 2: 기간 내 예약 9개
INSERT INTO reservation (id, guest_id, date, time_id, theme_id)
VALUES (11, 11, '2026-04-29', 1, 2),
       (12, 12, '2026-04-29', 2, 2),
       (13, 13, '2026-04-30', 1, 2),
       (14, 14, '2026-04-30', 2, 2),
       (15, 15, '2026-05-01', 1, 2),
       (16, 16, '2026-05-01', 2, 2),
       (17, 17, '2026-05-02', 1, 2),
       (18, 18, '2026-05-03', 1, 2),
       (19, 19, '2026-05-04', 1, 2);

-- Theme 3: 기간 내 예약 8개
INSERT INTO reservation (id, guest_id, date, time_id, theme_id)
VALUES (20, 20, '2026-04-29', 1, 3),
       (21, 21, '2026-04-29', 2, 3),
       (22, 22, '2026-04-30', 1, 3),
       (23, 23, '2026-04-30', 2, 3),
       (24, 24, '2026-05-01', 1, 3),
       (25, 25, '2026-05-01', 2, 3),
       (26, 26, '2026-05-02', 1, 3),
       (27, 27, '2026-05-03', 1, 3);

-- Theme 4: 기간 내 예약 7개
INSERT INTO reservation (id, guest_id, date, time_id, theme_id)
VALUES (28, 28, '2026-04-29', 1, 4),
       (29, 29, '2026-04-29', 2, 4),
       (30, 30, '2026-04-30', 1, 4),
       (31, 31, '2026-04-30', 2, 4),
       (32, 32, '2026-05-01', 1, 4),
       (33, 33, '2026-05-01', 2, 4),
       (34, 34, '2026-05-02', 1, 4);

-- Theme 5: 기간 내 예약 6개
INSERT INTO reservation (id, guest_id, date, time_id, theme_id)
VALUES (35, 35, '2026-04-29', 1, 5),
       (36, 36, '2026-04-29', 2, 5),
       (37, 37, '2026-04-30', 1, 5),
       (38, 38, '2026-04-30', 2, 5),
       (39, 39, '2026-05-01', 1, 5),
       (40, 40, '2026-05-01', 2, 5);

-- Theme 6: 기간 내 예약 5개
INSERT INTO reservation (id, guest_id, date, time_id, theme_id)
VALUES (41, 41, '2026-04-29', 1, 6),
       (42, 42, '2026-04-29', 2, 6),
       (43, 43, '2026-04-30', 1, 6),
       (44, 44, '2026-04-30', 2, 6),
       (45, 45, '2026-05-01', 1, 6);

-- Theme 7: 기간 내 예약 4개
INSERT INTO reservation (id, guest_id, date, time_id, theme_id)
VALUES (46, 46, '2026-04-29', 1, 7),
       (47, 47, '2026-04-29', 2, 7),
       (48, 48, '2026-04-30', 1, 7),
       (49, 49, '2026-04-30', 2, 7);

-- Theme 8: 기간 내 예약 3개
INSERT INTO reservation (id, guest_id, date, time_id, theme_id)
VALUES (50, 50, '2026-04-29', 1, 8),
       (51, 51, '2026-04-29', 2, 8),
       (52, 52, '2026-04-30', 1, 8);

-- Theme 9: 기간 내 예약 2개
INSERT INTO reservation (id, guest_id, date, time_id, theme_id)
VALUES (53, 53, '2026-04-29', 1, 9),
       (54, 54, '2026-04-29', 2, 9);

-- Theme 10: 기간 내 예약 1개
INSERT INTO reservation (id, guest_id, date, time_id, theme_id)
VALUES (55, 55, '2026-04-29', 1, 10);

-- Theme 11: 기간 밖 예약만 있음
INSERT INTO reservation (id, guest_id, date, time_id, theme_id)
VALUES (56, 56, '2026-04-28', 1, 11),
       (57, 57, '2026-05-06', 1, 11),
       (58, 58, '2026-05-06', 2, 11);

-- Theme 12: 예약 없음

ALTER TABLE theme
    ALTER COLUMN id RESTART WITH 13;
ALTER TABLE store
    ALTER COLUMN id RESTART WITH 2;
ALTER TABLE reservation_time
    ALTER COLUMN id RESTART WITH 7;
ALTER TABLE member
    ALTER COLUMN id RESTART WITH 59;
ALTER TABLE reservation
    ALTER COLUMN id RESTART WITH 59;
