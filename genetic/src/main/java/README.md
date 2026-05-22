# Algoritmo Genetico para o Problema da Mochila 0/1

Este modulo contem a implementacao em Java do Algoritmo Genetico (AG) usado para resolver as instancias do Problema da Mochila 0/1 disponibilizadas em:

```text
docs/inst_test/instancias
```

A entrega desta parte deve considerar apenas os resultados do AG. Os algoritmos de Colonia de Formigas e Tabu Search pertencem a outros elementos do grupo.

## Objetivo da entrega

A professora pediu uma tabela de resultados com uma linha por instancia e as seguintes colunas:

- Instancia;
- Solucao otima (SO);
- Solucao inicial;
- Solucao encontrada (SE);
- Percentagem de desvio em relacao a solucao otima;
- Tempo computacional.

No AG, a solucao inicial corresponde ao melhor individuo da populacao inicial apos aplicacao da reparacao greedy. Esta opcao e adequada porque o Algoritmo Genetico trabalha com uma populacao de solucoes, e nao com uma unica solucao inicial. Assim, antes do ciclo evolutivo, considera-se como solucao inicial a melhor solucao viavel ja disponivel na populacao.

## Funcionamento do AG

Cada solucao e representada por um cromossoma binario:

- `1`: o item e incluido na mochila;
- `0`: o item fica excluido.

O algoritmo executa os seguintes passos:

1. Le todas as instancias da pasta `docs/inst_test/instancias`.
2. Valida se os nomes reais das instancias coincidem com o mapa de valores otimos.
3. Gera uma populacao inicial aleatoria.
4. Aplica reparacao greedy para garantir que nenhuma solucao excede a capacidade da mochila.
5. Regista a melhor solucao da populacao inicial como solucao inicial.
6. Ordena a populacao por fitness, onde o fitness e o valor total dos itens selecionados.
7. Preserva os melhores individuos por elitismo.
8. Seleciona progenitores por torneio.
9. Aplica crossover de dois pontos.
10. Aplica mutacao bit-flip.
11. Repara os filhos com a heuristica greedy.
12. Atualiza a melhor solucao global.
13. Para quando atinge o numero maximo de geracoes ou o limite de estagnacao.

O desvio percentual e calculado por:

```text
(SO - SE) / SO * 100
```

Quanto mais proximo de zero for o desvio, mais proxima esta a solucao encontrada da solucao otima conhecida.

## Configuracao das experiencias

As experiencias podem ser configuradas no ficheiro:

```text
genetic/src/main/resources/genetic-experiments.properties
```

O ficheiro permite definir uma grelha de parametros. O programa executa o produto cartesiano dos valores configurados:

- `population`
- `generations`
- `crossoverRate`
- `mutationRate`
- `eliteSize`
- `tournamentSize`
- `stagnation`
- `seed`
- `parallelism`

O parametro `parallelism` tambem aceita uma lista, por exemplo `parallelism=1,4,8`. Nesse caso, a grelha e executada por grupos e cada resultado indica o numero de threads usado.

## Compilacao

A partir da raiz do projeto:

```bash
javac -encoding UTF-8 -d target/classes genetic/src/main/java/GeneticKnapsack.java
```

## Execucao por ficheiro properties

A partir da raiz do projeto:

```bash
java -cp target/classes GeneticKnapsack --config genetic/src/main/resources/genetic-experiments.properties
```

Tambem e possivel executar sem argumentos. Nesse caso, se existir o ficheiro `genetic-experiments.properties`, ele sera usado automaticamente.

## Execucao manual

Exemplo com uma unica configuracao de parametros:

```bash
java -cp target/classes GeneticKnapsack \
  --instances docs/inst_test/instancias \
  --output results/genetic/ag_resultados.csv \
  --population 50 \
  --generations 500 \
  --crossover 0.85 \
  --mutation 0.005 \
  --elite 3 \
  --tournament 3 \
  --stagnation 100 \
  --seed 42
```

## Ficheiros gerados

Os resultados do AG sao gerados na pasta:

```text
results/genetic
```

Ficheiros principais:

- `ga-grid-results.csv`: contem todas as execucoes da grelha de parametros.
- `ga-initial-solutions.csv`: contem a melhor solucao inicial do AG para cada execucao da grelha, incluindo valor, peso e indices dos itens selecionados.
- `ga-detailed-results.csv`: contem a melhor solucao do AG por instancia. Este e o ficheiro mais adequado para a tabela final da entrega.
- `ga-relatorio.md`: contem uma explicacao curta do AG e uma tabela resumida com os melhores resultados por instancia, incluindo o numero de threads usado.

## Colunas do CSV de solucoes iniciais

O ficheiro `ga-initial-solutions.csv` segue uma estrutura semelhante ao relatorio de solucoes iniciais da Colonia de Formigas:

- `instance`: nome da instancia;
- `population_size`, `generations`, `crossover_rate`, `mutation_rate`, `elite_size`, `tournament_size`, `stagnation`, `seed`, `threads`: configuracao experimental;
- `initial_value`: valor do melhor individuo da populacao inicial apos reparacao greedy;
- `initial_weight`: peso desse individuo inicial;
- `optimal_value`: solucao otima conhecida;
- `difference`: diferenca entre a solucao otima e a solucao inicial;
- `gap_percent`: desvio percentual da solucao inicial;
- `selected_item_indices`: indices dos itens selecionados na solucao inicial.

## Colunas do CSV resumido

O ficheiro `ga-detailed-results.csv` inclui:

- `instance`: nome da instancia;
- `optimal_value`: solucao otima conhecida (SO);
- `initial_value`: melhor individuo da populacao inicial apos reparacao greedy;
- `best_value`: melhor solucao encontrada pelo AG (SE);
- `gap_percent`: percentagem de desvio em relacao a solucao otima;
- `total_weight`: peso total da solucao encontrada;
- `threads`: numero de threads configurado em `parallelism`;
- `best_configuration`: melhor configuracao de parametros para essa instancia;
- `stop_generation`: geracao em que a execucao terminou;
- `elapsed_s`: tempo computacional em segundos.

Para a tabela pedida pela professora, devem ser usadas as colunas:

```text
instance, optimal_value, initial_value, best_value, gap_percent, threads, elapsed_s
```

## Valores otimos

Os valores otimos usados pelo AG estao definidos diretamente no codigo em `OPTIMAL_VALUES`. Durante a execucao, o programa valida explicitamente se os nomes das instancias lidas da pasta `docs/inst_test/instancias` coincidem com as chaves desse mapa.

Atualmente, as instancias esperadas sao:

```text
n_1000_1
n_1000_2
n_1000_3
n_1000_4
n_1000_5
n_1000_6
n_1000_7
n_1000_8
n_1000_9
n_1000_10
```
