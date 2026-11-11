package com.example.learn_note.interceptors;

import com.example.learn_note.pojo.Result;
import com.example.learn_note.utils.JwtUtil;
import com.example.learn_note.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.jta.JtaAfterCompletionSynchronization;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
//拦截器
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        令牌验证
        String token = request.getHeader("Authorization");//获取请求头中携带的token
        try {
            //从redis中获取相同的token
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            String redistoken = operations.get(token);
            //若能获取到，则
            if (redistoken==null){
                //token失效了
                throw new RuntimeException();//抛出异常，，跳转到catch
            }
            Map<String, Object> claims = JwtUtil.parseToken(token);
//            存入线程全局变量，方便后续重复使用
            ThreadLocalUtil.set(claims);
            return true;
        } catch (Exception e) {
//            http响应状态码为401
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        //清空Thread内容
        ThreadLocalUtil.remove();
    }
}

