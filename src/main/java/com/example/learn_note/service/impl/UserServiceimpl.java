package com.example.learn_note.service.impl;

import com.example.learn_note.mapper.UserMapper;
import com.example.learn_note.pojo.User;
import com.example.learn_note.service.UserService;
import com.example.learn_note.utils.Md5Util;
import com.example.learn_note.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UserServiceimpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User findbyusername(String username) {
        User u=userMapper.findbyusername(username);
        return u;
    }

    @Override
    public void register(String username, String password) {
//        加密
        String md5String = Md5Util.getMD5String(password);
//        添加
        userMapper.add(username,md5String);
    }

    @Override
    public void login(String username, String password) {

    }

    @Override
    public void update(User user) {
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updata(user);
    }

    @Override
    public void updateAvater(String avatarurl) {
        Map<String,Object> map= ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updataAvater(avatarurl,id);
    }

    @Override
    public void updatePwd(String newPwd) {
        Map<String,Object> map= ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updatePwd(Md5Util.getMD5String(newPwd),id);

    }
}
