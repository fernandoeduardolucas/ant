##Solução Inicial

Viewed TabuSearchCore.java:1-352

A solução inicial utilizada no algoritmo Tabu Search (presente na classe `TabuSearchCore.java`) é uma **solução gulosa (greedy)** baseada no **rácio valor/peso** de cada item.

O processo, definido no método `construirSolucaoGulosa()`, funciona da seguinte forma:

1. **Ordenação:** Todos os itens são ordenados de forma decrescente com base no seu rácio entre o valor e o peso (`valor / peso`). Os itens com o maior valor por unidade de peso ficam em primeiro lugar.
2. **Preenchimento:** O algoritmo percorre a lista ordenada de itens e vai adicionando cada um à mochila, desde que o seu peso não faça com que a capacidade máxima da mochila seja excedida.
3. **Ponto de partida:** A solução gerada por este método é definida como a solução atual (e guardada como a melhor solução inicial) antes de o Tabu Search começar a explorar a vizinhança.

Este é um ponto de partida bastante clássico e eficaz para o Problema da Mochila (Knapsack Problem), pois garante logo de início uma solução viável de boa qualidade antes de começar os movimentos *flip* e *swap*.

##Paralelismo

Edited readme.md
Viewed ts-grid-results.csv:1-49
Viewed TSExperimentRunner.java:1-311

Sim, **temos paralelismo implementado**, mas é importante distinguir a que nível:

1. **No Executor de Experiências (`TSExperimentRunner.java`)**: **Sim, existe paralelismo.**
   O ficheiro `TSExperimentRunner.java` utiliza um `ExecutorService` com uma `ExecutorCompletionService` (no método `executarExperimentosParalelos()`). Isto significa que quando o algoritmo é executado para fazer o *Grid Search* (testar várias combinações de parâmetros como `tenure_flip`, `tenure_swap`, `stall`, `seed`, etc.), as **diferentes combinações são executadas em paralelo** em várias *threads*. Por predefinição, o nível de paralelismo é igual ao número de processadores (cores) disponíveis na máquina (`Runtime.getRuntime().availableProcessors()`), a menos que seja definido o parâmetro `ts.parallelism` no ficheiro de propriedades.

2. **No Núcleo do Algoritmo (`TabuSearchCore.java`)**: **Não.**
   O funcionamento interno do algoritmo Tabu Search para uma única execução é estritamente sequencial. A exploração da vizinhança (procurar o melhor movimento *flip* ou *swap*) é feita através de ciclos clássicos de forma sequencial, sem dividir a carga de trabalho dessa iteração específica por múltiplas *threads*.

Em resumo: uma única execução do Tabu Search corre numa só *thread*, mas quando estás a fazer testes a avaliar múltiplos parâmetros (como os resultados que tens abertos no ficheiro `ts-grid-results.csv`), essas várias experiências correm ao mesmo tempo em *threads* separadas, tirando partido de todo o processador para acelerar o processo!
-----------------------------
##Performance----------------
-----------------------------
Não, não tem de estar com o mesmo número de execuções e **não tem lógica tentares igualar os parâmetros, porque a natureza matemática dos dois algoritmos é completamente diferente**. 

Deixa-me dar-te os argumentos teóricos (que inclusive podes e deves usar no teu relatório ou defesa da tese) sobre como funciona a comparação correta entre diferentes meta-heurísticas:

### 1. Parâmetros diferentes existem porque a biologia/matemática é diferente
O algoritmo das Formigas (MMAS) tem 8 parâmetros na tua grelha porque precisa de gerir feromonas (taxa de evaporação `rho`, pesos `alpha` e `beta`, intensidade de depósito `q`, número de formigas `m`). 
O Tabu Search não tem feromonas nem formigas; a sua memória baseia-se em "tempos de proibição" (tenure do flip, tenure do swap) e diversificação (`diversify`). **É impossível forçar os dois algoritmos a ter a mesma quantidade de variáveis porque eles funcionam de formas fundamentalmente distintas.**

### 2. O objetivo da "Grid Search" (Varrimento em Grelha)
A grelha de testes não faz parte do tempo ou do desempenho do algoritmo final. O objetivo de correres 65.000 testes nas formigas e 7.000 testes no Tabu é apenas um: **Encontrar a "Configuração Vencedora" (a melhor afinação possível) para cada algoritmo de forma isolada.**

### 3. Como se faz a comparação final no trabalho?
Na secção de comparação do teu trabalho, tu não comparas as 65.000 execuções contra as 7.290 execuções. Tu apenas **pegas no melhor de um e comparas com o melhor do outro**.

Por exemplo, para a instância `n_1000_1`:
* **Melhor Formiga Encontrada:** Demorou `X ms` e teve um GAP de `Y%` (usando os melhores hiperparâmetros que a grelha de 65.000 testes descobriu).
* **Melhor Tabu Encontrado:** Demorou `1040 ms` e teve um GAP de `0.0005%` (usando os melhores hiperparâmetros que a grelha de 7.290 testes descobriu).

**Conclusão:**
O que estás a avaliar é "Qual algoritmo é capaz de resolver melhor o Problema da Mochila quando está perfeitamente afinado?". O facto de precisares de mais combinações de testes para afinar as formigas (porque elas têm mais "botões" para rodar) é apenas um reflexo de que o MMAS é mais complexo de parametrizar, mas não invalida de todo a comparação final de desempenho (Tempo vs GAP da melhor configuração de cada um). Está tudo corretíssimo e metodologicamente impecável como tens!