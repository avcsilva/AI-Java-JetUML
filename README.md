# AI-Java-JetUML

Editor desktop de diagramas UML (Class, Sequence, State, Object e Use Case) escrito em Java 21 com JavaFX, construído inteiramente por IA como parte da disciplina EXA871 da UEFS.

A especificação está em [REQUISITOS.md](REQUISITOS.md) e o plano de arquitetura e de fases está em [PLANO.md](PLANO.md).

> **Estado atual:** a **Fase 1** (esqueleto da aplicação e área de trabalho) está implementada. As demais fases estão planejadas. A tabela de rastreabilidade (seção 4) indica, para cada requisito, se ele já foi implementado e onde, ou em qual fase será entregue.

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
  model/          Diagram, DiagramType, ToolType
  render/         Theme
  ui/             DiagramTab, DiagramCanvas, ToolPalette, ToolIconPainter, NotificationArea
  ui/dialogs/     DiagramSizeDialog, NotificationDurationDialog, SpinnerSupport
src/main/resources/br/uefs/umleditor/
  base.css        aparência padrão (clara) e estilo das notificações
  dark.css        aparência do modo escuro
src/test/java/br/uefs/umleditor/model/
  DiagramTypeTest
```

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
| RF01 Criar novo diagrama | ✅ Implementado | `app.WelcomePane(Consumer<DiagramType>)`; `app.MainWindow.createFileMenu()` (File > New); `app.MainWindow.newDiagram(DiagramType)`; `app.MainWindow.showWelcomeIfEmpty()`; `model.DiagramType`; `model.Diagram(DiagramType, int, int)`; `ui.DiagramTab(Diagram, String, Theme, boolean)` |
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
| RF11 Criar nós | ⏳ Fase 2 | `model.nodes.*`, `model.builder.DiagramBuilder`, `ui.CanvasController`, `command.DiagramOperation` |
| RF12 Edição automática de nós | ⏳ Fase 4 | `ui.PropertyEditor`, `app.UserPreferences` (Auto Edit Node), `ui.CanvasController` |
| RF13 Criar notas | ⏳ Fase 2 | `model.nodes.NoteNode`, `render` (renderizador de nota) |
| RF14 Conectar notas | ⏳ Fase 3 | `model.edges.NoteEdge`, `model.nodes.PointNode`, `model.rules.CommonRules` |
| RF15 Nós filhos em pacotes | ⏳ Fase 2 | `model.nodes.PackageNode`, `layout.PackageLayout`, `ui.CanvasController` (tecla Shift) |

### 4.4 Arestas, transições e validação

| Requisito | Situação | Classes e métodos |
|---|---|---|
| RF16 Criar arestas | ⏳ Fase 3 | `ui.CanvasController` (rubber band), `model.builder.DiagramBuilder`, `model.edges.*` |
| RF17 Layout automático | ⏳ Fase 3 | `layout.EdgePathCalculator` |
| RF18 Validação das conexões | ⏳ Fase 3 | `model.rules.ValidationResult`, `model.rules.ClassDiagramRules`, `ObjectDiagramRules`, `UseCaseDiagramRules`, `StateDiagramRules`, `CommonRules`; `ui.NotificationArea.showError(String)` |
| RF19 Autoarestas e transições | ⏳ Fase 3 | `model.rules.ClassDiagramRules`, `UseCaseDiagramRules`, `ObjectDiagramRules`, `StateDiagramRules` |
| RF20 Propriedades das arestas | ⏳ Fase 4 | `ui.PropertyEditor`, `model.Property`, `model.edges.*` |

### 4.5 Edição, propriedades e texto

| Requisito | Situação | Classes e métodos |
|---|---|---|
| RF21 Desfazer e refazer | ⏳ Fase 4 | `command.UndoManager`, `command.DiagramOperation`, `command.CompoundOperation` |
| RF22 Acessar propriedades | ⏳ Fase 4 | `ui.PropertyEditor`, `model.Property`, `model.PropertyKind` |
| RF23 Estereótipos UML | ⏳ Fase 4 | `ui.PropertyEditor` (Ctrl+Q nos campos) |
| RF24 Formatação de texto | ⏳ Fase 2 | `render.StyledText` |

### 4.6 Diagrama de objetos

| Requisito | Situação | Classes e métodos |
|---|---|---|
| RF25 Criar campos | ⏳ Fase 2 | `model.nodes.FieldNode`, `model.nodes.ObjectNode`, `model.builder` (construtor do diagrama de objetos) |
| RF26 Criar referências | ⏳ Fase 3 | `model.edges.ObjectReferenceEdge`, `model.rules.ObjectDiagramRules` |

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
| RF32 Seleção | ⏳ Fase 2 | `ui.SelectionModel`, `ui.CanvasController` (clique, Ctrl+clique, laço) |
| RF33 Selecionar tudo | ⏳ Fase 2 | `ui.SelectionModel`, `app.MainWindow` (Edit > Select All) |
| RF34 Recortar, copiar e colar | ⏳ Fase 6 | `ui.ElementClipboard`, `model.builder.DiagramBuilder` |
| RF35 Excluir elementos | ⏳ Fase 2 | `model.builder.DiagramBuilder`, `app.MainWindow` (Edit > Delete) |

### 4.9 Visualização e interface

| Requisito | Situação | Classes e métodos |
|---|---|---|
| RF36 Grade | 🟡 Parcial | Implementado: `ui.DiagramCanvas.setShowGrid(boolean)`, `ui.DiagramCanvas.paintGrid(...)`, `ui.DiagramCanvas.GRID_SIZE`, `app.UserPreferences.showGridProperty()`, `app.MainWindow.createViewMenu()`. Fase 2: alinhamento à grade (`layout.GridSnap`). |
| RF37 Zoom | ✅ Implementado | `ui.DiagramCanvas.zoomIn()`, `zoomOut()`, `resetZoom()`, `getZoom()`, `canZoomIn()`, `canZoomOut()`; `app.MainWindow.createViewMenu()`, `zoom(Consumer<DiagramCanvas>)`, `updateZoomItems()` |
| RF38 Tamanho do diagrama | 🟡 Parcial | Implementado: `ui.dialogs.DiagramSizeDialog`; `app.MainWindow.editDiagramSize()`; `app.UserPreferences.getDiagramWidth()`, `getDiagramHeight()`, `setDiagramSize(int, int)`, `getDefaultDiagramWidth()`, `getDefaultDiagramHeight()`; `model.Diagram.clampSize(int)`. Fase 6: aplicar aos arquivos abertos e ampliar o tamanho de diagramas maiores. |
| RF39 Fonte | ⏳ Fase 2 | `ui.dialogs.FontDialog`, `render.FontSettings` |
| RF40 Modo escuro | ✅ Implementado | `app.MainWindow.applyTheme()`, `applyStylesheets()`; `render.Theme.of(boolean)`; `ui.DiagramTab.setTheme(Theme)`; `ui.ToolPalette.setTheme(Theme)`; `ui.DiagramCanvas.setTheme(Theme)`; `dark.css`; `app.UserPreferences.darkModeProperty()` |

### 4.10 Notificações

| Requisito | Situação | Classes e métodos |
|---|---|---|
| RF41 Exibir notificações | ✅ Implementado | `ui.NotificationArea.showError(String)`, `showConfirmation(String)`, `show(String, String)`; posicionamento em `app.MainWindow` (`StackPane` no canto inferior esquerdo); classes `notification-error` e `notification-confirmation` em `base.css`. Hoje só confirmações são emitidas; os erros passam a ser emitidos a partir da Fase 3. |
| RF42 Duração das notificações | ✅ Implementado | `ui.dialogs.NotificationDurationDialog`; `app.MainWindow.editNotificationDuration()`; `app.UserPreferences.notificationDurationProperty()`, `DEFAULT_NOTIFICATION_DURATION`, `MAX_NOTIFICATION_DURATION`; `ui.NotificationArea.show(...)` (o valor 0 desativa as notificações) |

### 4.11 Requisitos não funcionais

| Requisito | Situação | Classes e métodos |
|---|---|---|
| RNF01 Desempenho | ✅ Implementado | `app.UserPreferences.getDefaultDiagramWidth()` e `getDefaultDiagramHeight()` (tamanho inicial proporcional à tela); `ui.DiagramCanvas.refresh()` (o canvas cobre só a área visível, não o diagrama inteiro) |
| RNF02 Formato aberto | ⏳ Fase 6 | Formato documentado na seção 2 deste README; `persistence.DiagramSerializer`, `persistence.DiagramDeserializer` |
| RNF03 Múltiplas formas de interação | 🟡 Parcial | Implementado: ferramentas por barra, atalho e menu de contexto (`ui.ToolPalette.selectTool(int)`, `ui.DiagramTab.handleToolShortcut(KeyEvent)`, `ui.ToolPalette.createContextMenu()`); zoom por menu e atalho (`app.MainWindow.createViewMenu()`). Fases 2–6: propriedades por duplo clique/Ctrl+Enter/menu, cópia de imagem por menu/barra/Ctrl+B e as demais operações. |
