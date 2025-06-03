package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.UserDAO;
import com.elearning.remoteensine.model.Professor;
import com.elearning.remoteensine.model.Student;
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.enums.UserType;
import org.springframework.stereotype.Service;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.time.LocalDateTime;

@Service
public class UserService {
  private UserDAO userDAO;

  public UserService(UserDAO userDAO) {
    this.userDAO = userDAO;
  }

  /**
   * Cria um novo usuário (Student ou Professor), hasheando a senha antes de salvar.
   *
   * @param name O nome do usuário.
   * @param email O e-mail do usuário (será verificado se já existe).
   * @param purePassword A senha em texto puro.
   * @param type O tipo de usuário (STUDENT ou PROFESSOR).
   * @param professorSpecialization A especialização, caso seja um Professor (pode ser null).
   * @return O objeto Usuario criado e salvo no banco.
   * @throws SQLException Se ocorrer um erro durante a operação com o banco de dados.
   * @throws IllegalArgumentException Se o e-mail já estiver cadastrado ou o tipo de usuário for inválido.
   */
  public User createUser(String name, String email, String purePassword, UserType type, String professorSpecialization)
      throws SQLException, IllegalArgumentException {
    // 1. Validação de Negócio: Verificar se o e-mail já existe
    if (userDAO.searchByEmail(email) != null) {
      throw new IllegalArgumentException("This e-mail '" + email + "is already registered.");
    }

    // 2. Hashing da Senha
    String hashedPassword = BCrypt.hashpw(purePassword, BCrypt.gensalt());
    User newUser;
    LocalDateTime registerDate = LocalDateTime.now();

    if (type == UserType.STUDENT) {
      Student student = new Student(name, email, hashedPassword);
      student.setRegisterDate(registerDate);
      newUser = student;
    } else if (type == UserType.PROFESSOR) {
      if (professorSpecialization == null || professorSpecialization.trim().isEmpty()) {
        throw new IllegalArgumentException("Especialização é obrigatória para professores.");
      }
      Professor professor = new Professor(name, email, hashedPassword, professorSpecialization);
      professor.setRegisterDate(registerDate);
      newUser = professor;
    } else {
      throw new IllegalArgumentException("Unknown user type: " + type);
    }

    // 3. Chamar o DAO para salvar
    return userDAO.saveUser(newUser);
  }

  /**
   * Valida as credenciais de login de um usuário.
   *
   * @param email O e-mail fornecido para login.
   * @param purePassword A senha em texto pura fornecida para login.
   * @return O objeto Usuario se a autenticação for bem-sucedida, null caso contrário.
   * @throws SQLException Se ocorrer um erro durante a operação com o banco de dados.
   */
  public User validateLogin(String email, String purePassword) throws SQLException {
    User bdUser = userDAO.searchByEmail(email);
    if (bdUser != null) {
      if (BCrypt.checkpw(purePassword, bdUser.getPassword())) {
        return bdUser;
      }
    }
    return null;
  }

  /**
   * Busca um usuário pelo seu ID.
   * @param idUser O ID do usuário.
   * @return O Usuario encontrado ou null.
   * @throws SQLException Erro de banco.
   */
  public User searchUserById(int idUser) throws SQLException {
    return userDAO.searchById(idUser);
  }

  /**
   * Busca um usuário pelo seu e-mail.
   * @param email O e-mail do usuário.
   * @return O Usuario encontrado ou null.
   * @throws SQLException Erro de banco.
   */
  public User searchUserByEmail(String email) throws SQLException {
    return userDAO.searchByEmail(email);
  }
}




