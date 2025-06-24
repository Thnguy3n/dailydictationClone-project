package com.example.audioservice.model.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssemblyResponse {
    private String text;
    private List<AssemblyWordInfoResponse> words;
}
