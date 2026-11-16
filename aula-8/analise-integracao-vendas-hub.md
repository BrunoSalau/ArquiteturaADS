# Integração da venda com o hub de faturamento

Ao fechar um pedido, a aplicação de vendas envia os dados do pedido ao hub por REST. Quando recebe a confirmação do faturamento, grava o ID da fatura no pedido. Na aplicação MVC, o fluxo de fechamento coordena a operação, enquanto um cliente HTTP concentra os detalhes da chamada. No hub, o adaptador REST encaminha a solicitação ao caso de uso de faturamento.

# 1. Hub indisponível no fechamento

**O que acontece com o pedido.** Se o hub estiver indisponível, o fechamento pode terminar sem um ID de fatura. Sem um registro durável da solicitação, uma falha entre salvar o pedido e chamar o hub também pode deixar o pedido sem faturamento e sem reenvio programado.

**O que o negócio percebe.** O operador vê um pedido aguardando faturamento. A emissão atrasa e os pedidos fechados podem divergir das faturas emitidas até a recuperação da integração.

**Solução proposta — caixa de saída transacional (Transactional Outbox).** Na aplicação MVC, grave o fechamento, o estado «faturamento pendente» e a solicitação de faturamento na mesma transação local. Um processo em segundo plano lê a caixa de saída e faz a chamada REST, com novas tentativas espaçadas para falhas transitórias. Ao receber o ID da fatura, grava-o no pedido e marca a solicitação como concluída na mesma transação local. A solicitação sobrevive a reinícios e pode ser retomada quando o hub voltar.

# 2. O hub emite a fatura, mas a resposta se perde

**O que acontece com o pedido.** O hub pode emitir a fatura e confirmar a operação no seu banco antes de a conexão cair. O pedido segue pendente porque a aplicação de vendas ficou sem o ID. Se o reenvio for tratado como uma nova emissão, o hub pode criar outra fatura para o mesmo pedido.

**O que o negócio percebe.** O operador vê o pedido pendente mesmo com uma fatura emitida. Uma emissão duplicada pode gerar cobrança duplicada e exigir conciliação manual.

**Solução proposta — chave de idempotência.** A aplicação de vendas gera uma chave estável por operação de faturamento e a reutiliza em todos os reenvios. Na fronteira REST do hub, a chave identifica a operação. Na persistência, o hub garante a unicidade da chave e grava seu vínculo com a fatura na mesma transação da emissão. Uma chamada repetida com a mesma chave e os mesmos dados devolve o ID da fatura já emitida, inclusive quando os reenvios ocorrem em paralelo. Assim, a aplicação MVC consegue atualizar o pedido sem disparar outra emissão.
