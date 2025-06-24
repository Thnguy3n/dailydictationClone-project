package com.example.audioservice.model.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WordData {
    private List<WordInfo> words;
}
