package com.example.learn_note.mapper;


import com.example.learn_note.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("select * from user where username=#{username}")

    User findbyusername(String username);

    @Insert("insert into user(username, password, create_time, update_time) VALUES (#{username},#{password},now(),now())")

    void add(String username, String password);

    @Update("update user set nickname=#{nickname},email=#{email},update_time=#{updateTime} where id=#{id}")
    void updata(User user);

    @Update("update user set user_pic=#{avatarurl},update_time=now() where id=#{id}")
    void updataAvater(String avatarurl,Integer id);

    @Update("update user set password=#{Md5newpwd},update_time=now() where id=#{id}")
    void updatePwd(String Md5newpwd,Integer id);
}
