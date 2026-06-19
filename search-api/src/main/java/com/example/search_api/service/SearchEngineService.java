package com.example.search_api.service;

import com.example.search_api.model.SearchResult;
import com.example.search_api.model.TermPosting;
import com.example.search_api.model.WebDocument;
import com.example.search_api.repository.TermPostingRepository;
import com.example.search_api.repository.WebDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchEngineService {

    private final TermPostingRepository indexRepository;
    private final WebDocumentRepository documentRepository;

    public SearchEngineService(TermPostingRepository indexRepository, WebDocumentRepository documentRepository) {
        this.indexRepository = indexRepository;
        this.documentRepository = documentRepository;
    }

    /**
     * Search across shards and return ranked results for the query.
     */
    public List<SearchResult> search(String query) {
        String cleanQuery = query.toLowerCase().replaceAll("[^a-z0-9\\s]", "");
        String[] terms = cleanQuery.split("\\s+");

        List<SearchResult> finalResults = new ArrayList<>();
        String[] shards = {"SHARD1", "SHARD2"};

        for (String shard : shards) {
            com.example.search_api.config.DbContextHolder.setDbType(shard);
            
            try {
                long totalDocs = documentRepository.count();
                if (totalDocs == 0) continue;

                Map<Long, Double> shardScores = new HashMap<>();

                for (String term : terms) {
                    if (term.length() <= 2) continue;
                    
                    List<TermPosting> postings = indexRepository.findByTerm(term);
                    long docFrequency = postings.size();
                    if (docFrequency == 0) continue;

                    double idf = Math.log((double) totalDocs / docFrequency);

                    for (TermPosting posting : postings) {
                        double tf = posting.getFrequency();
                        shardScores.put(posting.getDocumentId(), 
                            shardScores.getOrDefault(posting.getDocumentId(), 0.0) + (tf * idf));
                    }
                }

                for (Map.Entry<Long, Double> entry : shardScores.entrySet()) {
                    WebDocument doc = documentRepository.findById(entry.getKey()).orElse(null);
                    if (doc != null) {
                        String displayTitle = doc.getTitle().length() > 60 ? 
                                doc.getTitle().substring(0, 57) + "..." : doc.getTitle();
                        finalResults.add(new SearchResult(doc.getUrl(), "[" + shard + "] " + displayTitle, entry.getValue()));
                    }
                }
            } finally {
                com.example.search_api.config.DbContextHolder.clearDbType();
            }
        }

        return finalResults.stream()
                .sorted(Comparator.comparingDouble(SearchResult::getScore).reversed())
                .limit(15)
                .collect(Collectors.toList());
    }
}