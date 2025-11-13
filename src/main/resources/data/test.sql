INSERT INTO users (id, email, username, password, nickname, date_of_birth, gender)
VALUES (100, 'test@example.com', 'user1', '$2b$12$FSADIv4S.sk5UMH9RtdVDescoQ5xHdfyxiuX74fqxrZABxdeweGwO', 'TestNick',
        '2025-11-12', 'MALE');

INSERT INTO blogs (id, user_id, title, content, status, created_at, modified_at)
VALUES (100, 100, '블로그 제목', '테스트용입니다.', 'PUBLISHED', NOW(), NOW());