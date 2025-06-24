package com.example.audioservice.model.DTO;

import lombok.Data;

import java.util.List;

@Data
public class WordInfo {
    private int index;
    private List<String> acceptableAnswers;
}
