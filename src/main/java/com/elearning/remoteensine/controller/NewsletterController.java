package com.elearning.remoteensine.controller;
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.enums.UserType;
import com.elearning.remoteensine.service.NewsletterService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.sql.SQLException;


@Controller
@RequestMapping("/newsletter")
public class NewsletterController {

  private final NewsletterService newsletterService;

  public NewsletterController(NewsletterService newsletterService) {
    this.newsletterService = newsletterService;
  }

  @PostMapping("/inscrever")
  public String inscreverNaNewsletter(HttpSession session, RedirectAttributes redirectAttributes, HttpServletRequest request) {
    User usuarioLogado = (User) session.getAttribute("usuarioLogado");
    String referer = request.getHeader("Referer");

    if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.STUDENT) {
      redirectAttributes.addFlashAttribute("erro_global", "Apenas alunos logados podem se inscrever na newsletter.");
      return "redirect:" + (referer != null ? referer : "/login");
    }

    try {
      newsletterService.createEmail(usuarioLogado.getEmail());
      redirectAttributes.addFlashAttribute("sucesso_global", "Inscrição na newsletter realizada/confirmada com sucesso para " + usuarioLogado.getEmail() + "!");
    } catch (SQLException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_global", "Erro técnico ao tentar se inscrever na newsletter. Tente novamente.");
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("erro_global", e.getMessage());
    }
    return "redirect:" + (referer != null ? referer : "/cursos");
  }

  @PostMapping("/cancelar")
  public String cancelarInscricaoNewsletter(HttpSession session, RedirectAttributes redirectAttributes, HttpServletRequest request) {
    User usuarioLogado = (User) session.getAttribute("usuarioLogado");
    String referer = request.getHeader("Referer");

    if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.STUDENT) {
      redirectAttributes.addFlashAttribute("erro_global", "Apenas alunos logados podem cancelar a inscrição.");
      return "redirect:" + (referer != null ? referer : "/login");
    }

    try {
      boolean cancelado = newsletterService.cancelInscription(usuarioLogado.getEmail());
      if (cancelado) {
        redirectAttributes.addFlashAttribute("sucesso_global", "Inscrição na newsletter cancelada para " + usuarioLogado.getEmail() + ".");
      } else {
        redirectAttributes.addFlashAttribute("info_global", "Não foi possível cancelar a inscrição ou ela já estava cancelada.");
      }
    } catch (SQLException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_global", "Erro técnico ao tentar cancelar a inscrição. Tente novamente.");
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("erro_global", e.getMessage());
    }
    return "redirect:" + (referer != null ? referer : "/cursos");
  }
}