package com.example.search_api.repository; // Adjust if your package name is different

import com.example.search_api.model.WebDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebDocumentRepository extends JpaRepository<WebDocument, Long> {
}
