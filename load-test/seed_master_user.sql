-- 마스터 유저 1명 생성 (이미 존재하면 무시)
INSERT INTO p_users (
    user_id,
    affiliation_id,
    affiliation_type,
    name,
    password,
    slack_id,
    phone,
    email,
    role,
    status,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted_at,
    deleted_by
)
VALUES (
    1,
    NULL,
    'LOGISTICS',
    'master',
    '$2a$12$oRRDQZFsQVc6TKjti3KzNOPXU3aC4JX88WyUTgOYxJPgE98XDp8YK', -- Password123!
    'UMASTER001',
    '010-0000-0001',
    'master@klp.com',
    'MASTER',
    'APPROVED',
    NOW(),
    NOW(),
    1,
    1,
    NULL,
    NULL
)
ON CONFLICT (user_id) DO NOTHING;
