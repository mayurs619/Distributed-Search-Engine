package com.example.search_api.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DbContextHolderTest {

    @Test
    public void testThreadLocalIsolation() throws InterruptedException {
        DbContextHolder.setDbType("SHARD1");
        
        Thread thread = new Thread(() -> {
            DbContextHolder.setDbType("SHARD2");
            assertEquals("SHARD2", DbContextHolder.getDbType());
        });
        
        thread.start();
        thread.join();

        assertEquals("SHARD1", DbContextHolder.getDbType());
        
        DbContextHolder.clearDbType();
        assertNull(DbContextHolder.getDbType());
    }
}