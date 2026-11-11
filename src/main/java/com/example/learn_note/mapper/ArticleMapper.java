package com.example.learn_note.mapper;

import com.example.learn_note.pojo.Article;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArticleMapper {
    @org.apache.ibatis.annotations.Update("update article set title=#{title}, content=#{content}, cover_img=#{coverImg}, state=#{state}, category_id=#{categoryId}, update_time=now() where id=#{id} and create_user=#{createUser}")
    void update(Article article);

    @Insert("insert into article (title, content, cover_img, state, category_id, create_user, create_time, update_time) values (#{title},#{content},#{coverImg},#{state},#{categoryId},#{createUser},#{createTime},#{updateTime}) ")
    void add(Article article);


    //动态查询（因为有两个参数时不必要的）用映射配置文件的方式
    //映射配置文件（去官网找）（在resource中添加）必须要和对应接口在同一目录下（在resource中创捷目录结构），且名字与接口名字保持一致
    //映射文件中的namespace命名空间的值必须是对应接口的全类名，，eg:com.example.learn_note.mapper.ArticleMapper
    List<Article> list(String categoryId, String state, Integer userid);

    @Delete("delete from article where id=#{id}")
    void delete(Integer id);

    @Select("select * from article where id=#{id}")
    Article getarticle(Integer id);
}
