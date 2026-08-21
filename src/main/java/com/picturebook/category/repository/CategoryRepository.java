package com.picturebook.category.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.picturebook.category.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findAllByIsActiveTrueOrderByDisplayOrderAsc();

    boolean existsByName(String name);
}
