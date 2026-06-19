package com.example.search_api.config;

public class DbContextHolder {
    // ThreadLocal ensures that each concurrent request has its own isolated variable
    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static void setDbType(String dbType) {
        CONTEXT.set(dbType);
    }

    public static String getDbType() {
        return CONTEXT.get();
    }

    public static void clearDbType() {
        CONTEXT.remove();
    }
}