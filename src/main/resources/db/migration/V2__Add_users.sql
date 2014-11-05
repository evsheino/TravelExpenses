-- Insert three test users
INSERT INTO user(name, username, password, salt) VALUES
  ('John Doe', 'johnd', '$2a$10$ZnijUARkR/bAYxvvzHwZK.5i/YEy4tfd00dudIfixBfyg2hKjSBmy', '$2a$10$ZnijUARkR/bAYxvvzHwZK.'),
  ('Clint Eastwood', 'clinte', '$2a$10$93.ZW8mbuoU9kGGD71KilOALvfVu4rY.SgfP96AqVuIrAv9xNHZEq', '$2a$10$93.ZW8mbuoU9kGGD71KilO'),
  ('Foo Bar', 'foobar', '$2a$10$PoS5YHQgU5.EFN5qabXwrO424ZnmGGjkPlhpy9rVntL63Jw6ol1gm', '$2a$10$PoS5YHQgU5.EFN5qabXwrO');
