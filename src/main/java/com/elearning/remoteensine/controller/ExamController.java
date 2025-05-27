package com.elearning.remoteensine.controller;

import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.model.Exam;
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.ExamQuestion;
import com.elearning.remoteensine.model.ExamDefinition;
import com.elearning.remoteensine.model.enums.UserType;
import com.elearning.remoteensine.service.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.sql.SQLException;
import java.util.List;

@Controller
@RequestMapping("/cursos/{idCurso}/avaliar")
public class ExamController {

  private final ExamService examService;
  private final CourseService courseService;
  private final EnrollService enrollService;
  private final ExamDefinitionService examDefinitionService;
  private final ExamQuestionService examQuestionService;

  @Autowired
  public ExamController(ExamService examService, CourseService courseService,
                        EnrollService enrollService, ExamQuestionService examQuestionService,
                        ExamDefinitionService examDefinitionService) {
    this.examService = examService;
    this.courseService = courseService;
    this.enrollService = enrollService;
    this.examDefinitionService = examDefinitionService;
    this.examQuestionService = examQuestionService;
  }

  @GetMapping
  public String exibirFormularioAvaliacao(@PathVariable("idCurso") int idCurso,
                                          Model model, HttpSession session, RedirectAttributes redirectAttributes) {
    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.STUDENT) {
      redirectAttributes.addFlashAttribute("erro_geral", "Apenas alunos logados podem avaliar cursos.");
      return "redirect:/cursos/" + idCurso;
    }

    try {
      Course curso = courseService.searchCourseByIdComplete(idCurso);
      if (curso == null) {
        redirectAttributes.addFlashAttribute("erro_geral", "Curso não encontrado.");
        return "redirect:/cursos";
      }
      if (!enrollService.checkEnrolledStudents(usuarioLogado.getIdUser(), idCurso)) {
        redirectAttributes.addFlashAttribute("erro_geral", "Você precisa estar matriculado para avaliar este curso.");
        return "redirect:/cursos/" + idCurso;
      }

      if (examService.hasStudentEvaluatedCourse(usuarioLogado.getIdUser(), idCurso)) {
        redirectAttributes.addFlashAttribute("info_global", "Você já avaliou este curso.");
        return "redirect:/cursos/" + idCurso;
      }

      Exam novaAvaliacao = new Exam();
      novaAvaliacao.setCourseId(idCurso);
      novaAvaliacao.setStudentId(usuarioLogado.getIdUser());

      model.addAttribute("curso", curso);
      model.addAttribute("avaliacao", novaAvaliacao);
      return "form-avaliacao-curso";

    } catch (SQLException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_geral", "Erro ao preparar página de avaliação.");
      return "redirect:/cursos/" + idCurso;
    }
  }

  @PostMapping("/salvar")
  public String salvarAvaliacao(@PathVariable("idCurso") int idCurso,
                                @ModelAttribute("avaliacao") Exam avaliacao,
                                HttpSession session,
                                RedirectAttributes redirectAttributes,
                                Model model) {

    User usuarioLogado = (User) session.getAttribute("usuarioLogado");
    if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.STUDENT) {
      redirectAttributes.addFlashAttribute("erro_geral", "Ação não permitida.");
      return "redirect:/login";
    }
    try {
      examService.submitCourseEvaluation(idCurso, usuarioLogado.getIdUser(), avaliacao.getGrade(), avaliacao.getComment());
      redirectAttributes.addFlashAttribute("sucesso_global", "Avaliação enviada com sucesso!");
      return "redirect:/cursos/" + idCurso;

    } catch (SQLException e) {
      e.printStackTrace();
      model.addAttribute("erro_avaliacao", "Erro técnico ao salvar sua avaliação.");
      Course curso = null;
      try { curso = courseService.searchCourseByIdComplete(idCurso); } catch (SQLException ex) {/* ignore */}
      model.addAttribute("curso", curso);
      model.addAttribute("avaliacao", avaliacao);
      return "form-avaliacao-curso";
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      model.addAttribute("erro_avaliacao", e.getMessage());
      Course curso = null;
      try { curso = courseService.searchCourseByIdComplete(idCurso); } catch (SQLException ex) {/* ignore */}
      model.addAttribute("curso", curso);
      model.addAttribute("avaliacao", avaliacao);
      return "form-avaliacao-curso";
    }
  }
}
