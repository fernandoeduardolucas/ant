# Relatorio: Algoritmo Genetico - Problema da Mochila 0/1

## 1. Contexto e abordagem
Foi implementado um **Algoritmo Genetico (AG)** para o Problema da Mochila 0/1, onde cada solucao e representada por um cromossoma binario: o valor 1 indica que o item e selecionado e o valor 0 indica que fica fora da mochila. A funcao fitness corresponde ao valor total dos itens selecionados, considerando apenas solucoes viaveis apos reparacao.

A populacao inicial e gerada aleatoriamente e cada individuo e reparado por uma heuristica greedy. Assim, a solucao inicial reportada corresponde ao melhor individuo da populacao inicial ja reparada, pois e a melhor solucao disponivel antes de iniciar o ciclo evolutivo.

Em cada geracao, a populacao e ordenada por fitness, os melhores individuos sao preservados por elitismo e os restantes descendentes sao produzidos por selecao por torneio, crossover de dois pontos e mutacao bit-flip. Depois dos operadores geneticos, os filhos sao reparados por uma estrategia greedy que remove itens de menor racio valor/peso quando a capacidade e excedida e tenta completar a solucao com itens de bom racio que ainda caibam.

O algoritmo termina quando atinge o numero maximo de geracoes ou quando nao ocorre melhoria durante o limite de estagnacao configurado. O desvio percentual e calculado por **GAP = (SO - SE) / SO * 100**, onde SO e a solucao otima conhecida e SE e a melhor solucao encontrada pelo AG. Valores de GAP proximos de zero indicam solucoes muito proximas do otimo.

## 2. Tabela final resumida

A tabela apresenta apenas resultados do Algoritmo Genetico e conserva, para cada instancia, a melhor execucao encontrada na grelha de parametros.

| Instancia | Solucao otima (SO) | Solucao inicial | Solucao encontrada (SE) | % desvio | Threads | Tempo computacional (s) |
|-----------|--------------------|-----------------|-------------------------|----------|---------|-------------------------|
| n_1000_1 | 9999946233 | 9999826041 | 9999827305 | 0.001189% | 8 | 25.1930 |
| n_1000_2 | 9999964987 | 9999853865 | 9999857893 | 0.001071% | 8 | 25.7700 |
| n_1000_3 | 9999229281 | 9999228097 | 9999229005 | 0.000003% | 8 | 24.4160 |
| n_1000_4 | 9999239905 | 9999238295 | 9999239712 | 0.000002% | 8 | 23.3520 |
| n_1000_5 | 9999251796 | 9999249697 | 9999251471 | 0.000003% | 8 | 22.7190 |
| n_1000_6 | 9996100344 | 9996100244 | 9996100343 | 0.000000% | 8 | 14.7220 |
| n_1000_7 | 9996105266 | 9996105048 | 9996105254 | 0.000000% | 8 | 14.0650 |
| n_1000_8 | 9996111502 | 9839861382 | 9996111488 | 0.000000% | 8 | 14.7130 |
| n_1000_9 | 9980488131 | 9980482827 | 9980486930 | 0.000012% | 8 | 21.4470 |
| n_1000_10 | 9980507700 | 9980497634 | 9980505783 | 0.000019% | 8 | 24.1850 |

## 3. Interpretacao dos resultados

- **Desvio medio geral (GAP):** 0.0002%
- **Otimos alcancados (ou praticamente iguais ao otimo):** 8 de 10 instancias analisadas.

A comparacao entre a solucao inicial e a solucao encontrada permite observar o contributo do processo evolutivo relativamente ao melhor individuo inicial. A comparacao com SO quantifica a qualidade final da solucao atraves do GAP, enquanto o tempo computacional permite avaliar o custo das configuracoes testadas.
