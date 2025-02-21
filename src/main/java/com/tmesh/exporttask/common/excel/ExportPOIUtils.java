package com.tmesh.exporttask.common.excel;

import com.tmesh.exporttask.common.thread.ExportThreadTask;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author : TMesh
 * @version : 1.0.0
 * @description : POI导出工具类
 * @updateUser : TMesh
 */
public class ExportPOIUtils {

    private static final Logger logger = LogManager.getLogger(ExportPOIUtils.class);

    /**
     * 下载文件方法
     **/
    public static void downloadFromFile(HttpServletResponse response, String filePath) throws IOException {
        FileInputStream ips = null;
        File file = new File(filePath);
        String fileName = file.getName();

        // 设置response参数，可以打开下载页面
        ips = new FileInputStream(file);
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + new String((fileName + ".xls").getBytes(), StandardCharsets.ISO_8859_1));
        ServletOutputStream out = response.getOutputStream();
        try {              //读取文件流
            int len = 0;
            byte[] buffer = new byte[1024 * 10];
            while ((len = ips.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                ips.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 创建excel文档对象
     * 从POI 3.8版本开始，提供了一种基于XSSF的低内存占用的SXSSF方式。对于大型excel文件的创建，一个关键问题就是，要确保不会内存溢出。其实，就算生成很小的excel（比如几Mb），它用掉的内存是远大于excel文件实际的size的。如果单元格还有各种格式（比如，加粗，背景标红之类的），那它占用的内存就更多了。对于大型excel的创建且不会内存溢出的，就只有SXSSFWorkbook了。它的原理很简单，用硬盘空间换内存（就像hash map用空间换时间一样）。
     * SXSSFWorkbook是streaming版本的XSSFWorkbook,它只会保存最新的excel rows在内存里供查看，在此之前的excel rows都会被写入到硬盘里（Windows电脑的话，是写入到C盘根目录下的temp文件夹）。被写入到硬盘里的rows是不可见的/不可访问的。只有还保存在内存里的才可以被访问到。
     */
    public static SXSSFWorkbook createSXlsxWorkBook() {
        // 创建excel工作簿
        SXSSFWorkbook wb = new SXSSFWorkbook();
        // TODO 进行 Workbook 初始化操作
        return wb;
    }

    /**
     * 创建excel文档对象
     * 这种形式的出现是为了突破HSSFWorkbook的65535行局限。其对应的是excel2007(1048576行，16384列)扩展名为“.xlsx”，最多可以导出104万行，不过这样就伴随着一个问题---OOM内存溢出，原因是你所创建的book sheet row cell等此时是存在内存的并没有持久化。
     */
    public static XSSFWorkbook createXlsxWorkBook() {
        // 创建excel工作簿
        XSSFWorkbook wb = new XSSFWorkbook();
        // TODO 进行 Workbook 初始化操作
        return wb;
    }

    /**
     * 创建excel文档对象
     * poi导出excel最常用的方式；但是此种方式的局限就是导出的行数至多为65535行，超出65536条后系统就会报错。此方式因为行数不足七万行所以一般不会发生内存不足的情况（OOM）。
     */
    public static HSSFWorkbook createXlsWorkBook() {
        // 创建excel工作簿
        HSSFWorkbook wb = new HSSFWorkbook();
        // TODO 进行 Workbook 初始化操作
        return wb;
    }

    /**
     * 创建Workbook下的所有sheet、row和cell
     **/
    public static List<Sheet> createSheetList(Workbook wb, int pageSheet, long count, String[] columnNames) {
        List<Sheet> list = new ArrayList<>();
        for (int i = 0; i < pageSheet; i++) {
            long pageSize = count;
            if (pageSize > ExportThreadTask.SHEETLIMIT) {
                pageSize = ExportThreadTask.SHEETLIMIT;
            }
            Sheet sheet = createSheetAndRowCell(wb, columnNames, pageSize);
            list.add(sheet);
            count -= ExportThreadTask.SHEETLIMIT;
        }
        return list;
    }

    /**
     * 创建sheet并写上表头
     **/
    public static Sheet createSheet(Workbook wb, String[] columnNames) {
        Sheet sheet = wb.createSheet();
        if (columnNames != null && columnNames.length > 0) {
            Row row = sheet.createRow(0);
            for (int i = 0; i < columnNames.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(columnNames[i]);
            }
        }
        return sheet;
    }

    /**
     * 创建sheet指定sheet名称并写上表头
     **/
    private static Sheet createSheet(Workbook wb, String sheetName, String[] columnNames) {
        Sheet sheet = wb.createSheet(sheetName);
        if (columnNames != null && columnNames.length > 0) {
            Row row = sheet.createRow(0);
            for (int i = 0; i < columnNames.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(columnNames[i]);
            }
        }
        return sheet;
    }

    /**
     * 创建Workbook下的所有sheet、row和cell
     **/
    public static Sheet createSheetAndRowCell(Workbook wb, String[] columnNames, long count) {
        Sheet sheet = wb.createSheet();
        if (columnNames != null && columnNames.length > 0) {
            Row row = sheet.createRow(0);
            for (int i = 0; i < columnNames.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(columnNames[i]);
            }
            for (int i = 1; i < count + 1; i++) {
                row = sheet.createRow(i);
                for (int j = 0; j < columnNames.length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(columnNames[j]);
                }
            }
        }
        return sheet;
    }

    /**
     * 写入sheet下的所有数据
     **/
    public static void setRowData(Sheet sheet, List<Map<String, Object>> list, String[] keys, int lastNumber, boolean useAsyn) throws IOException {
        // TODO 进行 Workbook 数据操作
        int sumRowNum = lastNumber;
        // 处理异步写入的数据不同sheet row位置不对问题
        if (lastNumber >= ExportThreadTask.SHEETLIMIT) {
            lastNumber = lastNumber - BigDecimal.valueOf(lastNumber).divide(BigDecimal.valueOf(ExportThreadTask.SHEETLIMIT), RoundingMode.FLOOR).multiply(BigDecimal.valueOf(ExportThreadTask.SHEETLIMIT)).intValue();
        }
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> map = list.get(i);
            int rowNum = lastNumber + i;
            if ((sumRowNum - 1) % 10000 == 0) {
                logger.info("写入第{}-{}行", sumRowNum, sumRowNum + 10000 - 1);
//                try {
//                    Thread.sleep(200);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
            }
            if (useAsyn) {
                synchronized (sheet) {
                    setCellData(sheet, rowNum, map, keys);
                }
            } else {
                setCellData(sheet, rowNum, map, keys);
            }
            sumRowNum++;
        }
    }

    /**
     * 写入指定row的所有数据
     **/
    public static void setCellData(Sheet sheet, int rowNum, Map<String, Object> map, String[] keys) {
        Row row = sheet.getRow(rowNum);
        if (row == null) {
            // 并发会报错 java.util.ConcurrentModificationException
            row = sheet.createRow(rowNum);
        }
        for (int j = 0; j < keys.length; j++) {
            Cell cell = row.getCell(j);
            if (cell == null) {
                cell = row.createCell(j);
            }
            cell.setCellValue(map.get(keys[j]).toString());
        }
    }


    /**
     * 合并数据到excel里面
     **/
    public static void mergeDbData(Workbook wb, int pageSheet, Map<String, List<Map<String, Object>>> dbMap,
                                   String[] keys, String module, String fileName, boolean useAsyn) throws IOException {
        long begTimeL = System.currentTimeMillis();
        int lastNumber = 1;
        int num = 0;
        int index = 0;
        Set<String> keySet = dbMap.keySet();
        List<String> keyList = keySet.stream().map(Integer::parseInt).sorted().map(String::valueOf).toList();
        for (int i = 0; i < pageSheet; i++) {
            Sheet sheet = wb.getSheetAt(i);
            for (; index < keyList.size(); index++) {
                String key = keyList.get(index);
                num += dbMap.get(key).size();
                ExportPOIUtils.setRowData(sheet, dbMap.get(key), keys, lastNumber, useAsyn);
                lastNumber += dbMap.get(key).size();
                if (lastNumber >= ExportThreadTask.SHEETLIMIT) {
                    break;
                }
                dbMap.get(key).clear();
            }
            index++;
            lastNumber = 1;
        }
        long endTime = System.currentTimeMillis();
        logger.info("{}-fileName={},数据全部写入至excel实体...耗时={} 毫秒,大小:{} 行：", module, fileName, endTime - begTimeL, num);
    }

}
