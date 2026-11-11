package com.example.learn_note.controller;

import com.example.learn_note.pojo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/article")
public class ArticleControllerTest {
    @GetMapping("/list")
    public Result<String> list(/*@RequestHeader(name = "Authorization") String token,HttpServletResponse response*/){
////        验证令牌token
//        try {
//            Map<String, Object> claims = JwtUtil.parseToken(token);
//            return Result.success("所有的文章数据");
//        } catch (Exception e) {
////            http响应状态码为401
//            response.setStatus(401);
//            return Result.error("未登录");
//        }
        return Result.success("所有的文章数据");


    }
}
