package com.elearning.remoteensine.controller;

import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.VisitorQuestion;
import com.elearning.remoteensine.model.enums.UserType;
import com.elearning.remoteensine.service.UserService;
import com.elearning.remoteensine.service.VisitorQuestionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class VisitorQuestionController {

  private final VisitorQuestionService visitorQuestionService;
  private final UserService userService;

  public VisitorQuestionController(VisitorQuestionService visitorQuestionService, UserService userService) {
    this.visitorQuestionService = visitorQuestionService;
    this.userService = userService;
  }

  @GetMapping("/contato")
  public String exibirFormularioPergunta(Model model) {
    return "form-pergunta-visitante";
  }

  @PostMapping("/enviar")
  public String processarFormularioPergunta(
      @RequestParam(value = "nomeVisitante", required = false) String nomeVisitante,
      @RequestParam("emailVisitante") String emailVisitante,
      @RequestParam("textoPergunta") String textoPergunta,
      RedirectAttributes redirectAttributes,
      Model model) {


    try {
      if (emailVisitante.trim().isEmpty() || textoPergunta.trim().isEmpty()) {
        model.addAttribute("erro_pergunta", "E-mail e texto da pergunta são obrigatórios.");
        model.addAttribute("nomeVisitantePreservado", nomeVisitante);
        model.addAttribute("emailVisitantePreservado", emailVisitante);
        model.addAttribute("textoPerguntaPreservado", textoPergunta);
        return "form-pergunta-visitante";
      }

      visitorQuestionService.submitQuestion(nomeVisitante, emailVisitante, textoPergunta);
      redirectAttributes.addFlashAttribute("sucesso_pergunta", "Sua pergunta foi enviada com sucesso! Responderemos em breve.");
      return "redirect:/contato";

    } catch (SQLException e) {
      e.printStackTrace();
      model.addAttribute("erro_pergunta", "Erro técnico ao enviar sua pergunta. Tente novamente.");
      model.addAttribute("nomeVisitantePreservado", nomeVisitante);
      model.addAttribute("emailVisitantePreservado", emailVisitante);
      model.addAttribute("textoPerguntaPreservado", textoPergunta);
      return "form-pergunta-visitante";
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      model.addAttribute("erro_pergunta", e.getMessage());
      model.addAttribute("nomeVisitantePreservado", nomeVisitante);
      model.addAttribute("emailVisitantePreservado", emailVisitante);
      model.addAttribute("textoPerguntaPreservado", textoPergunta);
      return "form-pergunta-visitante";
    }
  }

  @GetMapping("/professor/perguntas-visitantes")
  public String listarPerguntasVisitantesParaProfessor(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
    System.out.println("--- PerguntaVisitanteController: GET /professor/perguntas-visitantes ---");
    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.PROFESSOR) {
      redirectAttributes.addFlashAttribute("erro_geral", "Acesso restrito a professores.");
      return "redirect:/login";
    }

    try {
      List<VisitorQuestion> todasPerguntas = visitorQuestionService.listAllQuestions(false);

      model.addAttribute("listaPerguntasVisitantes", todasPerguntas);
      System.out.println("Controller: Encontradas " + (todasPerguntas != null ? todasPerguntas.size() : 0) + " perguntas de visitantes.");
    } catch (Exception e) { // SQLException ou outras
      e.printStackTrace();
      model.addAttribute("erro_geral", "Erro ao carregar perguntas dos visitantes: " + e.getMessage());
      model.addAttribute("listaPerguntasVisitantes", new ArrayList<>());
    }
    return "professor/lista-perguntas-visitantes";


  }
  @PostMapping("/professor/perguntas-visitantes/{idPergunta}/responder")
  public String responderPerguntaVisitante(
      @PathVariable("idPergunta") int idPergunta,
      @RequestParam("textoResposta") String textoResposta,
      HttpSession session,
      RedirectAttributes redirectAttributes) {
    System.out.println("--- PerguntaVisitanteController: POST /professor/perguntas-visitantes/" + idPergunta + "/responder ---");
    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.PROFESSOR) {
      redirectAttributes.addFlashAttribute("erro_global", "Ação não permitida.");
      return "redirect:/login";
    }

    if (textoResposta == null || textoResposta.trim().isEmpty()) {
      redirectAttributes.addFlashAttribute("erro_resposta_" + idPergunta, "O texto da resposta não pode ser vazio.");
      return "redirect:/professor/perguntas-visitantes";
    }

    try {
      visitorQuestionService.answerQuestion(idPergunta, usuarioLogado.getIdUser(), textoResposta);
      redirectAttributes.addFlashAttribute("sucesso_resposta_" + idPergunta, "Resposta enviada com sucesso para a pergunta ID " + idPergunta + "!");
    } catch (Exception e) { // SQLException, IllegalArgumentException
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_resposta_" + idPergunta, "Erro ao enviar resposta: " + e.getMessage());
    }
    return "redirect:/professor/perguntas-visitantes";
  }
}