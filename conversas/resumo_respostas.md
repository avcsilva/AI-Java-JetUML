# Resumo das respostas

Resumo de cada resposta do assistente aos prompts desta sessão, na mesma numeração dos arquivos `prompt_NN.md`.

## Resposta ao prompt 01 — plano e Fase 1

- Li o `REQUISITOS.md` e verifiquei o ambiente (JDK 27.0.1, Maven 3.10.0).
- Apresentei um plano para aprovação com:
  - estrutura de pacotes e principais tipos;
  - dependências justificadas: JavaFX, `javafx-swing` para gravar imagens e JUnit 5;
  - leitor/escritor JSON próprio;
  - formato dos arquivos JSON;
  - 23 interpretações declaradas para requisitos ambíguos;
  - divisão em seis fases, com os requisitos de cada uma.
- Após a aprovação do plano (feita pelo botão de aprovação da interface, não por um prompt digitado), salvei o plano em `PLANO.md` e implementei a Fase 1:
  - projeto Maven;
  - tela de boas-vindas e File > New para os cinco tipos;
  - Exit;
  - barra de ferramentas com ícones desenhados pela aplicação, atalhos, menu de contexto, dicas e tooltips verbosos;
  - área de desenho de tamanho fixo, com grade e zoom;
  - diálogos de tamanho do diagrama e de duração das notificações;
  - modo escuro;
  - notificações;
  - preferências persistidas.
- Confirmei a compilação, 5 testes passando e a abertura da janela com `mvn clean javafx:run`.
- Requisitos entregues: RF01, RF06–RF10, RF36 (parcial), RF37, RF38, RF40–RF42 e RNF01.

## Resposta ao prompt 02 — README

- Criei o `README.md`, sem alterar código, com três partes:
  - instruções de compilação e execução;
  - documentação do formato JSON, então ainda planejado;
  - tabela de rastreabilidade de RF01–RF42 e RNF01–RNF03. Para requisitos já implementados, ela aponta as classes e métodos existentes; para os demais, a fase e as classes previstas.

## Resposta ao prompt 03 — commits

- Criei a branch `fase-1`, já que estava na `main`.
- Fiz três commits: o plano, o código da Fase 1 e o README.

## Resposta ao prompt 04 — merge na main

- Mesclei a `fase-1` na `main` por fast-forward, sem push.

## Resposta ao prompt 05 — Fase 2

- Na branch `fase-2`, implementei:
  - modelo de nós;
  - renderizadores atrás de uma interface de desenho;
  - criação de nós por clique, inclusive como filhos de pacotes e campos dentro de objetos;
  - Shift para incluir ou desanexar nós de pacotes;
  - formatação `/itálico/` e `_sublinhado_`;
  - seleção por clique, Ctrl+clique, laço e Select All;
  - mover com alinhamento à grade;
  - Delete;
  - Change Font;
  - operações reversíveis com histórico.
- Conferi o desenho por imagens geradas.
- 22 testes passando.
- Atualizei o README e fiz dois commits.
- Requisitos entregues: RF11, RF13, RF15, RF24, RF25 (parcial), RF32, RF33, RF35, RF36 e RF39.

## Resposta ao prompt 06 — merge na main

- Mesclei a `fase-2` na `main` por fast-forward, sem push.

## Resposta ao prompt 07 — Fase 3

- Na branch `fase-3`, implementei:
  - arestas de todos os diagramas, exceto chamadas e retornos;
  - linha elástica e autoarestas;
  - Note Connector, inclusive ancorado em ponto fixo;
  - referências a partir da parte Value dos campos;
  - cálculo automático dos caminhos: arestas paralelas, arcos e laços;
  - regras de conexão com notificação de erro.
- Registrei no plano e no README a interpretação de "mesmo tipo" de aresta: a mesma classe do modelo.
- 37 testes passando.
- Atualizei README e plano, fiz dois commits e mesclei na `main`.
- Requisitos entregues: RF14, RF16–RF19 e RF26.

## Resposta ao prompt 08 — Fase 4

- Na branch `fase-4`, implementei:
  - propriedades de cada elemento;
  - painel de propriedades modal, aberto por duplo clique, Ctrl+Enter ou Edit > Properties, com Tab entre os campos e Esc para fechar;
  - Ctrl+Q para inserir « »;
  - Auto Edit Node;
  - Undo/Redo, com cada abertura do painel desfeita como uma única ação.
- Verifiquei o painel por script.
- 44 testes passando.
- Registrei no plano que o painel é modal.
- Atualizei README e plano, fiz dois commits e mesclei na `main`.
- Requisitos entregues: RF12, RF20–RF23 e RF25 (completo).

## Resposta ao prompt 09 — Fase 5

- Na branch `fase-5`, implementei o diagrama de sequência:
  - caixas de ativação com Open Bottom;
  - chamadas e retornos;
  - primeira chamada entre linhas de vida;
  - autochamadas aninhadas;
  - chamadas de construtor com «create» e alinhamento do objeto;
  - layout automático das ativações;
  - regras de fluxo de controle com falha silenciosa;
  - exclusão em cascata das chamadas dependentes.
- Conferi o desenho por imagem.
- 54 testes passando.
- Atualizei o README, fiz dois commits e mesclei na `main`.
- Requisitos entregues: RF27–RF31.

## Resposta ao prompt 10 — Fase 6

- Na branch `fase-6`, implementei:
  - leitor e escritor JSON próprios e serialização dos diagramas;
  - Open, Recent Files, Save, Save As e Close, com confirmação de alterações não salvas;
  - validação completa ao abrir arquivos;
  - tamanho dos arquivos abertos segundo RF38;
  - Duplicate;
  - Export Image em SVG, PNG, JPEG, GIF e BMP;
  - Copy to Clipboard por menu, ferramenta e Ctrl+B;
  - Cut/Copy/Paste entre diagramas, com verificação de compatibilidade e recusa em diagramas de sequência.
- Verifiquei por script as exportações, a área de transferência e a reabertura de arquivos.
- 71 testes passando.
- Atualizei o README, marcando todos os requisitos como implementados.
- Fiz dois commits e mesclei na `main`.
- Requisitos entregues: RF02–RF06, RF34, RF38, RNF02 e RNF03.

## Resposta ao prompt 11 — registro da conversa

- Criei a pasta `conversas/`:
  - um arquivo por prompt (`prompt_01.md` a `prompt_11.md`) com o texto exato;
  - este resumo.
- Os prompts 04 e 06 correspondem a cliques no botão "Merge into main" da interface, registrados com o texto enviado por eles.
- Fiz commit, mesclei na `main` e enviei ao repositório remoto.
