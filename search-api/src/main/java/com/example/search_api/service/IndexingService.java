package com.example.search_api.service;

import com.example.search_api.model.TermPosting;
import com.example.search_api.model.WebDocument;
import com.example.search_api.repository.TermPostingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class IndexingService {

    private final TermPostingRepository indexRepository;

    public IndexingService(TermPostingRepository indexRepository) {
        this.indexRepository = indexRepository;
    }

    /**
     * Process the document content into term postings and persist the index entries.
     */
    @Transactional
    public void processDocument(WebDocument document) {
        if (document.getContent() == null || document.getContent().isEmpty()) return;

        String cleanText = document.getContent().toLowerCase().replaceAll("[^a-z0-9\\s]", "");
        String[] words = cleanText.split("\\s+");

        Map<String, Integer> wordCount = new HashMap<>();
        for (String word : words) {
            if (word.length() > 2) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }

        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            TermPosting posting = new TermPosting(entry.getKey(), document.getId(), entry.getValue());
            indexRepository.save(posting);
        }
        
        System.out.println("Indexed " + wordCount.size() + " unique terms for Document ID: " + document.getId());
    }
}