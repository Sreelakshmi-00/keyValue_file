package com.example.KeyValue_FileStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DataStore {
    private final String filePath;
    final Map<String, Entry> store;
    private final ObjectMapper objectMapper;

    private static final long MAX_VALUE_SIZE = 16 * 1024; // 16KB

    private static class Entry {
        private final String value;
        private final Instant expirationTime;

        public Entry(String value, Instant expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }

        public boolean isExpired() {
            return expirationTime != null && Instant.now().isAfter(expirationTime);
        }

        public String getValue() {
            return value;
        }
    }

    @Autowired
    public DataStore(@Value("${datastore.file.path:datastore.json}") String filePath) throws IOException {
        this.filePath = filePath;
        this.store = new ConcurrentHashMap<>();
        this.objectMapper = new ObjectMapper();
        load();
    }


    private void load() throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            Map<String, Entry> loadedStore = objectMapper.readValue(content, store.getClass());
            store.putAll(loadedStore);
        }
    }

    private void save() throws IOException {
        objectMapper.writeValue(new File(filePath), store);
    }

    public synchronized String create(String key, String value, Integer ttlInSeconds) throws IOException {
        if (key.length() > 32 || value.getBytes().length > MAX_VALUE_SIZE) {
            throw new IllegalArgumentException("Key or value exceeds allowed size");
        }

        if (store.containsKey(key) && !store.get(key).isExpired()) {
            throw new IllegalArgumentException("Key already exists");
        }

        Instant expirationTime = ttlInSeconds != null ? Instant.now().plusSeconds(ttlInSeconds) : null;
        store.put(key, new Entry(value, expirationTime));
        save();
        return "Key-Value pair added";
    }

    public String read(String key) {
        Entry entry = store.get(key);
        if (entry == null || entry.isExpired()) {
            store.remove(key); // Auto-delete expired keys
            return null;
        }
        return entry.getValue();
    }

    public synchronized String delete(String key) throws IOException {
        Entry entry = store.get(key);
        if (entry == null || entry.isExpired()) {
            return "Key not found or expired";
        }
        store.remove(key);
        save();
        return "Key deleted";
    }

    public synchronized String batchCreate(Map<String, String> keyValuePairs, Integer ttlInSeconds) throws IOException {
        if (keyValuePairs.size() > 100) {
            throw new IllegalArgumentException("Batch size exceeds the limit of 100");
        }
        for (Map.Entry<String, String> entry : keyValuePairs.entrySet()) {
            create(entry.getKey(), entry.getValue(), ttlInSeconds);
        }
        return "Batch created";
    }
}
