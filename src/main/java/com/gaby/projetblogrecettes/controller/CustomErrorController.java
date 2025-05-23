package com.gaby.projetblogrecettes.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Récupérer le code d'erreur
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            // Gérer les différents codes d'erreur
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("errorCode", "404");
                model.addAttribute("errorTitle", "Page non trouvée");
                model.addAttribute("errorMessage", "La page que vous recherchez n'existe pas ou a été déplacée.");
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("errorCode", "403");
                model.addAttribute("errorTitle", "Accès refusé");
                model.addAttribute("errorMessage", "Vous n'avez pas l'autorisation d'accéder à cette page.");
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("errorCode", "500");
                model.addAttribute("errorTitle", "Erreur interne du serveur");
                model.addAttribute("errorMessage", "Une erreur est survenue lors du traitement de votre requête.");
            } else {
                model.addAttribute("errorCode", statusCode);
                model.addAttribute("errorTitle", "Erreur");
                model.addAttribute("errorMessage", "Une erreur inattendue s'est produite.");
            }
        } else {
            model.addAttribute("errorCode", "Erreur");
            model.addAttribute("errorTitle", "Erreur inconnue");
            model.addAttribute("errorMessage", "Une erreur inattendue s'est produite.");
        }
        
        return "error";
    }
    
    public String getErrorPath() {
        return "/error";
    }
}
