package com.example.learn_note.service;

import com.example.learn_note.pojo.User;

import java.util.Map;


public interface UserService {
    User findbyusername(String username);

    void register(String username, String password);

    void login(String username, String password);

    void update(User user);

    void updateAvater(String avatarurl);

    void updatePwd(String newPwd);
}
