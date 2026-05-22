# Relatório: Algoritmo Genético - Problema da Mochila 0/1

## 1. Contexto e Abordagem
Neste trabalho, implementou-se um **Algoritmo Genético (AG)** para resolver o Problema da Mochila 0/1. A estrutura foca-se na otimização da escolha de itens, sujeita a uma restrição de capacidade total.

**Características do AG:**
- **Representação:** Binária (1 se o item está na mochila, 0 caso contrário)
- **População e Evolução:** Estratégia geracional estrita com substituição total
- **Seleção:** Torneio (garante pressão seletiva ajustável)
- **Cruzamento (Crossover):** 2 pontos (maior preservação de blocos genéticos face a 1 ponto)
- **Mutação:** Bit-flip (exploração do espaço de procura)
- **Reparação:** Heurística Greedy (garante viabilidade retirando os itens de menor rácio valor/peso e adicionando os melhores)
- **Elitismo:** Preservação dos melhores indivíduos para evitar perda da melhor solução

## 2. Resultados Consolidados (Melhores Configurações)

A tabela abaixo resume as melhores soluções encontradas para cada instância, após análise da grelha de testes.

| Instância | Ótimo | Melhor Valor (AG) | GAP (%) | Tempo (s) | Melhor Configuração (AG) |
|-----------|-------|-------------------|---------|-----------|--------------------------|
| n_1000_1 | 9999946233 | 9999827305 | 0.001189% | 0.8803 | pop=80 gen=500 cRate=0.85 mRate=0.005 elite=3 tourn=5 stag=50 seed=4 |
| n_1000_2 | 9999964987 | 9999858067 | 0.001069% | 0.8997 | pop=80 gen=500 cRate=0.75 mRate=0.005 elite=1 tourn=5 stag=100 seed=202 |
| n_1000_3 | 9999229281 | 9999229015 | 0.000003% | 0.8480 | pop=80 gen=500 cRate=0.95 mRate=0.005 elite=1 tourn=5 stag=50 seed=12 |
| n_1000_4 | 9999239905 | 9999239704 | 0.000002% | 0.8437 | pop=80 gen=500 cRate=0.85 mRate=0.005 elite=3 tourn=5 stag=100 seed=12 |
| n_1000_5 | 9999251796 | 9999251457 | 0.000003% | 0.9539 | pop=80 gen=500 cRate=0.95 mRate=0.005 elite=3 tourn=5 stag=100 seed=12 |
| n_1000_6 | 9996100344 | 9996100341 | 0.000000% | 0.3289 | pop=80 gen=300 cRate=0.85 mRate=0.005 elite=1 tourn=5 stag=50 seed=4 |
| n_1000_7 | 9996105266 | 9996105253 | 0.000000% | 0.4659 | pop=80 gen=300 cRate=0.95 mRate=0.005 elite=5 tourn=2 stag=100 seed=4 |
| n_1000_8 | 9996111502 | 9996111488 | 0.000000% | 0.3471 | pop=80 gen=300 cRate=0.95 mRate=0.010 elite=5 tourn=3 stag=50 seed=202 |
| n_1000_9 | 9980488131 | 9980487736 | 0.000004% | 0.7880 | pop=80 gen=500 cRate=0.75 mRate=0.001 elite=5 tourn=5 stag=20 seed=4 |
| n_1000_10 | 9980507700 | 9980506760 | 0.000009% | 0.8546 | pop=80 gen=500 cRate=0.85 mRate=0.001 elite=1 tourn=5 stag=50 seed=4 |

## 3. Análise e Discussão

- **Desvio Médio Geral (GAP):** 0.0002%
- **Ótimos Alcançados (ou quase ótimos):** 8 de 10 instâncias analisadas.

### Impacto dos Parâmetros
- **Tamanho da População & Gerações:** Populações maiores aumentam a diversidade inicial e previnem a convergência prematura, mas têm um custo computacional linearmente superior.
- **Pressão de Seleção (Torneio vs Elitismo):** Torneios maiores forçam a convergência rápida. O elitismo atuou como uma rede de segurança vital, impedindo que mutações destrutivas afetassem a melhor solução já encontrada.
- **Reparação Greedy:** A reparação não só garante a viabilidade das soluções, como injeta inteligência heurística no processo evolutivo, acelerando drasticamente a aproximação aos valores ótimos.

### Conclusões Relevantes
O Algoritmo Genético mostrou ser altamente competitivo, especialmente quando a fase de exploração (crossover e mutação) é equilibrada por uma heurística de reparação local eficiente. Como trabalho futuro, seria interessante incorporar parâmetros auto-adaptáveis ou testar operadores de cruzamento uniforme para avaliar o impacto na quebra de simetria nas instâncias mais densas.
