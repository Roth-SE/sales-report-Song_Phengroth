package edu.itc.salesreport.ingest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MonthLoaderTest {

    @Test
    void firesCorrectEventsAndTracksRows(@TempDir Path tempDir) throws Exception {
        Path monthDir = tempDir.resolve("2026-09");
        Files.createDirectory(monthDir);
        
        Files.writeString(monthDir.resolve("A-valid.csv"), CsvTransactionParser.HEADER + "\n" +
                "PNH,2026-09-01,R1,S1,P1,Grocery,1,10.00,0.00,CASH\n");
        Files.writeString(monthDir.resolve("B-invalid.csv"), CsvTransactionParser.HEADER + "\n" +
                "PNH,2026-09-01,R2,S1,P1,Grocery,-1,10.00,0.00,CASH\n");

        MonthLoader loader = new MonthLoader(new CsvTransactionParser());
        List<LoadEvent> events = new ArrayList<>();
        loader.subscribe(events::add);

        loader.load(YearMonth.of(2026, 9), tempDir);

        assertEquals(3, events.size());
        assertInstanceOf(LoadEvent.FileLoaded.class, events.get(0));
        assertInstanceOf(LoadEvent.FileLoaded.class, events.get(1));
        
        LoadEvent.LoadFinished finish = (LoadEvent.LoadFinished) events.get(2);
        assertEquals(1, finish.accepted());
        assertEquals(1, finish.rejected());
        
        assertEquals(1, loader.rejectedRows().size());
        assertTrue(loader.rejectedRows().get(0).contains("B-invalid.csv:2"));
    }

    @Test
    void unsubscribedListenerReceivesNothing(@TempDir Path tempDir) throws Exception {
        Path monthDir = tempDir.resolve("2026-09");
        Files.createDirectory(monthDir);
        Files.writeString(monthDir.resolve("A-valid.csv"), CsvTransactionParser.HEADER + "\n");

        MonthLoader loader = new MonthLoader(new CsvTransactionParser());
        List<LoadEvent> events = new ArrayList<>();
        
        Runnable unsubscribe = loader.subscribe(events::add);
        unsubscribe.run(); 

        loader.load(YearMonth.of(2026, 9), tempDir);
        assertEquals(0, events.size(), "Unsubscribed listener should not receive events");
    }
}