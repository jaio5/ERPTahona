UPDATE users
SET enabled = FALSE,
    requiere_cambio_password = TRUE,
    password = '{bcrypt}$2a$10$7EqJtq98hPqEX7fNZaFWoOhiYr0YHG6MlCezr2XbZYi1s4r8Wz.VS'
WHERE username = 'admin'
  AND password = 'admin';
