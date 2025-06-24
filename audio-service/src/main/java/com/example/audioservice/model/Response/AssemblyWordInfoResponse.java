package com.example.audioservice.model.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssemblyWordInfoResponse {
    private String text;
    private Integer start;
    private Integer end;
    private Double confidence;
}
