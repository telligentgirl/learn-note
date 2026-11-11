//package com.example.learn_note;
//
//import com.auth0.jwt.JWT;
//import com.auth0.jwt.JWTVerifier;
//import com.auth0.jwt.algorithms.Algorithm;
//import com.auth0.jwt.interfaces.Claim;
//import com.auth0.jwt.interfaces.DecodedJWT;
//import org.junit.jupiter.api.Test;
//
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//
//public class JWTtest {
//    @Test
//    public  void testgen(){
//        Map<String,Object> claims=new HashMap<>();
//        claims.put("id",1);
//        claims.put("username","张三");
////        生成JWT的代码
//        String token =JWT.create()
//                .withClaim("user",claims)//添加载荷
//                .withExpiresAt(new Date(System.currentTimeMillis()+1000*60*60*12))//添加过期时间
//                .sign(Algorithm.HMAC256("bigevent"));//指定算法，配置密钥
//        System.out.println(token);
//    }
////    测试jwt的代码
//    @Test
//    public void testprase(){
//        String token="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
//                "eyJ1c2VyIjp7ImlkIjoxLCJ1c2VybmFtZSI6IuW8oOS4iSJ9LCJleHAiOjE3OTMzODU1MTd9." +
//                "oJeznMU9HTx8Gf4wtxF98AQnySkftGoscmLU2NlQtQQ";
//        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256("bigevent")).build();
//        DecodedJWT decodedJWT = jwtVerifier.verify(token);
//        Map<String, Claim> decodedJWTClaims = decodedJWT.getClaims();
//        Claim user = decodedJWTClaims.get("user");
//        System.out.println(user);
//    }
//}
//
