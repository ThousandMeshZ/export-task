package com.tmesh.exporttask.common.thread;

/**
 * @author : TMesh
 * @version : 1.0.0
 * @description : 导出任务继承Runnable，并设置常量（这些可以看情况是否卸载配置文件里面）
 * @updateUser : TMesh
 */
public interface ExportThreadTask extends Runnable {
    // SHEETLIMIT 要是 OFFSET 的倍数
    // 偏移常量
    // TODO 这里可以做成动态配置
    public final static int OFFSET = 50000;
    // sheet数量大小常量
    public final static int SHEETLIMIT = 1000000;
    // excel 本地存储路径
//    public final static String DIR_PATH = "/data/excel/export";
    public final static String DIR_PATH = "D:/data/excel/export";


}