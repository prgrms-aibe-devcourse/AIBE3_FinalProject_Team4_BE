package com.back.domain.blog.blogdoc.repository;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.back.domain.blog.blogdoc.document.BlogDoc;
import com.back.domain.blog.blogdoc.document.BlogSortType;
import com.back.domain.blog.blogdoc.dto.BlogSearchCondition;
import com.back.domain.blog.blogdoc.dto.BlogSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BlogDocQueryRepositoryImpl implements BlogDocQueryRepository {
    private final ElasticsearchOperations operations;

    @Override
    public BlogSearchResult searchBlogs(BlogSearchCondition condition, @Nullable List<Long> authorIds, @Nullable List<Query> recommendQueries) {
        List<SortOptions> sorts = buildSorts(condition.sortType());
        List<Object> searchAfter = parseCursor(condition.cursor());

        Query esQuery = buildQuery(condition, authorIds, recommendQueries);
        var builder = NativeQuery.builder()
                .withQuery(esQuery)
                .withPageable(PageRequest.of(0, condition.size()));

        if (sorts != null && !sorts.isEmpty()) {
            builder.withSort(sorts);
        }

        if (searchAfter != null && !searchAfter.isEmpty()) {
            builder.withSearchAfter(searchAfter);
        }
        NativeQuery nativeQuery = builder.build();
        SearchHits<BlogDoc> hits = operations.search(nativeQuery, BlogDoc.class);
        List<SearchHit<BlogDoc>> searchHits = hits.getSearchHits();
        List<BlogDoc> docs = searchHits.stream()
                .map(SearchHit::getContent)
                .toList();

        // 무한스크롤 페이징 처리
        boolean hasNext = docs.size() == condition.size();
        String nextCursor = null;
        if (hasNext && !searchHits.isEmpty()) {
            List<Object> lastSortValues = searchHits.get(searchHits.size() - 1).getSortValues();
            nextCursor = toCursor(lastSortValues);
        }

        return new BlogSearchResult(docs, hasNext, nextCursor);
    }

    private String normalizeHashtagKeyword(String keyword) {
        String k = keyword.trim();
        if (k.startsWith("#")) {
            k = k.substring(1);
        }
        return k;
    }
    
    private Query buildQuery(BlogSearchCondition condition, @Nullable List<Long> authorIds, @Nullable List<Query> recommendQueries) {
        String keyword = condition.keyword();
        return Query.of(q -> q
                .bool(b -> {
                    // 추천순 쿼리
                    if (recommendQueries != null && !recommendQueries.isEmpty()) {
                        b.should(recommendQueries);
                        b.minimumShouldMatch("0");
                    }
                    //키워드 검색
                    if (keyword != null && !keyword.isBlank()) {

                        String normalizedKeyword = normalizeHashtagKeyword(keyword);

                        // 제목/내용 검색
                        b.should(s -> s.multiMatch(mm -> mm
                                .fields("title", "content")
                                .query(keyword)
                        ));

                        // 해시태그 정확 매치
                        b.should(s -> s.terms(t -> t
                                .field("hashtagName")
                                .terms(tv -> tv.value(List.of(FieldValue.of(normalizedKeyword))))
                        ));

                        // ▶ 제목/내용 또는 해시태그 둘 중 하나는 매치해야 함
                        b.minimumShouldMatch("1");
                    } else {
                        b.must(m -> m.matchAll(ma -> ma));
                    }
                    //published 상태 필터링
                    b.filter(f -> f.term(t -> t
                            .field("status")
                            .value("PUBLISHED")
                    ));
                    // 팔로잉 필터링
                    if (authorIds != null && !authorIds.isEmpty()) {
                        List<FieldValue> authorFieldValues = authorIds.stream()
                                .map(FieldValue::of)
                                .toList();
                        b.filter(f -> f.terms(t -> t
                                .field("userId")
                                .terms(tv -> tv.value(authorFieldValues))
                        ));
                    }
                    return b;
                })
        );
    }

    private List<SortOptions> buildSorts(BlogSortType sortType) {
        if (sortType == null) sortType = BlogSortType.LATEST;
        return switch (sortType) {
            case LATEST -> List.of(
                    SortOptions.of(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc))),
                    SortOptions.of(s -> s.field(f -> f.field("id").order(SortOrder.Desc)))
            );
            case VIEWS -> List.of(
                    SortOptions.of(s -> s.field(f -> f.field("viewCount").order(SortOrder.Desc))),
                    SortOptions.of(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc))),
                    SortOptions.of(s -> s.field(f -> f.field("id").order(SortOrder.Desc)))
            );
            case POPULAR -> List.of(
                    SortOptions.of(s -> s.field(f -> f.field("likeCount").order(SortOrder.Desc))),
                    SortOptions.of(s -> s.field(f -> f.field("bookmarkCount").order(SortOrder.Desc))),
                    SortOptions.of(s -> s.field(f -> f.field("viewCount").order(SortOrder.Desc))),
                    SortOptions.of(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc))),
                    SortOptions.of(s -> s.field(f -> f.field("id").order(SortOrder.Desc)))
            );
            case RECOMMEND -> List.of();
        };
    }

    private List<Object> parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) return null;

        String[] parts = cursor.split(",");
        if (parts.length != 2) return null;

        String sortVal = parts[0];
        Long id = Long.parseLong(parts[1]);

        List<Object> list = new ArrayList<>();
        list.add(sortVal);
        list.add(id);
        return list;
    }

    private String toCursor(List<Object> searchAfter) {
        if (searchAfter == null || searchAfter.size() < 2) return null;
        Object sortVal = searchAfter.get(0);
        Object idVal = searchAfter.get(1);
        return sortVal + "," + idVal;
    }
}