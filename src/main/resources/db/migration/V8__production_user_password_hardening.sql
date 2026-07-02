ALTER TABLE users MODIFY password VARCHAR(4096) NOT NULL;

UPDATE users
SET requiere_cambio_password = TRUE
WHERE username = 'admin'
  AND password = 'admin';
