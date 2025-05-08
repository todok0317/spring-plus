package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    @Query(
            value = "SELECT t FROM Todo t " +
                    "JOIN FETCH t.user " +
                    "WHERE (:weather IS NULL OR t.weather = :weather) " +
                    "AND (:startDate IS NULL OR t.modifiedAt >= :startDate) " +
                    "AND (:endDate IS NULL OR t.modifiedAt <= :endDate)",
            countQuery = "SELECT COUNT(t) FROM Todo t " +
                    "WHERE (:weather IS NULL OR t.weather = :weather) " +
                    "AND (:startDate IS NULL OR t.modifiedAt >= :startDate) " +
                    "AND (:endDate IS NULL OR t.modifiedAt <= :endDate)"
    )
    Page<Todo> searchTodos(
            @Param("weather") String weather,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );


}
