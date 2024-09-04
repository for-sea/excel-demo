package com.example.excel;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class ExcelUtilTest {
    private static String openurl = "http://localhost:8086";//连接地址
    private static String username = "admin";//用户名
    private static String password = "";//密码
    private static String database = "demo";//数据库

    @Test
    public void testExcelReader() throws IOException {
        File file = new File("D:\\Document\\Excel导入时序库\\Excel导入测试文件.xls");

        LocalExcelUnit excelUnit = new LocalExcelUnit();
        ExcelReader reader = ExcelUtil.getReader(file, 0);
        List<Map<String, Object>> excel = reader.readAll();

        // Map输出
        System.out.println("---Map输出---");
        excel.stream().forEach(s -> System.out.println(s));
        System.out.println("数据长度=" + excel.size());

        System.out.println("----------------");
        List<Map<String, Object>> data = new ArrayList<>();
        for (Map<String, Object> map : excel) {
            Set<Map.Entry<String, Object>> entrySet = map.entrySet();
            Object time = map.get("time");
            for (Map.Entry<String, Object> entry : entrySet) {
                if ("time".equals(entry.getKey())) {
                    continue;
                }
                Map<String, Object> tmpMap = new HashMap<>();
                String[] split = entry.getKey().split("-");
                tmpMap.put("dmid", split[0]);
                tmpMap.put("varname", split[1]);
                tmpMap.put("v", entry.getValue());
                tmpMap.put("time", time);
                data.add(tmpMap);

            }
        }
        // 打印Map列表
        data.stream().forEach(s -> System.out.println(s));
    }

    @Test
    public void testTimFormat() throws ParseException {
        String timeStr = "2024-04-19 10:00:00";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(timeStr);
        long time = date.getTime();
        System.out.println(time);
    }

    @Test
    public void testBatchPoints(){
        // 建立数据库连接
        InfluxDB influxDB = InfluxDBFactory.connect(openurl, username, password);
        influxDB.setDatabase(database);

        Point point1 = Point.measurement("meter_other")
                .tag("dmid", "800740")
                .tag("varname", "pttl")
                .addField("v", 0.245)
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .build();
        Point point2 = Point.measurement("meter_other")
                .tag("dmid", "800740")
                .tag("varname", "pttl")
                .addField("v", 0.245)
                .time(System.currentTimeMillis(), TimeUnit.SECONDS)
                .build();
        BatchPoints batchPoints = BatchPoints.database("demo").build();
        batchPoints.point(point1).point(point2);
        influxDB.write(point1);
    }
}