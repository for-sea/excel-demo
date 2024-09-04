package com.example.service;

import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.QueryResult;

import java.util.List;
import java.util.Map;

public interface InfluxDBService {
     void createRetentionPolicy();

     void insert(String measurement, Map<String, String> tags, Map<String, Object> fields);

    void insert(String measurement, long time, Map<String, String> tags, Map<String, Object> fields);

    void insertUDP(String measurement, long time, Map<String, String> tags, Map<String, Object> fields);

    QueryResult query(String command);

    List<Map<String, Object>> queryResultProcess(QueryResult queryResult);

    long countResultProcess(QueryResult queryResult);

    void createDB(String dbName);

    void batchInsert(BatchPoints batchPoints);

    void batchInsert(final String database, final String retentionPolicy,
                     final InfluxDB.ConsistencyLevel consistency, final List<String> records);

    void batchInsert(final InfluxDB.ConsistencyLevel consistency, final List<String> records);
}
