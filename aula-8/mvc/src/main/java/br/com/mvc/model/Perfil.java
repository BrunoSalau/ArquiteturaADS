package br.com.mvc.model;

/**
 * Perfil de quem está usando o sistema. Simplificado (sem login):
 * o usuário escolhe o perfil no menu e ele fica guardado na sessão.
 */
public enum Perfil {
  OPERADOR("Operador de estoque"),
  ADM("Administrador");

  private final String descricao;

  Perfil(String descricao) {
    this.descricao = descricao;
  }

  public String getDescricao() {
    return descricao;
  }
}
