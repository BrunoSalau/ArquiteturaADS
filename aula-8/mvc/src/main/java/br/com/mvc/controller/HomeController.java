package br.com.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

  @GetMapping("/")
  public String raiz() {
    return "redirect:/home";
  }

  @GetMapping("/home")
  public String home(Model model) {
    return "index";
  }
}
