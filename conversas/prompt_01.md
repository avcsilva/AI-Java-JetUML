Implemente, em Java, uma aplicação desktop de edição de diagramas UML que atenda integralmente aos requisitos funcionais e não funcionais descritos no arquivo REQUISITOS.md, na raiz deste diretório.

Restrições técnicas:
- Java 21, com build em Maven.
- Interface gráfica em JavaFX.
- O projeto deve compilar e executar a partir da raiz com: mvn clean javafx:run
- Identificadores de código (pacotes, classes, métodos, variáveis) em inglês.
- Dependências externas apenas quando necessárias (interface gráfica, testes ou outra necessidade que você justificar no plano), todas declaradas no pom.xml.
- Ícones e demais elementos visuais devem ser desenhados pela própria aplicação ou gerados por você. Não baixe imagens ou outros recursos.

Regras de trabalho:
- Use como especificação apenas o REQUISITOS.md e este prompt. Não pesquise na web e não utilize código de terceiros.
- Atenda a cada requisito (RF01 a RF42 e RNF01 a RNF03) exatamente como está escrito. Não adicione funcionalidades que os requisitos não pedem.
- Se algum requisito for ambíguo, declare a interpretação adotada no plano, antes de implementar.
- Antes de escrever código, apresente um plano com: estrutura de pacotes, principais tipos e suas responsabilidades, formato dos arquivos JSON e como cada requisito será atendido.
- O plano deve cobrir a arquitetura da aplicação inteira, mas dividir a implementação em fases. Cada fase deve terminar com a aplicação compilando e executando, e o plano deve indicar quais requisitos cada fase entrega.
- Após minha aprovação, salve o plano em PLANO.md e implemente apenas a fase 1. Ao final de cada fase, confirme que o projeto compila e que a aplicação inicia com o comando acima, e liste os requisitos entregues.
