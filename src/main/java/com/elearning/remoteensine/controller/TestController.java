package com.elearning.remoteensine.controller;

import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.model.Exam;
import com.elearning.remoteensine.model.ExamDefinition;
import com.elearning.remoteensine.model.ExamQuestion;
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.enums.ExerciseType;
import com.elearning.remoteensine.model.enums.UserType;
import com.elearning.remoteensine.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class TestController {
  private final ExamDefinitionService examDefinitionService;
  private final ExamQuestionService examQuestionService;
  private final CourseService courseService;
  private final EnrollService enrollService;
  private final ExamService examService;

  @Autowired
  public TestController(ExamDefinitionService examDefinitionService,
                         ExamQuestionService examQuestionService,
                         CourseService courseService,
                         EnrollService enrollService , ExamService examService) {
    this.examDefinitionService = examDefinitionService;
    this.examQuestionService = examQuestionService;
    this.courseService = courseService;
    this.enrollService = enrollService;
    this.examService = examService;
  }

  @GetMapping("/cursos/{idCurso}/provas/{idDefinicaoProva}/realizar")
  public String exibirPaginaRealizarProva(
      @PathVariable("idCurso") int idCurso,
      @PathVariable("idDefinicaoProva") int idDefinicaoProva,
      Model model, HttpSession session, RedirectAttributes redirectAttributes) {

    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.STUDENT) {
      redirectAttributes.addFlashAttribute("erro_geral", "Apenas alunos logados podem realizar provas.");
      return "redirect:/login";
    }

    try {
      Course curso = courseService.searchCourseByIdComplete(idCurso);
      ExamDefinition definicaoProva = examDefinitionService.getExamDefinitionById(idDefinicaoProva);

      if (curso == null || definicaoProva == null || definicaoProva.getIdCourse() != idCurso) {
        redirectAttributes.addFlashAttribute("erro_geral", "Prova ou curso inválido ou não associado.");
        return "redirect:/cursos/" + (curso != null ? curso.getIdCourse() : "");
      }

      if (!definicaoProva.isPublished()) {
        redirectAttributes.addFlashAttribute("erro_geral", "Esta prova não está disponível no momento.");
        return "redirect:/cursos/" + idCurso;
      }

      if (!enrollService.checkEnrolledStudents(usuarioLogado.getIdUser(), idCurso)) { // Use seu método
        redirectAttributes.addFlashAttribute("erro_geral", "Você não está matriculado neste curso para realizar esta prova.");
        return "redirect:/cursos/" + idCurso;
      }

      List<ExamQuestion> questoesDaProva = examQuestionService.getQuestionsForStudentToTakeExam(idDefinicaoProva);

      model.addAttribute("curso", curso);
      model.addAttribute("definicaoProva", definicaoProva);
      model.addAttribute("questoesDaProva", questoesDaProva != null ? questoesDaProva : new ArrayList<>());

      System.out.println("ProvaController: Exibindo prova '" + definicaoProva.getTitle() + "' com " +
          (questoesDaProva != null ? questoesDaProva.size() : "0") + " questões.");

    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_geral", "Você não tem permissão para acessar as questões desta prova.");
      return "redirect:/cursos/" + idCurso;
    } catch (Exception e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_geral", "Erro ao carregar a prova: " + e.getMessage());
      return "redirect:/cursos/" + idCurso;
    }

    return "exam/realizar-prova";
  }

  @PostMapping("/cursos/{idCurso}/provas/{idDefinicaoProva}/submeter")
  public String submeterProva(
      @PathVariable("idCurso") int idCurso,
      @PathVariable("idDefinicaoProva") int idDefinicaoProva,
      @RequestParam Map<String, String> todasRespostas,
      HttpSession session,
      RedirectAttributes redirectAttributes,
      Model model) {
    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.STUDENT) {
      redirectAttributes.addFlashAttribute("erro_geral", "Ação não permitida.");
      return "redirect:/login";
    }

    try {
      ExamDefinition definicaoProva = examDefinitionService.getExamDefinitionById(idDefinicaoProva);
      if (definicaoProva == null || definicaoProva.getIdCourse() != idCurso) {
        redirectAttributes.addFlashAttribute("erro_geral", "Prova ou curso inválido.");
        return "redirect:/cursos/" + idCurso;
      }

      List<ExamQuestion> questoesDaProva = examQuestionService.getQuestionsForStudentToTakeExam(idDefinicaoProva);

      int acertos = 0;
      double pontuacaoTotalObtida = 0.0;
      double pontuacaoTotalPossivel = 0.0;

      for (ExamQuestion questao : questoesDaProva) {
        pontuacaoTotalPossivel += questao.getGrade(); // 'grade' na ExamQuestion é a pontuação da questão
        String chaveResposta = "resposta_q" + questao.getIdExamQuestion();
        String respostaAluno = todasRespostas.get(chaveResposta);



        if (respostaAluno != null && !respostaAluno.trim().isEmpty()) {
          if (questao.getExerciseType() == ExerciseType.MULTIPLA_ESCOLHA || questao.getExerciseType() == ExerciseType.DISSERTATIVA) {
            if (respostaAluno.trim().equalsIgnoreCase(questao.getCorrectAnswer() != null ? questao.getCorrectAnswer().trim() : "")) {
              acertos++;
              pontuacaoTotalObtida += questao.getGrade();
            } else {
            }
          }
        }
      }
      int notaFinal = (int) Math.round(pontuacaoTotalObtida);

      Exam tentativaAluno = new Exam();
      tentativaAluno.setCourseId(idCurso);
      tentativaAluno.setIdExamDefinition(idDefinicaoProva);
      tentativaAluno.setStudentId(usuarioLogado.getIdUser());
      tentativaAluno.setGrade(notaFinal);
      tentativaAluno.setComment("Prova realizada em " + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
      tentativaAluno.setHourDate(LocalDateTime.now());

      examService.saveStudentExamAttempt(tentativaAluno);

      redirectAttributes.addFlashAttribute("sucesso_global", "Prova enviada com sucesso! Sua nota (preliminar): " + notaFinal);
      return "redirect:/cursos/" + idCurso;

    } catch (Exception e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_geral", "Erro ao submeter a prova: " + e.getMessage());
      return "redirect:/cursos/" + idCurso + "/provas/" + idDefinicaoProva + "/realizar"; // Volta para a página da prova
    }
  }
}

