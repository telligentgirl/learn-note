package com.example.learn_note.controller;

import com.example.learn_note.pojo.Category;
import com.example.learn_note.pojo.Result;
import com.example.learn_note.service.CategoryService;
import org.apache.coyote.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.security.Provider;
import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @PostMapping
    //@Validated(Category.Add.class)分组校验
    public Result add(@RequestBody @Validated(Category.Add.class) Category category){
        categoryService.add(category);
        return Result.success();
    }
    @GetMapping
    public Result<List<Category>> list(){
        List<Category> cs=categoryService.list();
        return Result.success(cs);
    }
    @PutMapping
    //@Validated(Category.Update.class)分组校验
    public Result update(@RequestBody @Validated(Category.Update.class) Category category){
        categoryService.updata(category);
        return Result.success();
    }
    @DeleteMapping
    public Result delete(@RequestParam("id") Integer id){
        categoryService.delete(id);
        return Result.success();
    }

}
