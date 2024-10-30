package com.example.KeyValue_FileStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

public class DataStoreTest {
    private DataStore dataStore;
    private final String testFilePath = "test_datastore.json";

    @BeforeEach
    public void setUp() throws IOException {
        // Initialize the DataStore before each test
        dataStore = new DataStore(testFilePath);
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Clean up the test file after each test
        Files.deleteIfExists(Paths.get(testFilePath));
    }

    @Test
    public void testCreateAndRead() throws IOException {
        String result = dataStore.create("testKey", "{\"name\":\"example\"}", 60);
        assertEquals("Key-Value pair added", result);

        String value = dataStore.read("testKey");
        assertEquals("{\"name\":\"example\"}", value);
    }

    @Test
    public void testDuplicateKeyError() throws IOException {
        dataStore.create("testKey", "{\"name\":\"example\"}", 60);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            dataStore.create("testKey", "{\"name\":\"newExample\"}", 60);
        });
        assertEquals("Key already exists", exception.getMessage());
    }

    @Test
    public void testExpiration() throws InterruptedException, IOException {
        dataStore.create("testKey", "{\"name\":\"example\"}", 1); // TTL 1 second
        Thread.sleep(2000); // Wait for 2 seconds
        String value = dataStore.read("testKey");
        assertNull(value); // Should be expired
    }

    @Test
    public void testBatchCreate() throws IOException {
        Map<String, String> keyValuePairs = new HashMap<>();
        keyValuePairs.put("key1", "{\"name\":\"value1\"}");
        keyValuePairs.put("key2", "{\"name\":\"value2\"}");

        String result = dataStore.batchCreate(keyValuePairs, 60);
        assertEquals("Batch created", result);

        assertEquals("{\"name\":\"value1\"}", dataStore.read("key1"));
        assertEquals("{\"name\":\"value2\"}", dataStore.read("key2"));
    }

    @Test
    public void testConcurrentAccess() throws InterruptedException {
        int numberOfThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    dataStore.create("testKey" + index, "{\"name\":\"value" + index + "\"}", 60);
                } catch (IOException e) {
                    fail("Exception thrown during concurrent access: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // Wait for all threads to finish
        assertEquals(numberOfThreads, dataStore.store.size()); // All keys should be stored
    }

    @Test
    public void testDelete() throws IOException {
        dataStore.create("testKey", "{\"name\":\"example\"}", 60);
        String deleteResult = dataStore.delete("testKey");
        assertEquals("Key deleted", deleteResult);

        String value = dataStore.read("testKey");
        assertNull(value); // Key should be deleted
    }

    @Test
    public void testDeleteExpiredKey() throws IOException, InterruptedException {
        dataStore.create("testKey", "{\"name\":\"example\"}", 1);
        Thread.sleep(2000); // Wait for 2 seconds
        String deleteResult = dataStore.delete("testKey");
        assertEquals("Key not found or expired", deleteResult);
    }

    @Test
    public void testBatchCreateErrorHandling() {
        Map<String, String> keyValuePairs = new HashMap<>();
        for (int i = 0; i < 101; i++) {
            keyValuePairs.put("key" + i, "{\"name\":\"value" + i + "\"}");
        }

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            dataStore.batchCreate(keyValuePairs, 60);
        });
        assertEquals("Batch size exceeds the limit of 100", exception.getMessage());
    }
}