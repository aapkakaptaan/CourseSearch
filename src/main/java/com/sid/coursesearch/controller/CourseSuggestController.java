package com.sid.coursesearch.controller;

import com.sid.coursesearch.service.CourseSuggestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses/search")
@RequiredArgsConstructor
public class CourseSuggestController {
    private final CourseSuggestService suggestService;

    @GetMapping("/suggest")
    public List<String> suggest(@RequestParam("q") String query) throws Exception {
        return suggestService.suggest(query);
    }
}