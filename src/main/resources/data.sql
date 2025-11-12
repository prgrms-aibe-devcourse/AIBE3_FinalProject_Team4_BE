INSERT INTO users (username, password, email)
VALUES ('user1', '1234', 'user1@test.com');

INSERT INTO hashtags (name)
VALUES ('#운동'),
       ('아키텍쳐');

INSERT INTO blogs (user_id, title, content, status, view_count, created_at)
VALUES (1, '첫 번째 블로그 글', '새로운 운동을 시작했습니다.', 'PUBLISHED', 0, NOW());

INSERT INTO blogs (user_id, title, content, status, view_count, created_at)
VALUES (1, '두 번째 글', '좋은 아키텍쳐에 대하여', 'PUBLISHED', 10, NOW());

INSERT INTO blog_hashtags (blog_id, hashtag_id)
VALUES (1, 1),
       (2, 2);

