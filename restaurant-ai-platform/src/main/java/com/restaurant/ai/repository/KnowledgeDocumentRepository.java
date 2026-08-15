package com.restaurant.ai.repository;

import com.restaurant.ai.entity.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, UUID> {

    List<KnowledgeDocument> findByRestaurantIdAndActiveTrue(UUID restaurantId);

    /**
     * Full-text search against the GIN index on to_tsvector(title || ' ' || content)
     * created in V1__init_schema.sql. plainto_tsquery is used (not to_tsquery) so
     * arbitrary customer phrasing never throws a tsquery syntax error. Ranked by
     * relevance, restricted to this restaurant's active documents only.
     */
    @Query(value = """
            SELECT * FROM knowledge_documents
            WHERE restaurant_id = :restaurantId
              AND active = true
              AND to_tsvector('english', title || ' ' || content) @@ plainto_tsquery('english', :searchText)
            ORDER BY ts_rank(to_tsvector('english', title || ' ' || content), plainto_tsquery('english', :searchText)) DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<KnowledgeDocument> searchByText(@Param("restaurantId") UUID restaurantId,
                                          @Param("searchText") String searchText,
                                          @Param("limit") int limit);
}
