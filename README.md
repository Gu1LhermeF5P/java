RESPOSTAS E PRINTS

Etapa 1 – Introdução & Contextualização
1- Usar uma chamada HTTP síncrona direta trava o serviço de upload, deixando-o lento e vulnerável a falhas. Com mensageria, o upload apenas "avisa" sobre a nova imagem e segue em frente, tornando o sistema mais rápido, robusto e escalável.Suas Desvantagens,Latência para o Usuário,Falhas em Cascata,Acoplamento Forte e Baixa Escalabilidade,A mensageria resolve isso ao desacoplar os serviços, tornando o sistema mais rápido e resiliente.

2- Se o ThumbnailService ficar indisponível, as mensagens não são perdidas. Elas ficam seguras na fila do broker. Quando o serviço voltar a funcionar, ele processará as mensagens acumuladas, garantindo que nenhum trabalho seja perdido e tornando o sistema resiliente.

3-Produtor: É o UploadService, pois ele cria e envia a mensagem inicial ("ImagemRecebida") após o upload.Consumidores: São o ThumbnailService e o FiltroService, pois eles recebem e reagem a essa mensagem para executar suas tarefas.

4- Para enviar mensagens apenas a serviços interessados (ex: um filtro para imagens de alta resolução), usa-se uma Exchange do tipo Topic. O produtor envia a mensagem com uma "etiqueta" (routing key) específica, como imagem.alta-resolucao. Apenas o serviço consumidor configurado para receber essa etiqueta exata processará a mensagem.

5- Para que o FiltroService rode somente após a miniatura ser criada, é preciso criar uma cadeia de eventos,O UploadService envia a mensagem "ImagemRecebida", O ThumbnailService a consome e, ao terminar, envia uma nova mensagem: "ThumbnailPronta" e O FiltroService é configurado para escutar apenas a mensagem "ThumbnailPronta", garantindo a ordem correta do fluxo.

Etapa 3 - Produtor

1- A parte mais desafiadora foi entender a abstração por trás da configuração de Queue, Exchange e Binding, que é menos intuitiva do que uma chamada direta.

2-Insight: A maior clareza veio ao usar o RabbitTemplate, que simplifica drasticamente o envio de mensagens, escondendo toda a complexidade de conexão e serialização em um único método.

3-Possível Falha: A falha mais provável é uma exceção de conexão se o broker RabbitMQ estiver offline. A solução envolve adicionar lógicas de retentativa (retry) e confirmação de publicação (publisher confirms).

4-Melhoria Rápida: Em 15 minutos, a melhoria seria externalizar os nomes da fila, exchange e routing key para o arquivo application.properties, tornando a configuração mais flexível e profissional.

5- Melhoria Rápida: Em 15 minutos, a melhoria seria externalizar os nomes da fila, exchange e routing key para o arquivo application.properties, tornando a configuração mais flexível e profissional.

Etapa 4 - Consumidor
1- Sim, o método anotado com @RabbitListener foi invocado exatamente como esperado. Após enviar uma requisição POST /orders com o JSON do pedido, o log da aplicação exibiu quase que instantaneamente a mensagem de confirmação do consumidor. O log mostrou o objeto OrderCreatedMessage com todos os seus dados, formatado pelo método toString() do record.
Ao verificar a interface do RabbitMQ, a mensagem que estava na fila order.queue foi consumida e o contador de mensagens na fila voltou a zero.

2- O objeto OrderCreatedMessage foi desserializado corretamente, sem nenhum erro. Os campos do JSON enviado pelo Postman (orderId, clientId, items, etc.) corresponderam perfeitamente aos campos definidos no record Java.
A causa mais provável para erros de mapeamento seria uma divergência entre os nomes dos campos. Por exemplo, se o JSON enviasse "client_id" (com underscore) em vez de "clientId" (camelCase), o conversor padrão do Spring (Jackson) não conseguiria mapear o valor, resultando em um campo nulo ou em uma exceção de desserialização, dependendo da configuração.

3- Com as configurações padrão (AcknowledgeMode.AUTO e defaultRequeueRejected = true), se uma exceção ocorresse dentro do método consumidor, o Spring AMQP automaticamente enviaria um nack (negative acknowledgment) ao broker com a instrução de re-enfileirar (requeue = true).
Isso criaria um loop infinito de reprocessamento. A mensagem voltaria para a fila, seria entregue novamente ao consumidor, a exceção ocorreria de novo, e o ciclo se repetiria indefinidamente. Isso é conhecido como uma mensagem "venenosa" (poison pill), que pode sobrecarregar o consumidor e poluir os logs com o mesmo erro em loop.

4-Sim, para qualquer aplicação séria, eu configuraria o acknowledgment manual. A escolha entre requeue = true e requeue = false dependeria da natureza do erro,requeue = true (Tentar novamente): Eu usaria esta opção para erros transitórios, que podem se resolver sozinhos em uma nova tentativa.O principal risco de deixar no modo automático é o loop infinito com mensagens "venenosas", que pode travar o processamento de outras mensagens válidas e consumir recursos desnecessariamente.

5-Por padrão, o consumidor é single-threaded (concurrentConsumers = 1). Se muitas mensagens chegassem de uma só vez, elas formariam uma fila e seriam processadas uma a uma, aumentando a latência.Com certeza! Aqui estão as respostas detalhadas para as perguntas de reflexão sobre o consumidor, seguidas por um resumo completo.

Respostas de Reflexão
1- O consumidor com @RabbitListener funcionou perfeitamente, recebendo e logando a mensagem enviada pelo produtor.

2- O JSON foi convertido para o objeto Java OrderCreatedMessage com sucesso, pois os nomes dos campos eram idênticos.

3- Com a configuração padrão, uma exceção no consumidor causaria um loop infinito de reprocessamento, pois a mensagem seria rejeitada e devolvida à fila repetidamente.

4- É essencial usar acknowledgment manual. A política seria: requeue = true para erros temporários (ex: falha de rede) e requeue = false para erros permanentes (ex: dados inválidos).

5- Para lidar com alta carga, é preciso configurar a concorrência (ex: setMaxConcurrentConsumers), permitindo que várias mensagens sejam processadas em paralelo de forma controlada.

6-A principal melhoria seria implementar uma Dead-Letter Queue (DLQ) para armazenar e analisar mensagens que falham de forma permanente, em vez de descartá-las.

7-As fragilidades do fluxo são a falta de garantia de entrega no produtor e o tratamento inadequado de erros e duplicatas no consumidor. As soluções são adicionar Publisher Confirms, uma DLQ e verificações de idempotência.

Etapa 5 - Conclusão
1- O que Funcionou Bem: A automação do Spring Boot foi o destaque. A facilidade de subir o RabbitMQ com Docker, declarar a infraestrutura (filas/exchanges) como código e, principalmente, a simplicidade do @RabbitListener para consumir mensagens funcionaram perfeitamente como esperado.

2-Dificuldade Enfrentada: O principal problema foi um erro 500 Internal Server Error ao enviar a primeira mensagem. A causa era um erro de serialização, pois o Spring, por padrão, não sabia como converter o objeto de pedido (OrderCreatedMessage) para JSON antes de enviá-lo ao RabbitMQ.

3- O problema foi diagnosticado analisando os logs da aplicação, que mostraram uma exceção clara (IllegalArgumentException) apontando o erro do conversor de mensagens. A solução foi configurar um conversor JSON (Jackson2JsonMessageConverter) na classe RabbitConfig para que a aplicação soubesse como serializar os objetos corretamente.
https://github.com/Gu1LhermeF5P/java    prints no GitHub


<img width="1287" height="609" alt="image" src="https://github.com/user-attachments/assets/60c415cf-fd3d-4434-a79c-650f25fa5e6a" />

<img width="1053" height="181" alt="image" src="https://github.com/user-attachments/assets/272bc18e-c582-4f49-913f-af041e20daef" />

<img width="365" height="475" alt="image" src="https://github.com/user-attachments/assets/3338ba61-e718-4832-bb05-966dba65c726" />

<img width="867" height="543" alt="image" src="https://github.com/user-attachments/assets/e98ea7a2-ae42-4169-9f98-b2d3ef352a11" />

<img width="1308" height="690" alt="image" src="https://github.com/user-attachments/assets/aa14387c-d926-40cf-acd4-0c343ded1085" />

<img width="1289" height="473" alt="image" src="https://github.com/user-attachments/assets/33ca3799-f651-4adf-b2b3-d2c22422106f" />

