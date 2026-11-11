package com.example.learn_note.exception;

import com.example.learn_note.pojo.Result;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
//全局异常处理器
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 数据库异常只在服务器记录，页面不展示 SQL、表结构和堆栈。
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public Result dataError(org.springframework.dao.DataIntegrityViolationException e) {
        e.printStackTrace();
        return Result.error("保存失败，请检查分类等参数；如果问题持续，请查看服务器日志。");
    }
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public Result validationError(org.springframework.web.bind.MethodArgumentNotValidException e) {
        var error = e.getBindingResult().getFieldError();
        return Result.error(error == null ? "请求参数不符合要求" : error.getDefaultMessage());
    }
    @ExceptionHandler(Exception.class)
    public Result handleexception(Exception e){
        e.printStackTrace();
        return Result.error(StringUtils.hasLength(e.getMessage())?e.getMessage() : "操作失败");

    }
}
