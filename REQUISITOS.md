# Requisitos

## 1. Diagramas e arquivos

**RF01: Criar novo diagrama.** Na tela de boas-vindas ou em File > New, o usuário escolhe o tipo: Class, Sequence, State, Object ou Use Case Diagram. O diagrama abre em uma nova aba.

**RF02: Abrir, salvar e fechar.** Os diagramas são salvos em arquivos JSON. O usuário gerencia os arquivos por File > Open (Ctrl+O), Recent Files, Close (Ctrl+W), Save (Ctrl+S) e Save As.

**RF03: Duplicar diagrama.** File > Duplicate (Ctrl+D) cria uma cópia do diagrama atual em uma nova aba, para explorar alternativas de projeto.

**RF04: Exportar como imagem.** File > Export Image salva o diagrama como imagem. Os formatos incluem SVG, PNG e JPEG, entre outros, definidos pela extensão escolhida em "Save as type".

**RF05: Copiar para a área de transferência.** Copia uma imagem do diagrama inteiro, sem a grade de fundo e ajustada aos limites do conteúdo. Está disponível por File > Copy to Clipboard, pela ferramenta To Clipboard da barra e por Ctrl+B. Ao concluir, exibe uma notificação de confirmação (RF42).

**RF06: Encerrar a aplicação.** File > Exit (Ctrl+Q).

## 2. Barra de ferramentas e área de trabalho

**RF07: Selecionar ferramenta.** A barra lateral exibe as ferramentas do tipo de diagrama atual, na ordem abaixo. Escolher uma ativa a ferramenta correspondente.
- Class: Select, Class, Interface, Package, Package Description, Note, Dependency, Inheritance, Realization, Association, Aggregation, Composition, Note Connector, To Clipboard.
- Sequence: Select, Implicit Parameter, Note, Call, Return, Note Connector, To Clipboard.
- State: Select, State, Initial State, Final State, Note, Transition, Note Connector, To Clipboard.
- Object: Select, Object, Field, Note, Reference, Collaboration, Note Connector, To Clipboard.
- Use Case: Select, Actor, Use Case, Note, Association, Include, Extend, Generalization, Note Connector, To Clipboard.

**RF08: Atalhos das ferramentas.** As ferramentas são ativadas pela sequência 1–9, 0, A, B…, de cima para baixo na barra: "1" para a primeira, "2" para a segunda etc.

**RF09: Menu de contexto.** Clicar com o botão direito em qualquer ponto do diagrama exibe as ferramentas da barra em um menu popup.

**RF10: Dicas de ferramenta.** Ao passar o mouse sobre uma ferramenta, aparece um tooltip com o tipo de elemento. Duas opções do menu View alteram isso:
- Show Tool Hint: exibe o nome de cada ferramenta ao lado do ícone.
- Verbose Tooltips: o tooltip traz uma descrição estendida do elemento UML.

## 3. Nós e pacotes

**RF11: Criar nós.** Com uma ferramenta de criação de nó ativa, clicar em um espaço vazio cria um nó daquele tipo.

**RF12: Edição automática de nós.** Com View > Auto Edit Node ativo, o painel de propriedades abre automaticamente sempre que um nó é criado. Vale apenas para nós.

**RF13: Criar notas.** O usuário pode adicionar Notas ao diagrama para inserir informações complementares.

**RF14: Conectar notas.** Com a ferramenta Note Connector:
- arrastar de um nó até a nota liga os dois, e a conexão acompanha a nota quando ela é movida;
- arrastar a partir da nota até um ponto do diagrama ancora a extremidade nesse ponto fixo.

**RF15: Nós filhos em pacotes.** Nós Package podem conter nós filhos.
- Com uma ferramenta de criação de nó ativa, clicar dentro de um pacote cria o nó diretamente como filho.
- Para incluir um nó existente, o usuário o posiciona sobre o pacote e pressiona Shift.
- Para desanexar um nó do pacote, também pressiona Shift.

## 4. Arestas, transições e validação

**RF16: Criar arestas.** O usuário seleciona a ferramenta de aresta, clica no nó de origem, arrasta a linha elástica (rubber band) e solta sobre o nó de destino.

**RF17: Layout automático.** O caminho das arestas é calculado automaticamente e se ajusta quando os nós de origem ou destino são movidos. Para alterar o caminho, o usuário move os nós.

**RF18: Validação das conexões.** Uma aresta só é criada se for uma conexão válida entre os elementos, segundo as regras do tipo de diagrama. Tentativas inválidas não têm efeito no diagrama e são reportadas por notificação (RF42). O sistema não deve permitir as seguintes infrações estruturais, rejeitando-as com notificação de erro:
- Criar mais de 1 aresta do mesmo tipo entre os mesmos dois nós (exceto em Diagrama de Estados).
- Criar ciclos diretos (vai-e-volta) com arestas de Herança, Dependência, Agregação ou Associação em diagramas de Classe.
- Criar ciclos de colaboração entre dois Objetos.
- Combinar Associação e Agregação simultaneamente entre as mesmas duas classes.

**RF19: Autoarestas e transições.** Alguns tipos de aresta podem ligar um nó a ele mesmo: com um tipo válido selecionado, o usuário arrasta a linha dentro de um nó compatível. O sistema deve bloquear e emitir notificação de erro nas seguintes tentativas:
- Criar auto-herança ou auto-dependência em Diagramas de Classes.
- Criar auto-associação, auto-herança ou auto-dependência para Atores ou Casos de Uso.
- Criar auto-colaboração para Objetos.
- No Diagrama de Estados: cada nó aceita até duas autotransições; entre dois nós são aceitas até duas transições em cada direção; tentar criar além desses limites ou criar transições partindo de um Estado Final ou chegando em um Estado Inicial é rejeitado com notificação de erro.

**RF20: Propriedades das arestas.** O painel de propriedades (RF22) permite editar as propriedades da aresta sem recriá-la. As propriedades variam conforme o diagrama e o tipo de aresta:
- Classes:
  - Composição, agregação e associação: rótulos inicial (Start Label), central (Middle Label) e final (End Label) e Directionality (Unspecified, Unidirectional ou Bidirectional). Composição e agregação também têm Aggregation Type (Aggregation ou Composition), o que permite trocar uma pela outra.
  - Realização, herança e dependência: Generalization Type (Inheritance ou Implementation) e Directionality (Unidirectional ou Bidirectional), ambos escolhidos na própria aresta.
- Sequência: as arestas Call e Return têm Middle Label.
- Objetos: a aresta Collaboration tem Middle Label.
- Estados: a aresta Transition tem Middle Label.
- Casos de uso: o Dependency Type da aresta pode ser alterado entre None, Include e Extend.

## 5. Edição, propriedades e texto

**RF21: Desfazer e refazer.** Edit > Undo (Ctrl+Z) e Redo (Ctrl+Y). O usuário pode desfazer ação por ação, em sequência, até voltar ao estado inicial do diagrama.

**RF22: Acessar propriedades.** As propriedades de um elemento abrem por duplo clique ou por Ctrl+Enter com o elemento selecionado (também em Edit > Properties). No painel, Tab navega entre os campos e Esc fecha o painel. Os campos disponíveis variam conforme o tipo de elemento:
- Diagrama de Classes:
  - Class: Nome, Atributos e Métodos.
  - Interface: Nome e Métodos.
  - Package: Nome.
  - Package Description: Nome e Conteúdo.
- Diagrama de Objetos:
  - Object: Nome.
  - Field: Nome e Valor, conforme definido no RF25.
- Diagrama de Casos de Uso:
  - Actor: Nome.
  - Use Case: Nome.
- Diagrama de Estados:
  - State: Nome. (Estados Iniciais e Finais não possuem propriedades textuais editáveis).
- Diagrama de Sequência:
  - Implicit Parameter: Nome.
  - Call: Opção "Open Bottom" para indicar fluxo incompleto (conforme RF31).
- Geral (Todos os diagramas):
  - Note: Texto livre.

**RF23: Estereótipos UML.** Nas propriedades de qualquer elemento, Ctrl+Q insere os delimitadores « » e posiciona o cursor entre eles.

**RF24: Formatação de texto.** Em uma linha de texto:
- entre barras (/texto/), é exibida em itálico (convenção UML para elementos abstratos);
- entre sublinhados (_texto_), é exibida sublinhada (convenção para membros estáticos).

## 6. Diagrama de objetos

**RF25: Criar campos.** Com a ferramenta Field ativa, clicar dentro de um objeto cria um campo. Nome e valor são editáveis nas propriedades (Ctrl+Enter). O sistema não deve permitir que um nó de Campo (FieldNode) exista como uma raiz solta no diagrama, ele deve obrigatoriamente pertencer a um objeto.

**RF26: Criar referências.** Com a ferramenta Reference ativa, o usuário arrasta a linha elástica da parte Value de um campo até o nó de objeto de destino.

## 7. Diagrama de sequência

**RF27: Criar chamadas.** Com a ferramenta Call ativa, arrastar da linha de vida do chamador até a do chamado cria a chamada inicial e as caixas de ativação de ambos. Chamadas adicionais são criadas arrastando de uma caixa de ativação até uma linha de vida.

**RF28: Criar autochamadas.** Com a ferramenta Call ativa, arrastar a linha dentro de uma caixa de ativação cria uma autochamada.

**RF29: Criar chamadas de construtor.** Com a ferramenta Call ativa, arrastar de uma linha de vida ou caixa de ativação até o nó de parâmetro implícito:
- rotula a chamada com «create»;
- alinha verticalmente o parâmetro implícito à chamada;
- anexa uma caixa de ativação ao parâmetro, para chamadas originadas no construtor.

**RF30: Validar sequência de chamadas.** O sistema verifica se chamadas e retornos resultam em uma sequência de controle válida. Adicionar arestas em pontos arbitrários que causariam um salto no fluxo de controle falha silenciosamente, sem notificação. Ademais, o sistema não deve permitir e deve invalidar estruturalmente:
- Conectar arestas de Chamada ou Retorno em elementos que não sejam Caixas de Ativação (CallNodes).
- Que um mesmo bloco de ativação receba chamadas de múltiplos chamadores diferentes simultaneamente.
- A existência de mais de um bloco de chamada raiz órfão (sem chamador) no mesmo diagrama.

**RF31: Sequência incompleta.** A propriedade Open Bottom da caixa de ativação representa uma sequência de chamadas incompleta.

## 8. Seleção, transferência e exclusão

**RF32: Seleção.** Com a ferramenta Select ativa:
- clicar em um elemento o seleciona ou desseleciona;
- Ctrl+clique adiciona ou remove elementos da seleção;
- clicar e arrastar a partir de um espaço vazio abre o laço, que seleciona todos os elementos da região.

**RF33: Selecionar tudo.** Edit > Select All (Ctrl+A) seleciona todos os elementos do diagrama (nós e arestas), por exemplo para movê-lo por inteiro.

**RF34: Recortar, copiar e colar.** Cut (Ctrl+X), Copy (Ctrl+C) e Paste (Ctrl+V) atuam sobre um ou mais elementos, no mesmo diagrama ou em outro. Colar em outro diagrama só é permitido se todos os elementos forem compatíveis com o tipo de destino, e a tentativa incompatível é reportada por notificação de erro. Colar em diagramas de sequência não é permitido, pois geraria um fluxo de controle inválido.

**RF35: Excluir elementos.** Os elementos selecionados são excluídos por Edit > Delete (tecla Delete).

## 9. Visualização e interface

**RF36: Grade.** View > Show Grid mostra ou oculta a grade. Os elementos se alinham (snap) à grade com ela visível ou não.

**RF37: Zoom.** O diagrama pode ser ampliado e reduzido até dois níveis em cada direção, por Zoom In (Ctrl+=), Zoom Out (Ctrl+-) e Reset Zoom (Ctrl+0), no menu View.

**RF38: Tamanho do diagrama.** Os diagramas têm tamanho fixo. View > Set Diagram Size define largura e altura em pixels (faixa de 250 a 4000), com Restore Defaults.
- O tamanho vale para novos diagramas e para arquivos abertos.
- Ao abrir um diagrama maior que a área configurada, o tamanho aumenta automaticamente para comportá-lo.
- Os valores padrão são relativos ao tamanho da tela.

**RF39: Fonte.** View > Change Font altera a fonte e o tamanho usados no diagrama, com Restore Default. Afeta apenas a aparência.

**RF40: Modo escuro.** View > Dark Mode alterna a interface entre os modos claro e escuro.

## 10. Notificações

**RF41: Exibir notificações.** Erros e confirmações aparecem como notificações temporárias no canto inferior esquerdo da janela: erros em vermelho, confirmações em verde.

**RF42: Duração das notificações.** View > Notification Duration define o tempo de exibição, de 0 a 10 segundos (padrão 5), com Restore Default. O valor 0 desativa todas as notificações.

## Requisitos Não Funcionais

**RNF01: Desempenho.** Diagramas maiores reduzem o desempenho da aplicação. O tamanho inicial deve ser proporcional às dimensões da tela (RF38).

**RNF02: Formato aberto.** Os arquivos JSON têm estrutura documentada e aberta, permitindo acesso e manipulação por ferramentas externas.

**RNF03: Múltiplas formas de interação.** As principais operações podem ser feitas por mais de um meio (mouse, menu, barra, atalho). Exemplos: propriedades por duplo clique ou Ctrl+Enter, e cópia de imagem por menu, barra ou Ctrl+B.