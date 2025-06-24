package com.example.audioservice.model.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DetailedTranscriptResponse {

    private String id;
    private String status;
    private String text;
    private List<Word> words;
    private String error;
    private Double confidence;
    private Integer audio_duration;

    @JsonProperty("language_code")
    private String languageCode;

    @JsonProperty("acoustic_model")
    private String acousticModel;

    @JsonProperty("language_model")
    private String languageModel;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Word {
        private String text;
        private Integer start;
        private Integer end;
        private Double confidence;
        private String speaker;

        @JsonProperty("punctuated_word")
        private String punctuatedWord;

        @JsonProperty("speaker_confidence")
        private Double speakerConfidence;
    }
}
