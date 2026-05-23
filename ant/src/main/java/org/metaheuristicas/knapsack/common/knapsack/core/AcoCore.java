package org.metaheuristicas.knapsack.common.knapsack.core;

import org.metaheuristicas.knapsack.common.knapsack.model.Item;
import org.metaheuristicas.knapsack.common.knapsack.model.Solucao;

import java.util.Arrays;
import java.util.Random;

/**
 * Núcleo do ACO/MMAS para mochila 0/1.
 *
 * <p>Mapeamento direto do documento {@code docs/03_Colonia_Formigas.docx}:
 * <ul>
 *   <li><b>Representação de feromonas</b>: vetor {@code tau[i]} (seção 2.1).</li>
 *   <li><b>Heurística local</b>: {@code eta[i] = valor/peso} normalizado (seção 2.2).</li>
 *   <li><b>Ciclo MMAS</b>: construção -> avaliação -> evaporação -> reforço -> limitação
 *   -> reinicialização por estagnação (seção 4.1).</li>
 *   <li><b>Busca local 1-flip</b> após construção de cada formiga (seção 4.2).</li>
 * </ul>
 *
 * <p>A ideia é manter o código legível "de ponta a ponta", com nomes de método
 * que representem cada etapa do pseudocódigo do documento.
 */
public class AcoCore {
    /** Vetor de itens da instância da mochila. */
    private final Item[] itens;
    /** Capacidade máxima da mochila (restrição hard do problema). */
    private final long capacidade;
    /** Número de formigas m (quantas soluções são construídas por ciclo). */
    private final int numFormigas;
    /** Número máximo de ciclos/iterações do MMAS. */
    private final int iteracoes;
    /** Peso da feromona (α) na decisão probabilística. */
    private final double alpha;
    /** Peso da heurística (β = influência de valor/peso). */
    private final double beta;
    /** Taxa de evaporação ρ em (0,1). */
    private final double rho;
    /** Intensidade de depósito q (mantido para rastreabilidade experimental). */
    private final double q;
    /** Limite de ciclos sem melhoria antes de reiniciar feromonas. */
    private final int limiteSemMelhoria;
    /** Gerador pseudoaleatório (controlado por seed para reprodutibilidade). */
    private final Random rng;

    /** Feromona por item: τ_i (força histórica de escolher item i). */
    private double[] tau;
    /** Heurística por item: η_i = (valor/peso) normalizado. */
    private double[] eta;
    /** Limite inferior de τ_i no MMAS. */
    private double tauMin = 1e-6;
    /** Limite superior de τ_i no MMAS. */
    private double tauMax = 1e6;
    /** Ordem reutilizável para evitar alocações na construção das soluções. */
    private final int[] ordemBuffer;
    /** Razão valor/peso pré-calculada por item (evita divisões repetidas). */
    private final double[] razaoValorPeso;

    public AcoCore(
            Item[] itens,
            long capacidade,
            int numFormigas,
            int iteracoes,
            double alpha,
            double beta,
            double rho,
            double q,
            int limiteSemMelhoria,
            long seed
    ) {
        this.itens = itens;
        this.capacidade = capacidade;
        this.numFormigas = numFormigas;
        this.iteracoes = iteracoes;
        this.alpha = alpha;
        this.beta = beta;
        this.rho = rho;
        this.q = q;
        this.limiteSemMelhoria = limiteSemMelhoria;
        this.rng = new Random(seed);
        this.ordemBuffer = new int[itens.length];
        this.razaoValorPeso = new double[itens.length];

        inicializarEstruturas();
    }

    public Solucao resolver() {
        // MMAS: começa com uma solução gulosa para ter um bom ponto inicial x*.
        Solucao melhorGlobal = construirSolucaoGulosaInicial();
        atualizarLimitesFeromonio(melhorGlobal);
        // MMAS: inicialização uniforme no limite superior.
        reiniciarFeromonio();

        int semMelhoria = 0;

        for (int t = 0; t < iteracoes; t++) {
            Solucao melhorIteracao = null;

            for (int formiga = 0; formiga < numFormigas; formiga++) {
                // Cada formiga constrói solução factível guiada por tau e eta.
                Solucao candidata = construirSolucaoProbabilistica();
                // Integração com busca local 1-flip (ACO memético da seção 4.2).
                Solucao refinada = melhorarComBuscaLocal1Flip(candidata);

                if (melhorIteracao == null || refinada.valorTotal > melhorIteracao.valorTotal) {
                    melhorIteracao = refinada;
                }
            }

            if (melhorIteracao != null && melhorIteracao.valorTotal > melhorGlobal.valorTotal) {
                melhorGlobal = melhorIteracao;
                semMelhoria = 0;
                atualizarLimitesFeromonio(melhorGlobal);
            } else {
                semMelhoria++;
            }

            // Atualização de feromonas do MMAS:
            // 1) evaporação global
            // Diversificação: evaporação reduz reforços antigos e abre espaço para novas combinações.
            evaporarFeromonio();
            // 2) reforço só da melhor solução (estratégia best/global-best)
            // Intensificação: reforço elitista concentra a busca na melhor solução conhecida.
            depositarFeromonio(melhorGlobal);
            // 3) truncamento em [tauMin, tauMax] para evitar estagnação
            limitarFeromonio();

            if (semMelhoria >= limiteSemMelhoria) {
                // Reinicialização periódica quando há estagnação.
                reiniciarFeromonio();
                semMelhoria = 0;
            }
        }

        return melhorGlobal;
    }

    private void inicializarEstruturas() {
        tau = new double[itens.length];
        eta = new double[itens.length];

        // Seção 2.2: heurística do KP é valor/peso.
        double maiorRazao = 0.0;
        for (int i = 0; i < itens.length; i++) {
            double razao = (double) itens[i].valor / itens[i].peso;
            razaoValorPeso[i] = razao;
            maiorRazao = Math.max(maiorRazao, razao);
        }

        if (maiorRazao == 0.0) {
            maiorRazao = 1.0;
        }

        for (int i = 0; i < itens.length; i++) {
            // Normalização para manter valores em escala estável.
            eta[i] = razaoValorPeso[i] / maiorRazao;
        }

        // Feromonas uniformes no início (estado sem preferência).
        Arrays.fill(tau, 1.0);
    }

    private Solucao construirSolucaoGulosaInicial() {
        // Objetivo: gerar x* inicial viável de forma rápida para:
        // 1) começar a busca com uma solução "forte";
        // 2) estimar limites de feromona do MMAS (tauMax/tauMin) com base em z(x*).
        //
        // Estratégia gulosa clássica do KP:
        // - ordenar itens por densidade (valor/peso) descrescente;
        // - inserir item se ainda couber na capacidade.
        Integer[] ordem = new Integer[itens.length];
        for (int i = 0; i < itens.length; i++) {
            ordem[i] = i;
        }

        // Ordenação por eficiência econômica: maior valor por unidade de peso primeiro.
        Arrays.sort(ordem, (a, b) -> Double.compare(razaoValorPeso[b], razaoValorPeso[a]));

        boolean[] escolhidos = new boolean[itens.length];
        long pesoAtual = 0;
        long valorAtual = 0;

        for (int indice : ordem) {
            // Regra de viabilidade: nunca ultrapassar a capacidade da mochila.
            if (pesoAtual + itens[indice].peso <= capacidade) {
                escolhidos[indice] = true;
                pesoAtual += itens[indice].peso;
                valorAtual += itens[indice].valor;
            }
        }

        return new Solucao(escolhidos, valorAtual, pesoAtual);
    }

    private Solucao construirSolucaoProbabilistica() {
        // Construção estocástica de uma solução por formiga:
        // cada decisão x_i ∈ {0,1} combina memória coletiva (tau) + heurística local (eta).
        boolean[] escolhidos = new boolean[itens.length];
        long pesoAtual = 0;
        long valorAtual = 0;

        for (int i = 0; i < itens.length; i++) {
            ordemBuffer[i] = i;
        }
        embaralharOrdemBuffer();

        for (int indice : ordemBuffer) {
            long capacidadeResidual = capacidade - pesoAtual;
            // Gestão de viabilidade (seção 2.3): se não cabe, exclui automaticamente.
            if (itens[indice].peso > capacidadeResidual) {
                continue;
            }

            // Regra binária de decisão (seção 2.2):
            // P(x_i = 1) = (tau_i^alpha * eta_i^beta) / (tau_i^alpha * eta_i^beta + (1-tau_i)^alpha)
            double tauNormalizado = normalizarTauParaProbabilidade(tau[indice]);
            double incluir = powFast(tauNormalizado, alpha) * powFast(eta[indice], beta);
            double excluir = powFast(Math.max(1e-12, 1.0 - tauNormalizado), alpha);
            // Probabilidade Bernoulli para a decisão binária de incluir o item i.
            double probIncluir = incluir / Math.max(1e-12, incluir + excluir);

            // Sorteio estocástico: garante exploração e evita comportamento totalmente guloso.
            if (rng.nextDouble() < probIncluir) {
                escolhidos[indice] = true;
                pesoAtual += itens[indice].peso;
                valorAtual += itens[indice].valor;
            }
        }

        return new Solucao(escolhidos, valorAtual, pesoAtual);
    }

    private void embaralharOrdemBuffer() {
        // Fisher-Yates in-place: sem boxing e sem alocação por formiga.
        for (int i = ordemBuffer.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = ordemBuffer[i];
            ordemBuffer[i] = ordemBuffer[j];
            ordemBuffer[j] = tmp;
        }
    }

    private double powFast(double base, double expoente) {
        if (expoente == 1.0) {
            return base;
        }
        if (expoente == 2.0) {
            return base * base;
        }
        if (expoente == 3.0) {
            return base * base * base;
        }
        return Math.pow(base, expoente);
    }

    private double normalizarTauParaProbabilidade(double tauValue) {
        return Math.min(1.0 - 1e-12, Math.max(1e-12, tauValue));
    }

    private Solucao melhorarComBuscaLocal1Flip(Solucao base) {
        // Busca local 1-flip:
        // tenta inverter o estado de um item por vez (0->1 ou 1->0),
        // aceitando melhorias estritas de valor enquanto mantém viabilidade.
        //
        // Papel meta-heurístico:
        // - intensificação: refina rapidamente a solução construída pela formiga;
        // - efeito memético: combina construção ACO + refinamento local.
        // Intensificação: busca local explora a vizinhança imediata da solução para refinamento.
        boolean[] escolhidos = Arrays.copyOf(base.escolhidos, base.escolhidos.length);
        long melhorValor = base.valorTotal;
        long melhorPeso = base.pesoTotal;
        boolean houveMelhoria;

        do {
            houveMelhoria = false;

            for (int i = 0; i < escolhidos.length; i++) {
                boolean novoEstado = !escolhidos[i];
                // Delta incremental evita recomputar peso/valor total do zero.
                long novoPeso = melhorPeso + (novoEstado ? itens[i].peso : -itens[i].peso);
                long novoValor = melhorValor + (novoEstado ? itens[i].valor : -itens[i].valor);

                if (novoEstado && novoPeso > capacidade) {
                    continue;
                }

                if (novoValor > melhorValor) {
                    // Estratégia first-improvement permissiva: aceita melhora local imediata.
                    escolhidos[i] = novoEstado;
                    melhorPeso = novoPeso;
                    melhorValor = novoValor;
                    houveMelhoria = true;
                }
            }
        } while (houveMelhoria);

        return new Solucao(escolhidos, melhorValor, melhorPeso);
    }

    private void atualizarLimitesFeromonio(Solucao melhor) {
        tauMax = 0.95;
        tauMin = 0.05;
    }

    private void evaporarFeromonio() {
        // Evaporação global:
        // tau_i <- (1-rho) * tau_i
        // Remove memória antiga e aumenta capacidade de escapar de ótimos locais.
        double fator = Math.max(0.0, 1.0 - rho);
        for (int i = 0; i < tau.length; i++) {
            tau[i] *= fator;
        }
    }

    private void depositarFeromonio(Solucao melhor) {

        double deposito = q * rho * 0.1;
        for (int i = 0; i < melhor.escolhidos.length; i++) {
            if (melhor.escolhidos[i]) {
                tau[i] += deposito;
            }
        }
    }

    private void limitarFeromonio() {
        // Truncamento MMAS em [tauMin, tauMax]:
        // protege contra explosão numérica e contra estagnação total do sistema.
        for (int i = 0; i < tau.length; i++) {
            if (tau[i] < tauMin) {
                tau[i] = tauMin;
            }
            if (tau[i] > tauMax) {
                tau[i] = tauMax;
            }
        }
    }

    private void reiniciarFeromonio() {
        Arrays.fill(tau, tauMax);
    }
}
