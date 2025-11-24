package com.back.domain.search.searchKeyword.repository;

import com.back.domain.search.searchKeyword.entity.SearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {

    List<SearchKeyword> findTop10ByModifiedAtGreaterThanEqualOrderBySearchCountDesc(LocalDateTime modifiedAtIsGreaterThan);

    List<SearchKeyword> findTop10ByKeywordContainingIgnoreCaseOrderBySearchCountDesc(String part);

    Optional<SearchKeyword> findByKeyword(String keyword);
}
