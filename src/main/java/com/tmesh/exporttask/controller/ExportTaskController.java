package com.tmesh.exporttask.controller;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tmesh.exporttask.common.excel.ExportPOIUtils;
import com.tmesh.exporttask.common.thread.utils.ThreadPoolExecutorUtils;
import com.tmesh.exporttask.dto.TaskListDto;
import com.tmesh.exporttask.entity.ExportTask;
import com.tmesh.exporttask.service.ExportTaskService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @ClassName: ExportTaskController
 * @description: 导出任务监控
 * @author: TMesh
 * @Version: 1.0
 **/
@RestController
@RequestMapping("/task")
public class ExportTaskController {
    private final Logger logger = LogManager.getLogger(ExportTaskController.class);
    
    @Autowired
    private ExportTaskService exportTaskService;

    @GetMapping("/list")
    public JSONObject list(TaskListDto taskListDto) {
        // TODO 系统的获取用户信息
        // String userName = user.getUserName;
        JSONObject jsonObject = null;
        try {
            QueryWrapper<ExportTask> queryWrapper = new QueryWrapper<>();

            // TODO 条件输入

            List<ExportTask> list = this.exportTaskService.list(queryWrapper);
            
            // TODO 数据组装

            jsonObject = new JSONObject();
            jsonObject.put("total", list.size());
            jsonObject.put("rows", list);
            // TODO 操作记录记录
//            OperationLogRecorder.insert(user, "task任务查询成功", "task任务查询", "查询");
        } catch (Exception e) {
            e.printStackTrace();
            // TODO 操作记录记录
//            OperationLogRecorder.insert(user, "task任务查询失败：" + e.getMessage(), "task任务查询", "查询");
            this.logger.error(e.getMessage(), e);
        }
        // TODO 这里可以自定义返回
        return jsonObject;
    }

    @PostMapping("/delete")
    public JSONObject delete(Map<String, Object> map) {
        String id = (String) map.get("id");
        this.exportTaskService.deleteByTaskId(id);
        JSONObject jsonObject =  new JSONObject();
        // TODO 操作记录记录
//        OperationLogRecorder.insert(user, "删除任务task成功...userId={}", "删除", id);
        // TODO 这里可以自定义返回
        return jsonObject;
    }

    @PostMapping("/download")
    public void download(String id, HttpServletResponse response) {
        JSONObject jsonObject = null;
        try {
            ExportTask exportTask = this.exportTaskService.getById(id);
            String filePath = exportTask.getFilePath();
            // TODO 下载文件操作
            ExportPOIUtils.downloadFromFile(response, filePath);
            // TODO 操作记录记录
//            OperationLogRecorder.insert(user, "下载任务task成功...userId={}", "下载", id);
        } catch (Exception e) {
            e.printStackTrace();
            this.logger.error(e.getMessage(), e);
            // TODO 操作记录记录
//            OperationLogRecorder.insert(user, "下载任务task失败...userId={}", "下载", id);
        }
    }

    public JSONObject getModule() {
        JSONObject jsonObject = null;
        // TODO 这里用于获取所有 Module 用于页面查询条件

        // TODO 这里可以自定义返回
        return jsonObject;
    }

    @GetMapping("/allExecuteDeatilInfo")
    public JSONObject threadPoolExecutorDetailInfo() {
        return ThreadPoolExecutorUtils.ThreadPoolSingleton.getDetailInfo();
    }

}