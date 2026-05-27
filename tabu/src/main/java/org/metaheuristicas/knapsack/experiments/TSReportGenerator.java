package org.metaheuristicas.knapsack.experiments;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Gera automaticamente um relatório Markdown (com número sequencial) com os melhores 
 * resultados da grelha de testes do Tabu Search.
 */
public class TSReportGenerator {

    private static final Map<String, Long> OPTIMA = new HashMap<>();
    private static final Map<String, Long> INITIAL_SOLUTIONS = new HashMap<>();

    static {
        OPTIMA.put("n_1000_1", 9999946233L);
        OPTIMA.put("n_1000_2", 9999964987L);
        OPTIMA.put("n_1000_3", 9999229281L);
        OPTIMA.put("n_1000_4", 9999239905L);
        OPTIMA.put("n_1000_5", 9999251796L);
        OPTIMA.put("n_1000_6", 9996100344L);
        OPTIMA.put("n_1000_7", 9996105266L);
        OPTIMA.put("n_1000_8", 9996111502L);
        OPTIMA.put("n_1000_9", 9980488131L);
        OPTIMA.put("n_1000_10", 9980507700L);

        INITIAL_SOLUTIONS.put("n_1000_1", 9981765208L);
        INITIAL_SOLUTIONS.put("n_1000_2", 9987797016L);
        INITIAL_SOLUTIONS.put("n_1000_3", 9981260200L);
        INITIAL_SOLUTIONS.put("n_1000_4", 9961739503L);
        INITIAL_SOLUTIONS.put("n_1000_5", 9930500811L);
        INITIAL_SOLUTIONS.put("n_1000_6", 9312506545L);
        INITIAL_SOLUTIONS.put("n_1000_7", 9507823943L);
        INITIAL_SOLUTIONS.put("n_1000_8", 9410173763L);
        INITIAL_SOLUTIONS.put("n_1000_9", 9980484517L);
        INITIAL_SOLUTIONS.put("n_1000_10", 9980502348L);
    }

    private static class BestResult {
        long bestValue = -1;
        long pesoTotal = -1;
        String config = "";
        long timeMs = -1;
        long totalGridTimeMs = 0;
    }

    public static void gerarRelatorio(Path csvPath) {
        if (!Files.exists(csvPath)) {
            System.err.println("Aviso: Ficheiro CSV não encontrado em " + csvPath + ". O relatório não pode ser gerado.");
            return;
        }

        Map<String, BestResult> bestResults = new TreeMap<>(); // TreeMap para ordenar alfabeticamente

        try (BufferedReader br = Files.newBufferedReader(csvPath)) {
            String line = br.readLine(); // ignorar header
            if (line == null) return;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length < 10) continue;

                // parts (novo): [0]instance, [1]iterations, [2]tenure_flip, [3]tenure_swap, [4]stall, [5]diversify, [6]seed, [7]best_value, [8]total_weight, [9]elapsed_ms, [10]started_at_epoch_ms, [11]ended_at_epoch_ms, [12]grid_wallclock_ms
                // parts (legado): [0]instance, [1]iterations, [2]tenure_flip, [3]tenure_swap, [4]stall, [5]diversify, [6]seed, [7]best_value, [8]total_weight, [9]elapsed_ms, [10]grid_wallclock_ms
                String instancePath = parts[0].replace("\\", "/");
                String[] pathParts = instancePath.split("/");
                String instName = pathParts[pathParts.length - 1];

                long bestValue = Long.parseLong(parts[7]);
                long pesoTotal = Long.parseLong(parts[8]);
                long timeMs = Long.parseLong(parts[9]);
                long gridWallclockMs;
                if (parts.length > 12) {
                    gridWallclockMs = Long.parseLong(parts[12]);
                } else if (parts.length > 10) {
                    gridWallclockMs = Long.parseLong(parts[10]);
                } else {
                    gridWallclockMs = 0;
                }

                BestResult currentBest = bestResults.get(instName);
                if (currentBest == null) {
                    currentBest = new BestResult();
                    bestResults.put(instName, currentBest);
                }
                
                // Usar o tempo wall-clock medido no runner
                currentBest.totalGridTimeMs = gridWallclockMs;
                
                boolean isBetter = false;
                if (currentBest.bestValue == -1) {
                    isBetter = true;
                } else if (bestValue > currentBest.bestValue) {
                    isBetter = true;
                } else if (bestValue == currentBest.bestValue) {
                    if (pesoTotal < currentBest.pesoTotal) {
                        isBetter = true;
                    } else if (pesoTotal == currentBest.pesoTotal && timeMs < currentBest.timeMs) {
                        isBetter = true;
                    }
                }

                if (isBetter) {
                    currentBest.bestValue = bestValue;
                    currentBest.pesoTotal = pesoTotal;
                    currentBest.timeMs = timeMs;
                    // format config params carefully
                    currentBest.config = String.format(Locale.US, "iters=%s, tFlip=%s, tSwap=%s, stall=%s, div=%s",
                            parts[1], parts[2], parts[3], parts[4], parts[5]);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao processar CSV para relatório: " + e.getMessage());
            return;
        }

        StringBuilder md = new StringBuilder();
        md.append("# Relatório: Pesquisa Tabu (Tabu Search) - Problema da Mochila 0/1\n\n");
        md.append("Este ficheiro foi gerado automaticamente após a execução da grelha de testes.\n\n");
        md.append("## Resultados Consolidados (Melhores Configurações)\n\n");
        md.append("| Instância | Solução Ótima (SO) | Solução Inicial | Solução Encontrada (SE) | Desvio (GAP %) | Tempo Grid (s) | Tempo Melhor (ms) | Configuração Vencedora (TS) |\n");
        md.append("| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |\n");

        for (Map.Entry<String, BestResult> entry : bestResults.entrySet()) {
            String inst = entry.getKey();
            BestResult r = entry.getValue();

            long so = OPTIMA.getOrDefault(inst, -1L);
            long init = INITIAL_SOLUTIONS.getOrDefault(inst, -1L);

            double gap = 0.0;
            if (so > 0) {
                gap = (double) (so - r.bestValue) / so * 100.0;
            }

            String soStr = so == -1 ? "N/A" : String.valueOf(so);
            String initStr = init == -1 ? "N/A" : String.valueOf(init);

            md.append(String.format(Locale.US, "| **%s** | %s | %s | %d | %.8f%% | %.3f s | %d ms | `%s` |\n",
                    inst, soStr, initStr, r.bestValue, gap, r.totalGridTimeMs / 1000.0, r.timeMs, r.config));
        }

        md.append("\n*Nota: O Desvio (GAP %) é calculado como `(SO - SE) / SO * 100`.*\n");

        // Encontrar nome de ficheiro sequencial: ts-relatorio_1.md, ts-relatorio_2.md, etc.
        int seq = 1;
        Path reportPath;
        Path parentDir = csvPath.getParent();
        if (parentDir == null) parentDir = Path.of(".");
        
        while (true) {
            reportPath = parentDir.resolve("ts-relatorio_" + seq + ".md");
            if (!Files.exists(reportPath)) {
                break;
            }
            seq++;
        }

        try {
            Files.createDirectories(reportPath.getParent());
            try (BufferedWriter bw = Files.newBufferedWriter(reportPath)) {
                bw.write(md.toString());
            }
            System.out.println("Relatório gerado com sucesso em: " + reportPath);
        } catch (IOException e) {
            System.err.println("Erro ao gravar ficheiro de relatório: " + e.getMessage());
        }
    }
}
