package com.example.search_api.controller;

import com.example.search_api.model.WebDocument;
import com.example.search_api.repository.WebDocumentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.example.search_api.service.IndexingService;
import com.example.search_api.service.SearchEngineService;
import com.example.search_api.config.DbContextHolder;
import com.example.search_api.model.SearchResult;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    private final WebDocumentRepository repository;
    private final IndexingService indexingService;
    private final SearchEngineService searchEngineService;

    public DocumentController(WebDocumentRepository repository, 
                              IndexingService indexingService, 
                              SearchEngineService searchEngineService) {
        this.repository = repository;
        this.indexingService = indexingService;
        this.searchEngineService = searchEngineService;
    }

    @PostMapping("/index")
    public ResponseEntity<String> indexDocument(@RequestBody WebDocument document) {
        try {
            int shardId = Math.abs(document.getUrl().hashCode()) % 2;
            String targetShard = (shardId == 0) ? "SHARD1" : "SHARD2";
            DbContextHolder.setDbType(targetShard);
            WebDocument savedDoc = repository.save(document);
            indexingService.processDocument(savedDoc); 
            return ResponseEntity.ok("Document indexed in " + targetShard + ": " + document.getUrl());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Skipped or Error: " + e.getMessage());
        } finally {
            DbContextHolder.clearDbType();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<WebDocument>> getAllDocuments() {
        List<WebDocument> documents = repository.findAll();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/search")
    public ResponseEntity<List<SearchResult>> search(@RequestParam String q) {
        List<SearchResult> results = searchEngineService.search(q);
        return ResponseEntity.ok(results);
    }
}