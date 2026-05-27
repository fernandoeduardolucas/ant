# Diagramas de Fluxo de Dados (DFD) — GA, Busca Tabu e ACO para Mochila 0/1

Este documento detalha o fluxo de dados das três meta-heurísticas usadas no problema da mochila 0/1, com ênfase nas estruturas de estado e nos ciclos internos de cada algoritmo.

---

## 1) DFD — Algoritmo Genético (GA)

Este diagrama resume o fluxo de dados principal de uma implementação típica de GA para mochila 0/1.

```mermaid
flowchart TD
    A["Entradas:<br/>instance, populationSize,<br/>generations, crossoverRate,<br/>mutationRate, eliteSize,<br/>tournamentSize, seed"] --> B["initializePopulation()<br/>createRandomSolution()+repairSolution()"]
    B --> C["geneticAlgorithm()<br/>best inicial (population.stream().max)"]
    C --> D["history.add(new GenerationRecord(...))<br/>best, initialValue"]

    D --> E{"generation <= generations?"}
    E -- Sim --> F["tournamentSelection()<br/>seleção de parent1/parent2"]
    F --> G["twoPointCrossover()<br/>ou clone dos pais"]
    G --> H["bitFlipMutation()<br/>em childGenes1/childGenes2"]
    H --> I["repairSolution()<br/>gera Chromosome viável"]
    I --> J["generationBest = population.stream().max(...)"]
    J --> K["best = generationBest.copy()<br/>ou generationsWithoutImprovement++"]
    K --> C

    E -- Não --> L["Saída:<br/>GeneticResult<br/>best, initialValue, history,<br/>stopGeneration, elapsed"]
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
    A["Entradas:<br/>itens, capacidade,<br/>iteracoes, tenureFlip,<br/>tenureSwap, limiteSemMelhoria,<br/>diversifyStrength, seed"] --> B["inicializar()<br/>construirSolucaoGulosa()"]
    B --> C["melhorEscolhidos = copy(solucaoAtual)<br/>melhorValor, melhorPeso"]
    C --> D["resolver()"]

    D --> E{"iter < iteracoes?"}
    E -- Sim --> F["explorarVizinhanca(iter, usarDiversificacao)"]
    F --> G["aplicarFlip() ou aplicarSwap()<br/>+ atualiza tabu/frequencia"]
    G --> H{"valorAtual > melhorValor?"}
    H -- Sim --> I["melhorEscolhidos = copy(solucaoAtual)<br/>melhorValor/melhorPeso"]
    H -- Não --> J["semMelhoria++"]
    I --> K{"semMelhoria >= limiteSemMelhoria?"}
    J --> K
    K -- Sim --> L["diversificar()<br/>reset parcial + limpeza tabu"]
    K -- Não --> E
    L --> E

    E -- Não --> N["Saída:<br/>new Solucao(melhorEscolhidos,<br/>melhorValor, melhorPeso)"]
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
- `construirSolucaoGulosaInicial()`: constrói uma solução viável inicial usada para definir o primeiro `melhorGlobal`.
- `semMelhoria`: contador de estagnação para reinicialização de feromônio.

---

## 3) Diagrama de Fluxo de Dados — Ant Colony Optimization (ACO)

```mermaid
flowchart TD
    A["Entradas<br/>- Instância da mochila<br/>- Parâmetros ACO"] --> B["inicializarEstruturas()<br/>tau + eta"]
    B --> C{"Critério de parada<br/>atingido?"}

    C -- Não --> D["construirSolucaoProbabilistica()<br/>(uma por formiga)"]
    D --> E["melhorarComBuscaLocal1Flip()<br/>refino local"]
    E --> F["atualizar melhorGlobal / semMelhoria"]
    F --> G["evaporarFeromonio()"]
    G --> H["depositarFeromonio(melhorGlobal)"]
    H --> J["limitarFeromonio()"]
    J --> K{"semMelhoria >= limiteSemMelhoria?"}
    K -- Sim --> L["reiniciarFeromonio()"]
    K -- Não --> C
    L --> C

    C -- Sim --> I["Saídas<br/>- Melhor solução<br/>- Valor e peso<br/>- Iterações<br/>- Estatísticas"]
```

### Fluxos de dados principais (ACO)
- **Instância**: itens (valor, peso) e capacidade da mochila.
- **Parâmetros**: número de formigas, taxa de evaporação, influência de feromônio (`alpha`), influência heurística (`beta`) e máximo de iterações.
- **Memória de busca**: matriz/vetor de feromônio, melhor solução da iteração e melhor solução global.
- **Saídas**: melhor solução viável, histórico de convergência (opcional), tempo e métricas de execução.

---

## Observações
- Os três fluxos podem usar penalização, reparo ou ambos para tratar inviabilidade de capacidade.
- Para reprodutibilidade experimental, registre `seed`, tempo de execução e configuração completa dos hiperparâmetros.
- No bloco de ACO, os nomes já usam os métodos reais de `AcoCore` (sem tradução).
