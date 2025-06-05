package com.elearning.remoteensine.controller;

import com.elearning.remoteensine.model.*;
import com.elearning.remoteensine.model.enums.ExerciseType;
import com.elearning.remoteensine.model.enums.UserType;
import com.elearning.remoteensine.service.*;

import java.util.ArrayList;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.elearning.remoteensine.model.enums.ExerciseType.MULTIPLA_ESCOLHA;

@Controller
@RequestMapping("/cursos")
public class CourseController {
  private final CourseService courseService;
  private final EnrollService enrollService;
  private final CommentService commentService;
  private final UserService userService;
  private final ClassroomService classroomService;
  private final NewsletterService newsletterService;
  private final DoubtService doubtService;
  private final ExerciseService exerciseService;
  private final ExamDefinitionService examDefinitionService;
  private final StudentsExerciseAnswerService studentsAnswerService;
  private final ExamQuestionService examQuestionService;

  public CourseController(CourseService courseService, UserService userService,
                          EnrollService enrollService, CommentService commentService,
                          ClassroomService classroomService, NewsletterService newsletterService,
                          DoubtService doubtService, ExerciseService exerciseService,
                          ExamDefinitionService examDefinitionService,
                          StudentsExerciseAnswerService studentsAnswerService,
                          ExamQuestionService examQuestionService) {
    this.courseService = courseService;
    this.userService = userService;
    this.enrollService = enrollService;
    this.commentService = commentService;
    this.classroomService = classroomService;
    this.newsletterService = newsletterService;
    this.doubtService = doubtService;
    this.exerciseService = exerciseService;
    this.examDefinitionService = examDefinitionService;
    this.studentsAnswerService = studentsAnswerService;
    this.examQuestionService = examQuestionService;
  }

  @GetMapping
  public String listarTodosCursos(Model model, HttpSession session) {
    List<Course> listaDeCursosReais = null;
    try {
      listaDeCursosReais = courseService.listAllCoursesWithProfessors();
      User usuarioLogado = (User) session.getAttribute("usuarioLogado");
      if (usuarioLogado != null) {
        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("isProfessor", usuarioLogado.getUserType() == UserType.PROFESSOR);
      } else {
        model.addAttribute("usuarioLogado", null);
        model.addAttribute("isProfessor", false);
      }
      if (listaDeCursosReais != null) {
        model.addAttribute("listaDeCursos", listaDeCursosReais);
      } else {
        model.addAttribute("listaDeCursos", new ArrayList<Course>());
      }
      if (usuarioLogado != null) {
        model.addAttribute("usuarioLogado", usuarioLogado);
        if (usuarioLogado.getUserType() == UserType.STUDENT) {
          try {
            boolean isInscritoNewsletter = newsletterService.checkInscriptionStatus(usuarioLogado.getEmail());
            model.addAttribute("isInscritoNewsletter", isInscritoNewsletter);
          } catch (SQLException e) {
            System.err.println("Erro ao verificar status da newsletter para " + usuarioLogado.getEmail() + ": " + e.getMessage());
            model.addAttribute("isInscritoNewsletter", false);
          }
        } else {
          model.addAttribute("isProfessor", false);
        }
      }
    } catch (SQLException e) {
      model.addAttribute("erro_cursos", "Não foi possível carregar a lista de cursos.");
      model.addAttribute("listaDeCursos", new ArrayList<Course>());
    } catch (Exception e) {
      e.printStackTrace();
      model.addAttribute("erro_geral_cursos", "Ocorreu um erro inesperado ao carregar os cursos.");
      model.addAttribute("listaDeCursos", new ArrayList<Course>());
    }
    return "cursos";
  }

  @GetMapping("/novo")
  public String exibirFormularioNovoCurso(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    if (usuarioLogado == null) {
      redirectAttributes.addFlashAttribute("erro_login", "Você precisa estar logado para criar um curso.");
      return "redirect:/login";
    }
    if (usuarioLogado.getUserType() != UserType.PROFESSOR) {

      redirectAttributes.addFlashAttribute("erro_permissao", "Apenas professores podem criar cursos.");
      return "redirect:/cursos";
    }
    model.addAttribute("curso", new Course());
    model.addAttribute("professorLogado", (Professor) usuarioLogado);
    return "form-curso";
  }

  @PostMapping("/salvar")
  public String salvarNovoCurso(@ModelAttribute("curso") Course course,
                                HttpSession session,
                                RedirectAttributes redirectAttributes,
                                Model model) {
    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.PROFESSOR) {
      redirectAttributes.addFlashAttribute("erro_geral", "Ação não permitida.");
      return "redirect:/cursos";
    }
    if (course.getTitle() == null || course.getTitle().trim().isEmpty()) {
      model.addAttribute("erro_titulo", "O título do curso é obrigatório.");
      model.addAttribute("professorLogado", (Professor) usuarioLogado);
      return "form-curso";
    }
    try {
      course.setResponsibleProfessor((Professor) usuarioLogado);
      courseService.createCourse(
          course.getTitle(),
          course.getDescription(),
          usuarioLogado.getIdUser(),
          course.getPrice(),
          course.getCategory(),
          course.getHoursLoad(),
          getYoutubeVideoIdFromUrlProvided(course.getPresentationVideo())
      );
      redirectAttributes.addFlashAttribute("sucesso_curso", "Curso '" + course.getTitle() + "' criado com sucesso!");
      return "redirect:/cursos";
    } catch (SQLException e) {
      e.printStackTrace();
      model.addAttribute("erro_geral", "Erro de banco de dados ao salvar o curso.");
      model.addAttribute("professorLogado", (Professor) usuarioLogado);
      return "form-curso";
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      model.addAttribute("erro_geral", "Erro ao criar curso: " + e.getMessage());
      model.addAttribute("professorLogado", (Professor) usuarioLogado);
      return "form-curso";
    }
  }

  @GetMapping("/{idCurso}")
  public String exibirDetalhesCurso(@PathVariable("idCurso") int idCurso, Model model, HttpSession session) {

    User usuarioLogado = (User) session.getAttribute("usuarioLogado");
    Course curso = null;
    List<Comment> comentarios = new ArrayList<>();
    List<Doubt> duvidas = new ArrayList<>();
    List<Classroom> aulasDoCurso = new ArrayList<>();
    List<ExamDefinition> definicoesProvaParaGerenciamento = new ArrayList<>();
    List<ExamDefinition> provasDisponiveisParaAluno = new ArrayList<>();
    List<GradeStudent> notasProvaPorCurso = new ArrayList<>();

    boolean isAluno = false;
    boolean isProfessor = false;
    boolean isAlunoMatriculado = false;
    boolean podeInteragir = false;
    boolean podeComentar = false;
    boolean isProfessorDono = false;

    try {
      curso = courseService.searchCourseByIdComplete(idCurso);
      if (curso == null) {
        model.addAttribute("erro_geral", "Curso não encontrado.");
      } else {
        model.addAttribute("curso", curso);
        comentarios = commentService.listStudentsCommentsInCourse(idCurso);
        duvidas = doubtService.listCourseDoubts(idCurso);
        aulasDoCurso = classroomService.listCoursesClasses(idCurso);
      }
      if (usuarioLogado != null) {
        if (usuarioLogado.getUserType() == UserType.PROFESSOR) {
          model.addAttribute("isProfessor", true);
          if (curso.getResponsibleProfessor() != null && curso.getResponsibleProfessor().getIdUser() == usuarioLogado.getIdUser()) {
            isProfessorDono = true;
            definicoesProvaParaGerenciamento = examDefinitionService.getExamDefinitionsForCourseManagement(idCurso, usuarioLogado.getIdUser());
            model.addAttribute("definicoesProvaParaProfessor", definicoesProvaParaGerenciamento); // Use a chave correta que a view espera
          }
        } else if (usuarioLogado.getUserType() == UserType.STUDENT) {
          isAluno = true;
          isAlunoMatriculado = enrollService.checkEnrolledStudents(usuarioLogado.getIdUser(), idCurso);
          podeInteragir = isAlunoMatriculado;
          podeComentar = isAlunoMatriculado;
          if (isAlunoMatriculado) {
            provasDisponiveisParaAluno = examDefinitionService.listarProvasPorCursoPublicadasNaoSubmetidas(idCurso);
            notasProvaPorCurso = examDefinitionService.listarNotasDoAlunoPorProvaECursoId(idCurso, usuarioLogado.getIdUser());
            model.addAttribute("provasDisponiveisParaAluno", provasDisponiveisParaAluno);
            model.addAttribute("notasProvaPorCurso", notasProvaPorCurso);
          }
        }
      }
    } catch (Exception e) {

      e.printStackTrace();
      model.addAttribute("erro_geral", "Erro ao carregar detalhes do curso: " + e.getMessage());
      if (curso == null) model.addAttribute("curso", new Course());
    }


    model.addAttribute("comentarios", comentarios);
    model.addAttribute("duvidas", duvidas);
    model.addAttribute("aulasDoCurso", aulasDoCurso);
    model.addAttribute("definicoesProvaParaProfessor", definicoesProvaParaGerenciamento);
    model.addAttribute("provasDisponiveisParaAluno", provasDisponiveisParaAluno);

    model.addAttribute("usuarioLogado", usuarioLogado);
    model.addAttribute("isAluno", isAluno);
    model.addAttribute("isProfessor", isProfessor);
    model.addAttribute("isAlunoMatriculado", isAlunoMatriculado);
    model.addAttribute("podeInteragir", podeInteragir);
    model.addAttribute("podeComentar", podeComentar);
    model.addAttribute("isProfessorDono", isProfessorDono);

    return "detalhes-curso";
  }

  @PostMapping("/{idCurso}/comentar")
  public String adicionarComentario(
      @PathVariable("idCurso") int idCurso,
      @RequestParam("textoComentario") String textoComentario,
      HttpSession session,
      RedirectAttributes redirectAttributes,
      Model model) {

    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.STUDENT) {
      redirectAttributes.addFlashAttribute("erro_geral", "Você precisa estar logado como aluno para comentar.");
      return "redirect:/login";
    }
    try {
      commentService.createComment(idCurso, usuarioLogado.getIdUser(), textoComentario);
      redirectAttributes.addFlashAttribute("sucesso_comentario", "Comentário adicionado com sucesso!");
    } catch (SQLException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_comentario", "Erro ao salvar comentário. Tente novamente.");
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("erro_comentario", e.getMessage());
    }
    return "redirect:/cursos/" + idCurso;
  }

  @PostMapping("/{idCurso}/matricular")
  public String matricularNoCurso(
      @PathVariable("idCurso") int idCurso,
      HttpSession session,
      RedirectAttributes redirectAttributes) {
    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.STUDENT) {
      redirectAttributes.addFlashAttribute("erro_geral", "Você precisa estar logado como aluno para se matricular.");
      return "redirect:/login";
    }
    try {
      boolean sucesso = enrollService.makeEnroll(usuarioLogado.getIdUser(), idCurso);
      if (sucesso) {
        redirectAttributes.addFlashAttribute("sucesso_matricula", "Matrícula realizada com sucesso!");
      } else {
        redirectAttributes.addFlashAttribute("erro_matricula", "Não foi possível realizar a matrícula.");
      }
    } catch (SQLException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_matricula", "Erro técnico ao tentar realizar a matrícula.");
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("erro_matricula", e.getMessage());
    }
    return "redirect:/cursos/" + idCurso;
  }

  @GetMapping("/{idCurso}/aulas")
  public String exibirAulasDoCurso(@PathVariable("idCurso") int idCurso,
                                   Model model,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    if (usuarioLogado == null) {
      redirectAttributes.addFlashAttribute("erro_login", "Você precisa estar logado para acessar as aulas.");
      return "redirect:/login";
    }
    if (usuarioLogado.getUserType() != UserType.STUDENT) {
      redirectAttributes.addFlashAttribute("erro_permissao", "Apenas alunos podem acessar as aulas dos cursos.");
      return "redirect:/cursos/" + idCurso;
    }

    try {
      boolean isMatriculado = enrollService.checkEnrolledStudents(usuarioLogado.getIdUser(), idCurso);
      if (!isMatriculado) {
        redirectAttributes.addFlashAttribute("erro_matricula", "Você não está matriculado neste curso para acessar as aulas.");
        return "redirect:/cursos/" + idCurso;
      }

      Course curso = courseService.searchCourseByIdComplete(idCurso);
      if (curso == null) {
        redirectAttributes.addFlashAttribute("erro_geral", "Curso não encontrado.");
        return "redirect:/cursos";
      }
      model.addAttribute("curso", curso);

      List<Classroom> aulas = classroomService.listCoursesClasses(idCurso);
      model.addAttribute("aulas", aulas);
    } catch (SQLException e) {
      e.printStackTrace();
      model.addAttribute("erro_geral", "Erro ao carregar as aulas do curso.");
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      model.addAttribute("erro_geral", e.getMessage());
    }

    return "curso-aulas";
  }

  @GetMapping("/{idCurso}/aulas/{idAula}")
  public String exibirConteudoAula(@PathVariable("idCurso") int idCurso,
                                   @PathVariable("idAula") int idAula,
                                   Model model, HttpSession session, RedirectAttributes redirectAttributes) {
    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.STUDENT) {
      redirectAttributes.addFlashAttribute("erro_geral", "Apenas alunos logados podem assistir às aulas.");
      return "redirect:/login";
    }

    try {
      boolean isMatriculado = enrollService.checkEnrolledStudents(usuarioLogado.getIdUser(), idCurso);
      if (!isMatriculado) {
        redirectAttributes.addFlashAttribute("erro_matricula", "Você não está matriculado neste curso.");
        return "redirect:/cursos/" + idCurso;
      }

      Course curso = courseService.searchCourseByIdComplete(idCurso);
      Classroom aula = classroomService.searchClass(idAula);

      if (curso == null || aula == null || aula.getCourseId() != idCurso) { // Use aula.getCourseId()
        redirectAttributes.addFlashAttribute("erro_geral", "Curso ou aula inválida.");
        return "redirect:/cursos";
      }

      model.addAttribute("curso", curso);
      model.addAttribute("aula", aula);

      List<Exercise> exercicios = exerciseService.listClassExercise(idAula);
      model.addAttribute("exercicios", exercicios);

      java.util.Map<Integer, StudentsExerciseAnswer> respostasDoAluno = new java.util.HashMap<>();
      if (exercicios != null) {
        for (Exercise ex : exercicios) {
          StudentsExerciseAnswer resposta = studentsAnswerService.getStudentAnswerForExercise(usuarioLogado.getIdUser(), ex.getExerciseId());
          if (resposta != null) {
            respostasDoAluno.put(ex.getExerciseId(), resposta);
          }
        }
      }
      model.addAttribute("respostasDoAluno", respostasDoAluno);

      List<Classroom> todasAulasDoCurso = classroomService.listCoursesClasses(idCurso);
      model.addAttribute("todasAulasDoCurso", todasAulasDoCurso);
      int indiceAulaAtual = -1;
      for (int i = 0; i < todasAulasDoCurso.size(); i++) {
        if (todasAulasDoCurso.get(i).getClassroomId() == idAula) {
          indiceAulaAtual = i;
          break;
        }
      }
      model.addAttribute("indiceAulaAtual", indiceAulaAtual);


    } catch (SQLException e) {
      e.printStackTrace();
      model.addAttribute("erro_geral", "Erro ao carregar a aula ou exercícios.");
      return "redirect:/cursos/" + idCurso + "/aulas";
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      model.addAttribute("erro_geral", e.getMessage());
      return "redirect:/cursos/" + idCurso + "/aulas";
    }
    return "ver-aula";
  }

  @PostMapping("/{idCurso}/aulas/{idAula}/deletar")
  public String deletarAula(
          @PathVariable("idCurso") int idCurso,
          @PathVariable("idAula") int idAula,
          HttpSession session,
          RedirectAttributes redirectAttributes) {

    System.out.println("CONTROLLER: Tentando DELETAR Aula: " + idAula + " do Curso ID: " + idCurso);
    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.PROFESSOR) {
      redirectAttributes.addFlashAttribute("erro_geral", "Ação não permitida.");
      return "redirect:/login";
    }

    try {
      boolean deletadoComSucesso = classroomService.deleteAula(idAula, idCurso, usuarioLogado.getIdUser());

      if (deletadoComSucesso) {
        redirectAttributes.addFlashAttribute("sucesso_global", "Aula deletada com sucesso!");
      } else {
        redirectAttributes.addFlashAttribute("erro_global", "Não foi possível deletar a aula.");
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_permissao", e.getMessage());
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_global", e.getMessage());
    } catch (SQLException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_global", "Erro técnico ao deletar a aula.");
    } catch (Exception e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_global", "Ocorreu um erro inesperado ao deletar a aula.");
    }
    return "redirect:/cursos/" + idCurso;
  }

  @PostMapping("/{idCurso}/aulas/{idAula}/exercicios/{idExercicio}/responder")
  public String salvarRespostaExercicio(
      @PathVariable("idCurso") int idCurso,
      @PathVariable("idAula") int idAula,
      @PathVariable("idExercicio") int idExercicio,
      @RequestParam("textoResposta") String textoResposta,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    User usuarioLogado = (User) session.getAttribute("usuarioLogado");
    if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.STUDENT) {
      redirectAttributes.addFlashAttribute("erro_geral", "Ação não permitida.");
      return "redirect:/login";
    }

    try {
      studentsAnswerService.submitOrUpdateAnswer(
          idExercicio,
          usuarioLogado.getIdUser(),
          idAula,
          idCurso,
          textoResposta
      );
      redirectAttributes.addFlashAttribute("sucesso_exercicio_" + idExercicio, "Resposta enviada com sucesso!");

    } catch (SQLException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_exercicio_" + idExercicio, "Erro técnico ao salvar sua resposta.");
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_exercicio_" + idExercicio, e.getMessage());
    }
    return "redirect:/cursos/" + idCurso + "/aulas/" + idAula;
  }

  @GetMapping("/{idCurso}/aulas/nova")
  public String exibirFormularioNovaAula(@PathVariable("idCurso") int idCurso,
                                         Model model, HttpSession session, RedirectAttributes redirectAttributes) {
    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    try {
      Course curso = courseService.searchCourseByIdComplete(idCurso);
      if (curso == null) {
        redirectAttributes.addFlashAttribute("erro_geral", "Curso não encontrado.");
        return "redirect:/cursos";
      }

      if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.PROFESSOR ||
          curso.getResponsibleProfessor() == null || curso.getResponsibleProfessor().getIdUser() != usuarioLogado.getIdUser()) {
        redirectAttributes.addFlashAttribute("erro_permissao", "Você não tem permissão para adicionar aulas a este curso.");
        return "redirect:/cursos/" + idCurso;
      }

      model.addAttribute("curso", curso);
      model.addAttribute("aula", new Classroom());
      return "form-aula";

    } catch (SQLException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_geral", "Erro ao carregar informações para nova aula.");
      return "redirect:/cursos/" + idCurso;
    }
  }

  @PostMapping("/{idCurso}/aulas/salvar")
  public String salvarAula(@PathVariable("idCurso") int idCurso,
                           @ModelAttribute("aula") Classroom aula,
                           HttpSession session,
                           RedirectAttributes redirectAttributes,
                           Model model) throws SQLException {
    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    try {
      Course curso = courseService.searchCourseByIdComplete(idCurso);
      if (curso == null) {
        redirectAttributes.addFlashAttribute("erro_geral", "Curso não encontrado ao tentar salvar aula.");
        return "redirect:/cursos";
      }

      if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.PROFESSOR ||
          curso.getResponsibleProfessor() == null || curso.getResponsibleProfessor().getIdUser() != usuarioLogado.getIdUser()) {
        redirectAttributes.addFlashAttribute("erro_permissao", "Você não tem permissão para salvar aulas neste curso.");
        return "redirect:/cursos/" + idCurso;
      }

      if (aula.getTitle() == null || aula.getTitle().trim().isEmpty()) {
        model.addAttribute("erro_aula", "O título da aula é obrigatório.");
        model.addAttribute("curso", curso);
        return "form-aula";
      }

      aula.setCourseId(idCurso);

      if(aula.getContentURL() != null && !aula.getContentURL().trim().isEmpty()) {
        aula.setContentURL(getYoutubeVideoIdFromUrlProvided(aula.getContentURL()));
      }

      classroomService.saveOrUpdateClass(aula, idCurso, usuarioLogado.getIdUser());

      redirectAttributes.addFlashAttribute("sucesso_aula", "Aula salva com sucesso!");
      return "redirect:/cursos/" + idCurso;

    } catch (SQLException e) {
      e.printStackTrace();
      model.addAttribute("erro_geral", "Erro de banco de dados ao salvar a aula.");
      model.addAttribute("curso", courseService.searchCourseByIdComplete(idCurso));
      return "form-aula";
    } catch (IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace();
      model.addAttribute("erro_aula", e.getMessage());
      model.addAttribute("curso", courseService.searchCourseByIdComplete(idCurso));
      return "form-aula";
    }
  }

  @GetMapping("/{idCurso}/aulas/{idAula}/gerenciar")
  public String exibirGerenciamentoAula(@PathVariable("idCurso") int idCurso,
                                        @PathVariable("idAula") int idAula,
                                        Model model, HttpSession session, RedirectAttributes redirectAttributes) {
    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    try {
      Course curso = courseService.searchCourseByIdComplete(idCurso);
      Classroom aula = classroomService.searchClass(idAula);

      if (curso == null || aula == null || aula.getCourseId() != idCurso) {
        redirectAttributes.addFlashAttribute("erro_geral", "Curso ou aula não encontrados, ou aula não pertence ao curso.");
        return "redirect:/cursos";
      }

      if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.PROFESSOR ||
          curso.getResponsibleProfessor() == null || curso.getResponsibleProfessor().getIdUser() != usuarioLogado.getIdUser()) {
        redirectAttributes.addFlashAttribute("erro_permissao", "Você não tem permissão para gerenciar esta aula.");
        return "redirect:/cursos/" + idCurso;
      }

      List<Exercise> exerciciosDaAula = exerciseService.listClassExercise(idAula);

      model.addAttribute("curso", curso);
      model.addAttribute("aula", aula);
      model.addAttribute("exercicios", exerciciosDaAula);
      model.addAttribute("novoExercicio", new Exercise());
      model.addAttribute("tiposExercicio", ExerciseType.values());

    } catch (SQLException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_geral", "Erro ao carregar dados para gerenciamento da aula.");
      return "redirect:/cursos/" + idCurso;
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_geral", e.getMessage());
      return "redirect:/cursos/" + idCurso;
    }

    return "aula-gerenciar";
  }

  @PostMapping("/{idCurso}/aulas/{idAula}/exercicios/salvar")
  public String salvarExercicio(@PathVariable("idCurso") int idCurso,
                                @PathVariable("idAula") int idAula,
                                @ModelAttribute("novoExercicio") Exercise exercicio,
                                HttpSession session,
                                RedirectAttributes redirectAttributes,
                                Model model) {

    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    try {
      Course curso = courseService.searchCourseByIdComplete(idCurso);
      Classroom aula = classroomService.searchClass(idAula);

      if (curso == null || aula == null || aula.getCourseId() != idCurso) {
        redirectAttributes.addFlashAttribute("erro_geral", "Curso ou aula inválidos.");
        return "redirect:/cursos";
      }

      if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.PROFESSOR ||
          curso.getResponsibleProfessor() == null || curso.getResponsibleProfessor().getIdUser() != usuarioLogado.getIdUser()) {
        redirectAttributes.addFlashAttribute("erro_permissao", "Você não tem permissão para adicionar exercícios a esta aula.");
        return "redirect:/cursos/" + idCurso + "/aulas/" + idAula + "/gerenciar";
      }

      if (exercicio.getStatement() == null || exercicio.getStatement().trim().isEmpty() || exercicio.getExerciseType() == null) {
        model.addAttribute("erro_exercicio", "Enunciado e tipo de exercício são obrigatórios.");
        model.addAttribute("curso", curso);
        model.addAttribute("aula", aula);
        model.addAttribute("exercicios", exerciseService.listClassExercise(idAula));
        model.addAttribute("tiposExercicio", ExerciseType.values());
        model.addAttribute("novoExercicio", exercicio);
        return "aula-gerenciar";
      }

      exerciseService.addClassExercise(
          idAula,
          usuarioLogado.getIdUser(),
          exercicio.getStatement(),
          exercicio.getExerciseType(),
          exercicio.getOptions(),
          exercicio.getCorrectAnswer()
      );

      redirectAttributes.addFlashAttribute("sucesso_exercicio", "Exercício adicionado com sucesso!");

    } catch (SQLException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_exercicio", "Erro de banco de dados ao salvar o exercício.");
    } catch (IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_exercicio", e.getMessage());
    }
    return "redirect:/cursos/" + idCurso + "/aulas/" + idAula + "/gerenciar";
  }

  @GetMapping("/{idCurso}/exames/novo")
  public String exibirFormularioNovaDefinicaoExame(@PathVariable("idCurso") int idCurso,
                                                   Model model, HttpSession session, RedirectAttributes redirectAttributes) {
    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    try {
      Course curso = courseService.searchCourseByIdComplete(idCurso);
      if (curso == null) {
        redirectAttributes.addFlashAttribute("erro_geral", "Curso não encontrado.");
        return "redirect:/cursos";
      }

      if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.PROFESSOR ||
          curso.getResponsibleProfessor() == null || curso.getResponsibleProfessor().getIdUser() != usuarioLogado.getIdUser()) {
        redirectAttributes.addFlashAttribute("erro_permissao", "Você não tem permissão para adicionar exames a este curso.");
        return "redirect:/cursos/" + idCurso;
      }

      model.addAttribute("curso", curso);
      ExamDefinition examDefinition = new ExamDefinition();
      examDefinition.setIdCourse(idCurso); // Pré-popula o ID do curso
      model.addAttribute("examDefinition", examDefinition);
      return "exam/form-exam-definition";

    } catch (Exception e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_geral", "Erro ao preparar formulário de novo exame: " + e.getMessage());
      return "redirect:/cursos/" + idCurso;
    }
  }

  @PostMapping("/{idCurso}/exames/salvar")
  public String salvarDefinicaoExame(@PathVariable("idCurso") int idCurso,
                                     @ModelAttribute("examDefinition") ExamDefinition examDefinition,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes,
                                     Model model) throws SQLException {
    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    try {
      Course curso = courseService.searchCourseByIdComplete(idCurso);
      if (curso == null) {
        redirectAttributes.addFlashAttribute("erro_geral", "Curso não encontrado ao tentar salvar o exame.");
        return "redirect:/cursos";
      }

      if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.PROFESSOR ||
          curso.getResponsibleProfessor() == null || curso.getResponsibleProfessor().getIdUser() != usuarioLogado.getIdUser()) {
        redirectAttributes.addFlashAttribute("erro_permissao", "Você não tem permissão para salvar exames neste curso.");
        return "redirect:/cursos/" + idCurso;
      }

      if (examDefinition.getTitle() == null || examDefinition.getTitle().trim().isEmpty()) {
        model.addAttribute("erro_exam", "O título do exame é obrigatório.");
        model.addAttribute("curso", curso);
        return "exam/form-exam-definition";
      }

      examDefinition.setIdCourse(idCurso);

      examDefinitionService.createExamDefinition(
          idCurso,
          usuarioLogado.getIdUser(),
          examDefinition.getTitle(),
          examDefinition.getDescription()
      );

      redirectAttributes.addFlashAttribute("sucesso_global", "Definição de exame '" + examDefinition.getTitle() + "' salva com sucesso!");
      return "redirect:/cursos/" + idCurso;

    } catch (SQLException e) {
      e.printStackTrace();
      model.addAttribute("erro_geral", "Erro de banco de dados ao salvar a definição do exame.");
      model.addAttribute("curso", courseService.searchCourseByIdComplete(idCurso)); // Tenta recarregar curso
      return "exam/form-exam-definition";
    } catch (IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace();
      model.addAttribute("erro_exam", e.getMessage());
      model.addAttribute("curso", courseService.searchCourseByIdComplete(idCurso));
      return "exam/form-exam-definition";
    }
  }

  @GetMapping("/{idCurso}/exames/{idExamDefinition}/questoes")
  public String exibirGerenciamentoQuestoesExame(
      @PathVariable("idCurso") int idCurso,
      @PathVariable("idExamDefinition") int idExamDefinition,
      Model model, HttpSession session, RedirectAttributes redirectAttributes) {

    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    try {
      Course curso = courseService.searchCourseByIdComplete(idCurso);
      ExamDefinition examDef = examDefinitionService.getExamDefinitionById(idExamDefinition);

      if (curso == null || examDef == null || examDef.getIdCourse() != idCurso) {
        redirectAttributes.addFlashAttribute("erro_geral", "Exame ou curso não encontrado ou não associado corretamente.");
        return "redirect:/cursos";
      }

      if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.PROFESSOR ||
          curso.getResponsibleProfessor() == null || curso.getResponsibleProfessor().getIdUser() != usuarioLogado.getIdUser()) {
        redirectAttributes.addFlashAttribute("erro_permissao", "Você não tem permissão para gerenciar questões deste exame.");
        return "redirect:/cursos/" + idCurso;
      }

      List<ExamQuestion> questoesDoExame = examQuestionService.getQuestionsForExamDefinition(idExamDefinition, usuarioLogado.getIdUser());

      model.addAttribute("curso", curso);
      model.addAttribute("examDefinition", examDef);
      model.addAttribute("listaQuestoes", questoesDoExame);
      model.addAttribute("novaQuestao", new ExamQuestion(idExamDefinition, null, null, null, null, 0, 0)); // Objeto para o form, pré-seta idExamDefinition
      model.addAttribute("tiposExercicio", ExerciseType.values()); // Para o select do tipo de questão


    } catch (Exception e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_geral", "Erro ao carregar gerenciamento de questões: " + e.getMessage());
      return "redirect:/cursos/" + idCurso;
    }
    return "exam/gerenciar-questoes-exame";
  }

  @PostMapping("/{idCurso}/exames/{idExamDefinition}/questoes/adicionar")
  public String adicionarQuestaoAoExame(
      @PathVariable("idCurso") int idCurso,
      @PathVariable("idExamDefinition") int idExamDefinition,
      @ModelAttribute("novaQuestao") ExamQuestion novaQuestao,
      HttpSession session,
      RedirectAttributes redirectAttributes,
      Model model) {

    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    Course curso = null;
    ExamDefinition examDef = null;
    try {
      curso = courseService.searchCourseByIdComplete(idCurso);
      examDef = examDefinitionService.getExamDefinitionById(idExamDefinition);

      if (curso == null || examDef == null || examDef.getIdCourse() != idCurso) {
        redirectAttributes.addFlashAttribute("erro_geral", "Exame ou curso inválido ao tentar adicionar questão.");
        return "redirect:/cursos";
      }

      if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.PROFESSOR ||
          curso.getResponsibleProfessor() == null || curso.getResponsibleProfessor().getIdUser() != usuarioLogado.getIdUser()) {
        redirectAttributes.addFlashAttribute("erro_permissao", "Não autorizado.");
        return "redirect:/cursos/" + idCurso + "/exames/" + idExamDefinition + "/questoes";
      }

      if (novaQuestao.getStatement() == null || novaQuestao.getStatement().trim().isEmpty() || novaQuestao.getExerciseType() == null)
        return recarregarPaginaComErro(idExamDefinition, model, curso, examDef, usuarioLogado.getIdUser(),"Enunciado e tipo de questão são obrigatórios.");


      if (novaQuestao.getExerciseType().equals(ExerciseType.MULTIPLA_ESCOLHA)) {
        Set<String> opcoesValidas = Set.of("A", "B", "C", "D");

        if (novaQuestao.getCorrectAnswer() == null || novaQuestao.getCorrectAnswer().trim().isEmpty())
          return recarregarPaginaComErro(idExamDefinition, model, curso, examDef, usuarioLogado.getIdUser(),
                  "É obrigatório informar a resposta correta para a questão de multipla escolha.");

        if (!opcoesValidas.contains(novaQuestao.getCorrectAnswer()))
          return recarregarPaginaComErro(idExamDefinition, model, curso, examDef, usuarioLogado.getIdUser(),
                  "As opções de resposta devem ser apenas A, B, C ou D.");

        try {
          String optionsJson = novaQuestao.getOptions();
          ObjectMapper mapper = new ObjectMapper();
          List<Map<String, String>> opcoes = mapper.readValue(optionsJson, new TypeReference<List<Map<String, String>>>() {
          });
          for (Map<String, String> item : opcoes) {
            if (!item.containsKey("opcao") || !item.containsKey("resposta") || item.get("opcao") == null || item.get("resposta") == null)
              return recarregarPaginaComErro(idExamDefinition, model, curso, examDef, usuarioLogado.getIdUser(),
                      "Cada item deve conter os campos 'opcao' (alternativa) e 'resposta'. Formato JSON inválido!");

            if (!opcoesValidas.contains(item.get("opcao").trim().toUpperCase()))
              return recarregarPaginaComErro(idExamDefinition, model, curso, examDef, usuarioLogado.getIdUser(),
                      "As opções devem ser apenas A, B, C ou D.");
          }
        } catch (Exception e) {
          return recarregarPaginaComErro(idExamDefinition, model, curso, examDef, usuarioLogado.getIdUser(),
                  "Formato JSON inválido para resposta de multipla escolha!");
        }
      }

      novaQuestao.setIdDefinitionExam(idExamDefinition);

      examQuestionService.addQuestionToExamDefinition(
          idExamDefinition,
          usuarioLogado.getIdUser(),
          novaQuestao.getStatement(),
          novaQuestao.getExerciseType(),
          novaQuestao.getOptions(),
          novaQuestao.getCorrectAnswer(),
          novaQuestao.getGrade(),
          novaQuestao.getExamSequence()
      );
      redirectAttributes.addFlashAttribute("sucesso_questao", "Questão adicionada com sucesso ao exame!");

    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("erro_questao", "Erro ao adicionar questão: " + e.getMessage());
    }
    return "redirect:/cursos/" + idCurso + "/exames/" + idExamDefinition + "/questoes";
  }

  @GetMapping("/{idCurso}/minhas-notas")
  public String exibirNotasDoCursoParaAluno(
          @PathVariable("idCurso") int idCurso,
          Model model,
          HttpSession session,
          RedirectAttributes redirectAttributes) {

    System.out.println("--- CursoController: GET /cursos/" + idCurso + "/minhas-notas ---");
    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.STUDENT) {
      redirectAttributes.addFlashAttribute("erro_geral", "Apenas alunos logados podem ver suas notas.");
      return "redirect:/login";
    }

    try {
      Course curso = courseService.searchCourseByIdComplete(idCurso);
      if (curso == null) {
        redirectAttributes.addFlashAttribute("erro_geral", "Curso não encontrado.");
        return "redirect:/cursos";
      }
      model.addAttribute("curso", curso);

      List<GradeStudent> notasDoAlunoNoCurso = examDefinitionService.listarNotasDoAlunoPorProvaECursoId(idCurso, usuarioLogado.getIdUser());
      model.addAttribute("listaNotasProvas", notasDoAlunoNoCurso);
      System.out.println("Controller: Encontradas " + notasDoAlunoNoCurso.size() + " notas de provas para o aluno ID " + usuarioLogado.getIdUser() + " no curso ID " + idCurso);

    } catch (SQLException e) {
      e.printStackTrace();
      model.addAttribute("erro_geral", "Erro ao carregar suas notas para este curso: " + e.getMessage());
      model.addAttribute("listaNotasProvas", new ArrayList<>());
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      model.addAttribute("erro_geral", e.getMessage());
      model.addAttribute("listaNotasProvas", new ArrayList<>());
    }

    return "notas-prova";
  }

  private String recarregarPaginaComErro(int idExamDefinition, Model model, Course curso, ExamDefinition examDef, int idUsuarioLogado, String mensagemErro) throws SQLException, IllegalAccessException {
    model.addAttribute("curso", curso);
    model.addAttribute("examDefinition", examDef);
    model.addAttribute("listaQuestoes", examQuestionService.getQuestionsForExamDefinition(idExamDefinition, idUsuarioLogado));
    model.addAttribute("tiposExercicio", ExerciseType.values());
    model.addAttribute("erro_questao", mensagemErro);
    return "exam/gerenciar-questoes-exame";
  }

  @GetMapping("/{idCurso}/exames/{idExamDefinition}/editar")
  public String exibirFormularioEditarDefinicaoExame(
      @PathVariable("idCurso") int idCurso,
      @PathVariable("idExamDefinition") int idExamDefinition,
      Model model,
      HttpSession session,
      RedirectAttributes redirectAttributes) {

    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    try {
      Course curso = courseService.searchCourseByIdComplete(idCurso);
      ExamDefinition examDef = examDefinitionService.getExamDefinitionById(idExamDefinition);

      if (curso == null || examDef == null || examDef.getIdCourse() != idCurso) {
        redirectAttributes.addFlashAttribute("erro_geral", "Definição de exame ou curso não encontrado, ou não associado corretamente.");
        return "redirect:/cursos";
      }

      if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.PROFESSOR ||
          curso.getResponsibleProfessor() == null || curso.getResponsibleProfessor().getIdUser() != usuarioLogado.getIdUser()) {
        redirectAttributes.addFlashAttribute("erro_permissao", "Você não tem permissão para editar este exame.");
        return "redirect:/cursos/" + idCurso;
      }

      model.addAttribute("curso", curso);
      model.addAttribute("examDefinition", examDef);

    } catch (Exception e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_geral", "Erro ao carregar definição de exame para edição: " + e.getMessage());
      return "redirect:/cursos/" + idCurso;
    }
    return "exam/form-exam-definition";
  }

  @PostMapping("/{idCurso}/exames/{idExame}/publicar")
  public String publicarExame(@PathVariable("idCurso") int idCurso,
                              @PathVariable("idExame") int idExamDefinition,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {


    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.PROFESSOR) {
      System.err.println("CONTROLLER: Falha na permissão para publicar - usuário não é professor ou não está logado."); // LOG C2
      redirectAttributes.addFlashAttribute("erro_global", "Ação não permitida. Faça login como professor.");
      return "redirect:/login";
    }
    try {

      boolean publicado = examDefinitionService.publishExamDefinition(idExamDefinition, usuarioLogado.getIdUser());
      if (publicado) {
        redirectAttributes.addFlashAttribute("sucesso_global", "Exame publicado com sucesso!");
      } else {
        redirectAttributes.addFlashAttribute("erro_global", "Não foi possível publicar o exame.");
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_permissao", e.getMessage());
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_global", e.getMessage());
    } catch (SQLException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_global", "Erro técnico ao publicar o exame. Tente novamente.");
    } catch (Exception e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_global", "Ocorreu um erro inesperado ao publicar o exame.");
    }

    return "redirect:/cursos/" + idCurso;
  }

  @PostMapping("/{idCurso}/exames/{idExame}/despublicar")
  public String despublicarExame(@PathVariable("idCurso") int idCurso,
                                 @PathVariable("idExame") int idExamDefinition,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.PROFESSOR) {
      redirectAttributes.addFlashAttribute("erro_global", "Ação não permitida. Faça login como professor.");
      return "redirect:/login";
    }

    try {
      boolean despublicadoComSucesso = examDefinitionService.unpublishExamDefinition(idExamDefinition, usuarioLogado.getIdUser());

      if (despublicadoComSucesso) {
        redirectAttributes.addFlashAttribute("sucesso_global", "Exame despublicado com sucesso!");
      } else {
        redirectAttributes.addFlashAttribute("erro_global", "Não foi possível despublicar o exame (serviço indicou falha).");
      }
    } catch (Exception e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_global", "Erro ao despublicar exame: " + e.getMessage());
    }
    return "redirect:/cursos/" + idCurso;
  }
  @PostMapping("/{idCurso}/exames/{idExame}/deletar")
  public String deletarDefinicaoExame(@PathVariable("idCurso") int idCurso,
                                      @PathVariable("idExame") int idExamDefinition,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {

    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.PROFESSOR) {
      redirectAttributes.addFlashAttribute("erro_global", "Ação não permitida. Faça login como professor.");
      return "redirect:/login";
    }

    try {
      boolean deletadoComSucesso = examDefinitionService.deleteExamDefinition(idExamDefinition, idCurso, usuarioLogado.getIdUser());

      if (deletadoComSucesso) {
        redirectAttributes.addFlashAttribute("sucesso_global", "Definição de exame deletada com sucesso!");
      } else {
        redirectAttributes.addFlashAttribute("erro_global", "Não foi possível deletar a definição do exame (serviço indicou falha).");
      }
    } catch (Exception e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_global", "Erro ao deletar definição de exame: " + e.getMessage());
    }
    return "redirect:/cursos/" + idCurso;
  }
  @PostMapping("/{idCurso}/duvidas/criar")
  public String criarNovaDuvida(
      @PathVariable("idCurso") int idCurso,
      @RequestParam("tituloDuvida") String titulo,
      @RequestParam("descricaoDuvida") String descricao,
      HttpSession session,
      RedirectAttributes redirectAttributes,
      Model model) {

    System.out.println("--- DENTRO DE CourseController.criarNovaDuvida para Curso ID: " + idCurso + " ---");
    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.STUDENT) { // Use seu enum UserType
      redirectAttributes.addFlashAttribute("erro_geral", "Você precisa estar logado como aluno para enviar dúvidas.");
      return "redirect:/login";
    }

    try {
      boolean isMatriculado = enrollService.checkEnrolledStudents(usuarioLogado.getIdUser(), idCurso);
      if (!isMatriculado) {
        redirectAttributes.addFlashAttribute("erro_duvida", "Você não está matriculado neste curso para enviar dúvidas.");

        return "redirect:/cursos/" + idCurso;
      }

      if (titulo == null || titulo.trim().isEmpty() || descricao == null || descricao.trim().isEmpty()) {
        redirectAttributes.addFlashAttribute("erro_duvida", "Título e descrição da dúvida são obrigatórios.");
        return "redirect:/cursos/" + idCurso + "#secao-duvidas";
      }

      doubtService.createDoubt(idCurso, usuarioLogado.getIdUser(), titulo, descricao);
      redirectAttributes.addFlashAttribute("sucesso_duvida", "Sua dúvida foi enviada com sucesso!");

    } catch (SQLException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_duvida", "Erro técnico ao enviar sua dúvida. Tente novamente.");
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_duvida", e.getMessage());
    }


    return "redirect:/cursos/" + idCurso + "#duvidas-section";
  }

  @PostMapping("/{idCurso}/duvidas/{idDuvida}/responder")
  public String responderDuvidaAluno(
      @PathVariable("idCurso") int idCurso,
      @PathVariable("idDuvida") int idDuvida,
      @RequestParam("textoResposta") String textoResposta,
      HttpSession session,
      RedirectAttributes redirectAttributes,
      Model model) {

    System.out.println("--- Controller: POST responderDuvidaAluno para Curso ID: " + idCurso + ", Dúvida ID: " + idDuvida + " ---");
    User usuarioLogado = (User) session.getAttribute("usuarioLogado");

    if (usuarioLogado == null || usuarioLogado.getUserType() != UserType.PROFESSOR) {
      redirectAttributes.addFlashAttribute("erro_geral", "Apenas professores logados podem responder dúvidas.");
      return "redirect:/login";
    }

    try {
      Doubt duvidaRespondida = doubtService.answerDoubt(idDuvida, usuarioLogado.getIdUser(), textoResposta);

      if (duvidaRespondida != null) {
        redirectAttributes.addFlashAttribute("sucesso_resposta_duvida_" + idDuvida, "Dúvida respondida com sucesso!");
      } else {

        redirectAttributes.addFlashAttribute("erro_resposta_duvida_" + idDuvida, "Não foi possível registrar a resposta.");
      }

    } catch (SQLException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_resposta_duvida_" + idDuvida, "Erro técnico ao responder a dúvida. Tente novamente.");
    } catch (IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace();
      redirectAttributes.addFlashAttribute("erro_resposta_duvida_" + idDuvida, e.getMessage());
    }


    return "redirect:/cursos/" + idCurso + "#duvida-" + idDuvida;
  }

  private String getYoutubeVideoIdFromUrlProvided(String urlVideoYoutube){
    Pattern pattern = Pattern.compile("v=([^&]+)");
    Matcher videoIdFromUrl = pattern.matcher(urlVideoYoutube);
    final String baseUrlYoutubeEmbed = "https://www.youtube.com/embed/";

    if (videoIdFromUrl.find())
      return baseUrlYoutubeEmbed.concat(videoIdFromUrl.group(1));

    return urlVideoYoutube;
  }
}

