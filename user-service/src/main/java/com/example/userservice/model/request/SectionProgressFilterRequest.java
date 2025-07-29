package com.example.userservice.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionProgressFilterRequest {
    private String username;
    private List<Long> sectionIds;
    private String progressFilter;
}