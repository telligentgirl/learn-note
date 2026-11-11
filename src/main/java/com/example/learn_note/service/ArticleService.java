package com.example.learn_note.service;

import com.example.learn_note.pojo.Article;
import com.example.learn_note.pojo.PageBean;

public interface ArticleService {
    void add(Article article);
    void update(Article article);

    PageBean<Article> list(Integer pageNum, Integer pageSize, String categoryId, String state);

    void delete(Integer id);

    Article getarticle(Integer id);
}
