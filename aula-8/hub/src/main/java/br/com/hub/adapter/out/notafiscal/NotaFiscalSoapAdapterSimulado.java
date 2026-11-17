package br.com.hub.adapter.out.notafiscal;

import br.com.hub.application.port.out.EmissaoNotaFiscalPort;
import br.com.hub.domain.model.Fatura;
import br.com.hub.domain.model.Pedido;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adaptador SIMULADO de emissão de NF-e por SOAP.
 * Monta o envelope SOAP que seria enviado ao web service da SEFAZ,
 * registra no log e devolve uma chave de acesso de 44 dígitos.
 */
@Component
public class NotaFiscalSoapAdapterSimulado implements EmissaoNotaFiscalPort {

  private static final Logger log = LoggerFactory.getLogger(NotaFiscalSoapAdapterSimulado.class);

  private static final String ENDPOINT = "https://sefaz.simulada/ws/NFeAutorizacao4.asmx";
  private static final String UF_SP = "35";
  private static final String CNPJ_EMITENTE = "12345678000195";
  private static final String MODELO_NFE = "55";
  private static final String SERIE = "001";
  private static final String TIPO_EMISSAO_NORMAL = "1";

  @Override
  public String emitir(Fatura fatura, Pedido pedido) {
    String envelope = """
        <soap12:Envelope xmlns:soap12="http://www.w3.org/2003/05/soap-envelope">
          <soap12:Body>
            <nfeDadosMsg xmlns="http://www.portalfiscal.inf.br/nfe/wsdl/NFeAutorizacao4">
              <enviNFe versao="4.00">
                <NFe><infNFe>
                  <emit><CNPJ>%s</CNPJ></emit>
                  <dest><xNome>%s</xNome><email>%s</email></dest>
                  <total><vNF>%s</vNF></total>
                  <infAdic><infCpl>Fatura %s - Pedido %d - %d item(ns)</infCpl></infAdic>
                </infNFe></NFe>
              </enviNFe>
            </nfeDadosMsg>
          </soap12:Body>
        </soap12:Envelope>""".formatted(CNPJ_EMITENTE, pedido.cliente(), pedido.emailCliente(),
        fatura.getValor(), fatura.getId(), pedido.pedidoId(), pedido.itens().size());

    log.info("[SOAP simulado] POST {}\n{}", ENDPOINT, envelope);

    String chave = gerarChaveAcesso(pedido.pedidoId());

    log.info("[SOAP simulado] retorno SEFAZ: cStat=100 (Autorizado o uso da NF-e), chNFe={}", chave);
    return chave;
  }

  /**
   * cUF(2) + AAMM(4) + CNPJ(14) + modelo(2) + série(3) + número(9)
   * + tipo emissão(1) + código numérico(8) + DV(1) = 44 dígitos.
   */
  static String gerarChaveAcesso(Long numeroNota) {
    String aamm = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMM"));
    String numero = String.format("%09d", numeroNota % 1_000_000_000L);
    String codigoNumerico = String.format("%08d", ThreadLocalRandom.current().nextInt(100_000_000));

    String semDv = UF_SP + aamm + CNPJ_EMITENTE + MODELO_NFE + SERIE + numero
        + TIPO_EMISSAO_NORMAL + codigoNumerico;
    return semDv + digitoVerificador(semDv);
  }

  private static int digitoVerificador(String chave43) {
    int soma = 0;
    int peso = 2;
    for (int i = chave43.length() - 1; i >= 0; i--) {
      soma += (chave43.charAt(i) - '0') * peso;
      peso = peso == 9 ? 2 : peso + 1;
    }
    int resto = soma % 11;
    return (resto == 0 || resto == 1) ? 0 : 11 - resto;
  }
}
