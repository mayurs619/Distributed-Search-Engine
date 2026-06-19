package com.example.search_api.service;

import com.example.search_api.model.SearchResult;
import com.example.search_api.model.TermPosting;
import com.example.search_api.model.WebDocument;
import com.example.search_api.repository.TermPostingRepository;
import com.example.search_api.repository.WebDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SearchEngineServiceTest {

    @Mock
    private TermPostingRepository indexRepository;

    @Mock
    private WebDocumentRepository documentRepository;

    @InjectMocks
    private SearchEngineService searchEngineService;

    private WebDocument mockDocument;

    @BeforeEach
    void setUp() {
        mockDocument = new WebDocument();
        mockDocument.setId(1L);
        mockDocument.setUrl("https://test.com");
        mockDocument.setTitle("Test Title");
    }

    @Test
    public void testTfIdfCalculation() {
        when(documentRepository.count()).thenReturn(10L, 0L);
        
        TermPosting posting = new TermPosting("algorithm", 1L, 5);
        when(indexRepository.findByTerm("algorithm")).thenReturn(List.of(posting, new TermPosting("algorithm", 2L, 1)));
        when(documentRepository.findById(1L)).thenReturn(Optional.of(mockDocument));

        List<SearchResult> results = searchEngineService.search("algorithm");

        assertEquals(1, results.size(), "Should return exactly one mapped result for Document 1");
        double expectedScore = 5 * Math.log(10.0 / 2.0);
        assertEquals(expectedScore, results.get(0).getScore(), 0.0001, "TF-IDF score did not match mathematical expectation");
    }
}