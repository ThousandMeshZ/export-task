package com.tmesh.exporttask.controller;

import com.alibaba.fastjson2.JSONObject;
import com.tmesh.exporttask.dto.UserExportTaskDto;
import com.tmesh.exporttask.service.UserSerivce;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : TMesh
 * @version : 1.0.0
 * @description : 用户控制器
 * @updateUser : TMesh
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private final Logger logger = LogManager.getLogger(UserController.class);
    
    @Autowired
    private UserSerivce userSerivce;

    
    @GetMapping("/exportList")
    public JSONObject exportList(UserExportTaskDto userExportTaskDto) {
        JSONObject jsonObject = new JSONObject();
        // TODO 系统的获取用户信息
        // String userName = user.getUserName;
        for (int i = 0; i < 3; i++) {
//            this.userSerivce.exportList(userExportTaskDto);
            this.userSerivce.exportListByAnnotation(userExportTaskDto);
        }
        return jsonObject;
    }
}

