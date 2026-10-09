package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.SaleTransaction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public final class MonthLoader {

    private final TransactionParser parser;
    private final List<LoadListener> listeners = new CopyOnWriteArrayList<>();
    private final List<String> rejectedRows = new ArrayList<>();

    public MonthLoader(TransactionParser parser) {
        this.parser = parser;
    }

    public Runnable subscribe(LoadListener listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public List<String> rejectedRows() {
        return List.copyOf(rejectedRows);
    }

    public List<SaleTransaction> load(YearMonth month, Path dataDir) {
        long start = System.nanoTime();
        List<SaleTransaction> allTransactions = new ArrayList<>();
        Path monthDir = dataDir.resolve(month.toString());

        if (!Files.exists(monthDir)) {
            return allTransactions;
        }

        try (Stream<Path> stream = Files.list(monthDir)) {
            List<Path> files = stream.filter(p -> p.toString().endsWith(".csv"))
                    .sorted()
                    .toList();
            
            int totalFiles = files.size();
            int filesDone = 0;
            int totalAccepted = 0;
            int totalRejected = 0;

            for (Path file : files) {
                filesDone++;
                int accepted = 0;
                int rejected = 0;

                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                // Start from index 1 to skip header
                for (int i = 1; i < lines.size(); i++) {
                    try {
                        allTransactions.add(parser.parse(lines.get(i)));
                        accepted++;
                    } catch (InvalidRowException e) {
                        rejectedRows.add(file.getFileName() + ":" + (i + 1) + " " + e.getMessage());
                        rejected++;
                    }
                }
                totalAccepted += accepted;
                totalRejected += rejected;
                publish(new LoadEvent.FileLoaded(file, accepted, rejected, filesDone, totalFiles));
            }

            Duration took = Duration.ofNanos(System.nanoTime() - start);
            publish(new LoadEvent.LoadFinished(totalAccepted, totalRejected, took));

        } catch (IOException e) {
            throw new RuntimeException("Failed to read data directory", e);
        }

        return allTransactions;
    }

    private void publish(LoadEvent event) {
        listeners.forEach(l -> l.onEvent(event));
    }
}