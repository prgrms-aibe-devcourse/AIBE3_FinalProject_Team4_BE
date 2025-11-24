package com.back.domain.search.searchHistory.repository;

import com.back.domain.search.searchHistory.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    List<SearchHistory> findTop10ByUserIdOrderByCreatedAtDesc(Long userId); //최근 10개만 조회
}
