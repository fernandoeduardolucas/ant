# Diagramas de Fluxo de Dados — GA, Busca Tabu e ACO

Este documento descreve o fluxo de dados para três meta-heurísticas aplicadas ao problema da mochila: **Algoritmo Genético (GA)**, **Busca Tabu (Tabu Search)** e **Ant Colony Optimization (ACO)**.

---

## 1) Diagrama de Fluxo de Dados — Algoritmo Genético (GA)

```mermaid
flowchart TD
    A["Entradas<br/>- Instância da mochila<br/>- Parâmetros GA"] --> B["Leitura e Validação da Instância"]
    B --> C["Geração da População Inicial"]
    C --> D["Avaliação de Aptidão<br/>Fitness + Penalização de Inviabilidade"]
    D --> E{"Critério de parada<br/>atingido?"}

    E -- Não --> F["Seleção de Pais"]
    F --> G["Cruzamento"]
    G --> H["Mutação"]
    H --> I["Reparo/Viabilização<br/>(opcional)"]
    I --> J["Avaliação dos Filhos"]
    J --> K["Substituição / Elitismo"]
    K --> D

    E -- Sim --> L["Melhor Solução Encontrada"]
    L --> M["Saídas<br/>- Vetor de decisão<br/>- Valor total<br/>- Peso total<br/>- Estatísticas"]
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
    A["Entradas<br/>- Instância da mochila<br/>- Parâmetros Tabu"] --> B["Leitura e Validação da Instância"]
    B --> C["Geração de Solução Inicial"]
    C --> D["Avaliação da Solução Atual"]
    D --> E["Gerar Vizinhança<br/>(movimentos)"]
    E --> F["Filtrar Movimentos Tabu<br/>+ Regra de Aspiração"]
    F --> G["Selecionar Melhor Vizinho Admissível"]
    G --> H["Atualizar Solução Atual"]
    H --> I["Atualizar Lista Tabu<br/>(tenure/expiração)"]
    I --> J["Atualizar Melhor Solução Global"]
    J --> K{"Critério de parada<br/>atingido?"}

    K -- Não --> E
    K -- Sim --> L["Saídas<br/>- Melhor solução<br/>- Valor e peso<br/>- Iterações<br/>- Estatísticas"]
```

### Fluxos de dados principais (Tabu)
- **Instância**: itens e capacidade da mochila.
- **Parâmetros**: tenure tabu, número máximo de iterações, tamanho da vizinhança, política de aspiração.
- **Memória de busca**: lista tabu (movimentos proibidos e validade), solução atual, melhor solução global.
- **Saídas**: melhor solução viável, trajetória da busca (opcional), métricas de convergência.

---

## 3) Diagrama de Fluxo de Dados — Ant Colony Optimization (ACO)

```mermaid
flowchart TD
    A["Entradas<br/>- Instância da mochila<br/>- Parâmetros ACO"] --> B["Inicializar Feromônio e Heurística"]
    B --> C{"Critério de parada<br/>atingido?"}

    C -- Não --> D["Construção de Soluções<br/>(uma por formiga)"]
    D --> E["Avaliar Soluções<br/>Fitness + Viabilidade"]
    E --> F["Atualizar Melhor Solução Global"]
    F --> G["Evaporação de Feromônio"]
    G --> H["Deposição de Feromônio<br/>(formigas selecionadas)"]
    H --> C

    C -- Sim --> I["Saídas<br/>- Melhor solução<br/>- Valor e peso<br/>- Iterações<br/>- Estatísticas"]
```

### Fluxos de dados principais (ACO)
- **Instância**: itens (valor, peso) e capacidade da mochila.
- **Parâmetros**: número de formigas, taxa de evaporação, influência de feromônio (`alpha`), influência heurística (`beta`) e máximo de iterações.
- **Memória de busca**: matriz/vetor de feromônio, melhor solução da iteração e melhor solução global.
- **Saídas**: melhor solução viável, histórico de convergência (opcional), tempo e métricas de execução.

---

## Observações
- Os três fluxos podem ser implementados com controle de inviabilidade por penalização ou reparo.
- Para experimentos reproduzíveis, recomenda-se registrar **seed aleatória**, tempo e configuração completa dos parâmetros.
