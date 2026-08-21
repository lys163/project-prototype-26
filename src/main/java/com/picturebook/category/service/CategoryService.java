package com.picturebook.category.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.picturebook.category.dto.CategoryCreateRequest;
import com.picturebook.category.dto.CategoryListResponse;
import com.picturebook.category.entity.Category;
import com.picturebook.category.repository.CategoryRepository;
import com.picturebook.global.exception.CustomException;
import com.picturebook.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 카테고리 생성 (추후에 관리자 서버로 분리 예정)
    @Transactional
    public void createCategory(CategoryCreateRequest request) {

        validateCategoryName(request.name());
        
        Category category = Category.create(request.name(), request.displayOrder());

        categoryRepository.save(category);
    }

    private void validateCategoryName(String name){
        if (categoryRepository.existsByName(name)){
            throw new CustomException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }
    }

    @Transactional(readOnly = true)
    public List<CategoryListResponse> getCategories(){
        
        return categoryRepository
            .findAllByIsActiveTrueOrderByDisplayOrderAsc()
            .stream()
            .map(category -> new CategoryListResponse(
                category.getId(),
                category.getName()
            ))
            .toList();
    }
}
