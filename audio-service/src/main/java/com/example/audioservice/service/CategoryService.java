package com.example.audioservice.service;

import com.example.audioservice.model.DTO.CategoryDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CategoryService {
    ResponseEntity<CategoryDTO> addCategory(CategoryDTO categoryDTO);
    ResponseEntity<List<CategoryDTO>> getAllCategories();
}
