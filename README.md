# AI-Java-JetUML

Editor desktop de diagramas UML (Class, Sequence, State, Object e Use Case) escrito em Java 21 com JavaFX, construído inteiramente por IA como parte da disciplina EXA871 da UEFS.

A especificação está em [REQUISITOS.md](REQUISITOS.md) e o plano de arquitetura e de fases está em [PLANO.md](PLANO.md).

> **Estado atual:** as seis fases do [PLANO.md](PLANO.md) estão implementadas e todos os requisitos (RF01 a RF42, RNF01 a RNF03) estão atendidos. A tabela de rastreabilidade (seção 4) indica as classes e os métodos que implementam cada um.

---

## 1. Compilação e execução

### Pré-requisitos

| Ferramenta | Versão |
|---|---|
| JDK | 21 ou superior (testado com 27.0.1). O código é compilado com `--release 21`. |
| Maven | 3.x (testado com 3.10.0) |
| Acesso à internet | Apenas na primeira compilação, para o Maven baixar as dependências declaradas no `pom.xml`. |

As dependências são baixadas automaticamente pelo Maven:

| Dependência | Uso |
|---|---|
| `org.openjfx:javafx-controls` 21.0.6 | Interface gráfica |
| `org.openjfx:javafx-swing` 21.0.6 | Codificação de imagens PNG/JPEG/GIF/BMP no Export Image (RF04) |
| `org.junit.jupiter:junit-jupiter` 5.10.2 | Testes unitários (escopo `test`) |

### Comandos

Todos os comandos são executados na raiz do repositório.

Compilar e executar a aplicação:

```bash
mvn clean javafx:run
```

Compilar e executar os testes unitários:

```bash
mvn clean test
```

Apenas compilar:

```bash
mvn clean compile
```

### Observações

- Com JDK mais novo que o 21, o JavaFX 21 imprime um aviso sobre `sun.misc.Unsafe` ao iniciar. Ele não afeta o funcionamento.
- As preferências do usuário (grade, dicas de ferramenta, Auto Edit Node, modo escuro, fonte, duração das notificações, tamanho do diagrama) e a lista de arquivos recentes são guardadas com `java.util.prefs.Preferences` e mantidas entre execuções.

### Estrutura do projeto

```
pom.xml
src/main/java/br/uefs/umleditor/
  app/            UmlEditorApp, MainWindow, WelcomePane, UserPreferences
  geom/           Point, Rectangle, Dimension
  model/          Diagram, DiagramType, ToolType, DiagramElement, Node, NamedNode, ParentNode,
                  Edge, Property, BoundsCalculator, ElementCompatibility, InvalidDiagramException
  model/nodes/    ClassNode, InterfaceNode, PackageNode, PackageDescriptionNode, NoteNode,
                  PointNode, StateNode, InitialStateNode, FinalStateNode, ObjectNode, FieldNode,
                  ActorNode, UseCaseNode, ImplicitParameterNode, CallNode, NodeFactory
  model/edges/    DependencyEdge, GeneralizationEdge, AssociationEdge, AggregationEdge,
                  ClassRelationshipEdge, Directionality, NoteEdge, StateTransitionEdge,
                  ObjectReferenceEdge, ObjectCollaborationEdge, LabeledEdge,
                  UseCaseAssociationEdge, UseCaseDependencyEdge, UseCaseGeneralizationEdge,
                  CallEdge, ReturnEdge, EdgeFactory
  model/rules/    DiagramRules, ClassDiagramRules, StateDiagramRules, ObjectDiagramRules,
                  UseCaseDiagramRules, SequenceDiagramRules, ValidationResult,
                  DiagramStructureValidator
  model/builder/  DiagramBuilder, SequenceDiagramBuilder
  command/        DiagramOperation, CompoundOperation, AddNodeOperation, RemoveNodeOperation,
                  MoveNodesOperation, ReparentNodeOperation, AddEdgeOperation,
                  RemoveEdgeOperation, ChangePropertyOperation, UndoManager
  layout/         GridSnap, EdgePathCalculator, SequenceLayout
  render/         Theme, RenderContext, FontSettings, TextStyle, StyledText, TextBlock,
                  GraphicsAdapter, FxGraphicsAdapter, SvgGraphicsAdapter, LineStyle, ArrowHead,
                  EdgeRenderer, DiagramRenderer
  render/nodes/   NodeRenderer e um renderizador por família de nós
  persistence/    DiagramSerializer, DiagramDeserializer, DiagramContent, RecentFiles
  persistence/json/ JsonParser, JsonWriter, JsonException
  export/         DiagramImage, ImageFormat
  ui/             DiagramTab, DiagramCanvas, CanvasController, SelectionModel, ToolPalette,
                  ToolIconPainter, NotificationArea, PropertyEditor, StereotypeInsertion,
                  ElementClipboard
  ui/dialogs/     DiagramSizeDialog, FontDialog, NotificationDurationDialog, SpinnerSupport
src/main/resources/br/uefs/umleditor/
  base.css        aparência padrão (clara) e estilo das notificações
  dark.css        aparência do modo escuro
src/test/java/br/uefs/umleditor/
  model/DiagramTypeTest, model/PropertiesTest, model/builder/DiagramBuilderTest,
  model/builder/SequenceDiagramBuilderTest, model/builder/CopyPasteTest,
  model/rules/EdgeRulesTest, command/UndoManagerTest, layout/EdgePathCalculatorTest,
  render/StyledTextTest, ui/StereotypeInsertionTest, persistence/DiagramFileTest,
  persistence/json/JsonTest, export/ImageFormatTest
```

### Como usar

- **Criar diagrama:** botões da tela de boas-vindas ou File > New.
- **Arquivos:** File > Open (`Ctrl+O`) abre um arquivo `.json`; File > Recent Files lista os 8 últimos arquivos abertos ou salvos; File > Save (`Ctrl+S`) grava no arquivo do diagrama (ou pergunta onde, se ele ainda não tem arquivo); File > Save As grava em outro arquivo; File > Close (`Ctrl+W`) fecha a aba. Ao fechar uma aba, ou sair, com alterações não salvas, a aplicação pergunta se deve salvar. Um arquivo aberto recebe o tamanho configurado em View > Set Diagram Size, ampliado se o conteúdo for maior.
- **Duplicar:** File > Duplicate (`Ctrl+D`) abre uma cópia do diagrama atual em uma nova aba, ainda sem arquivo.
- **Exportar imagem:** File > Export Image salva o diagrama como SVG, PNG, JPEG, GIF ou BMP, conforme o tipo escolhido em "Save as type" (ou a extensão digitada no nome do arquivo).
- **Copiar imagem:** File > Copy to Clipboard, a ferramenta To Clipboard da barra ou `Ctrl+B` copiam uma imagem do diagrama inteiro para a área de transferência e mostram uma notificação de confirmação. A imagem exportada ou copiada é recortada nos limites do conteúdo, não tem a grade e usa sempre as cores claras.
- **Recortar, copiar e colar:** Edit > Cut (`Ctrl+X`), Copy (`Ctrl+C`) e Paste (`Ctrl+V`) atuam sobre os elementos selecionados, no mesmo diagrama ou em outra aba. Os elementos colados aparecem deslocados e ficam selecionados. Colar em um diagrama de outro tipo só é permitido se todos os elementos existirem naquele tipo (uma nota, por exemplo, pode ir para qualquer diagrama); caso contrário, aparece uma notificação de erro. Colar em diagramas de sequência não é permitido. Campos e caixas de ativação selecionados sem o objeto ou a linha de vida a que pertencem não são copiados.
- **Escolher ferramenta:** clique na barra lateral, teclas `1`–`9`, `0`, `A`, `B`… ou clique direito no diagrama.
- **Criar nó:** com uma ferramenta de nó ativa, clique em um espaço vazio. Clicar dentro de um pacote cria o nó como filho do pacote; com a ferramenta Field, clique dentro de um objeto para criar um campo. Clicar sobre outro nó não tem efeito.
- **Criar aresta:** com uma ferramenta de aresta ativa, pressione o botão sobre o nó de origem, arraste a linha elástica e solte sobre o nó de destino. Arrastar dentro de um mesmo nó cria uma autoaresta, quando o tipo permite. Soltar em espaço vazio não tem efeito. Conexões inválidas não alteram o diagrama e aparecem como notificação de erro em vermelho.
- **Note Connector:** arraste de um elemento até uma nota para ligá-los; arraste de uma nota até um espaço vazio para ancorar a extremidade naquele ponto fixo.
- **Reference (diagrama de objetos):** arraste a partir da parte Value de um campo (à direita do `=`) até o objeto de destino.
- **Caminho das arestas:** é calculado automaticamente e acompanha os nós quando eles são movidos; para mudar o caminho, mova os nós.
- **Diagrama de sequência:**
  - *Primeira chamada:* com a ferramenta Call, arraste da linha de vida do chamador até a linha de vida do chamado. São criadas as caixas de ativação dos dois. A altura onde o arraste começa define a altura da primeira ativação.
  - *Outras chamadas:* arraste de uma caixa de ativação até outra linha de vida. A nova chamada entra entre as chamadas já existentes daquela ativação, conforme a altura onde o arraste começou.
  - *Autochamada:* arraste dentro de uma caixa de ativação. A nova ativação aparece aninhada, deslocada para a direita.
  - *Chamada de construtor:* arraste de uma ativação até a caixa (o retângulo com o nome) de um parâmetro implícito que ainda não tem ativações. A chamada recebe o rótulo «create», o parâmetro é alinhado verticalmente à chamada e ganha uma caixa de ativação logo abaixo.
  - *Retorno:* com a ferramenta Return, arraste de uma ativação até a ativação que a chamou (ou qualquer ponto da linha de vida dela).
  - *Posições:* as caixas de ativação são posicionadas automaticamente a partir das chamadas; mover um parâmetro implícito leva junto suas ativações.
  - *Tentativas inválidas* (segunda ativação sem chamador, soltar na própria linha de vida fora da ativação, retorno para quem não chamou, segundo retorno, construtor de objeto que já tem ativações etc.) não têm efeito e não geram notificação.
  - *Exclusão:* excluir uma chamada ou ativação exclui também as chamadas que dependem dela.
  - *Open Bottom:* nas propriedades de uma caixa de ativação, deixa a caixa sem o lado inferior, indicando sequência incompleta.
- **Selecionar:** com a ferramenta Select, clique em um elemento (nó ou aresta); `Ctrl`+clique adiciona ou remove da seleção; arrastar a partir de um espaço vazio desenha o laço, que seleciona os elementos inteiramente contidos nele. Edit > Select All (`Ctrl+A`) seleciona tudo.
- **Mover:** arraste os elementos selecionados. A posição se alinha à grade de 10 px, com a grade visível ou não, e os elementos não saem da área do diagrama. Campos acompanham o objeto e não são movidos sozinhos.
- **Pacotes:** com um nó selecionado posicionado sobre um pacote, pressione `Shift` para incluí-lo no pacote; com um filho de pacote selecionado, `Shift` o desanexa. O pacote se ajusta para conter os filhos.
- **Excluir:** Edit > Delete (tecla `Delete`). Excluir um nó exclui também seus filhos e as arestas ligadas a eles.
- **Propriedades:** dê duplo clique em um elemento, ou selecione-o e use Edit > Properties (`Ctrl+Enter`). O painel mostra os campos daquele tipo de elemento (nome, atributos, métodos, rótulos, tipos etc.) e as mudanças aparecem no diagrama enquanto são digitadas. `Tab` e `Shift+Tab` passam de um campo a outro, `Esc` fecha o painel e `Ctrl+Q` insere `«»` com o cursor entre os delimitadores. O painel é modal: o diagrama volta a aceitar edição quando ele é fechado. Estados Inicial e Final, Note Connector, Reference e as associações e generalizações de casos de uso não têm propriedades editáveis.
- **Auto Edit Node:** com View > Auto Edit Node ativo, o painel de propriedades abre sozinho sempre que um nó é criado (vale apenas para nós).
- **Desfazer e refazer:** Edit > Undo (`Ctrl+Z`) e Edit > Redo (`Ctrl+Y`), ação por ação, até o estado inicial do diagrama. Todas as alterações feitas em uma abertura do painel de propriedades formam uma única ação.
- **Formatação de texto:** uma linha escrita como `/texto/` aparece em itálico e `_texto_` aparece sublinhada (podem ser combinadas).
- **Fonte:** View > Change Font escolhe a família e o tamanho (8 a 36) do texto dos diagramas, com Restore Default.

### Regras de conexão

| Diagrama | Arestas e extremidades aceitas | Rejeitado com notificação de erro |
|---|---|---|
| Todos | Note Connector: de qualquer elemento até uma nota, ou de uma nota até um ponto vazio | Ligação que não envolva nota dessa forma; conector repetido |
| Classes | Dependency: classes, interfaces e pacotes. Inheritance, Realization, Association, Aggregation e Composition: classes e interfaces | Aresta repetida (mesmo tipo, origem e destino); ciclo direto de Herança, Realização, Dependência, Agregação/Composição ou Associação; Associação e Agregação/Composição entre as mesmas duas classes; auto-herança, autorrealização e autodependência |
| Estados | Transition entre estados | Transição partindo de Estado Final ou chegando em Estado Inicial; mais de 2 autotransições por nó; mais de 2 transições na mesma direção entre dois nós |
| Objetos | Reference: da parte Value de um campo até um objeto. Collaboration: entre objetos | Referência fora da parte Value; aresta repetida; ciclo de colaboração; autocolaboração |
| Casos de uso | Association: atores e casos de uso. Include/Extend: casos de uso. Generalization: dois atores ou dois casos de uso | Aresta repetida; qualquer autoaresta |

"Mesmo tipo" significa a mesma classe de aresta do modelo: Inheritance e Realization são ambas `GeneralizationEdge`, Aggregation e Composition são ambas `AggregationEdge`, e Include e Extend são ambas `UseCaseDependencyEdge`, pois o tipo de cada uma pode ser trocado nas propriedades (RF20). Por isso, por exemplo, não é possível ter uma Aggregation e uma Composition da mesma classe A para a mesma classe B. Pelo mesmo motivo, trocar o tipo de uma aresta no painel de propriedades nunca cria uma conexão proibida, e não há verificação adicional nessa troca.

---

## 2. Formato JSON dos arquivos de diagrama (RNF02)

Cada arquivo guarda um único diagrama, como um objeto JSON em UTF-8. As coordenadas são absolutas, em pixels, com origem no canto superior esquerdo do diagrama. Os `id` são inteiros válidos apenas dentro do arquivo e servem para que arestas e listas de filhos referenciem os nós.

### 2.1 Objeto raiz

| Campo | Tipo | Descrição |
|---|---|---|
| `formatVersion` | string | Versão do formato. Atualmente `"1.0"`. |
| `diagram` | string | Tipo do diagrama: `ClassDiagram`, `SequenceDiagram`, `StateDiagram`, `ObjectDiagram` ou `UseCaseDiagram`. |
| `width` | inteiro | Largura do diagrama quando foi salvo, em pixels (250 a 4000). Ao abrir o arquivo, vale o tamanho configurado na aplicação, ampliado se o conteúdo for maior (RF38); o valor é mantido em File > Duplicate. |
| `height` | inteiro | Altura do diagrama quando foi salvo, em pixels (250 a 4000), com o mesmo uso de `width`. |
| `nodes` | array | Todos os nós, inclusive os filhos de outros nós. |
| `edges` | array | Todas as arestas. |

### 2.2 Nós

Campos comuns a todos os nós:

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | inteiro | Identificador único no arquivo. |
| `type` | string | Tipo do nó (tabela abaixo). |
| `x`, `y` | número | Posição do canto superior esquerdo. |
| `children` | array de inteiros | `id` dos nós filhos (pacotes, campos de objeto, caixas de ativação). Vazio quando não há filhos. |

Propriedades específicas de cada tipo:

| `type` | Diagramas | Propriedades | Filhos permitidos |
|---|---|---|---|
| `ClassNode` | Class | `name`, `attributes`, `methods` (texto multilinha) | — |
| `InterfaceNode` | Class | `name`, `methods` | — |
| `PackageNode` | Class | `name` | `ClassNode`, `InterfaceNode`, `PackageNode`, `PackageDescriptionNode` |
| `PackageDescriptionNode` | Class | `name`, `contents` | — |
| `NoteNode` | todos | `text` | — |
| `PointNode` | todos | nenhuma (âncora fixa de um Note Connector) | — |
| `ImplicitParameterNode` | Sequence | `name` | `CallNode` |
| `CallNode` | Sequence | `openBottom` (booleano) | — |
| `StateNode` | State | `name` | — |
| `InitialStateNode` | State | nenhuma | — |
| `FinalStateNode` | State | nenhuma | — |
| `ObjectNode` | Object | `name` | `FieldNode` |
| `FieldNode` | Object | `name`, `value` | — (sempre filho de um `ObjectNode`) |
| `ActorNode` | Use Case | `name` | — |
| `UseCaseNode` | Use Case | `name` | — |

### 2.3 Arestas

Campos comuns a todas as arestas:

| Campo | Tipo | Descrição |
|---|---|---|
| `type` | string | Tipo da aresta (tabela abaixo). |
| `start` | inteiro | `id` do nó de origem. |
| `end` | inteiro | `id` do nó de destino. |

Propriedades específicas de cada tipo:

| `type` | Diagramas | Propriedades e valores |
|---|---|---|
| `DependencyEdge` | Class | `directionality`: `Unidirectional` \| `Bidirectional` |
| `GeneralizationEdge` | Class | `generalizationType`: `Inheritance` \| `Implementation` (Realization) |
| `AssociationEdge` | Class | `startLabel`, `middleLabel`, `endLabel`; `directionality`: `Unspecified` \| `Unidirectional` \| `Bidirectional` |
| `AggregationEdge` | Class | as mesmas de `AssociationEdge` e `aggregationType`: `Aggregation` \| `Composition` |
| `NoteEdge` | todos | nenhuma |
| `CallEdge` | Sequence | `middleLabel`; `constructorCall` (booleano, verdadeiro em chamadas de construtor «create») |
| `ReturnEdge` | Sequence | `middleLabel` |
| `StateTransitionEdge` | State | `middleLabel` |
| `ObjectReferenceEdge` | Object | nenhuma (`start` é um `FieldNode`) |
| `ObjectCollaborationEdge` | Object | `middleLabel` |
| `UseCaseAssociationEdge` | Use Case | nenhuma |
| `UseCaseDependencyEdge` | Use Case | `dependencyType`: `None` \| `Include` \| `Extend` |
| `UseCaseGeneralizationEdge` | Use Case | nenhuma |

Em diagramas de sequência, a ordem das `CallEdge` no array `edges` define a ordem vertical das chamadas.

### 2.4 Exemplo

```json
{
  "formatVersion": "1.0",
  "diagram": "ClassDiagram",
  "width": 1920,
  "height": 1080,
  "nodes": [
    { "id": 0, "type": "PackageNode", "x": 100, "y": 80, "name": "domain", "children": [1] },
    { "id": 1, "type": "ClassNode", "x": 120, "y": 120,
      "name": "/Shape/", "attributes": "- id : int", "methods": "+ area() : double",
      "children": [] },
    { "id": 2, "type": "NoteNode", "x": 400, "y": 60, "text": "observação", "children": [] }
  ],
  "edges": [
    { "type": "AggregationEdge", "start": 0, "end": 1, "aggregationType": "Composition",
      "startLabel": "", "middleLabel": "", "endLabel": "1..*", "directionality": "Unspecified" },
    { "type": "NoteEdge", "start": 1, "end": 2 }
  ]
}
```

### 2.5 Regras de validação na abertura

Ao abrir um arquivo, a aplicação verifica sua estrutura. Se houver erro, o arquivo não é aberto e uma notificação de erro é exibida. São rejeitados:

- JSON malformado, `diagram` desconhecido ou tipos de nó/aresta que não existem naquele tipo de diagrama;
- `id` duplicados, ou `start`, `end` e `children` que não apontam para nós existentes;
- filhos que o pai não aceita (por exemplo, uma nota dentro de um pacote), nós com mais de um pai ou contenção circular;
- `FieldNode` que não seja filho de um `ObjectNode` e `CallNode` fora de uma linha de vida (RF25);
- arestas que violam as regras de conexão do tipo de diagrama, como arestas repetidas, ciclos diretos ou autoarestas proibidas (RF18, RF19);
- em diagramas de sequência: `CallEdge`/`ReturnEdge` cujos extremos não sejam `CallNode`, `CallNode` com mais de um chamador, retorno que não volta ao chamador ou mais de um `CallNode` sem chamador (RF30).

Propriedades de texto ausentes são lidas como vazias, e propriedades de escolha ausentes recebem o valor padrão da aresta. Valores de escolha (`directionality`, `generalizationType`, `aggregationType`, `dependencyType`) aceitam maiúsculas ou minúsculas.

O mesmo formato é usado internamente para os elementos copiados com Cut/Copy (RF34), com `width` e `height` iguais a 0.

---

## 3. Legenda da rastreabilidade

| Situação | Significado |
|---|---|
| ✅ Implementado | Atendido pelo código atual. |

Caminhos abreviados: `app` = `br.uefs.umleditor.app`, `model` = `br.uefs.umleditor.model`, `ui` = `br.uefs.umleditor.ui`, e assim por diante.

## 4. Rastreabilidade dos requisitos

### 4.1 Diagramas e arquivos

| Requisito | Situação | Classes e métodos |
|---|---|---|
| RF01 Criar novo diagrama | ✅ Implementado | `app.WelcomePane(Consumer<DiagramType>)`; `app.MainWindow.createFileMenu()` (File > New); `app.MainWindow.newDiagram(DiagramType)`; `app.MainWindow.showWelcomeIfEmpty()`; `model.DiagramType`; `model.Diagram(DiagramType, int, int)`; `ui.DiagramTab(Diagram, String, RenderContext, boolean)` |
| RF02 Abrir, salvar e fechar | ✅ Implementado | `app.MainWindow.open()`, `openFile(Path)`, `save(DiagramTab)`, `saveAs(DiagramTab)`, `write(DiagramTab, Path)`, `closeCurrent()`, `confirmClose(DiagramTab)`, `updateRecentMenu()`; `persistence.RecentFiles`; `persistence.DiagramSerializer.toJson(Diagram)`; `persistence.DiagramDeserializer.read(String)`, `readDiagram(String)`; `persistence.json.JsonParser`, `JsonWriter`; `model.rules.DiagramStructureValidator.build(...)`; `ui.DiagramTab.getFile()`, `setFile(Path)`, `isModified()` |
| RF03 Duplicar diagrama | ✅ Implementado | `app.MainWindow.duplicate()` (File > Duplicate, Ctrl+D): serializa o diagrama com `persistence.DiagramSerializer.toJson(Diagram)` e o relê com `persistence.DiagramDeserializer.readDiagram(String)` em uma nova aba |
| RF04 Exportar como imagem | ✅ Implementado | `app.MainWindow.exportImage()` (filtros de "Save as type"); `export.ImageFormat` (SVG, PNG, JPEG, GIF, BMP), `fromFileName(String)`; `export.DiagramImage.export(...)`, `toImage(...)`, `toSvg(...)`, `contentBounds(...)`; `render.SvgGraphicsAdapter` |
| RF05 Copiar para a área de transferência | ✅ Implementado | `export.DiagramImage.copyToClipboard(Diagram, FontSettings)` (sem grade, recortada no conteúdo); `ui.DiagramTab.copyImageToClipboard()` (notificação de confirmação), `runActionTool(ToolType)` (ferramenta To Clipboard); `app.MainWindow.createFileMenu()` (Copy to Clipboard, Ctrl+B) |
| RF06 Encerrar a aplicação | ✅ Implementado | `app.MainWindow.exit()` (File > Exit, Ctrl+Q) e `stage.setOnCloseRequest(...)`, ambos com `confirmCloseAll()` para diagramas não salvos |

### 4.2 Barra de ferramentas e área de trabalho

| Requisito | Situação | Classes e métodos |
|---|---|---|
| RF07 Selecionar ferramenta | ✅ Implementado | `model.ToolType`; `model.DiagramType.getTools()`; `ui.ToolPalette(DiagramType, Theme, Consumer<ToolType>)`; `ui.ToolPalette.selectTool(int)`; `ui.ToolPalette.selectedToolProperty()`; `ui.ToolIconPainter.createIcon(ToolType, Theme)` e `paint(Canvas, ToolType, Theme)` |
| RF08 Atalhos das ferramentas | ✅ Implementado | `model.DiagramType.shortcutFor(int)`; `model.DiagramType.toolIndexForShortcut(char)`; `ui.DiagramTab.handleToolShortcut(KeyEvent)` |
| RF09 Menu de contexto | ✅ Implementado | `ui.ToolPalette.createContextMenu()`; construtor de `ui.DiagramTab` (`setOnContextMenuRequested`) |
| RF10 Dicas de ferramenta | ✅ Implementado | `model.ToolType.getDisplayName()` e `getDescription()`; `ui.ToolPalette.setShowToolHint(boolean)`; `ui.ToolPalette.setVerboseTooltips(boolean)`; `app.MainWindow.createViewMenu()`; `app.UserPreferences.showToolHintProperty()` e `verboseTooltipsProperty()` |

### 4.3 Nós e pacotes

| Requisito | Situação | Classes e métodos |
|---|---|---|
| RF11 Criar nós | ✅ Implementado | `model.nodes.NodeFactory.create(ToolType)`; `model.builder.DiagramBuilder.createNode(Node, Point)`, `findNodeAt(Point)`, `keepInsideDiagram(Node)`; `command.AddNodeOperation`; `ui.CanvasController.mousePressed(MouseEvent)`, `createNode(Node, Point)`; `ui.DiagramTab.perform(DiagramOperation)` |
| RF12 Edição automática de nós | ✅ Implementado | `app.UserPreferences.autoEditNodeProperty()`; `app.MainWindow.createViewMenu()` (Auto Edit Node); `ui.DiagramTab.setAutoEditNode(boolean)`, `isAutoEditNode()`; `ui.CanvasController.createNode(Node, Point)` (abre o painel só após criar um nó) |
| RF13 Criar notas | ✅ Implementado | `model.nodes.NoteNode`; `render.nodes.NoteNodeRenderer.getBounds(NoteNode)` e `draw(NoteNode, GraphicsAdapter)`; criação pela ferramenta Note (RF11), disponível em todos os tipos de diagrama |
| RF14 Conectar notas | ✅ Implementado | `model.edges.NoteEdge`; `model.nodes.PointNode`; `model.rules.DiagramRules.check(...)` e `checkNoteEdge(...)`; `model.builder.DiagramBuilder.createEdge(Edge, Point, Point)` e `createPoint(Point)` (âncora em ponto fixo); `layout.EdgePathCalculator.getPath(Edge)` (a conexão acompanha a nota); `render.nodes.PointNodeRenderer`; `model.builder.DiagramBuilder.removeElements(...)` (o ponto é removido com o conector) |
| RF15 Nós filhos em pacotes | ✅ Implementado | `model.ParentNode.canAccept(Node)`, `addChild(int, Node)`, `removeChild(Node)`; `model.nodes.PackageNode.canAccept(Node)`; `model.builder.DiagramBuilder.createNode(Node, Point)` (clique dentro do pacote), `toggleParent(...)`, `findPackageAt(Point, Node)`; `command.ReparentNodeOperation`; `ui.CanvasController.keyPressed(KeyEvent)` (tecla Shift); `render.nodes.PackageNodeRenderer.getBounds(NamedNode)` (o pacote se ajusta aos filhos); `render.DiagramRenderer.layout(Diagram)` |

### 4.4 Arestas, transições e validação

| Requisito | Situação | Classes e métodos |
|---|---|---|
| RF16 Criar arestas | ✅ Implementado | `model.edges.EdgeFactory.create(ToolType)`; `ui.CanvasController.startEdge(Edge, Point)`, `mouseDragged(MouseEvent)`, `finishEdge(Point)`; `ui.DiagramCanvas.setRubberBand(Point, Point)` (linha elástica); `model.builder.DiagramBuilder.createEdge(Edge, Point, Point)`; `command.AddEdgeOperation`; `model.Diagram.addEdge(...)` |
| RF17 Layout automático | ✅ Implementado | `layout.EdgePathCalculator.getPath(Edge)`, `betweenNodes(Edge)` (segmentos entre as bordas, arestas paralelas afastadas, transições opostas em arco), `selfLoop(Edge)`, `referencePath(FieldNode, Node)`, `exitPoint(...)`; `render.EdgeRenderer.draw(Edge, List<Point>, GraphicsAdapter)`; `render.ArrowHead.draw(...)`. O caminho não é armazenado: é recalculado a cada desenho a partir da posição dos nós. |
| RF18 Validação das conexões | ✅ Implementado | `model.rules.DiagramRules.forType(DiagramType)`, `check(Diagram, Edge, Node, Node)`; `model.rules.ClassDiagramRules.checkEdge(...)` (duplicidade, ciclos diretos, Associação + Agregação); `model.rules.ObjectDiagramRules.checkEdge(...)` (ciclos de colaboração); `model.rules.UseCaseDiagramRules`; `model.rules.ValidationResult`; `ui.CanvasController.finishEdge(Point)` → `ui.DiagramTab.reportError(String)` → `ui.NotificationArea.showError(String)` |
| RF19 Autoarestas e transições | ✅ Implementado | `model.builder.DiagramBuilder.createEdge(...)` (arraste dentro do mesmo nó); `model.rules.ClassDiagramRules.checkEdge(...)` (auto-herança e autodependência); `model.rules.UseCaseDiagramRules.checkEdge(...)`; `model.rules.ObjectDiagramRules.checkEdge(...)` (autocolaboração); `model.rules.StateDiagramRules.checkEdge(...)` e `MAX_TRANSITIONS_PER_DIRECTION`; `layout.EdgePathCalculator.selfLoop(Edge)` |
| RF20 Propriedades das arestas | ✅ Implementado | `getProperties()` de `model.edges.ClassRelationshipEdge` (Start/Middle/End Label, Directionality), `AggregationEdge` (+ Aggregation Type), `GeneralizationEdge` (Generalization Type), `DependencyEdge` (Directionality Unidirectional/Bidirectional), `LabeledEdge` (Middle Label de Transition e Collaboration), `UseCaseDependencyEdge` (Dependency Type None/Include/Extend); `ui.PropertyEditor.edit(...)`; `render.EdgeRenderer.appearanceOf(Edge)`. `CallEdge` e `ReturnEdge` têm Middle Label por herdarem de `LabeledEdge`. |

### 4.5 Edição, propriedades e texto

| Requisito | Situação | Classes e métodos |
|---|---|---|
| RF21 Desfazer e refazer | ✅ Implementado | `app.MainWindow.createEditMenu()` (Undo Ctrl+Z, Redo Ctrl+Y); `ui.DiagramTab.undo()`, `redo()`, `perform(DiagramOperation)`, `record(DiagramOperation)`; `command.UndoManager.execute(...)`, `record(...)`, `undo()`, `redo()`; operações reversíveis do pacote `command` (inclusive `ChangePropertyOperation`) |
| RF22 Acessar propriedades | ✅ Implementado | `model.DiagramElement.getProperties()` e as implementações em `model.NamedNode`, `model.nodes.ClassNode`, `InterfaceNode`, `PackageDescriptionNode`, `FieldNode`, `NoteNode`; `model.Property`; `ui.PropertyEditor.edit(...)`, `createInput(...)`, `moveFocusOnTab(...)` (Tab), filtro de Esc; `ui.CanvasController.mousePressed(MouseEvent)` (duplo clique); `ui.DiagramTab.editSelectedProperties()`, `editProperties(DiagramElement)`; `app.MainWindow.createEditMenu()` (Properties, Ctrl+Enter). Open Bottom da caixa de ativação: `model.nodes.CallNode.getProperties()`. |
| RF23 Estereótipos UML | ✅ Implementado | `ui.PropertyEditor.insertStereotypeOnCtrlQ(KeyEvent, TextInputControl)`; `ui.StereotypeInsertion.insert(String, int, int)` |
| RF24 Formatação de texto | ✅ Implementado | `render.StyledText.parse(String)`, `lines(String)`; `render.TextStyle.with(StyledLine)`; `render.TextBlock.measure(...)` e `draw(...)`; `render.FxGraphicsAdapter.drawText(...)` (itálico pela fonte, sublinhado desenhado sob o texto) |

### 4.6 Diagrama de objetos

| Requisito | Situação | Classes e métodos |
|---|---|---|
| RF25 Criar campos | ✅ Implementado | `model.nodes.FieldNode.requiresParent()`, `getProperties()` (Name e Value); `model.nodes.ObjectNode.canAccept(Node)`; `model.Diagram.addRootNode(int, Node)` (rejeita campo como raiz); `model.builder.DiagramBuilder.createRequiredChild(Node, Node)`; `render.nodes.ObjectNodeRenderer.getFieldBounds(ObjectNode, FieldNode)`; `render.nodes.FieldNodeRenderer`; edição por `ui.PropertyEditor` (Ctrl+Enter) |
| RF26 Criar referências | ✅ Implementado | `model.edges.ObjectReferenceEdge`; `model.builder.DiagramBuilder.createEdge(...)` (início na parte Value, destino em campo convertido para o objeto); `model.BoundsCalculator.getConnectionBounds(Node)`; `render.nodes.FieldNodeRenderer.getValueBounds(FieldNode)`; `model.rules.ObjectDiagramRules.checkEdge(...)`; `layout.EdgePathCalculator.referencePath(...)` |

### 4.7 Diagrama de sequência

| Requisito | Situação | Classes e métodos |
|---|---|---|
| RF27 Criar chamadas | ✅ Implementado | `model.edges.EdgeFactory.create(ToolType)` (Call); `model.builder.SequenceDiagramBuilder.createEdge(...)`, `createCall(...)` (primeira chamada a partir da linha de vida, demais a partir de ativações), `insertionIndex(CallNode, double)`; `model.nodes.CallNode`; `model.nodes.ImplicitParameterNode.canAccept(Node)`; `model.edges.CallEdge`; `layout.SequenceLayout.apply()`, `place(CallNode, double)`; `layout.EdgePathCalculator.callPath(CallEdge)`; `render.nodes.CallNodeRenderer`; `render.DiagramRenderer.layout(Diagram)` |
| RF28 Criar autochamadas | ✅ Implementado | `model.builder.SequenceDiagramBuilder.createCall(...)` (arraste que começa e termina na mesma ativação); `layout.SequenceLayout.place(...)` (ativação aninhada deslocada); `layout.EdgePathCalculator.callPath(CallEdge)` (laço no lado direito) |
| RF29 Criar chamadas de construtor | ✅ Implementado | `model.builder.SequenceDiagramBuilder.createCall(...)` (destino na caixa do parâmetro, via `getConnectionBounds`); `model.edges.CallEdge.setConstructorCall(boolean)`, `CREATE_LABEL`; `layout.SequenceLayout.place(...)` (alinha o parâmetro à chamada e anexa a ativação abaixo da caixa); `render.nodes.ImplicitParameterNodeRenderer.getBoxBounds(...)` |
| RF30 Validar sequência de chamadas | ✅ Implementado | `model.rules.SequenceDiagramRules.checkEdge(...)` (apenas entre CallNodes, um chamador por ativação, retorno só para o chamador e uma vez), `callerOf(...)`; `model.builder.SequenceDiagramBuilder.createCall(...)` (uma única ativação raiz), `createReturn(...)`, `removeElements(...)`; falhas retornam `ValidationResult.SILENT_FAILURE` e não geram notificação. A mesma verificação é feita ao abrir arquivos (`model.rules.DiagramStructureValidator`). |
| RF31 Sequência incompleta | ✅ Implementado | `model.nodes.CallNode.isOpenBottom()`, `setOpenBottom(boolean)`, `getProperties()`; `render.nodes.CallNodeRenderer.draw(...)` (sem o lado inferior) |

### 4.8 Seleção, transferência e exclusão

| Requisito | Situação | Classes e métodos |
|---|---|---|
| RF32 Seleção | ✅ Implementado | `ui.SelectionModel.set(DiagramElement)`, `toggle(DiagramElement)`, `setAll(...)`, `addAll(...)`, `clear()`; `ui.CanvasController.startSelection(Point, boolean)` (clique e Ctrl+clique), `mouseDragged(MouseEvent)` (laço); `model.builder.DiagramBuilder.findElementAt(Point)`, `findElementsInside(Rectangle)`; `layout.EdgePathCalculator.contains(Edge, Point)`; `ui.DiagramCanvas.setLasso(Rectangle)`; `render.DiagramRenderer.draw(...)` (marcadores de seleção) |
| RF33 Selecionar tudo | ✅ Implementado | `app.MainWindow.createEditMenu()` (Select All, Ctrl+A); `ui.DiagramTab.selectAll()`; `model.builder.DiagramBuilder.getAllElements()` (nós e arestas); `model.builder.DiagramBuilder.movableNodes(...)` (mover a seleção inteira) |
| RF34 Recortar, copiar e colar | ✅ Implementado | `app.MainWindow.createEditMenu()` (Cut Ctrl+X, Copy Ctrl+C, Paste Ctrl+V); `ui.ElementClipboard` (compartilhado entre as abas); `ui.DiagramTab.copySelection(...)`, `cutSelection(...)`, `paste(...)` (recusa em sequência e elementos incompatíveis, com notificação); `model.builder.DiagramBuilder.copyFragment(...)`, `paste(...)`; `model.ElementCompatibility.accepts(DiagramType, DiagramElement)` |
| RF35 Excluir elementos | ✅ Implementado | `app.MainWindow.createEditMenu()` (Delete, tecla Delete); `ui.DiagramTab.deleteSelection()`; `model.builder.DiagramBuilder.removeElements(...)`, `topmostNodes(...)`; `command.RemoveEdgeOperation`, `command.RemoveNodeOperation`, `command.CompoundOperation`; em sequência, `model.builder.SequenceDiagramBuilder.removeElements(...)` remove também as chamadas dependentes |

### 4.9 Visualização e interface

| Requisito | Situação | Classes e métodos |
|---|---|---|
| RF36 Grade | ✅ Implementado | Exibição: `ui.DiagramCanvas.setShowGrid(boolean)`, `paintGrid(...)`, `app.UserPreferences.showGridProperty()`, `app.MainWindow.createViewMenu()`. Alinhamento, com a grade visível ou não: `layout.GridSnap.snap(double)`, `snap(Point)`, `GRID_SIZE`, usados em `model.builder.DiagramBuilder.createNode(...)` e `ui.CanvasController.dragNodes(Point)` |
| RF37 Zoom | ✅ Implementado | `ui.DiagramCanvas.zoomIn()`, `zoomOut()`, `resetZoom()`, `getZoom()`, `canZoomIn()`, `canZoomOut()`; `app.MainWindow.createViewMenu()`, `zoom(Consumer<DiagramCanvas>)`, `updateZoomItems()` |
| RF38 Tamanho do diagrama | ✅ Implementado | `ui.dialogs.DiagramSizeDialog`; `app.MainWindow.editDiagramSize()`, `newDiagram(DiagramType)`, `fitSize(Diagram)` (arquivos abertos recebem o tamanho configurado, ampliado para comportar o conteúdo); `app.UserPreferences.getDiagramWidth()`, `getDiagramHeight()`, `setDiagramSize(int, int)`, `getDefaultDiagramWidth()`, `getDefaultDiagramHeight()`; `model.Diagram.clampSize(int)`; `export.DiagramImage.contentBounds(...)` |
| RF39 Fonte | ✅ Implementado | `ui.dialogs.FontDialog`; `app.MainWindow.editFont()`; `app.UserPreferences.fontSettingsProperty()`; `render.FontSettings.defaults()`, `font(TextStyle)`; `render.RenderContext.setFontSettings(FontSettings)`, `measure(String, TextStyle)`; `ui.DiagramTab.setFontSettings(FontSettings)` |
| RF40 Modo escuro | ✅ Implementado | `app.MainWindow.applyTheme()`, `applyStylesheets()`; `render.Theme.of(boolean)`; `ui.DiagramTab.setTheme(Theme)`; `ui.ToolPalette.setTheme(Theme)`; `render.RenderContext.setTheme(Theme)`; `dark.css`; `app.UserPreferences.darkModeProperty()` |

### 4.10 Notificações

| Requisito | Situação | Classes e métodos |
|---|---|---|
| RF41 Exibir notificações | ✅ Implementado | `ui.NotificationArea.showError(String)`, `showConfirmation(String)`, `show(String, String)`; posicionamento em `app.MainWindow` (`StackPane` no canto inferior esquerdo); `ui.DiagramTab.reportError(String)`; classes `notification-error` e `notification-confirmation` em `base.css` |
| RF42 Duração das notificações | ✅ Implementado | `ui.dialogs.NotificationDurationDialog`; `app.MainWindow.editNotificationDuration()`; `app.UserPreferences.notificationDurationProperty()`, `DEFAULT_NOTIFICATION_DURATION`, `MAX_NOTIFICATION_DURATION`; `ui.NotificationArea.show(...)` (o valor 0 desativa as notificações) |

### 4.11 Requisitos não funcionais

| Requisito | Situação | Classes e métodos |
|---|---|---|
| RNF01 Desempenho | ✅ Implementado | `app.UserPreferences.getDefaultDiagramWidth()` e `getDefaultDiagramHeight()` (tamanho inicial proporcional à tela); `ui.DiagramCanvas.refresh()` (o canvas cobre só a área visível, não o diagrama inteiro) |
| RNF02 Formato aberto | ✅ Implementado | Formato documentado na seção 2 deste README; `persistence.DiagramSerializer`, `persistence.DiagramDeserializer`, `persistence.json.JsonParser`, `persistence.json.JsonWriter` (JSON indentado, em UTF-8, legível por outras ferramentas) |
| RNF03 Múltiplas formas de interação | ✅ Implementado | Ferramentas: barra, teclas 1–9/0/A… e menu de contexto (`ui.ToolPalette.selectTool(int)`, `ui.DiagramTab.handleToolShortcut(KeyEvent)`, `ui.ToolPalette.createContextMenu()`). Propriedades: duplo clique, Ctrl+Enter e Edit > Properties (`ui.CanvasController`, `app.MainWindow.createEditMenu()`). Cópia de imagem: menu, ferramenta To Clipboard e Ctrl+B (`ui.DiagramTab.copyImageToClipboard()`). Arquivos, edição e visualização: menu e atalho (`app.MainWindow.createFileMenu()`, `createEditMenu()`, `createViewMenu()`). Seleção: clique, Ctrl+clique, laço e Ctrl+A. Fechar aba: botão da aba e Ctrl+W. |
