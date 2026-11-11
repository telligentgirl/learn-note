package com.example.learn_note.controller;

import com.example.learn_note.pojo.Article;
import com.example.learn_note.pojo.PageBean;
import com.example.learn_note.pojo.Result;
import com.example.learn_note.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.example.learn_note.utils.ThreadLocalUtil;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;
    @Autowired
    private com.example.learn_note.mapper.CategoryMapper categoryMapper;

    private boolean ownsCategory(Integer id) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        return categoryMapper.list((Integer) claims.get("id")).stream()
            .anyMatch(category -> Objects.equals(category.getId(), id));
    }

    @PutMapping
    public Result update(@RequestBody @Validated Article article) {
        if (article.getId() == null || !owns(articleService.getarticle(article.getId())))
            return Result.error("笔记不存在或无权访问");
        if (!ownsCategory(article.getCategoryId())) return Result.error("请选择自己的分类");
        articleService.update(article);
        return Result.success();
    }
    @PostMapping
    public Result add(@RequestBody @Validated Article article){
        if (!ownsCategory(article.getCategoryId())) return Result.error("请选择自己的分类");
        articleService.add(article);
        return Result.success();
    }
    @DeleteMapping
    public Result delete(@RequestParam Integer id){
        // 列表按用户过滤还不够：删除和详情也必须核对归属。
        if (!owns(articleService.getarticle(id))) return Result.error("笔记不存在或无权访问");
        articleService.delete(id);
        return Result.success();
    }
    @GetMapping("/detail")
    public Result<Article> getarticle(@RequestParam Integer id){
        Article article =articleService.getarticle(id);
        if (!owns(article)) return Result.error("笔记不存在或无权访问");
        return Result.success(article);

    }
    private boolean owns(Article article) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        return article != null && Objects.equals(article.getCreateUser(), claims.get("id"));
    }

    @GetMapping
    public Result<PageBean<Article>> list(Integer pageNum, Integer pageSize, @RequestParam(required = false) String categoryId, @RequestParam(required = false) String state){
        PageBean<Article> pageBean=articleService.list(pageNum,pageSize,categoryId,state);
        return Result.success(pageBean);
    }
}
