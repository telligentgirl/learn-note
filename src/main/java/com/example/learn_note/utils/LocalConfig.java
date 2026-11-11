package com.example.learn_note.utils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
public final class LocalConfig {
    private static final Properties LOCAL = load();
    private LocalConfig() {}
    private static Properties load() {
        Properties result = new Properties();
        Path path = Path.of("config", "local.properties");
        if (Files.exists(path)) {
            try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                result.load(reader);
            } catch (java.io.IOException e) {
                throw new IllegalStateException("无法读取本地配置文件", e);
            }
        }
        return result;
    }
    public static String require(String name) {
        String value = System.getenv(name);
        if (value == null) value = LOCAL.getProperty(name);
        if (value == null || value.isBlank())
            throw new IllegalStateException("请配置环境变量或 config/local.properties 中的 " + name);
        return value;
    }
}
