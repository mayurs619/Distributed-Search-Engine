package com.example.search_api.repository;

import com.example.search_api.model.TermPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TermPostingRepository extends JpaRepository<TermPosting, Long> {
    List<TermPosting> findByTerm(String term);
}