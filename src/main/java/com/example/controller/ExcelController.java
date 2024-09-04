package com.example.controller;

import com.example.excel.LocalExcelUnit;
import com.example.service.InfluxDBService;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/excel")
public class ExcelController {

    private static String openurl = "http://localhost:8086";//连接地址
    private static String username = "admin";//用户名
    private static String password = "";//密码
    private static String database = "demo";//数据库


    @Autowired
    private LocalExcelUnit excelUnit;

    @Autowired
    private InfluxDBService influxDBService;

    @PostMapping("/import")
    public List<Map<String, Object>> importExcel(@RequestParam MultipartFile multipartFile, @RequestParam String sheetName) throws IOException, ParseException {
        // 导入Excel
        List<Map<String, Object>> sheet1 = excelUnit.importExcel(multipartFile, sheetName);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 建立数据库连接
        InfluxDB influxDB = InfluxDBFactory.connect(openurl, username, password);
        influxDB.setDatabase(database);

        // 批量添加Point
        BatchPoints batchPoints = BatchPoints.database("demo").build();
        for (Map<String, Object> map : sheet1) {
            String timeStr = (String) map.get("time");
            Date timeDate = simpleDateFormat.parse(timeStr);
            long time = timeDate.getTime() / 1000;
            Point point = Point.measurement("meter_other")
                    .tag("dmid", (String) map.get("dmid"))
                    .tag("varname", (String) map.get("varname"))
                    .addField("v", new BigDecimal(String.valueOf(map.get("v"))).doubleValue())
                    .time(time, TimeUnit.SECONDS)
                    .build();
            batchPoints.point(point);
        }
        // 写入InfluxDB
        influxDB.write(batchPoints);
        return sheet1;
    }
}
