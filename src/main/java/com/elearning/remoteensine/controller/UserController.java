package com.elearning.remoteensine.controller;

import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.enums.UserType;
import com.elearning.remoteensine.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestMapping;

import java.sql.SQLException;

@Controller
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/cadastro")
  public String displayRegistrationForm(Model model) {
    return "cadastro";
  }

  @PostMapping("/cadastro")
  public String processFormRegistration(
      @RequestParam String nome,
      @RequestParam String email,
      @RequestParam String senha,
      @RequestParam("tipoUsuarioStr") String tipoUsuarioStr,
      @RequestParam(required = false) String especializacao,
      RedirectAttributes redirectAttributes,
      Model model) {
    UserType userType;
    try {
      userType = UserType.valueOf(tipoUsuarioStr.toUpperCase());
    } catch (IllegalArgumentException e) {
      model.addAttribute("erro_tipoUsuario", "Tipo de usuário inválido.");
      model.addAttribute("nome", nome);
      model.addAttribute("email", email);
      model.addAttribute("especializacao", especializacao);
      return "cadastro";
    }
    if (nome.trim().isEmpty() || email.trim().isEmpty() || senha.trim().isEmpty()) {
      model.addAttribute("erro_geral", "Nome, e-mail e senha são obrigatórios.");
      model.addAttribute("nome", nome);
      model.addAttribute("email", email);
      model.addAttribute("especializacao", especializacao);
      return "cadastro";
    }
    if (userType == UserType.PROFESSOR && (especializacao == null || especializacao.trim().isEmpty())) {
      model.addAttribute("erro_especializacao", "Especialização é obrigatória para professores.");
      model.addAttribute("nome", nome);
      model.addAttribute("email", email);
      model.addAttribute("tipoUsuarioSelecionado", tipoUsuarioStr);
      return "cadastro";
    }
    try {
      userService.createUser(nome, email, senha, userType, especializacao);
      redirectAttributes.addFlashAttribute("sucesso_cadastro", "Usuário cadastrado com sucesso! Faça o login.");
      return "redirect:/login";
    } catch (SQLException e) {

      e.printStackTrace();
      model.addAttribute("erro_geral", "Erro ao salvar usuário no banco de dados. Tente novamente.");
      model.addAttribute("nome", nome);
      model.addAttribute("email", email);
      model.addAttribute("especializacao", especializacao);
      model.addAttribute("tipoUsuarioSelecionado", tipoUsuarioStr);
      return "cadastro";
    } catch (IllegalArgumentException e) {
      model.addAttribute("erro_email", e.getMessage());
      model.addAttribute("nome", nome);
      model.addAttribute("email", email);
      model.addAttribute("especializacao", especializacao);
      model.addAttribute("tipoUsuarioSelecionado", tipoUsuarioStr);
      return "cadastro";
    }
  }
  
  @GetMapping("/login")
  public String displayRegistrationLogin(Model model) {
    return "login";
  }

  @PostMapping("/login")
  public String processarFormularioLogin(
      @RequestParam String email,
      @RequestParam String senha,
      RedirectAttributes redirectAttributes,
      Model model,
      HttpServletRequest request
  ) {
    try {
      User usuarioAutenticado = userService.validateLogin(email, senha);

      if (usuarioAutenticado != null) {
        System.out.println("Login BEM-SUCEDIDO para: " + usuarioAutenticado.getName());
        HttpSession session = request.getSession(true);
        session.setAttribute("usuarioLogado", usuarioAutenticado);
        session.setMaxInactiveInterval(30 * 60);
        System.out.println("Usuário ID " + usuarioAutenticado.getIdUser() + " armazenado na sessão.");
        redirectAttributes.addFlashAttribute("mensagemGlobal", "Login realizado com sucesso!");
        return "redirect:/cursos";
      } else {
        System.out.println("Login FALHOU para email: " + email);
        model.addAttribute("erro_login", "E-mail ou senha inválidos. Tente novamente.");
        return "login";
      }
    } catch (SQLException e) {
      System.err.println("ERRO SQLException no Login Controller: " + e.getMessage());
      e.printStackTrace();
      model.addAttribute("erro_login", "Erro técnico durante o login. Tente novamente mais tarde.");
      return "login";
    } catch (Exception e) {
      System.err.println("ERRO Exception no Login Controller: " + e.getMessage());
      e.printStackTrace();
      model.addAttribute("erro_login", "Ocorreu um erro inesperado. Tente novamente.");
      return "login";
    }
  }

  @PostMapping("/logout")
  public String logout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
      System.out.println("Sessão invalidada (logout).");
    }
    redirectAttributes.addFlashAttribute("mensagemGlobal", "Logout realizado com sucesso.");
    return "redirect:/login";
  }

  @GetMapping("/perfil")
  public String exibirPaginaPerfil(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
    System.out.println("--- UserController: GET /perfil ---");
    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    if (usuarioLogado == null) {
      redirectAttributes.addFlashAttribute("erro_login", "Você precisa estar logado para ver seu perfil.");
      return "redirect:/login";
    }

    model.addAttribute("usuarioPerfil", usuarioLogado);

    boolean isProfessor = (usuarioLogado.getUserType() == UserType.PROFESSOR);
    boolean isStudent = (usuarioLogado.getUserType() == UserType.STUDENT);

    model.addAttribute("isProfessorProfile", isProfessor);
    model.addAttribute("isStudentProfile", isStudent);


    System.out.println("UserController: Exibindo perfil para usuário: " + usuarioLogado.getName() +
        ", isProfessorProfile: " + isProfessor + ", isStudentProfile: " + isStudent);
    System.out.println("UserController: Conteúdo final do Model: " + model.asMap());

    return "perfil-usuario";
  }

}
