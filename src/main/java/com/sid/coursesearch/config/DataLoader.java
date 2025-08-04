package com.sid.coursesearch.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sid.coursesearch.document.CourseDocument;
import com.sid.coursesearch.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final CourseRepository courseRepository;

    @Override
    public void run(String... args) throws Exception {
        if (courseRepository.count() == 0) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule()); // <-- Fix here!
            try (InputStream is = getClass().getResourceAsStream("/sample-courses.json")) {
                List<CourseDocument> courses = mapper.readValue(is, new TypeReference<>() {});
                for (CourseDocument c : courses) {
                    c.setSuggest(new String[]{c.getTitle()});
                }
                courseRepository.saveAll(courses);
                System.out.println("------------✔ Successfully indexed " + courses.size() + " courses into Elasticsearch");
            }
        }
    }
}