package com.example.search_api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "inverted_index", indexes = {
    @Index(name = "idx_term", columnList = "term")
})
public class TermPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String term;

    @Column(nullable = false)
    private Long documentId;

    @Column(nullable = false)
    private Integer frequency;

    // Constructors
    public TermPosting() {}

    public TermPosting(String term, Long documentId, Integer frequency) {
        this.term = term;
        this.documentId = documentId;
        this.frequency = frequency;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getTerm() { return term; }
    public Long getDocumentId() { return documentId; }
    public Integer getFrequency() { return frequency; }
}