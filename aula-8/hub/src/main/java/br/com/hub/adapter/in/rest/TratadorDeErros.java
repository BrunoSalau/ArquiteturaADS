package br.com.hub.adapter.in.rest;

import br.com.hub.domain.exception.RegraFaturamentoException;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratadorDeErros {

  @ExceptionHandler(RegraFaturamentoException.class)
  public ResponseEntity<Map<String, String>> regraDeNegocio(RegraFaturamentoException e) {
    return ResponseEntity.status(422).body(Map.of("erro", e.getMessage()));
  }
}
