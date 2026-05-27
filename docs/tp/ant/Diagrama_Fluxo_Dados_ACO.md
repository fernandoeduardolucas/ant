# Diagramas de Fluxo de Dados — Algoritmo Genético e Busca Tabu

Este documento descreve o fluxo de dados para duas meta-heurísticas aplicadas ao problema da mochila: **Algoritmo Genético (GA)** e **Busca Tabu (Tabu Search)**.

---

## 1) Diagrama de Fluxo de Dados — Algoritmo Genético (GA)

```mermaid
flowchart TD
    A[Entradas\n- Instância da mochila\n- Parâmetros GA] --> B[Leitura e Validação da Instância]
    B --> C[Geração da População Inicial]
    C --> D[Avaliação de Aptidão\nFitness + Penalização de Inviabilidade]
    D --> E{Critério de parada\natingido?}

    E -- Não --> F[Seleção de Pais]
    F --> G[Cruzamento]
    G --> H[Mutação]
    H --> I[Reparo/Viabilização\n(opcional)]
    I --> J[Avaliação dos Filhos]
    J --> K[Substituição / Elitismo]
    K --> D

    E -- Sim --> L[Melhor Solução Encontrada]
    L --> M[Saídas\n- Vetor de decisão\n- Valor total\n- Peso total\n- Estatísticas]
```

### Fluxos de dados principais (GA)
- **Instância**: itens (valor, peso) e capacidade da mochila.
- **Parâmetros**: tamanho da população, taxa de cruzamento, taxa de mutação, número máximo de gerações, estratégia de seleção.
- **Estado evolutivo**: população atual, fitness por indivíduo, melhor indivíduo global, geração corrente.
- **Saídas**: melhor solução viável, histórico de evolução (opcional), tempo de execução.

---

## 2) Diagrama de Fluxo de Dados — Busca Tabu (Tabu Search)

```mermaid
flowchart TD
    A[Entradas\n- Instância da mochila\n- Parâmetros Tabu] --> B[Leitura e Validação da Instância]
    B --> C[Geração de Solução Inicial]
    C --> D[Avaliação da Solução Atual]
    D --> E[Gerar Vizinhança\n(movimentos)]
    E --> F[Filtrar Movimentos Tabu\n+ Regra de Aspiração]
    F --> G[Selecionar Melhor Vizinho Admissível]
    G --> H[Atualizar Solução Atual]
    H --> I[Atualizar Lista Tabu\n(tenure/expiração)]
    I --> J[Atualizar Melhor Solução Global]
    J --> K{Critério de parada\natingido?}

    K -- Não --> E
    K -- Sim --> L[Saídas\n- Melhor solução\n- Valor e peso\n- Iterações\n- Estatísticas]
```

### Fluxos de dados principais (Tabu)
- **Instância**: itens e capacidade da mochila.
- **Parâmetros**: tenure tabu, número máximo de iterações, tamanho da vizinhança, política de aspiração.
- **Memória de busca**: lista tabu (movimentos proibidos e validade), solução atual, melhor solução global.
- **Saídas**: melhor solução viável, trajetória da busca (opcional), métricas de convergência.

---

## Observações
- Ambos os fluxos podem ser implementados com controle de inviabilidade por penalização ou reparo.
- Para experimentos reproduzíveis, recomenda-se registrar **seed aleatória**, tempo e configuração completa dos parâmetros.
