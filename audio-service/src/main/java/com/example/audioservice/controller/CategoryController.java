package com.example.audioservice.controller;
import com.example.audioservice.model.DTO.CategoryDTO;
import com.example.audioservice.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@CrossOrigin(origins = {"http://localhost:5555", "http://10.0.2.2:5555"})
@RestController(value = "categories")
@RequestMapping( "/api/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @PostMapping("/add")
    public ResponseEntity<CategoryDTO> addCategory(@RequestBody CategoryDTO categoryDTO) {
        return categoryService.addCategory(categoryDTO);
    }
    @GetMapping("/list")
    public ResponseEntity<List<CategoryDTO>> getAllCategory() {
        return categoryService.getAllCategories();
    }
}
