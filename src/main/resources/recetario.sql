DROP DATABASE IF EXISTS recetario;
CREATE DATABASE recetario;
USE recetario;

CREATE TABLE recetas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    tiempo_preparacion INT NOT NULL,
    dificultad VARCHAR(20) NOT NULL,
    raciones INT NOT NULL
);

CREATE TABLE estadisticas (
    id INT PRIMARY KEY,
    total_recetas INT NOT NULL
);

INSERT INTO recetas (nombre, categoria, tiempo_preparacion, dificultad, raciones)
VALUES
('Tortilla de patatas', 'Principal', 30, 'Fácil', 4),
('Ensalada César', 'Ensalada', 15, 'Fácil', 2),
('Lasaña', 'Pasta', 75, 'Media', 6),
('Gazpacho', 'Sopa', 20, 'Fácil', 4),
('Tarta de queso', 'Postre', 90, 'Media', 8),
('Paella', 'Arroz', 60, 'Difícil', 6),
('Croquetas de jamón', 'Entrante', 45, 'Media', 4),
('Brownie', 'Postre', 40, 'Fácil', 8);

INSERT INTO estadisticas (id, total_recetas) VALUES (1, 8);

DELIMITER //
CREATE PROCEDURE buscarReceta(IN p_nombre VARCHAR(100))
BEGIN
    SELECT id, nombre, categoria, tiempo_preparacion, dificultad, raciones
    FROM recetas
    WHERE nombre = p_nombre;
END //
DELIMITER ;
