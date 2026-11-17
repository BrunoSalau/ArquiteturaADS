package br.com.mvc.controller;

import br.com.mvc.model.Perfil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

  @PostMapping
  public String trocar(@RequestParam Perfil perfil, HttpSession session,
      @RequestHeader(value = "Referer", required = false) String referer) {
    session.setAttribute(PerfilAdvice.ATRIBUTO_SESSAO, perfil);
    return "redirect:" + (referer != null ? referer : "/produtos");
  }
}
