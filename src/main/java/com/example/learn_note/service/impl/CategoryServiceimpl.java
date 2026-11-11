package com.example.learn_note.service.impl;

import com.example.learn_note.mapper.CategoryMapper;
import com.example.learn_note.pojo.Category;
import com.example.learn_note.service.CategoryService;
import com.example.learn_note.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceimpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Override
    public void add(Category category) {
        Map<String,Object> map= ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        //参数中没有createusers  所以需要在service层将category对象的createusers设置好，，这样mapper层才能调用到该属性
        category.setCreateUser(id);
        categoryMapper.add(category);

    }

    @Override
    public List<Category> list() {
        Map<String,Object> map= ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");

        return categoryMapper.list(id);
    }

    @Override
    public void updata(Category category) {
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.update(category);
    }

    @Override
    public void delete(Integer id) {
        categoryMapper.delete(id);
    }

}
