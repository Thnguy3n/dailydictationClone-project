package com.example.audioservice.service.Impl;

import com.example.audioservice.entity.CategoryEntity;
import com.example.audioservice.model.DTO.CategoryDTO;
import com.example.audioservice.repository.CategoryRepository;
import com.example.audioservice.service.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Override
    public ResponseEntity<CategoryDTO> addCategory(CategoryDTO categoryDTO) {
        CategoryEntity categoryEntity = modelMapper.map(categoryDTO, CategoryEntity.class) ;
        categoryRepository.save(categoryEntity);
        return new ResponseEntity<>(modelMapper.map(categoryEntity, CategoryDTO.class), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryEntity> categoryEntities = categoryRepository.findAll();
        List<CategoryDTO> categoryDTOS = categoryEntities.stream()
                .map(entity -> modelMapper.map(entity, CategoryDTO.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(categoryDTOS, HttpStatus.OK);
    }
}
