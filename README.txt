=============================================================================
DOCUMENTAÇÃO DO PROJETO: PLATAFORMA DE ENSINO REMOTO
=============================================================================

1. INTRODUÇÃO
-----------------------------------------------------------------------------
Nome do Projeto: Plataforma de Ensino Remoto (Ex: Remote Ensine / E-Learning)
Descrição Breve: Uma aplicação web desenvolvida como um sistema de gerenciamento de aprendizado (LMS) leve, permitindo que Professores criem e gerenciem conteúdo educacional (cursos, aulas, exercícios, provas) e que Alunos se inscrevam em cursos, consumam o conteúdo e interajam com a plataforma e instrutores.
Propósito: Desenvolvido como Trabalho de Conclusão de Curso (TCC), com foco na aplicação de conceitos de desenvolvimento web full-stack utilizando Java e tecnologias associadas.

2. FUNCIONALIDADES IMPLEMENTADAS
-----------------------------------------------------------------------------
* Gerenciamento de Usuários:
    - Cadastro de novos usuários (distinção entre Aluno e Professor).
    - Autenticação de usuários (Login).
    - Logout do sistema.
    - Visualização do perfil do usuário logado.

* Gerenciamento de Cursos (Perspectiva do Professor):
    - CRUD para Cursos (Create, Read, Update basic info, Delete).
    - Gerenciamento de Aulas (Classrooms) dentro de um Curso.
    - Gerenciamento de Exercícios (Exercises) dentro de cada Aula.
    - Gerenciamento de Definições de Exame/Prova (ExamDefinition) para um Curso, incluindo:
        - Criação (título, descrição).
        - Publicação/Despublicação.
        - Deleção.
        - Adição e gerenciamento de Questões de Exame (ExamQuestion) específicas para cada ExamDefinition.

* Experiência do Aluno:
    - Listagem e visualização dos detalhes dos cursos disponíveis.
    - Matrícula em cursos.
    - Acesso e visualização do conteúdo das Aulas de cursos matriculados (incluindo vídeos).
    - Visualização e submissão de respostas para Exercícios dentro das Aulas.
    - Visualização de Provas (ExamDefinition) publicadas para um curso.
    - Realização de Provas (visualização das ExamQuestions, submissão de respostas e registro da tentativa/resultado usando a classe Exam.java).
    - Interação com Cursos: Adicionar Comentários e criar Dúvidas.
    - Realizar Avaliação do Curso (submissão de nota e comentário sobre o curso, usando a classe Exam.java do aluno).

* Interação do Professor:
    - Responder Dúvidas de Alunos.
    - Visualizar e Responder Perguntas de Visitantes.

* Interação do Visitante:
    - Visualizar lista de cursos e detalhes de cursos.
    - Visualizar vídeos de apresentação dos cursos.
    - Enviar perguntas gerais (Formulário de Contato).

* Newsletter:
    - Alunos logados podem se inscrever/cancelar inscrição.

3. TECNOLOGIAS UTILIZADAS
-----------------------------------------------------------------------------
* Backend:
    - Java: Versão 17 (ou a utilizada no projeto)
    - Spring Boot: Versão 3.2.5 (ou a utilizada)
        - Spring MVC: Para controllers web.
        - Thymeleaf: Motor de templates.
    - JDBC: Para acesso direto ao banco de dados MySQL.
    - jBCrypt: Para hashing de senhas.

* Banco de Dados:
    - MySQL: Versão 8.0 (ou similar).

* Frontend:
    - HTML5
    - CSS3
    - Thymeleaf
    - JavaScript (mínimo)

* Ferramenta de Build:
    - Apache Maven

* Ambiente de Desenvolvimento:
    - IDE: IntelliJ IDEA, Eclipse, VS Code, etc.

4. ESTRUTURA DO PROJETO (VISÃO GERAL DOS PACOTES JAVA)
-----------------------------------------------------------------------------
- com.elearning.remoteensine (ou seu pacote raiz)
    - controller: (UserController, CourseController, ExamController, ProvaController, VisitorQuestionController, NewsletterController, etc.)
    - service: (UserService, CourseService, ExamDefinitionService, ExamQuestionService, ExamService, CommentService, DoubtService, ClassroomService, EnrollService, VisitorQuestionService, NewsletterService, etc.)
    - dao: (UserDAO, CourseDAO, ExamDefinitionDAO, ExamQuestionDAO, ExamDAO, CommentDAO, DoubtDAO, ClassroomDAO, EnrollDAO, StudentsExerciseAnswerDAO, VisitorQuestionDAO, NewsletterDAO, etc.)
    - model: (User, Student, Professor, Course, Classroom, Exercise, StudentsExerciseAnswer, ExamDefinition, ExamQuestion, Exam, Comment, Doubt, NewsletterInscricao, VisitorQuestion)
        - enums: (UserType, ExerciseType, DoubtStatus)
    - util: (DatabaseConnector.java)

* Recursos (src/main/resources/):
    - static/css/style.css
    - templates/
        - fragments/navbar.html
        - exam/ (form-exam-definition.html, gerenciar-questoes-exame.html, realizar-prova.html)
        - professor/ (lista-perguntas-visitantes.html)
        - (index.html, cursos.html, detalhes-curso.html, form-curso.html, login.html, cadastro.html, perfil-usuario.html, curso-aulas.html, ver-aula.html, form-aula.html, form-pergunta-visitante.html)
    - application.properties
    - db_scripts/schema.sql (Recomendado ter um script consolidado)

5. SCHEMA DO BANCO DE DADOS
-----------------------------------------------------------------------------
Recomenda-se consultar o arquivo `schema.sql` do projeto para a definição detalhada de todas as tabelas:
users (ou usuarios), courses (ou cursos), classrooms (ou aulas), exercises (ou exercicios), student_exercise_answers (ou respostas_exercicio_aluno),
 exam_definitions (ou definicoes_prova), exam_questions (ou questoes_exame), exams_courses (para tentativas/resultados de provas dos alunos),
  comments (ou comentarios_curso), doubts (ou duvidas_curso), student_courses (matrículas), newsletter_inscricoes, visitor_questions (ou perguntas_visitantes).

6. POSSÍVEIS MELHORIAS FUTURAS (Opcional)
-----------------------------------------------------------------------------
- Implementação de sistema de pagamento.
- Sistema de notas e feedback mais detalhado.
- Upload de arquivos para materiais e submissões.
- Dashboards personalizados.
- Sistema de notificações.
- Implementação completa do Spring Security.
- Migração da camada DAO para Spring Data JPA/Hibernate.
- Testes unitários e de integração.

=============================================================================
GUIA DE INSTALAÇÃO E EXECUÇÃO: PLATAFORMA DE ENSINO REMOTO
=============================================================================

Este guia descreve os passos para configurar e executar a aplicação em um novo ambiente.

1. PRÉ-REQUISITOS DE SOFTWARE
-----------------------------------------------------------------------------
- Java Development Kit (JDK): Versão 17 ou superior.
  - Verifique com: java -version
  - Configure a variável de ambiente JAVA_HOME.
- Apache Maven: Versão 3.6.x ou superior.
  - Verifique com: mvn -version
  - Configure o Maven no PATH do sistema (ou use o Maven embutido da IDE).
- MySQL Server: Versão 8.0 ou compatível.
  - Certifique-se de que o servidor MySQL está instalado e rodando.
  - Tenha um usuário MySQL com permissões para criar banco de dados e tabelas.
- Cliente Git (Opcional): Se o projeto estiver em um repositório Git.
- IDE (Opcional, mas Recomendado): IntelliJ IDEA, Eclipse, VS Code.

2. CONFIGURAÇÃO DO BANCO DE DADOS MYSQL
-----------------------------------------------------------------------------
1. Conecte-se ao seu servidor MySQL (ex: MySQL Workbench, DBeaver, linha de comando).
   mysql -u SEU_USUARIO_MYSQL -p

2. Crie o Banco de Dados (substitua 'ensinoremoto_tcc' se usou outro nome):
   CREATE DATABASE IF NOT EXISTS ensinoremoto_tcc CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

3. Crie as Tabelas:
   - Localize o arquivo `schema.sql` no projeto (ex: `src/main/resources/db_scripts/schema.sql`).
   - Selecione o banco de dados: USE ensinoremoto_tcc;
   - Execute o script `schema.sql`:
     source /caminho/completo/para/seu/schema.sql;
     (Ou copie e cole o conteúdo na sua ferramenta MySQL e execute).

3. CONFIGURAÇÃO DO PROJETO JAVA
-----------------------------------------------------------------------------
1. Obtenha o Código do Projeto:
   - Via Git: git clone URL_DO_REPOSITORIO
   - Ou copie a pasta do projeto.

2. Configure a Conexão com o Banco de Dados na Aplicação:
   - Navegue até `src/main/resources/` no projeto.
   - Abra/Crie o arquivo `application.properties`.
   - Adicione/Modifique as seguintes linhas com suas credenciais:
     ```properties
     # Configuração do DataSource MySQL
     spring.datasource.url=jdbc:mysql://localhost:3306/ensinoremoto_tcc?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
     spring.datasource.username=SEU_USUARIO_MYSQL
     spring.datasource.password=SUA_SENHA_MYSQL
     spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

     # Configurações do Thymeleaf para desenvolvimento
     spring.thymeleaf.cache=false

     # Porta do servidor (opcional, padrão é 8080)
     # server.port=8080
     ```
   - **Observação:** Se você ainda estiver usando a classe `DatabaseConnector.java` manualmente, edite as credenciais diretamente nela. É fortemente recomendado usar o `application.properties` para configuração de banco de dados com Spring Boot.

4. COMPILE O PROJETO USANDO MAVEN
-----------------------------------------------------------------------------
1. Abra um terminal ou prompt de comando.
2. Navegue até o diretório raiz do projeto (onde está o `pom.xml`).
3. Execute:
   ```bash
   mvn clean package

Isso baixará as dependências e criará um arquivo .jar na pasta target/.

EXECUTE A APLICAÇÃO SPRING BOOT
No terminal, navegue até a pasta target/ dentro do projeto.

Execute o arquivo JAR gerado (o nome pode variar conforme seu artifactId e version no pom.xml):

Bash

java -jar NOME_DO_SEU_ARQUIVO_ARTIFACT-VERSAO.jar
Exemplo: java -jar E_Learning-1.0-SNAPSHOT.jar

Aguarde a aplicação iniciar. Verifique o console para mensagens de inicialização.

ACESSE A APLICAÇÃO

Abra um navegador web.

Acesse: http://localhost:8080 (ou a porta/caminho de contexto configurado).

(OPCIONAL) IMPORTAR O PROJETO EM UMA IDE

IntelliJ IDEA: "File" -> "Open..." -> Selecione a pasta raiz do projeto.
Eclipse: "File" -> "Import..." -> "Maven" -> "Existing Maven Projects" -> Navegue até a pasta raiz.
