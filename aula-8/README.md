# Aula 8 — Integração da venda ao hub

Base: `Arquitetura-ADS-main/aula-8/mvc`, com o hub compatível de `aula-7/hub` incluído aqui em `hub/`.
Foram preservados os produtos, estoque, perfis, pedidos, telas Thymeleaf e a persistência JPA.

## Executar no Windows

Requisitos: JDK 21 ou superior e Maven 3.9 ou superior. Em dois terminais, dentro de `aula-8`:

```powershell
mvn -f hub/pom.xml spring-boot:run
```

```powershell
mvn -f mvc/pom.xml spring-boot:run
```

Abra http://localhost:8080/pedidos, crie um pedido, adicione um produto disponível e clique em **Fechar pedido e faturar**.
O hub atende na porta 8081. O ID devolvido aparece no detalhe do pedido, com status `FATURADO`.
O fechamento também baixa o estoque. Reenviar um pedido fechado não baixa o estoque novamente.

O hub usa adaptadores **simulados** para boleto, nota fiscal e e-mail. Nenhum documento fiscal ou boleto bancário real é emitido.

## Contrato REST

`POST http://localhost:8081/faturas`, com `Content-Type: application/json`:

```json
{
  "pedidoId": 1,
  "cliente": "Cliente de exemplo",
  "emailCliente": "cliente@example.com",
  "valorTotal": 249.90,
  "itens": [{"descricao": "Teclado mecânico", "quantidade": 1, "precoUnitario": 249.90}]
}
```

Resposta: HTTP 201 com `{"faturaId":"UUID da fatura"}`. O cliente valida que o ID é um UUID.

## Falhas e persistência

Com o hub desligado, feche outro pedido. Ele permanece `FECHADO`, sem ID de fatura e com o erro gravado.
A tela avisa que é necessário conferir o hub antes de reenviar: timeout não prova que o faturamento falhou.
O reenvio manual continua disponível como na base; **não há idempotência nem circuit breaker implementados**.
As duas soluções são propostas e justificadas em `analise.md`.

Cada aplicação usa seu próprio H2 em arquivo em `data/`, relativo ao diretório de execução.
Os dados permanecem após reiniciar, e os produtos de exemplo só são inseridos se o banco estiver vazio.
Para configurar outro endereço, defina `HUB_URL`; o timeout é definido por `HUB_TIMEOUT_MS` (padrão 5000).
`DB_URL` permite configurar a conexão H2 separadamente em cada terminal.

## Testes

```powershell
mvn -f mvc/pom.xml test
mvn -f hub/pom.xml test
```

Os testes MVC usam um servidor HTTP local de teste para verificar JSON, persistência do ID, falha HTTP,
timeout, resposta inválida e reenvio sem nova baixa de estoque. Não precisam do hub em execução.

Para entregar, coloque esta pasta `aula-8` no repositório GitHub da disciplina. Não inclua `target/` nem `data/`.
