package com.example.excel;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class LocalExcelUnit {
    static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<Map<String, Object>> importExcel(MultipartFile file, String sheetName) throws IOException {
        String name = file.getOriginalFilename();
        Workbook workbook = null;
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            // 创建Excel操作对象
            workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheet(sheetName);
            // 获取数据总行数
            int totalRowNum = sheet.getLastRowNum();
            // 获取数据总列数
            int cellLength = sheet.getRow(0).getPhysicalNumberOfCells();
            // 获取表头
            Row firstRow = sheet.getRow(0);
            List<String> keys = new ArrayList<>();
            for (int i = 0; i < cellLength; i++) {
                Cell cell = firstRow.getCell(i);
                keys.add(String.valueOf(getCellValue(cell)));
            }
            // 从第i行开始获取
            for (int i = 1; i <= totalRowNum; i++) {
                Map<String, Object> map = new LinkedHashMap<>();
                // 获取第i行对象
                Row row = sheet.getRow(i);
                // 遇到空行即结束
                if (row == null) {
                    break;
                }
                // 如果一行里所有单元都为空，则放不进list里面
                int a = 0;
                for (int j = 0; j < cellLength; j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null) {
                        continue;
                    }
                    // 获取列值
                    Object value = getCellValue(cell);
                    map.put(keys.get(j), value);
                }
                if (!checkNullMap(map)) {
                    list.add(map);
                }
            }
        } finally {
            if (workbook != null){
                workbook.close();
            }
        }
        List<Map<String, Object>> data = format(list);
        return data;
    }

    // 如果map中存储的value都是null则返回true
    private static boolean checkNullMap(Map<String, Object> map){
        for (Object value :
                map.values()) {
            if (Objects.nonNull(value)) {
                return false;
            }
        }
        return true;
    }

    private static Object getCellValue(Cell cell){
        CellType cellType = cell.getCellType();
        Object cellValue = null;

        if (cellType == CellType._NONE){
            cellValue = null;
        }else if (cellType == CellType.NUMERIC){
            // 数值型
            if (DateUtil.isCellDateFormatted(cell)){
                // 日期类型
                Date d = cell.getDateCellValue();
                cellValue = dateTimeFormatter.format(LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault()));
            }else {
                double numericCellValue = cell.getNumericCellValue();
                BigDecimal bdVal = new BigDecimal(numericCellValue);
                if ((bdVal + ".0").equals(Double.toString(numericCellValue))){
                    // 整型
                    cellValue = bdVal;
                }else if (String.valueOf(numericCellValue).contains("E10")){
                    // 科学计数法
                    cellValue = new BigDecimal(numericCellValue).toPlainString();
                }else {
                    // 浮点型
                    cellValue = numericCellValue;
                }
            }
        }else if (cellType == CellType.STRING){
            // 字符串型
            cellValue = cell.getStringCellValue();
            if (cellValue != null){
                cellValue = cellValue.toString().trim();
            }
        }else if (cellType == CellType.FORMULA){
            // 公式型
            cellValue = cell.getCellFormula();
        }else if (cellType == CellType.BLANK){
            // 空值
            cellValue = "";
        }else if (cellType == CellType.BOOLEAN){
            // 布尔型
            cellValue = cell.getBooleanCellValue();
        }else if (cellType == CellType.ERROR){
            // 错误
            cellValue = cell.getErrorCellValue();
        }
        return cellValue;
    }

    private List<Map<String, Object>> format(List<Map<String, Object>> excel){
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
        return data;
    }

}