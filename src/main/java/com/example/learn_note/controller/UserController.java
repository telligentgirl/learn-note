package com.example.learn_note.controller;

import com.example.learn_note.pojo.Result;
import com.example.learn_note.pojo.User;
import com.example.learn_note.service.UserService;
import com.example.learn_note.utils.JwtUtil;
import com.example.learn_note.utils.Md5Util;
import com.example.learn_note.utils.ThreadLocalUtil;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {
    @Autowired
    private UserService userservice;
    @Autowired
//    注入stringRedisTemplate对象
    private StringRedisTemplate redisTemplate;

    @PostMapping("/register")
    //        校验@pattern
    public Result register(@Pattern(regexp = "^\\S{5,16}$") String username, @Pattern(regexp = "^\\S{5,16}$") String password) {
//        查询用户
            User u = userservice.findbyusername(username);
            if (u == null) {
//            没有占用
//            注册
                userservice.register(username, password);
                return Result.success();
            } else {
                return Result.error("用户名已被占用");
            }

    }
    @PostMapping("/login")
    public Result<String> login (@Pattern(regexp = "^\\S{5,16}$") String username, @Pattern(regexp = "^\\S{5,16}$") String password){
        User u = userservice.findbyusername(username);
        if (u == null) {
//            用户不存在
            return Result.error("当前用户不存在");
        }
//            判断密码是否正确  当前数据库中的密码是密文
        if (Md5Util.getMD5String(password).equals(u.getPassword())){
//                登录成功
//            生成令牌token
            Map<String,Object> claims=new HashMap<>();
            claims.put("id",u.getId());
            claims.put("username",u.getUsername());
            String token = JwtUtil.genToken(claims);
//            把token存储到redis中
            ValueOperations<String, String> Operations = redisTemplate.opsForValue();
            Operations.set(token,token,12, TimeUnit.HOURS);
            return Result.success(token);
        }

        return Result.error("密码错误");

    }
    @GetMapping("/userInfo")
    public Result<User> userInfo(/*@RequestHeader(name = "Authorization") String token*/){
//        根据用户名查询用户
//        Map<String, Object> map = JwtUtil.parseToken(token);
//        String username = map.get("username").toString();
        Map<String,Object> map = ThreadLocalUtil.get();
        String username = map.get("username").toString();
        User user = userservice.findbyusername(username);
        return Result.success(user);
    }
    @PutMapping("/update")
    //@RequestBody将前端传回来的json字符串转换成一个实体类对象
    //@Validated 这样实体类里面添加的校验注解参能生效
    public Result update(@RequestBody @Validated User user){
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (!java.util.Objects.equals(user.getId(), claims.get("id"))) {
            return Result.error("只能修改自己的资料");
        }
        userservice.update(user);
        return Result.success();
    }
    @PatchMapping("/updateAvatar")
    //@RequestParam 把请求参数绑定到方法参数上
    //@URL参数校验
    public Result updateAvater(@RequestParam @URL String avatarurl){
        userservice.updateAvater(avatarurl);
        return Result.success();
    }
    @PatchMapping("/updatePwd")
    public Result updatePwd(@RequestBody Map<String,String> params,@RequestHeader("Authorization") String token){
        //校验参数不为空
        String oldPwd = params.get("old_pwd");
        String newPwd = params.get("new_pwd");
        String rePwd = params.get("re_pwd");
        if (!StringUtils.hasLength(oldPwd) || !StringUtils.hasLength(newPwd)||!StringUtils.hasLength(rePwd)){
            return Result.error("缺少必要参数");
        }
        //校验原密码是否正确
        Map<String,Object> map=ThreadLocalUtil.get();
        String username = map.get("username").toString();
        User user = userservice.findbyusername(username);
        if (!user.getPassword().equals(Md5Util.getMD5String(oldPwd))){
            return Result.error("原密码不正确");
        }
        //校验新密码与确认密码是否不同
        if (!newPwd.equals(rePwd)){
            return Result.error("两次填写的新密码不一样");
        }
        //密码更新成功后删除redis中对应的token
        //需要根据当前用的token来定位，所以传入参数
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.getOperations().delete(token);

        userservice.updatePwd(newPwd);
        return Result.success();
    }
}
