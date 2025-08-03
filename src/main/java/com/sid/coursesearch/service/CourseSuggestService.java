package com.sid.coursesearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseSuggestService {
    private final ElasticsearchClient elasticsearchClient;

    public List<String> suggest(String prefix) throws Exception {
        SearchResponse<Void> response = elasticsearchClient.search(s -> s
            .index("courses")
            .suggest(sug -> sug
                .suggesters("title-suggest", s1 -> s1
                    .prefix(prefix)
                    .completion(c -> c
                        .field("suggest")
                        .skipDuplicates(true)
                        .size(10)
                    )
                )
            ),
            Void.class
        );

        // The Suggest API returns a map: Map<String, List<Suggestion<Void>>>
        List<Suggestion<Void>> suggestions = response.suggest() != null
            ? response.suggest().get("title-suggest")
            : Collections.emptyList();

        if (suggestions == null) return List.of();

        return suggestions.stream()
            .flatMap(sugg -> sugg.completion() != null ? sugg.completion().options().stream() : null)
            .map(CompletionSuggestOption::text)
            .distinct()
            .collect(Collectors.toList());
    }
}