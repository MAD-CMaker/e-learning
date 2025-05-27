-- Seleciona o banco de dados para garantir que as tabelas sejam criadas no lugar certo
USE ensinoremoto_tcc;


CREATE TABLE IF NOT EXISTS users (
                                     user_id INT AUTO_INCREMENT PRIMARY KEY,
                                     name VARCHAR(255) NOT NULL,
                                     email VARCHAR(255) NOT NULL,
                                     password VARCHAR(255) NOT NULL,
                                     user_type VARCHAR(50),
                                     specialization VARCHAR(50),
                                     register_hour TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela de Cursos
CREATE TABLE IF NOT EXISTS courses (
                                       course_id INT AUTO_INCREMENT PRIMARY KEY,
                                       title VARCHAR(255) NOT NULL,
                                       description TEXT,
                                       responsible_professor_id INT NOT NULL,
                                       price DECIMAL(10, 2) DEFAULT 0.00,
                                       video_presentation_url VARCHAR(500) DEFAULT 'https://www.youtube.com/embed' NOT NULL,
                                       category VARCHAR(100),
                                       load_hour INT,
                                       creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       FOREIGN KEY (responsible_professor_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela de Aulas
CREATE TABLE IF NOT EXISTS classes (
                                       classroom_id INT AUTO_INCREMENT PRIMARY KEY,
                                       course_id INT NOT NULL,
                                       title VARCHAR(255) NOT NULL,
                                       description TEXT,
                                       content_url VARCHAR(500),
                                       sequence INT DEFAULT 0,
                                       FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE -- Se o curso for deletado, suas aulas são deletadas
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela de Exercícios
CREATE TABLE IF NOT EXISTS exercises (
                                         exercise_id INT AUTO_INCREMENT PRIMARY KEY,
                                         classroom_id INT NOT NULL,
                                         statement TEXT NOT NULL,
                                         exercise_type ENUM('MULTIPLA_ESCOLHA', 'DISSERTATIVA', 'PRATICA_CODIGO') NOT NULL,
                                         correct_answer TEXT NULL,
                                         FOREIGN KEY (classroom_id) REFERENCES classes(classroom_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela de Comentários em Cursos
CREATE TABLE IF NOT EXISTS courses_comments (
                                                comment_id INT AUTO_INCREMENT PRIMARY KEY,
                                                course_id INT NOT NULL,
                                                student_id INT NOT NULL,
                                                text TEXT NOT NULL,
                                                hour_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
                                                FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela de Dúvidas em Cursos
CREATE TABLE IF NOT EXISTS courses_doubts (
                                              doubt_id INT AUTO_INCREMENT PRIMARY KEY,
                                              course_id INT NOT NULL,
                                              student_id INT NOT NULL,
                                              title VARCHAR(255) NOT NULL,
                                              description TEXT NOT NULL,
                                              creation_hour_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              answer TEXT NULL,
                                              answer_hour_date TIMESTAMP NULL,
                                              answer_professor_id INT NULL,
                                              status ENUM('ABERTA', 'RESPONDIDA', 'FECHADA') DEFAULT 'ABERTA',
                                              FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
                                              FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                              FOREIGN KEY (answer_professor_id ) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS exam_definitions (
                                                id_exam_definition INT AUTO_INCREMENT PRIMARY KEY,
                                                id_course INT NOT NULL,
                                                title VARCHAR(255) NOT NULL,
                                                description TEXT,
                                                creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                FOREIGN KEY (id_course) REFERENCES courses(course_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela de Avaliações de Cursos
CREATE TABLE IF NOT EXISTS exams_courses (
                                             exam_id INT AUTO_INCREMENT PRIMARY KEY,
                                             course_id INT NOT NULL,
                                             id_exam_definition INT NOT NULL,
                                             student_id INT NOT NULL,
                                             grade INT NOT NULL CHECK (grade >= 0 AND grade <= 10),
                                             comment TEXT,
                                             hour_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
                                             FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                             FOREIGN KEY (id_exam_definition) REFERENCES exam_definitions(id_exam_definition) ON DELETE CASCADE,
                                             UNIQUE KEY uk_student_exam_definition_attempt (student_id, id_exam_definition)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela de Inscrições na Newsletter
CREATE TABLE IF NOT EXISTS newsletter_inscription (
                                                      inscription_id INT AUTO_INCREMENT PRIMARY KEY,
                                                      email VARCHAR(255) NOT NULL UNIQUE,
                                                      inscription_hour TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                      active BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS exam_questions (
                                              id_exam_question INT AUTO_INCREMENT PRIMARY KEY,
                                              id_exam_definition INT NOT NULL,
                                              statement TEXT NOT NULL,
                                              exercise_type ENUM('MULTIPLA_ESCOLHA', 'DISSERTATIVA', 'PRATICA_CODIGO') NOT NULL,
                                              options TEXT NULL,
                                              correct_answer TEXT NULL,
                                              grade DECIMAL(5,2) DEFAULT 0.00,
                                              exam_sequence INT DEFAULT 0,
                                              published BOOLEAN DEFAULT FALSE NOT NULL,
                                              FOREIGN KEY (id_exam_definition) REFERENCES exam_definitions(id_exam_definition) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela de Perguntas de Visitantes (Perguntas Gerais)
CREATE TABLE IF NOT EXISTS visitors_questions (
                                                  question_id INT AUTO_INCREMENT PRIMARY KEY,
                                                  visitor_name VARCHAR(255),
                                                  visitor_email VARCHAR(255),
                                                  question_text TEXT NOT NULL,
                                                  question_hour_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                  answer TEXT NULL,
                                                  answer_hour_date TIMESTAMP NULL,
                                                  professor_responsible_id INT NULL, -- FK para Usuario (Professor)
                                                  FOREIGN KEY (professor_responsible_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela de Associação: Students e Cursos (Matrículas)
CREATE TABLE IF NOT EXISTS students_courses (
                                                student_id INT NOT NULL,
                                                course_id INT NOT NULL,
                                                enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                progress DOUBLE DEFAULT 0.0,
                                                PRIMARY KEY (student_id, course_id),
                                                FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                                FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Tabela de Respostas de Exercícios por Alunos (Opcional, dependendo da complexidade)
CREATE TABLE IF NOT EXISTS student_exercise_answers (
                                                        id_student_answer_exercise INT AUTO_INCREMENT PRIMARY KEY,
                                                        id_exercise INT NOT NULL,
                                                        id_student INT NOT NULL,
                                                        id_class INT NOT NULL,
                                                        id_course INT NOT NULL,
                                                        answer_text TEXT,
                                                        send_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                        is_correct BOOLEAN NULL,
                                                        grade DECIMAL(5,2) NULL,
                                                        feedback_professor TEXT NULL,
                                                        FOREIGN KEY (id_exercise) REFERENCES exercises(exercise_id) ON DELETE CASCADE,
                                                        FOREIGN KEY (id_student) REFERENCES users(user_id) ON DELETE CASCADE,
                                                        FOREIGN KEY (id_class) REFERENCES classes(classroom_id) ON DELETE CASCADE,
                                                        FOREIGN KEY (id_course) REFERENCES courses(course_id) ON DELETE CASCADE,
                                                        UNIQUE KEY uk_student_exercise_answer (id_student, id_exercise)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices para otimização de consultas (exemplos)
CREATE INDEX idx_courses_title ON courses(title);
CREATE INDEX idx_classes_id_course ON classes(course_id);
CREATE INDEX idx_users_email ON users(email);


-- Adicionando alguns dados iniciais (opcional, para teste)
INSERT INTO users (email, password, user_type, specialization, register_hour, name) VALUES
                                                                                        ('admin@ensinoremoto.com', 'senha_hasheada_admin', 'PROFESSOR', 'Administração do Sistema',CURRENT_TIMESTAMP ,'Professor Admin'),
                                                                                        ('aluno@exemplo.com', 'senha_hasheada_aluno', 'STUDENT', NULL, CURRENT_TIMESTAMP,'Aluno Exemplo');

INSERT INTO courses (title, description, responsible_professor_id , price, category, load_hour) VALUES
                                                                                                    ('Java para Iniciantes', 'Aprenda os fundamentos de Java.', 1, 99.90, 'Programação', 40),
                                                                                                    ('MySQL Básico', 'Introdução ao banco de dados MySQL.', 1, 79.50, 'Banco de Dados', 20);

# INSERT INTO courses (z, id_curso) VALUES
#     (2, 1); -- Aluno Exemplo matriculado em Java para Iniciantes