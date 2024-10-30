package com.example.KeyValue_FileStore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class DataStoreService {
    private final DataStore dataStore;

    @Autowired
    public DataStoreService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public String create(String key, String value, Integer ttlInSeconds) throws IOException {
        return dataStore.create(key, value, ttlInSeconds);
    }

    public String read(String key) {
        return dataStore.read(key);
    }

    public String delete(String key) throws IOException {
        return dataStore.delete(key);
    }

    public String batchCreate(Map<String, String> keyValuePairs, Integer ttlInSeconds) throws IOException {
        return dataStore.batchCreate(keyValuePairs, ttlInSeconds);
    }
}
