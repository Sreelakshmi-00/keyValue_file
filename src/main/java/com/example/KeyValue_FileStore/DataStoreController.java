package com.example.KeyValue_FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/datastore")
public class DataStoreController {
    private final DataStoreService dataStoreService;

    @Autowired
    public DataStoreController(DataStoreService dataStoreService) {
        this.dataStoreService = dataStoreService;
    }

    @PostMapping("/create")
    public String create(@RequestParam String key, @RequestParam String value, @RequestParam(required = false) Integer ttl) throws IOException {
        return dataStoreService.create(key, value, ttl);
    }

    @GetMapping("/read")
    public String read(@RequestParam String key) {
        return dataStoreService.read(key);
    }

    @DeleteMapping("/delete")
    public String delete(@RequestParam String key) throws IOException {
        return dataStoreService.delete(key);
    }

    @PostMapping("/batchCreate")
    public String batchCreate(@RequestBody Map<String, String> keyValuePairs, @RequestParam(required = false) Integer ttl) throws IOException {
        return dataStoreService.batchCreate(keyValuePairs, ttl);
    }
}
