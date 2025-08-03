package com.sid.coursesearch.dto;

import lombok.*;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseSearchResponse {
    private String id;
    private String title;
    private String category;
    private double price;
    private Instant nextSessionDate;
}
