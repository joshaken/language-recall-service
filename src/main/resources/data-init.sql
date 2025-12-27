INSERT INTO sentences (sentence_type, content, level, create_time)
VALUES (1, '请给我一杯水。', 5, now()),
       (2, '这个问题我会尽快处理。', 4, now()),
       (3, '今天过得怎么样？', 5, now())
;

INSERT INTO users (USERNAME, CURRENT_SENTENCE_ID, MODE)
VALUES ('tap', 1, 1)
;
