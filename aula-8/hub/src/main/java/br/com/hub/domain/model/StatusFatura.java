package br.com.hub.domain.model;

public enum StatusFatura {
  /** Fatura criada e persistida (RF01). */
  CRIADA,
  /** Boleto emitido e código de barras guardado (RF02). */
  BOLETO_EMITIDO,
  /** Nota fiscal emitida e chave de acesso guardada (RF03). */
  NOTA_FISCAL_EMITIDA,
  /** Cliente notificado e hash do e-mail guardado (RF04). */
  FATURADA
}
