-- Desbloqueo automático de usuarios: contador de bloqueos acumulados.
-- (users.fecha_bloqueo ya existe desde V1)
ALTER TABLE users ADD COLUMN contador_bloqueos INT DEFAULT 0;
