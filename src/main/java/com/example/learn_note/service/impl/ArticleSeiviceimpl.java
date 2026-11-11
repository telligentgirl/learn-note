package com.example.learn_note.service.impl;

import com.example.learn_note.mapper.ArticleMapper;
import com.example.learn_note.pojo.Article;
import com.example.learn_note.pojo.PageBean;
import com.example.learn_note.service.ArticleService;
import com.example.learn_note.utils.ThreadLocalUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ArticleSeiviceimpl implements ArticleService {
    @Autowired
    private ArticleMapper articleMapper;
    @Override
    public void update(Article article) {
        // 纯文字页面不传封面：编辑时保留历史封面，不覆盖旧数据。
        if (article.getCoverImg() == null) {
            Article existing = articleMapper.getarticle(article.getId());
            article.setCoverImg(existing != null && existing.getCoverImg() != null
                ? existing.getCoverImg() : "");
        }
        Map<String, Object> claims = ThreadLocalUtil.get();
        article.setCreateUser((Integer) claims.get("id"));
        articleMapper.update(article);
    }
    @Override
    public void add(Article article) {
        // 兼容旧表 cover_img NOT NULL；空字符串表示无封面，前端无需传此字段。
        if (article.getCoverImg() == null) article.setCoverImg("");
//        补充属性值
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userid= (Integer) map.get("id");
        article.setCreateUser(userid);
        articleMapper.add(article);
    }

    @Override
    public PageBean<Article> list(Integer pageNum, Integer pageSize, String categoryId, String state) {
//        创建pagebean对象用来封装查询好的数据
        PageBean<Article> pageBean=new PageBean<>();
//        开启分页查询(借助mybtis插件pagehelper)
        PageHelper.startPage(pageNum,pageSize);

//        调用mapper完成查询
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userid = (Integer) map.get("id");
        List<Article> articles= articleMapper.list(categoryId,state,userid);
        //page中提供了 获取pagehelper分页查询后得到的 总记录条数 和 当前页数据 的方法
        //所以需要强制转换成page类型
        Page<Article> page= (Page<Article>) articles;
        //将数据填充到pageBean对象中
        pageBean.setTotal(page.getTotal());
        pageBean.setItems(page.getResult());
        return pageBean;
    }

    @Override
    public void delete(Integer id) {
        articleMapper.delete(id);
    }

    @Override
    public Article getarticle(Integer id) {
        Article article=articleMapper.getarticle(id);
        return article;
    }
}
