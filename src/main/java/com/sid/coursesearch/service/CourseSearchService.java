package com.sid.coursesearch.service;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;

import com.sid.coursesearch.document.CourseDocument;
import com.sid.coursesearch.dto.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CourseSearchService {

    private final ElasticsearchClient elasticsearchClient;

    public PagedCourseResponse searchCourses(
            String keyword,
            Integer minAge, Integer maxAge,
            String category, String type,
            Double minPrice, Double maxPrice,
            Instant startDate,
            String sort,
            int page, int size
    ) throws Exception {

        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();

        // 🔍 Full-text match with fuzziness for title
        if (keyword != null && !keyword.isBlank()) {
            mustQueries.add(Query.of(q -> q.bool(b -> b.should(
                List.of(
                    Query.of(q1 -> q1.match(m -> m.field("title").query(keyword).fuzziness("AUTO"))),
                    Query.of(q2 -> q2.match(m -> m.field("description").query(keyword)))
                )
            ))));
        }

        // 🎯 Exact filters
        if (category != null) {
            filterQueries.add(Query.of(q -> q.term(t -> t.field("category").value(category))));
        }
        if (type != null) {
            filterQueries.add(Query.of(q -> q.term(t -> t.field("type").value(type))));
        }

        // 🎯 Age Range Filter
        if (minAge != null || maxAge != null) {
            RangeQuery.Builder rangeBuilder = new RangeQuery.Builder()
                .field("minAge");
            if (minAge != null) {
                rangeBuilder.gte(JsonData.of(minAge));
            }
            if (maxAge != null) {
                rangeBuilder.lte(JsonData.of(maxAge));
            }
            filterQueries.add(Query.of(q -> q.range(rangeBuilder.build())));
        }

        // 💰 Price Range Filter
        if (minPrice != null || maxPrice != null) {
            RangeQuery.Builder rangeBuilder = new RangeQuery.Builder()
                .field("price");
            if (minPrice != null) {
                rangeBuilder.gte(JsonData.of(minPrice));
            }
            if (maxPrice != null) {
                rangeBuilder.lte(JsonData.of(maxPrice));
            }
            filterQueries.add(Query.of(q -> q.range(rangeBuilder.build())));
        }

        // 🗓️ Start Date Filter
        if (startDate != null) {
            filterQueries.add(Query.of(q -> q.range(r -> r
                .field("nextSessionDate")
                .gte(JsonData.of(startDate))
            )));
        }

        // 🧠 Final query
        Query finalQuery = Query.of(q -> q.bool(b -> b
            .must(mustQueries)
            .filter(filterQueries)
        ));

        // 🧮 Sorting
        List<SortOptions> sortOptions = new ArrayList<>();
        switch (sort) {
            case "priceAsc" -> sortOptions.add(SortOptions.of(s -> s.field(f -> f.field("price").order(SortOrder.Asc))));
            case "priceDesc" -> sortOptions.add(SortOptions.of(s -> s.field(f -> f.field("price").order(SortOrder.Desc))));
            default -> sortOptions.add(SortOptions.of(s -> s.field(f -> f.field("nextSessionDate").order(SortOrder.Asc))));
        }

        // 🚀 Run search
        SearchResponse<CourseDocument> response = elasticsearchClient.search(s -> s
                .index("courses")
                .query(finalQuery)
                .sort(sortOptions)
                .from(page * size)
                .size(size),
            CourseDocument.class
        );

        List<CourseSearchResponse> hits = response.hits().hits().stream()
            .map(Hit::source)
            .filter(Objects::nonNull)
            .map(course -> CourseSearchResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .category(course.getCategory())
                .price(course.getPrice())
                .nextSessionDate(course.getNextSessionDate())
                .build())
            .toList();

        return PagedCourseResponse.builder()
                .total(response.hits().total().value())
                .courses(hits)
                .build();
    }
}