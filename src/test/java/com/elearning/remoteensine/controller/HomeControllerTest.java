package com.elearning.remoteensine.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HomeControllerTest {

    @Mock
    private Model model;

    private HomeController homeController;

    @BeforeEach
    void setUp() {
        homeController = new HomeController();
    }

    @Test
    void paginaInicial_shouldReturnIndexView() {
        // Act
        String viewName = homeController.paginaInicial(model);
        
        // Assert
        assertEquals("index", viewName);
    }

    @Test
    void paginaInicial_shouldAddWelcomeMessageAttribute() {
        // Act
        homeController.paginaInicial(model);
        
        // Assert
        verify(model).addAttribute(eq("mensagemBoasVindas"), eq("Bem-vindo à Plataforma de Ensino Remoto!"));
    }

    @Test
    void paginaInicial_shouldAddCurrentDateTimeAttribute() {
        // Act
        homeController.paginaInicial(model);
        
        // Assert
        verify(model).addAttribute(eq("dataHora"), anyString());
    }
}