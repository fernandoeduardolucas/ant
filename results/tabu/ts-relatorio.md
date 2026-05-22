# Relatório: Pesquisa Tabu (Tabu Search) - Problema da Mochila 0/1

## 1. Contexto e Abordagem
Neste trabalho, implementou-se a meta-heurística **Pesquisa Tabu (Tabu Search - TS)** para resolver o Problema da Mochila 0/1.

**Características do Tabu Search:**
- **Solução Inicial:** Gerada através de um método construtivo guloso (*greedy*) com base no rácio `valor/peso` dos itens.
- **Estrutura de Vizinhança:** Movimentos híbridos compostos por:
  - **1-flip:** Adição de um item de fora para dentro da mochila ou remoção de um item da mochila.
  - **Swap:** Troca de um item selecionado por um item não selecionado, melhorando a capacidade de ajuste fino.
- **Lista Tabu:** Memória de curto prazo para evitar ciclos e revisita de soluções recentes. Possui *tenures* separados para movimentos de *flip* e *swap*.
- **Critério de Aspiração:** Um movimento tabu é aceite se resultar numa solução melhor do que a melhor solução global encontrada até ao momento.
- **Diversificação por Frequência:** Penalização de itens frequentemente modificados quando a busca dá sinais de estagnação.
- **Reinicialização Adaptativa:** Após um limite de iterações sem melhoria (*stall limit*), o algoritmo reinicia a partir da melhor solução conhecida com perturbações aleatórias.

---

## 2. Resultados Consolidados (Melhores Configurações)

A tabela abaixo resume as melhores soluções encontradas para cada instância a partir do varrimento em grelha (*grid search*), comparando com a solução ótima (SO) e a solução inicial do método construtivo.

| Instância | Solução Ótima (SO) | Solução Inicial | Solução Encontrada (SE) | Desvio (GAP %) | Tempo (s) | Configuração Vencedora (TS) |
| :--- | :---: | :---: | :---: | :---: | :---: | :--- |
| **n_1000_1** | 9999946233 | 9981765208 | 9999946166 | 0.00000067% | 0.086 s | `iters=300, tFlip=10, tSwap=7, stall=5, div=0.3` |
| **n_1000_2** | 9999964987 | 9987797016 | 9999964902 | 0.00000085% | 0.098 s | `iters=500, tFlip=10, tSwap=7, stall=5, div=0.3` |
| **n_1000_3** | 9999229281 | 9981260200 | 9999229058 | 0.00000223% | 0.052 s | `iters=300, tFlip=10, tSwap=7, stall=10, div=0.1` |
| **n_1000_4** | 9999239905 | 9961739503 | 9999239804 | 0.00000101% | 0.060 s | `iters=300, tFlip=10, tSwap=7, stall=5, div=0.1` |
| **n_1000_5** | 9999251796 | 9930500811 | 9999251743 | 0.00000053% | 0.056 s | `iters=300, tFlip=10, tSwap=7, stall=10, div=0.1` |
| **n_1000_6** | 9996100344 | 9312506545 | 9996100344 | 0.00000000% | 0.014 s | `iters=100, tFlip=10, tSwap=7, stall=5, div=0.2` |
| **n_1000_7** | 9996105266 | 9507823943 | 9996105266 | 0.00000000% | 0.014 s | `iters=100, tFlip=10, tSwap=7, stall=5, div=0.2` |
| **n_1000_8** | 9996111502 | 9410173763 | 9996111502 | 0.00000000% | 0.014 s | `iters=100, tFlip=10, tSwap=7, stall=5, div=0.1` |
| **n_1000_9** | 9980488131 | 9980484517 | 9980486293 | 0.00001842% | 0.145 s | `iters=500, tFlip=10, tSwap=7, stall=5, div=0.1` |
| **n_1000_10** | 9980507700 | 9980502348 | 9980504397 | 0.00003309% | 0.028 s | `iters=100, tFlip=10, tSwap=7, stall=5, div=0.1` |

*Nota: O Desvio (GAP %) é calculado como `(SO - SE) / SO * 100`.*

---

## 3. Análise e Discussão

- **Qualidade das Soluções (GAPs extremamente baixos):**
  A Pesquisa Tabu obteve resultados excelentes. Em 3 das 10 instâncias (`n_1000_6`, `n_1000_7` e `n_1000_8`), o desvio foi de **0.00%**, o que significa que o algoritmo encontrou a **solução ótima exata**. Nas restantes instâncias, o desvio máximo foi de apenas **0.000033%** (`n_1000_10`), demonstrando uma precisão impressionante.
  
- **Melhoria face à Solução Inicial:**
  O método construtivo guloso fornece uma solução de boa qualidade em instâncias do tipo *rácio*, mas o Tabu Search consegue refinar significativamente estes valores. Nas instâncias mais difíceis para a heurística inicial (como `n_1000_6`, `n_1000_7` e `n_1000_8`, com desvios iniciais visíveis), o TS recuperou toda a diferença até ao ótimo absoluto.

- **Eficiência Temporal:**
  O tempo computacional é extremamente reduzido. O TS resolveu qualquer uma das instâncias de 1000 itens em menos de **0.15 segundos** (150 ms), sendo significativamente mais rápido que a implementação do Algoritmo Genético, mantendo ao mesmo tempo uma taxa de precisão/GAP igual ou superior.

- **Importância da Vizinhança Swap e da Diversificação:**
  A combinação de movimentos 1-flip com a vizinhança de troca (*swap*) permitiu contornar ótimos locais de forma muito ágil. A reinicialização adaptativa (diversificação) injetou a perturbação necessária nas instâncias em que o algoritmo estagnou, permitindo que a busca continuasse a melhorar a solução.
