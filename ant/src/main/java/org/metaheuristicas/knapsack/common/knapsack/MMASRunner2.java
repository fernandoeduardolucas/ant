package org.metaheuristicas.knapsack.common.knapsack;

import org.metaheuristicas.knapsack.common.knapsack.model.ACOKnapsack;
import org.metaheuristicas.knapsack.common.knapsack.model.Instancia;
import org.metaheuristicas.knapsack.common.knapsack.model.Solucao;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Runner MMAS que escreve apenas o relatório final em CSV.
 */
public final class MMASRunner2 {

    private MMASRunner2() {
    }

    public static void main(String[] args) throws Exception {
        Path propertiesPath = args.length > 0
                ? Path.of(args[0])
                : Path.of("ant/src/main/resources/mmas-experiments.properties");

        Properties p = carregarProperties(propertiesPath);
        List<String> instancias = resolverInstancias(p);

        List<Integer> ants = parseIntList(p, 30, "mmas.numero.formigas", "mmas.ants");
        List<Integer> iters = parseIntList(p, 300, "mmas.numero.ciclos", "mmas.iterations");
        List<Double> alphas = parseDoubleList(p, 1.0, "mmas.peso.feromona", "mmas.alpha");
        List<Double> betas = parseDoubleList(p, 3.0, "mmas.peso.heuristica", "mmas.beta");
        List<Double> rhos = parseDoubleList(p, 0.2, "mmas.taxa.evaporacao", "mmas.rho");
        List<Double> qs = parseDoubleList(p, 1.0, "mmas.intensidade.deposito", "mmas.q");
        List<Integer> stalls = parseIntList(p, 80, "mmas.ciclos.sem.melhoria", "mmas.stall.limit");
        List<Long> seeds = parseLongList(p, 12345L, "mmas.semente", "mmas.seed");

        int paralelismo = Integer.parseInt(
                readPropertyFirst(
                        p,
                        String.valueOf(Runtime.getRuntime().availableProcessors()),
                        "mmas.paralelismo",
                        "mmas.parallelism"
                )
        );
        if (paralelismo <= 0) {
            throw new IllegalArgumentException("mmas.parallelism deve ser > 0");
        }

        Path finalReportOutput = Path.of(readPropertyFirst(
                p,
                "results/ant/mmas-relatorio.csv",
                "mmas.saida.relatorio.final",
                "mmas.output.final.report"
        ));
        Path finalReportOutputParent = finalReportOutput.getParent();
        if (finalReportOutputParent != null) {
            Files.createDirectories(finalReportOutputParent);
        }

        int totalRuns = instancias.size() * ants.size() * iters.size() * alphas.size() * betas.size()
                * rhos.size() * qs.size() * stalls.size() * seeds.size();

        System.out.printf(
                "Iniciando experiências MMAS Runner2 (%d execuções, paralelismo=%d)...%n",
                totalRuns,
                paralelismo
        );

        List<ExperimentTask> tasks = new ArrayList<>(totalRuns);
        Map<String, Instancia> instanciaPorPath = new HashMap<>();
        for (String instanciaPath : instancias) {
            Instancia instancia = ACOKnapsack.carregarInstancia(Path.of(instanciaPath));
            instanciaPorPath.put(instanciaPath, instancia);

            for (int ant : ants) {
                for (int iter : iters) {
                    for (double alpha : alphas) {
                        for (double beta : betas) {
                            for (double rho : rhos) {
                                for (double q : qs) {
                                    for (int stall : stalls) {
                                        for (long seed : seeds) {
                                            tasks.add(new ExperimentTask(
                                                    instanciaPath,
                                                    instancia,
                                                    ant,
                                                    iter,
                                                    alpha,
                                                    beta,
                                                    rho,
                                                    q,
                                                    stall,
                                                    seed
                                            ));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Map<String, ExperimentResult> melhorPorInstancia = new HashMap<>();
        List<ExperimentResult> todosResultados = new ArrayList<>(totalRuns);
        executarExperimentosParalelos(tasks, paralelismo, totalRuns, melhorPorInstancia, todosResultados);

        escreverRelatorioFinalCsv(finalReportOutput, todosResultados, melhorPorInstancia, instanciaPorPath);
        System.out.println("Relatório final CSV: " + finalReportOutput);
    }

    private static void executarExperimentosParalelos(
            List<ExperimentTask> tasks,
            int paralelismo,
            int totalRuns,
            Map<String, ExperimentResult> melhorPorInstancia,
            List<ExperimentResult> todosResultados
    ) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(paralelismo);
        CompletionService<ExperimentResult> completion = new ExecutorCompletionService<>(executor);
        try {
            for (ExperimentTask task : tasks) {
                completion.submit(task::execute);
            }

            for (int concluido = 1; concluido <= tasks.size(); concluido++) {
                ExperimentResult resultado;
                try {
                    resultado = completion.take().get();
                } catch (ExecutionException e) {
                    throw new IllegalStateException("Falha ao executar configuração MMAS", e.getCause());
                }

                atualizarMelhorPorInstancia(melhorPorInstancia, resultado);
                todosResultados.add(resultado);

                System.out.printf(
                        "[%d/%d] %s | valor=%d, %d ms%n",
                        concluido,
                        totalRuns,
                        resultado.instanciaPath(),
                        resultado.valorTotal(),
                        resultado.elapsedMs()
                );
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private static void atualizarMelhorPorInstancia(
            Map<String, ExperimentResult> melhorPorInstancia,
            ExperimentResult candidato
    ) {
        ExperimentResult atual = melhorPorInstancia.get(candidato.instanciaPath());
        if (atual == null || candidato.isBetterThan(atual)) {
            melhorPorInstancia.put(candidato.instanciaPath(), candidato);
        }
    }

    private static void escreverRelatorioFinalCsv(
            Path finalReportOutput,
            List<ExperimentResult> todosResultados,
            Map<String, ExperimentResult> melhorPorInstancia,
            Map<String, Instancia> instanciaPorPath
    ) throws IOException {
        Properties otimos = carregarProperties(Path.of("ant/src/main/resources/optimal-values.properties"));
        Map<String, Long> startByInstance = new HashMap<>();
        Map<String, Long> endByInstance = new HashMap<>();
        for (ExperimentResult resultado : todosResultados) {
            startByInstance.merge(resultado.instanciaPath(), resultado.startedAtNanos(), Math::min);
            endByInstance.merge(resultado.instanciaPath(), resultado.finishedAtNanos(), Math::max);
        }

        List<String> instanciasOrdenadas = melhorPorInstancia.keySet().stream().sorted().toList();
        try (BufferedWriter writer = Files.newBufferedWriter(finalReportOutput)) {
            writer.write("Instância,Solução ótima (SO),Solução inicial,Solução encontrada (SE),% de desvio em relação à SO,Tempo computacional total");
            writer.newLine();
            for (String instanciaPath : instanciasOrdenadas) {
                ExperimentResult melhor = melhorPorInstancia.get(instanciaPath);
                String nomeInstancia = Path.of(instanciaPath).getFileName().toString();
                Long so = parseLongOrNull(otimos.getProperty(nomeInstancia));
                Solucao inicial = construirSolucaoGulosaInicial(instanciaPorPath.get(instanciaPath));
                String gap = so == null ? "" : String.format(Locale.US, "%.6f%%", ((so - melhor.valorTotal()) / (double) so) * 100.0);
                double tempoTotal = (endByInstance.get(instanciaPath) - startByInstance.get(instanciaPath)) / 1_000_000_000.0;
                writer.write(String.format(Locale.US, "%s,%s,%d,%d,%s,%.4f",
                        nomeInstancia,
                        so == null ? "" : so.toString(),
                        inicial.valorTotal,
                        melhor.valorTotal,
                        gap,
                        tempoTotal));
                writer.newLine();
            }
        }
    }

    private static Long parseLongOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        return Long.parseLong(value.trim());
    }

    private static Solucao construirSolucaoGulosaInicial(Instancia instancia) {
        Integer[] ordem = new Integer[instancia.itens.length];
        for (int i = 0; i < instancia.itens.length; i++) {
            ordem[i] = i;
        }

        java.util.Arrays.sort(ordem, (a, b) -> Double.compare(
                (double) instancia.itens[b].valor / instancia.itens[b].peso,
                (double) instancia.itens[a].valor / instancia.itens[a].peso
        ));

        boolean[] escolhidos = new boolean[instancia.itens.length];
        long pesoAtual = 0;
        long valorAtual = 0;

        for (int indice : ordem) {
            if (pesoAtual + instancia.itens[indice].peso <= instancia.capacidade) {
                escolhidos[indice] = true;
                pesoAtual += instancia.itens[indice].peso;
                valorAtual += instancia.itens[indice].valor;
            }
        }

        return new Solucao(escolhidos, valorAtual, pesoAtual);
    }

    private static Properties carregarProperties(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Ficheiro de propriedades não encontrado: " + path);
        }

        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(path)) {
            properties.load(reader);
        }
        return properties;
    }

    private static List<String> resolverInstancias(Properties p) throws IOException {
        List<String> instancias = parseStringList(p, "mmas.instancias", "mmas.instances");
        if (!instancias.isEmpty()) {
            return instancias;
        }

        String dir = readPropertyFirst(p, "", "mmas.instancias.dir", "mmas.instances.dir").trim();
        if (dir.isEmpty()) {
            throw new IllegalArgumentException("Defina mmas.instancias (ou mmas.instances) ou mmas.instancias.dir (ou mmas.instances.dir)");
        }

        Path base = Path.of(dir);
        if (!Files.isDirectory(base)) {
            throw new IllegalArgumentException("Diretoria de instâncias não existe: " + base);
        }

        List<String> ficheiros = new ArrayList<>();
        try (Stream<Path> paths = Files.list(base)) {
            paths
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .forEach(path -> ficheiros.add(path.toString()));
        }

        if (ficheiros.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma instância encontrada em: " + base);
        }

        return ficheiros;
    }

    private static String readPropertyFirst(Properties p, String fallback, String... keys) {
        for (String key : keys) {
            String value = p.getProperty(key);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return fallback;
    }

    private static List<String> parseStringList(Properties p, String... keys) {
        String value = readPropertyFirst(p, "", keys).trim();
        List<String> parsed = new ArrayList<>();
        if (value.isEmpty()) {
            return parsed;
        }
        for (String token : value.split(",")) {
            String cleaned = token.trim();
            if (!cleaned.isEmpty()) {
                parsed.add(cleaned);
            }
        }
        return parsed;
    }

    private static List<Integer> parseIntList(Properties p, int fallback, String... keys) {
        String value = readPropertyFirst(p, String.valueOf(fallback), keys);
        List<Integer> parsed = new ArrayList<>();
        for (String token : value.split(",")) {
            parsed.add(Integer.parseInt(token.trim()));
        }
        return parsed;
    }

    private static List<Long> parseLongList(Properties p, long fallback, String... keys) {
        String value = readPropertyFirst(p, String.valueOf(fallback), keys);
        List<Long> parsed = new ArrayList<>();
        for (String token : value.split(",")) {
            parsed.add(Long.parseLong(token.trim()));
        }
        return parsed;
    }

    private static List<Double> parseDoubleList(Properties p, double fallback, String... keys) {
        String value = readPropertyFirst(p, String.valueOf(fallback), keys);
        List<Double> parsed = new ArrayList<>();
        for (String token : value.split(",")) {
            parsed.add(Double.parseDouble(token.trim()));
        }
        return parsed;
    }

    private record ExperimentTask(
            String instanciaPath,
            Instancia instancia,
            int ant,
            int iter,
            double alpha,
            double beta,
            double rho,
            double q,
            int stall,
            long seed
    ) {
        ExperimentResult execute() {
            long startedAtNanos = System.nanoTime();

            ACOKnapsack.ParametrosMMAS parametros = new ACOKnapsack.ParametrosMMAS(
                    ant,
                    iter,
                    alpha,
                    beta,
                    rho,
                    q,
                    stall,
                    seed
            );

            Solucao melhor = ACOKnapsack.resolverComParametros(
                    instancia.itens,
                    instancia.capacidade,
                    parametros
            );
            long finishedAtNanos = System.nanoTime();
            long elapsedMs = (finishedAtNanos - startedAtNanos) / 1_000_000L;
            return new ExperimentResult(
                    instanciaPath,
                    instancia.capacidade,
                    instancia.itens.length,
                    ant,
                    iter,
                    alpha,
                    beta,
                    rho,
                    q,
                    stall,
                    seed,
                    melhor.valorTotal,
                    melhor.pesoTotal,
                    elapsedMs,
                    startedAtNanos,
                    finishedAtNanos
            );
        }
    }

    private record ExperimentResult(
            String instanciaPath,
            int capacidade,
            int totalItens,
            int ant,
            int iter,
            double alpha,
            double beta,
            double rho,
            double q,
            int stall,
            long seed,
            long valorTotal,
            long pesoTotal,
            long elapsedMs,
            long startedAtNanos,
            long finishedAtNanos
    ) {
        boolean isBetterThan(ExperimentResult other) {
            if (valorTotal != other.valorTotal) {
                return valorTotal > other.valorTotal;
            }
            if (pesoTotal != other.pesoTotal) {
                return pesoTotal < other.pesoTotal;
            }
            return elapsedMs < other.elapsedMs;
        }
    }
}
