# AI-Java-JetUML

Editor desktop de diagramas UML (Class, Sequence, State, Object e Use Case) escrito em Java 21 com JavaFX, construído inteiramente por IA como parte da disciplina EXA871 da UEFS.

A especificação está em [REQUISITOS.md](REQUISITOS.md) e o plano de arquitetura e de fases está em [PLANO.md](PLANO.md).

> **Estado atual:** as **Fases 1 a 4** estão implementadas: esqueleto da aplicação e área de trabalho; nós, renderização e seleção; arestas, layout automático e validação das conexões (exceto chamadas e retornos do diagrama de sequência); desfazer/refazer e painel de propriedades. As demais fases estão planejadas. A tabela de rastreabilidade (seção 4) indica, para cada requisito, se ele já foi implementado e onde, ou em qual fase será entregue.

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
| `org.openjfx:javafx-swing` 21.0.6 | Codificação de imagens PNG/JPEG/GIF/BMP no Export Image (Fase 6) |
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
- As preferências do usuário (grade, dicas de ferramenta, modo escuro, duração das notificações, tamanho do diagrama) são guardadas com `java.util.prefs.Preferences` e mantidas entre execuções.

### Estrutura do projeto

```
pom.xml
src/main/java/br/uefs/umleditor/
  app/            UmlEditorApp, MainWindow, WelcomePane, UserPreferences
  geom/           Point, Rectangle, Dimension
  model/          Diagram, DiagramType, ToolType, DiagramElement, Node, NamedNode, ParentNode,
                  Edge, Property, BoundsCalculator
  model/nodes/    ClassNode, InterfaceNode, PackageNode, PackageDescriptionNode, NoteNode,
                  PointNode, StateNode, InitialStateNode, FinalStateNode, ObjectNode, FieldNode,
                  ActorNode, UseCaseNode, ImplicitParameterNode, NodeFactory
  model/edges/    DependencyEdge, GeneralizationEdge, AssociationEdge, AggregationEdge,
                  ClassRelationshipEdge, Directionality, NoteEdge, StateTransitionEdge,
                  ObjectReferenceEdge, ObjectCollaborationEdge, LabeledEdge,
                  UseCaseAssociationEdge, UseCaseDependencyEdge, UseCaseGeneralizationEdge,
                  EdgeFactory
  model/rules/    DiagramRules, ClassDiagramRules, StateDiagramRules, ObjectDiagramRules,
                  UseCaseDiagramRules, SequenceDiagramRules, ValidationResult
  model/builder/  DiagramBuilder
  command/        DiagramOperation, CompoundOperation, AddNodeOperation, RemoveNodeOperation,
                  MoveNodesOperation, ReparentNodeOperation, AddEdgeOperation,
                  RemoveEdgeOperation, ChangePropertyOperation, UndoManager
  layout/         GridSnap, EdgePathCalculator
  render/         Theme, RenderContext, FontSettings, TextStyle, StyledText, TextBlock,
                  GraphicsAdapter, FxGraphicsAdapter, LineStyle, ArrowHead, EdgeRenderer,
                  DiagramRenderer
  render/nodes/   NodeRenderer e um renderizador por família de nós
  ui/             DiagramTab, DiagramCanvas, CanvasController, SelectionModel, ToolPalette,
                  ToolIconPainter, NotificationArea, PropertyEditor, StereotypeInsertion
  ui/dialogs/     DiagramSizeDialog, FontDialog, NotificationDurationDialog, SpinnerSupport
src/main/resources/br/uefs/umleditor/
  base.css        aparência padrão (clara) e estilo das notificações
  dark.css        aparência do modo escuro
src/test/java/br/uefs/umleditor/
  model/DiagramTypeTest, model/PropertiesTest, model/builder/DiagramBuilderTest,
  model/rules/EdgeRulesTest, command/UndoManagerTest, layout/EdgePathCalculatorTest,
  render/StyledTextTest, ui/StereotypeInsertionTest
```

### Como usar (funcionalidades disponíveis)

- **Criar diagrama:** botões da tela de boas-vindas ou File > New.
- **Escolher ferramenta:** clique na barra lateral, teclas `1`–`9`, `0`, `A`, `B`… ou clique direito no diagrama.
- **Criar nó:** com uma ferramenta de nó ativa, clique em um espaço vazio. Clicar dentro de um pacote cria o nó como filho do pacote; com a ferramenta Field, clique dentro de um objeto para criar um campo. Clicar sobre outro nó não tem efeito.
- **Criar aresta:** com uma ferramenta de aresta ativa, pressione o botão sobre o nó de origem, arraste a linha elástica e solte sobre o nó de destino. Arrastar dentro de um mesmo nó cria uma autoaresta, quando o tipo permite. Soltar em espaço vazio não tem efeito. Conexões inválidas não alteram o diagrama e aparecem como notificação de erro em vermelho.
- **Note Connector:** arraste de um elemento até uma nota para ligá-los; arraste de uma nota até um espaço vazio para ancorar a extremidade naquele ponto fixo.
- **Reference (diagrama de objetos):** arraste a partir da parte Value de um campo (à direita do `=`) até o objeto de destino.
- **Caminho das arestas:** é calculado automaticamente e acompanha os nós quando eles são movidos; para mudar o caminho, mova os nós.
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

> **Situação:** a leitura e a gravação de arquivos (RF02) serão implementadas na **Fase 6**. Esta seção é a especificação do formato que será adotado, conforme o [PLANO.md](PLANO.md).

Cada arquivo guarda um único diagrama, como um objeto JSON em UTF-8. As coordenadas são absolutas, em pixels, com origem no canto superior esquerdo do diagrama. Os `id` são inteiros válidos apenas dentro do arquivo e servem para que arestas e listas de filhos referenciem os nós.

### 2.1 Objeto raiz

| Campo | Tipo | Descrição |
|---|---|---|
| `formatVersion` | string | Versão do formato. Atualmente `"1.0"`. |
| `diagram` | string | Tipo do diagrama: `ClassDiagram`, `SequenceDiagram`, `StateDiagram`, `ObjectDiagram` ou `UseCaseDiagram`. |
| `width` | inteiro | Largura do diagrama em pixels (250 a 4000). |
| `height` | inteiro | Altura do diagrama em pixels (250 a 4000). |
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
| `CallEdge` | Sequence | `middleLabel` |
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

- JSON malformado, `diagram` desconhecido ou tipos de nó/aresta incompatíveis com o diagrama;
- `id` duplicados, ou `start`, `end` e `children` que não apontam para nós existentes;
- `FieldNode` que não seja filho de um `ObjectNode` (RF25);
- em diagramas de sequência: `CallEdge`/`ReturnEdge` cujos extremos não sejam `CallNode`, `CallNode` com mais de um chamador ou mais de um `CallNode` raiz (RF30).

Se o diagrama for maior que o tamanho configurado, `width` e `height` são ampliados para comportá-lo (RF38).

---

## 3. Legenda da rastreabilidade

| Situação | Significado |
|---|---|
| ✅ Implementado | Atendido pelo código atual. |
| 🟡 Parcial | Parte do requisito já está implementada; o restante tem a fase indicada. |
| ⏳ Fase N | Ainda não implementado; as classes listadas são as previstas no [PLANO.md](PLANO.md) e podem mudar durante a implementação. |

Caminhos abreviados: `app` = `br.uefs.umleditor.app`, `model` = `br.uefs.umleditor.model`, `ui` = `br.uefs.umleditor.ui`, e assim por diante.

## 4. Rastreabilidade dos requisitos

### 4.1 Diagramas e arquivos

| Requisito | Situação | Classes e métodos |
|---|---|---|
| RF01 Criar novo diagrama | ✅ Implementado | `app.WelcomePane(Consumer<DiagramType>)`; `app.MainWindow.createFileMenu()` (File > New); `app.MainWindow.newDiagram(DiagramType)`; `app.MainWindow.showWelcomeIfEmpty()`; `model.DiagramType`; `model.Diagram(DiagramType, int, int)`; `ui.DiagramTab(Diagram, String, RenderContext, boolean)` |
| RF02 Abrir, salvar e fechar | ⏳ Fase 6 | `persistence.json.JsonParser`, `persistence.json.JsonWriter`, `persistence.DiagramSerializer`, `persistence.DiagramDeserializer`, `persistence.RecentFiles`, `model.rules.DiagramStructureValidator`, `app.MainWindow` (Open, Recent Files, Close, Save, Save As) |
| RF03 Duplicar diagrama | ⏳ Fase 6 | `app.MainWindow` (File > Duplicate), `persistence.DiagramSerializer` |
| RF04 Exportar como imagem | ⏳ Fase 6 | `export.ImageExporter`, `render.SvgGraphicsAdapter`, `render.DiagramPainter` |
| RF05 Copiar para a área de transferência | ⏳ Fase 6 | `export.ClipboardImageExporter`; `ui.DiagramTab.runActionTool(ToolType)` (hoje sem efeito); `app.MainWindow` (Copy to Clipboard, Ctrl+B) |
| RF06 Encerrar a aplicação | ✅ Implementado | `app.MainWindow.createFileMenu()` (item Exit, Ctrl+Q, `Platform.exit()`). A confirmação de alterações não salvas entra na Fase 6. |

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
| RF20 Propriedades das arestas | ✅ Implementado | `getProperties()` de `model.edges.ClassRelationshipEdge` (Start/Middle/End Label, Directionality), `AggregationEdge` (+ Aggregation Type), `GeneralizationEdge` (Generalization Type), `DependencyEdge` (Directionality Unidirectional/Bidirectional), `LabeledEdge` (Middle Label de Transition e Collaboration), `UseCaseDependencyEdge` (Dependency Type None/Include/Extend); `ui.PropertyEditor.edit(...)`; `render.EdgeRenderer.appearanceOf(Edge)`. Call e Return (Middle Label) chegam com o diagrama de sequência, na Fase 5. |

### 4.5 Edição, propriedades e texto

| Requisito | Situação | Classes e métodos |
|---|---|---|
| RF21 Desfazer e refazer | ✅ Implementado | `app.MainWindow.createEditMenu()` (Undo Ctrl+Z, Redo Ctrl+Y); `ui.DiagramTab.undo()`, `redo()`, `perform(DiagramOperation)`, `record(DiagramOperation)`; `command.UndoManager.execute(...)`, `record(...)`, `undo()`, `redo()`; operações reversíveis do pacote `command` (inclusive `ChangePropertyOperation`) |
| RF22 Acessar propriedades | ✅ Implementado | `model.DiagramElement.getProperties()` e as implementações em `model.NamedNode`, `model.nodes.ClassNode`, `InterfaceNode`, `PackageDescriptionNode`, `FieldNode`, `NoteNode`; `model.Property`; `ui.PropertyEditor.edit(...)`, `createInput(...)`, `moveFocusOnTab(...)` (Tab), filtro de Esc; `ui.CanvasController.mousePressed(MouseEvent)` (duplo clique); `ui.DiagramTab.editSelectedProperties()`, `editProperties(DiagramElement)`; `app.MainWindow.createEditMenu()` (Properties, Ctrl+Enter). A opção Open Bottom de Call chega na Fase 5. |
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
| RF27 Criar chamadas | ⏳ Fase 5 | `model.builder.SequenceDiagramBuilder`, `model.nodes.CallNode`, `model.edges.CallEdge`, `layout.SequenceLayout` |
| RF28 Criar autochamadas | ⏳ Fase 5 | `model.builder.SequenceDiagramBuilder` |
| RF29 Criar chamadas de construtor | ⏳ Fase 5 | `model.builder.SequenceDiagramBuilder`, `layout.SequenceLayout` |
| RF30 Validar sequência de chamadas | ⏳ Fase 5 | `model.rules.SequenceDiagramRules`, `model.rules.DiagramStructureValidator` |
| RF31 Sequência incompleta | ⏳ Fase 5 | `model.nodes.CallNode` (`openBottom`), renderizador de `CallNode` |

### 4.8 Seleção, transferência e exclusão

| Requisito | Situação | Classes e métodos |
|---|---|---|
| RF32 Seleção | ✅ Implementado | `ui.SelectionModel.set(DiagramElement)`, `toggle(DiagramElement)`, `setAll(...)`, `addAll(...)`, `clear()`; `ui.CanvasController.startSelection(Point, boolean)` (clique e Ctrl+clique), `mouseDragged(MouseEvent)` (laço); `model.builder.DiagramBuilder.findElementAt(Point)`, `findElementsInside(Rectangle)`; `layout.EdgePathCalculator.contains(Edge, Point)`; `ui.DiagramCanvas.setLasso(Rectangle)`; `render.DiagramRenderer.draw(...)` (marcadores de seleção) |
| RF33 Selecionar tudo | ✅ Implementado | `app.MainWindow.createEditMenu()` (Select All, Ctrl+A); `ui.DiagramTab.selectAll()`; `model.builder.DiagramBuilder.getAllElements()` (nós e arestas); `model.builder.DiagramBuilder.movableNodes(...)` (mover a seleção inteira) |
| RF34 Recortar, copiar e colar | ⏳ Fase 6 | `ui.ElementClipboard`, `model.builder.DiagramBuilder` |
| RF35 Excluir elementos | ✅ Implementado | `app.MainWindow.createEditMenu()` (Delete, tecla Delete); `ui.DiagramTab.deleteSelection()`; `model.builder.DiagramBuilder.removeElements(...)`, `topmostNodes(...)`; `command.RemoveEdgeOperation`, `command.RemoveNodeOperation`, `command.CompoundOperation` |

### 4.9 Visualização e interface

| Requisito | Situação | Classes e métodos |
|---|---|---|
| RF36 Grade | ✅ Implementado | Exibição: `ui.DiagramCanvas.setShowGrid(boolean)`, `paintGrid(...)`, `app.UserPreferences.showGridProperty()`, `app.MainWindow.createViewMenu()`. Alinhamento, com a grade visível ou não: `layout.GridSnap.snap(double)`, `snap(Point)`, `GRID_SIZE`, usados em `model.builder.DiagramBuilder.createNode(...)` e `ui.CanvasController.dragNodes(Point)` |
| RF37 Zoom | ✅ Implementado | `ui.DiagramCanvas.zoomIn()`, `zoomOut()`, `resetZoom()`, `getZoom()`, `canZoomIn()`, `canZoomOut()`; `app.MainWindow.createViewMenu()`, `zoom(Consumer<DiagramCanvas>)`, `updateZoomItems()` |
| RF38 Tamanho do diagrama | 🟡 Parcial | Implementado: `ui.dialogs.DiagramSizeDialog`; `app.MainWindow.editDiagramSize()`; `app.UserPreferences.getDiagramWidth()`, `getDiagramHeight()`, `setDiagramSize(int, int)`, `getDefaultDiagramWidth()`, `getDefaultDiagramHeight()`; `model.Diagram.clampSize(int)`. Fase 6: aplicar aos arquivos abertos e ampliar o tamanho de diagramas maiores. |
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
| RNF02 Formato aberto | ⏳ Fase 6 | Formato documentado na seção 2 deste README; `persistence.DiagramSerializer`, `persistence.DiagramDeserializer` |
| RNF03 Múltiplas formas de interação | 🟡 Parcial | Implementado: ferramentas por barra, atalho e menu de contexto (`ui.ToolPalette.selectTool(int)`, `ui.DiagramTab.handleToolShortcut(KeyEvent)`, `ui.ToolPalette.createContextMenu()`); zoom por menu e atalho (`app.MainWindow.createViewMenu()`); Select All e Delete por menu e teclado (`app.MainWindow.createEditMenu()`); seleção de nós e arestas por clique e por laço (`ui.CanvasController`). propriedades por duplo clique, Ctrl+Enter e Edit > Properties; desfazer e refazer por menu e atalho. Fases 5–6: cópia de imagem por menu/barra/Ctrl+B e as operações de arquivo. |
