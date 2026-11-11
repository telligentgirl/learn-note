package com.example.learn_note.service;

import com.example.learn_note.pojo.Category;

import java.util.List;


public interface CategoryService {
    void add(Category category);
//文章分类列表查询
    List<Category> list();

    void updata(Category category);

    void delete(Integer id);
}
