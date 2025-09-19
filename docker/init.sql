-- Conectarse a la base para crear esquema y tablas
CREATE SCHEMA IF NOT EXISTS auth AUTHORIZATION auth_user;

CREATE TABLE auth.role (
    uniqueid SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE auth.users (
    id_user SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    identity_document VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(50),
    base_salary NUMERIC(12,2),
    password VARCHAR(200) NOT NULL,
    id_role INT NOT NULL,
    CONSTRAINT fk_user_role FOREIGN KEY (id_role) REFERENCES auth.role(uniqueid)
);

INSERT INTO auth.role (name, description) VALUES ('Administrador', 'Usuario con todos los permisos');
INSERT INTO auth.role (name, description) VALUES ('Cliente', 'Usuario cliente');
INSERT INTO auth.role (name, description) VALUES ('Asesor', 'Usuario asesor');

INSERT INTO auth.users (
    first_name, last_name, email, identity_document, phone, base_salary, password, id_role
) VALUES (
    'Juan', 'Pérez', 'juan.perez@example.com', 'DNI123456', '+123456789', 2500.00, '$2a$10$sNg11W1YXhBd8kvh0UkFceAb4R/zvEZS6zaknEebNn7aY9PadttOG', 1
);
