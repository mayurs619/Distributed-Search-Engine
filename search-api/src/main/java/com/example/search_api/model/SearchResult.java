package com.example.search_api.model;

public class SearchResult {
    private String url;
    private String title;
    private double score;

    public SearchResult(String url, String title, double score) {
        this.url = url;
        this.title = title;
        this.score = score;
    }

    // Getters are required for Spring Boot to convert this to JSON
    public String getUrl() { return url; }
    public String getTitle() { return title; }
    public double getScore() { return score; }
}