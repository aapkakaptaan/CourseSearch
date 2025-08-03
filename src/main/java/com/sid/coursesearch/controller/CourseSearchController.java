package com.sid.coursesearch.controller;

import com.sid.coursesearch.dto.PagedCourseResponse;
import com.sid.coursesearch.service.CourseSearchService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/courses/search")
@RequiredArgsConstructor
public class CourseSearchController {

    private final CourseSearchService searchService;

    @GetMapping
    public PagedCourseResponse searchCourses(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) Integer minAge,
        @RequestParam(required = false) Integer maxAge,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
        @RequestParam(defaultValue = "upcoming") String sort,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) throws Exception {
        return searchService.searchCourses(
            q, minAge, maxAge, category, type,
            minPrice, maxPrice, startDate, sort, page, size
        );
    }
}
