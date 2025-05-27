package com.elearning.remoteensine.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class HomeController {

  @GetMapping("/")
  public String paginaInicial(Model model) {
    model.addAttribute("mensagemBoasVindas", "Bem-vindo à Plataforma de Ensino Remoto!");
    String dataHoraAtual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    model.addAttribute("dataHora", dataHoraAtual);
    return "index";
  }
}