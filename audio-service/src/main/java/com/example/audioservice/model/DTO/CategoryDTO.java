package com.example.audioservice.model.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryDTO {
    @NotBlank(message = "Category title cannot be blank")
    private String title;
}
