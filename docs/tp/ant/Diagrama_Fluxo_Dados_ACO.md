# Diagramas de Fluxo de Dados (DFD) — GA, Busca Tabu e ACO para Mochila 0/1

Este documento detalha o fluxo de dados das três meta-heurísticas usadas no problema da mochila 0/1, com ênfase nas estruturas de estado e nos ciclos internos de cada algoritmo.

---

## 1) DFD — Algoritmo Genético (GA)

Este diagrama resume o fluxo de dados principal de uma implementação típica de GA para mochila 0/1.

```mermaid
flowchart TD
    A["Entradas:<br/>itens, capacidade,<br/>tamPop, geracoes,<br/>taxaCruzamento, taxaMutacao,<br/>elitismo, seed"] --> B["inicializarPopulacao()<br/>gera cromossomos binários"]
    B --> C["avaliarPopulacao()<br/>fitness + penalização/reparo"]
    C --> D["atualizarMelhorGlobal()<br/>melhorIndividuo, melhorValor"]

    D --> E{"geracao < maxGeracoes?"}
    E -- Sim --> F["selecionarPais()<br/>roleta/torneio/ranking"]
    F --> G["cruzarPais()<br/>1 ponto / 2 pontos / uniforme"]
    G --> H["mutarFilhos()<br/>flip bit com taxaMutacao"]
    H --> I["repararOuPenalizarFilhos()<br/>garante viabilidade"]
    I --> J["avaliarFilhos()<br/>fitness dos descendentes"]
    J --> K["substituirPopulacao()<br/>elitismo + sobreviventes"]
    K --> C

    E -- Não --> L["Saída:<br/>melhorGlobal<br/>vetor, valorTotal, pesoTotal,<br/>histórico por geração"]
```

### Dicionário rápido de dados (GA)
- `populacao`: conjunto de cromossomos da geração corrente.
- `fitness[i]`: aptidão do indivíduo `i` após penalização/reparo.
- `melhorGlobal`: melhor indivíduo em toda a execução.
- `geracao`: contador do ciclo evolutivo.

---

## 2) DFD — Busca Tabu (Tabu Search)

Este diagrama resume o fluxo de dados principal de uma implementação de Busca Tabu para mochila 0/1.

```mermaid
flowchart TD
    A["Entradas:<br/>itens, capacidade,<br/>iteracoesMax, tenureTabu,<br/>tamVizinhanca, aspiracao,<br/>seed"] --> B["gerarSolucaoInicial()<br/>gulosa/aleatória viável"]
    B --> C["avaliar(solucaoAtual)<br/>valor, peso, penalidade"]
    C --> D["melhorGlobal = solucaoAtual"]

    D --> E{"iteracao < iteracoesMax?"}
    E -- Sim --> F["gerarVizinhanca()<br/>movimentos 1-flip/2-flip"]
    F --> G["filtrarAdmissiveis()<br/>remove tabu sem aspiração"]
    G --> H["selecionarMelhorVizinho()<br/>entre admissíveis"]
    H --> I["aplicarMovimento()<br/>atualiza solucaoAtual"]
    I --> J["atualizarListaTabu()<br/>inserção + expiração"]
    J --> K{"solucaoAtual melhor que melhorGlobal?"}
    K -- Sim --> L["melhorGlobal = solucaoAtual"]
    K -- Não --> M["manter melhorGlobal"]
    L --> E
    M --> E

    E -- Não --> N["Saída:<br/>melhorGlobal<br/>vetor, valorTotal, pesoTotal,<br/>trajetória e métricas"]
```

### Dicionário rápido de dados (Tabu)
- `solucaoAtual`: solução no ponto corrente da busca.
- `listaTabu`: memória de curto prazo com movimentos proibidos e validade.
- `melhorGlobal`: melhor solução observada durante toda a execução.
- `iteracao`: contador principal da busca.

---

## 3) DFD — Ant Colony Optimization (ACO)

Este diagrama resume o fluxo de dados principal do `AcoCore` para mochila 0/1.

```mermaid
flowchart TD
    A["Entradas:<br/>itens, capacidade,<br/>numFormigas, iteracoes,<br/>alpha, beta, rho, q, seed"] --> B["inicializarEstruturas()<br/>gera eta e tau inicial"]
    B --> C["construirSolucaoGulosaInicial()<br/>gera melhorGlobal inicial"]
    C --> D["atualizarLimitesFeromonio(melhorGlobal)<br/>define tauMin/tauMax"]

    D --> E{{"Loop de iterações"}}
    E --> F{{"Loop de formigas"}}
    F --> G["construirSolucaoProbabilistica()<br/>usa tau, eta, alpha, beta"]
    G --> H["melhorarComBuscaLocal1Flip()"]
    H --> I["atualizar melhorIteracao"]
    I --> F

    F --> J{"melhorIteracao > melhorGlobal?"}
    J -- Sim --> K["melhorGlobal = melhorIteracao<br/>semMelhoria = 0<br/>atualiza tauMin/tauMax"]
    J -- Não --> L["semMelhoria++"]

    K --> M["evaporarFeromonio()<br/>tau = tau*(1-rho)"]
    L --> M
    M --> N["depositarFeromonio(melhorGlobal)<br/>tau[i]+=q/valor"]
    N --> O["limitarFeromonio()<br/>clamp em tauMin/tauMax"]
    O --> P{"semMelhoria >= limite?"}
    P -- Sim --> Q["reiniciarFeromonio()<br/>tau = tauMax"]
    P -- Não --> E
    Q --> E

    E --> R["Saída: melhorGlobal<br/>escolhidos, valorTotal, pesoTotal"]
```

### Dicionário rápido de dados (ACO)
- `tau[i]`: nível de feromônio por item (memória coletiva).
- `eta[i]`: heurística local por item (ex.: valor/peso normalizado).
- `melhorIteracao`: melhor solução encontrada na iteração atual.
- `melhorGlobal`: melhor solução encontrada em toda a execução.
- `semMelhoria`: contador de estagnação para reinicialização de feromônio.

---

## Observações
- Os três fluxos podem usar penalização, reparo ou ambos para tratar inviabilidade de capacidade.
- Para reprodutibilidade experimental, registre `seed`, tempo de execução e configuração completa dos hiperparâmetros.
- Os nomes de função no diagrama são descritivos e podem ser mapeados para os métodos concretos da implementação.
