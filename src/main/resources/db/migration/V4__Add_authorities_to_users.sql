-- Regular user
INSERT INTO authority (user_id, authority) VALUES ( (SELECT id FROM user WHERE username='johnd'), 'USER');

-- Supervisor
INSERT INTO authority (user_id, authority) VALUES ( (SELECT id FROM user WHERE username='foobar'), 'USER');
INSERT INTO authority (user_id, authority) VALUES ( (SELECT id FROM user WHERE username='foobar'), 'SUPERVISOR');

-- User with all authorities
INSERT INTO authority (user_id, authority) VALUES ( (SELECT id FROM user WHERE username='clinte'), 'USER');
INSERT INTO authority (user_id, authority) VALUES ( (SELECT id FROM user WHERE username='clinte'), 'SUPERVISOR');
INSERT INTO authority (user_id, authority) VALUES ( (SELECT id FROM user WHERE username='clinte'), 'ADMIN');
