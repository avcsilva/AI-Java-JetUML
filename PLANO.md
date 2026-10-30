# Plano — Editor de diagramas UML em Java/JavaFX

## Contexto

O repositório contém apenas `README.md` e `REQUISITOS.md` (RF01–RF42, RNF01–RNF03). O objetivo é construir do zero uma aplicação desktop de edição de diagramas UML (Class, Sequence, State, Object, Use Case) em Java 21 + JavaFX, com build Maven, executável por `mvn clean javafx:run`. Ambiente verificado: JDK 27.0.1 instalado (compilaremos com `--release 21`) e Maven 3.10.0.

Após aprovação: este plano é salvo como `PLANO.md` na raiz e **apenas a Fase 1** é implementada.

---

## 1. Dependências (pom.xml)

| Dependência | Motivo |
|---|---|
| `org.openjfx:javafx-controls` 21.0.x | Interface gráfica (exigida). |
| `org.openjfx:javafx-swing` 21.0.x | Necessária para `SwingFXUtils` + `javax.imageio`, que codificam PNG/JPEG/GIF/BMP no Export Image (RF04). Sem ela, o JavaFX não grava imagens em arquivo. |
| `org.junit.jupiter:junit-jupiter` 5.10.x (scope test) | Testes unitários do modelo, validação, JSON e undo. |

Plugins: `maven-compiler-plugin` (release 21), `maven-surefire-plugin`, `javafx-maven-plugin` 0.0.8 (`mainClass` = `br.uefs.umleditor.app.UmlEditorApp`). Projeto **não modular** (sem `module-info.java`), para simplificar testes.

**JSON:** sem biblioteca externa — um leitor/escritor JSON próprio e pequeno (`persistence.json`), suficiente para o formato definido abaixo. Preferências do usuário via `java.util.prefs.Preferences` (JDK).

---

## 2. Estrutura de pacotes e tipos principais

Pacote raiz: `br.uefs.umleditor`. O modelo não depende de JavaFX (testável isoladamente).

```
app/           UmlEditorApp (Application), MainWindow (menus, abas, layout),
               WelcomePane, UserPreferences (Preferences: grade, dicas, auto edit,
               dark mode, fonte, duração de notificação, tamanho, arquivos recentes)
geom/          Point, Rectangle, Dimension, Line, Direction (imutáveis, double)
model/         Diagram (raízes, arestas, tipo, tamanho), DiagramType (enum: nome,
               lista de ferramentas, regras, tipos de nó/aresta aceitos),
               DiagramElement, Node (abstrato; pai/filhos, posição), Edge (abstrato;
               start/end), Property / PropertyKind (texto, multilinha, enum, boolean)
model/nodes/   ClassNode, InterfaceNode, PackageNode, PackageDescriptionNode, NoteNode,
               PointNode (âncora de nota), ImplicitParameterNode, CallNode, StateNode,
               InitialStateNode, FinalStateNode, ObjectNode, FieldNode, ActorNode,
               UseCaseNode
model/edges/   DependencyEdge, GeneralizationEdge (Inheritance|Implementation),
               AssociationEdge, AggregationEdge (Aggregation|Composition), NoteEdge,
               CallEdge, ReturnEdge, StateTransitionEdge, ObjectReferenceEdge,
               ObjectCollaborationEdge, UseCaseAssociationEdge,
               UseCaseDependencyEdge (None|Include|Extend), UseCaseGeneralizationEdge
model/rules/   ValidationResult (VALID | SILENT_FAILURE | ERROR(msg)), EdgeRule,
               CommonRules, ClassDiagramRules, StateDiagramRules, ObjectDiagramRules,
               UseCaseDiagramRules, SequenceDiagramRules, DiagramStructureValidator
               (usado ao abrir/colar arquivos)
model/builder/ DiagramBuilder (abstrato) + um por tipo: traduz gestos (criar nó em ponto,
               criar aresta entre pontos, mover, excluir, colar) em operações, aplicando
               as regras; SequenceDiagramBuilder trata chamadas/ativações
command/       DiagramOperation (execute/undo), CompoundOperation, UndoManager
layout/        EdgePathCalculator (RF17), SequenceLayout (posição vertical das
               ativações), PackageLayout (pacote se ajusta aos filhos), GridSnap
render/        GraphicsAdapter (interface de desenho: linhas, retângulos, elipses,
               texto, traço tracejado…), FxGraphicsAdapter (GraphicsContext),
               SvgGraphicsAdapter (gera SVG), Theme (cores claro/escuro), FontSettings,
               StyledText (RF24: /itálico/ e _sublinhado_), NodeRenderer/EdgeRenderer
               por tipo, ArrowHead, LineStyle, DiagramPainter (desenha diagrama inteiro)
persistence/   json/ (JsonValue, JsonObject, JsonArray, JsonParser, JsonWriter),
               DiagramSerializer, DiagramDeserializer, RecentFiles
export/        ImageExporter (SVG/PNG/JPEG/GIF/BMP), ClipboardImageExporter
ui/            DiagramTab (aba: diagrama, arquivo, UndoManager, seleção), DiagramCanvas
               (Canvas em ScrollPane, zoom, grade), ToolPalette (barra lateral),
               (ferramentas descritas por model.ToolType: nome e descrição verbosa),
               ToolIconPainter (ícones desenhados via renderers), CanvasController
               (mouse: criação, rubber band, laço, arraste; teclado: atalhos, Shift),
               SelectionModel, ElementClipboard (copiar/colar elementos),
               PropertyEditor (painel de propriedades), NotificationArea
ui/dialogs/    DiagramSizeDialog, FontDialog, NotificationDurationDialog
```

Fluxo central: `CanvasController` recebe gestos → `DiagramBuilder` valida (`model/rules`) e devolve `DiagramOperation` ou `ValidationResult` de erro → `UndoManager` executa → `DiagramCanvas` redesenha via `DiagramPainter` → erros vão para `NotificationArea`. O mesmo `DiagramPainter` desenha na tela, nos ícones da barra, no PNG/clipboard e no SVG (via `GraphicsAdapter`), garantindo uma única lógica de desenho.

---

## 3. Formato JSON (RNF02)

Documentado em `docs/FORMATO_JSON.md` (entregue na fase 6). Coordenadas absolutas em pixels; ids numéricos locais ao arquivo.

```json
{
  "formatVersion": "1.0",
  "diagram": "ClassDiagram",
  "width": 1920, "height": 1080,
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

- `diagram`: `ClassDiagram | SequenceDiagram | StateDiagram | ObjectDiagram | UseCaseDiagram`.
- Cada nó traz `id`, `type`, `x`, `y`, `children` (ids) e suas propriedades textuais/booleanas (ex.: `FieldNode`: `name`, `value`; `CallNode`: `openBottom`; `PointNode`: só posição).
- Cada aresta traz `type`, `start`, `end` e suas propriedades (`middleLabel`, `generalizationType`, `dependencyType`, etc.). A ordem das `CallEdge` no array define a ordem das chamadas.
- Ao abrir, o arquivo passa pelo `DiagramStructureValidator`; arquivo inválido → notificação de erro e nada é aberto.

---

## 4. Interpretações adotadas (requisitos ambíguos)

1. **RF01 / boas-vindas:** aba “Welcome” exibida ao iniciar e quando não há diagramas abertos, com um botão por tipo de diagrama.
2. **RF02 Close/Exit:** se houver alterações não salvas, pede confirmação (Salvar / Descartar / Cancelar) — interpretado como parte necessária de “fechar” sem perda silenciosa de dados. Recent Files: submenu com até 8 arquivos, persistido em Preferences.
3. **RF04/RF05:** Export Image e Copy to Clipboard usam a mesma imagem: conteúdo recortado aos limites dos elementos (+ margem), sem grade, sempre no tema claro. Formatos do “Save as type”: SVG, PNG, JPEG, GIF, BMP.
4. **RF08:** teclas sem modificador, ativas quando o foco está no diagrama (não em campos de texto). Sequência 1…9, 0, A, B, C, D… conforme a posição na barra (inclui To Clipboard, que executa a cópia).
5. **RF09:** o menu de contexto lista as mesmas ferramentas da barra; escolher uma ativa a ferramenta (To Clipboard executa a cópia).
6. **RF15:** Shift com a ferramenta Select e nó(s) selecionado(s): nó que é filho de pacote → é desanexado (vira raiz, mantendo a posição); nó raiz cujo centro está sobre um pacote → vira filho do pacote mais interno. Pacotes aceitam como filhos: Class, Interface, Package e Package Description. Pacotes se redimensionam para conter os filhos.
7. **RF17:** arestas como segmentos retos entre as bordas dos nós (direção pelo centro), com afastamento paralelo quando há várias entre o mesmo par; autoarestas como laços em cantos distintos do nó; transições de estado opostas desenhadas em arco; referências de objeto saem da parte Value do campo. O caminho não é editável.
8. **RF18 duplicidade:** “mesmo tipo entre os mesmos dois nós” = mesmo tipo, mesma origem e mesmo destino. **Ciclo direto:** rejeitar A→B se já existe B→A do mesmo tipo (Herança, Dependência, Agregação, Associação). Agregação e Composição são a mesma aresta com tipo alternável (RF20), então ambas contam como “Agregação” nesta regra e na regra Associação+Agregação (em qualquer direção). Pelo mesmo motivo, “mesmo tipo” é a mesma classe de aresta do modelo: Inheritance e Realization (GeneralizationEdge), Aggregation e Composition (AggregationEdge) e Include e Extend (UseCaseDependencyEdge) contam como o mesmo tipo na verificação de duplicidade e de ciclos (detalhado na Fase 3).
9. **RF19 Classes:** autoarestas permitidas apenas para Association, Aggregation e Composition; Dependency, Inheritance e Realization (que é uma generalização) são bloqueadas. **Use Case:** todas as autoarestas em Atores/Casos de Uso são bloqueadas (association, generalization, include/extend). **Estados:** autotransição em Estado Inicial/Final é bloqueada pela regra de origem/destino.
10. **RF20 Realização/Herança/Dependência:** Realization e Inheritance são a mesma aresta `GeneralizationEdge` com **Generalization Type** (Inheritance/Implementation); Dependency tem **Directionality** (Unidirectional/Bidirectional). Alterações de propriedade que violariam RF18/RF19 são revertidas com notificação de erro.
11. **RF14:** Note Connector válido de qualquer nó até uma nota, ou de uma nota até um ponto vazio (cria `PointNode` invisível como âncora). Soltar em vazio a partir de um nó que não é nota não tem efeito.
12. **RF21:** cada gesto completo é um passo de desfazer (criação, exclusão, arraste, colagem, recorte, Shift em pacote, e uma sessão inteira do painel de propriedades).
13. **RF22:** painel de propriedades é uma janela flutuante não modal ao lado do elemento; edições aplicam-se ao vivo; Tab também navega em campos multilinha; Esc fecha. Nós sem campos editáveis (Initial/Final State, PointNode) não abrem painel. Em Sequence, “Call: Open Bottom” refere-se à caixa de ativação (`CallNode`, RF31); a aresta Call tem Middle Label (RF20).
14. **RF24:** formatação aplica-se a uma linha inteira delimitada (`/texto/` ou `_texto_`, combináveis); os delimitadores não são exibidos. O nome de Object é exibido sublinhado (notação UML de objeto).
15. **RF25:** Field fora de um objeto não tem efeito. Ao copiar, campos selecionados sem o objeto pai não são copiados.
16. **RF27–RF30 (Sequence):** arrastar de uma linha de vida sem ativação só é válido se ainda não existir chamada raiz (cria a raiz). Soltar em qualquer ponto da linha de vida/ativação do destino sempre cria nova caixa de ativação. A ordem vertical das chamadas é calculada (layout automático) a partir da posição do arraste. Return só é válido de uma ativação para a ativação que a chamou, uma vez por chamada. **Todas** as tentativas inválidas em diagramas de sequência falham silenciosamente (RF30 prevalece sobre a regra geral de notificação do RF18); as invariantes estruturais (apenas CallNodes, um chamador por ativação, uma raiz) também são verificadas ao abrir arquivos. Excluir uma ativação/chamada remove a subárvore de chamadas dependente.
17. **RF32:** clique seleciona o elemento (substituindo a seleção); clique em vazio limpa a seleção; Ctrl+clique alterna. O laço seleciona elementos inteiramente contidos na região. Arrastar elementos selecionados os move (com snap).
18. **RF34:** colagem com deslocamento de uma célula da grade; elementos colados ficam selecionados. Nota é compatível com todos os tipos.
19. **RF36:** grade de 10 px; snap sempre ativo.
20. **RF37:** níveis de zoom 0,5 · 0,75 · 1 · 1,5 · 2.
21. **RF38/RNF01:** padrão = largura e altura da área visível da tela principal (limitados a 250–4000). Elementos não podem ser posicionados fora da área do diagrama. Preferências (tamanho, fonte, grade, dicas, modo escuro, duração) persistem entre execuções.
22. **RF40:** modo escuro altera a interface (CSS) e as cores do canvas; exportações seguem o item 3.
23. **RF41/42:** notificações empilhadas no canto inferior esquerdo, desaparecem após a duração configurada (inteiro de 0 a 10 s).

---

## 5. Fases de implementação

Cada fase termina com `mvn clean javafx:run` iniciando a aplicação e `mvn test` passando.

### Fase 1 — Esqueleto da aplicação e área de trabalho
pom.xml, `UmlEditorApp`, `MainWindow` com menus File/Edit/View (apenas itens desta fase), tela de boas-vindas, abas, File > New (5 tipos), File > Exit; `DiagramType` com as listas de ferramentas; `ToolPalette` com ícones desenhados, atalhos, tooltips simples/verbosos, Show Tool Hint; menu de contexto; `DiagramCanvas` com tamanho fixo, grade e zoom; diálogos de tamanho e de duração de notificação; dark mode; `NotificationArea`; `UserPreferences`. Ferramentas selecionáveis, mas ainda sem efeito no diagrama (To Clipboard e criação entram nas fases seguintes).
**Entrega:** RF01, RF06, RF07, RF08, RF09, RF10, RF37, RF38, RF40, RF41, RF42, RNF01; RF36 parcial (exibição da grade — snap na Fase 2).

### Fase 2 — Modelo, nós, renderização e seleção
Modelo completo de nós, `GraphicsAdapter`/renderers, `StyledText`, criação de nós por clique, pacotes com filhos e Shift, campos em objetos, notas, seleção/laço/Select All, mover com snap, Delete, Change Font. Operações já como `DiagramOperation`.
**Entrega:** RF11, RF13, RF15, RF24, RF25, RF32, RF33, RF35, RF36 (completo), RF39.

### Fase 3 — Arestas, layout e validação (exceto Sequence)
Ferramentas de aresta com rubber band, todas as arestas de Class/State/Object/Use Case e Note Connector (incluindo âncora em ponto), `EdgePathCalculator`, regras de validação com notificações, autoarestas, referências a partir de campos. Testes unitários das regras.
**Entrega:** RF14, RF16, RF17, RF18, RF19, RF26.

### Fase 4 — Edição: desfazer, propriedades
`UndoManager` no menu Edit, painel de propriedades (duplo clique, Ctrl+Enter, Edit > Properties, Tab, Esc), Ctrl+Q para « », Auto Edit Node, propriedades de arestas.
**Entrega:** RF12, RF20, RF21, RF22, RF23.

### Fase 5 — Diagrama de sequência
`CallNode`, `CallEdge`, `ReturnEdge`, `SequenceLayout`, autochamadas, chamadas de construtor «create», Open Bottom, `SequenceDiagramRules`.
**Entrega:** RF27, RF28, RF29, RF30, RF31.

### Fase 6 — Arquivos e transferência
JSON (parser/writer próprios, serializer/deserializer, validação estrutural), Open/Save/Save As/Close/Recent Files, Duplicate, Export Image (SVG/PNG/JPEG/GIF/BMP), Copy to Clipboard (menu, barra, Ctrl+B), Cut/Copy/Paste entre diagramas, documentação `docs/FORMATO_JSON.md`, revisão de RNF03 (cada operação principal por menu + atalho/barra/mouse).
**Entrega:** RF02, RF03, RF04, RF05, RF34, RNF02, RNF03.

---

## 6. Fase 1 em detalhe (o que será implementado agora)

Arquivos principais:
- `pom.xml`
- `src/main/java/br/uefs/umleditor/app/UmlEditorApp.java`, `MainWindow.java`, `WelcomePane.java`, `UserPreferences.java`
- `src/main/java/br/uefs/umleditor/model/DiagramType.java`, `ToolType.java` (descritor de ferramenta: nome, descrição verbosa), `Diagram.java` (tipo e tamanho)
- `src/main/java/br/uefs/umleditor/ui/ToolPalette.java`, `ToolIconPainter.java`, `DiagramTab.java`, `DiagramCanvas.java`, `NotificationArea.java`
- `src/main/java/br/uefs/umleditor/ui/dialogs/DiagramSizeDialog.java`, `NotificationDurationDialog.java`
- `src/main/java/br/uefs/umleditor/render/Theme.java`
- `src/main/resources/br/uefs/umleditor/light.css`, `dark.css`
- Testes: `DiagramTypeTest` (ordem das ferramentas e atalhos RF07/RF08).

Menus da Fase 1: File (New ▸ 5 tipos, Exit Ctrl+Q); View (Show Grid, Show Tool Hint, Verbose Tooltips, Zoom In Ctrl+=, Zoom Out Ctrl+-, Reset Zoom Ctrl+0, Set Diagram Size…, Dark Mode, Notification Duration…). Uma notificação de confirmação verde é exibida ao alterar tamanho/duração, permitindo verificar RF41/RF42 já nesta fase.

## 7. Verificação

- `mvn clean test` — testes unitários.
- `mvn clean javafx:run` executado em segundo plano; confirmar no log que a janela inicia sem exceções e encerrar o processo.
- Checagem manual descrita ao usuário: criar cada tipo de diagrama, conferir barra/atalhos/tooltips, zoom, grade, dark mode, diálogos e notificações.
- Ao fim de cada fase: listar os requisitos entregues e o status de compilação/execução.
