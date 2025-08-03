package com.sid.coursesearch.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PagedCourseResponse {
    private long total;
    private List<CourseSearchResponse> courses;
}
