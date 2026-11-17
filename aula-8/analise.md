# Integração da venda ao hub de faturamento

## Implementação

A base fornecida já contém a integração REST. Foram preservados o MVC com Thymeleaf, a persistência JPA e as regras de estoque.
O `PedidoController` solicita o fechamento ao `PedidoService`. O serviço baixa o estoque, marca o pedido como `FECHADO` e chama o `HubFaturamentoClient`.
O cliente envia `pedidoId`, cliente, e-mail, `valorTotal` e itens para `POST /faturas` no hub hexagonal.
Quando recebe um `faturaId` válido, registra o ID e altera o pedido para `FATURADO`.

Se o hub falhar, a exceção de integração é capturada: a transação local confirma o fechamento e a baixa de estoque,
gravando o erro e deixando o pedido `FECHADO`, sem fatura vinculada. O reenvio manual tenta apenas o faturamento, sem repetir a baixa.
Um erro nas regras de estoque interrompe o fechamento e provoca rollback local.
O banco da venda e o banco do hub são independentes: a transação JPA da venda não desfaz operações realizadas no hub.

O ID, o pedido e o erro são persistidos no H2 em arquivo. O hub incluído é o da aula 7 da mesma base; seus adaptadores de boleto, nota e e-mail são simulações.

## Problema 1 — Hub indisponível ou lento

**Situação:** o hub está fora do ar ou demora mais que o timeout. A chamada síncrona mantém a requisição da venda esperando.

**Pedido:** se a gravação local for concluída, ele permanece `FECHADO`, com estoque baixado, sem `faturaId` e com erro de integração.
Se houver muitas chamadas lentas, requisições e conexões de banco ficam ocupadas enquanto aguardam o hub, prejudicando outras operações.
Não é possível afirmar apenas pelo timeout que nenhuma fatura foi criada.

**Negócio:** o vendedor encontra demora e pedidos sem confirmação de cobrança. O financeiro precisa conferir e recuperar os faturamentos pendentes.
Em uma integração real, a cobrança e os documentos podem atrasar; neste projeto esses serviços externos são simulados.

**Solução proposta: circuit breaker.** Ele atua no MVC, envolvendo a chamada do `HubFaturamentoClient`.
Após um limite de falhas, abre o circuito e interrompe temporariamente novas chamadas, respondendo rapidamente e reduzindo o risco de falhas em cascata.
Depois de um intervalo, permite chamadas de teste para detectar a recuperação do hub.
O fallback preserva a pendência e informa que o faturamento ainda não foi confirmado.

O circuit breaker protege a aplicação de vendas, mas não recupera sozinho os pedidos pendentes nem torna o hub disponível.
A recuperação exige conferência e reprocessamento. Retry com backoff só deve ser automatizado após garantir a idempotência descrita abaixo.
O timeout existente limita a espera; ele não equivale a um circuit breaker.

## Problema 2 — Resposta perdida e faturamento duplicado

**Situação:** o hub cria a fatura, mas a conexão cai antes de a venda receber a resposta. Também pode ocorrer falha no commit local depois de o hub responder.

**Pedido:** na perda de resposta, fica `FECHADO` sem `faturaId`, apesar de a fatura existir no hub.
Como o hub atual cria uma nova fatura a cada chamada, clicar em **Reenviar ao hub** pode gerar outra fatura para o mesmo pedido.
Na falha de commit local, o fechamento e a baixa podem ser revertidos localmente, embora a fatura remota continue existindo.

**Negócio:** a mesma compra pode aparecer mais de uma vez no faturamento, causando conciliação e retrabalho.
Com adaptadores reais, o risco inclui cobrança, nota fiscal e notificação duplicadas. Nos adaptadores simulados, esse efeito aparece nos registros e logs.

**Solução proposta: idempotência.** Atua no caso de uso `FaturamentoService` do hub, na porta `FaturaRepositoryPort` e no adaptador de persistência.
A venda usa uma chave estável por pedido, por exemplo `sistema-origem + pedidoId`.
O hub persiste essa chave, o conteúdo relevante da solicitação e o resultado. Uma repetição equivalente devolve o mesmo `faturaId`;
uma repetição com a mesma chave e valores diferentes deve ser rejeitada.

Uma restrição única na chave e uma operação atômica de reserva impedem que chamadas simultâneas iniciem dois faturamentos.
A reserva deve ocorrer **antes** dos efeitos externos; consultar e depois inserir sem controle de concorrência não basta.
O hub também precisa registrar o progresso de boleto, nota e e-mail e evitar a repetição dos efeitos já concluídos,
usando chaves idempotentes nos provedores quando disponíveis. Uma simples restrição única ao final não desfaz boletos ou notificações já emitidos.

Assim, após uma resposta perdida, o reenvio recupera a fatura original e permite vinculá-la ao pedido.
No projeto entregue, a tela alerta sobre o risco antes do reenvio, mas o alerta não substitui a implementação de idempotência no hub.

## Escopo

Circuit breaker e idempotência são soluções **propostas**, não implementadas nesta atividade.
A chamada REST, o tratamento de falhas, o armazenamento do ID e a interface de reenvio fazem parte da implementação.
O PowerPoint fornecido contém apenas o enunciado e não identifica as soluções estudadas; foram mantidas as duas soluções discutidas na análise da base.
