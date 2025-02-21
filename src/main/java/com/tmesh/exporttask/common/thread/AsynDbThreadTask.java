package com.tmesh.exporttask.common.thread;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tmesh.exporttask.entity.ExportTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * @author : TMesh
 * @version : 1.0.0
 * @description : 异步设置数据任务
 * @updateUser : TMesh
 */
public class AsynDbThreadTask<T> extends CommonDbDataThreadTask<T> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * 初始化数据
     **/
    public AsynDbThreadTask(String fileName, Sheet sheet, int limit, String module, BaseMapper<T> mapper,
                            QueryWrapper<T> queryWapper, ExportTask exportTask, int lastNumber, int offset) {
        super(fileName, sheet, limit, module, mapper, queryWapper, exportTask, lastNumber, offset);
    }


    /**
     * 可以重写 run 方法，实现需要的功能 
     **/
//    @Override
//    public void run() {
//    }
}