package br.com.mvc.integracao.hub;

/** JSON devolvido pelo hub: {"faturaId": "..."} */
public record FaturamentoResponse(String faturaId) {
}
