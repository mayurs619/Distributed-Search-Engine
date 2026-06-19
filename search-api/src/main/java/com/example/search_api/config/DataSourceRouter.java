package com.example.search_api.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DataSourceRouter extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        // Spring calls this method automatically before executing a query.
        // It returns either "SHARD1" or "SHARD2" based on what we set in the ContextHolder.
        return DbContextHolder.getDbType();
    }
}