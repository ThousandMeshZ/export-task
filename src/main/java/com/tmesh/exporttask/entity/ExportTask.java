package com.tmesh.exporttask.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author : TMesh
 * @version : 1.0.0
 * @description : 导出任务实体类
 * @updateUser : TMesh
 */
@TableName("EXPORT_TASK")
@Data
@Accessors(chain = true)
public class ExportTask implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * ID
     **/
    @TableId
    private long id;

    /**
     * 模块名
     **/
    private String module;

    /**
     * 文件名
     **/
    private String fileName;

    /**
     * 文件路径
     **/
    private String filePath;

    /**
     * 状态，0：未开始，1：开始，2：成功，3：失败
     **/
    private int status;

    /**
     * 用户名
     **/
    private String userName;

    /**
     * sql语句
     **/
    private String query;

    /**
     * 创建时间
     **/
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss SSS") // JSON格式 响应给浏览器
//	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss SSS") // 接收
    @JSONField(format="yyyy-MM-dd HH:mm:ss SSS") // fastjson 中用 @JSONField 格式化日期格式/指定日期属性的格式
//  JSON.toJSONStringWithDateFormat(list,"yyyy-MM-dd HH:mm:ss SSS"); //list结果集
//  JSONObject.DEFFAULT_DATE_FORMAT="yyyy-MM-dd HH:mm:ss SSS";//设置日期格式
//  JSONObject.toJSONString(list, SerializerFeature.WriteDateUseDateFormat);//list结果集
    private Date createTime;

    /**
     * 开始时间
     **/
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss SSS") // JSON格式 响应给浏览器
    @JSONField(format="yyyy-MM-dd HH:mm:ss SSS") // fastjson 中用 @JSONField 格式化日期格式/指定日期属性的格式
    private Date startTime;

    /**
     * 结束时间
     **/
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss SSS") // JSON格式 响应给浏览器
    @JSONField(format="yyyy-MM-dd HH:mm:ss SSS") // fastjson 中用 @JSONField 格式化日期格式/指定日期属性的格式
    private Date endTime;

    /**
     * 总时间消费
     **/
    private long timeCost;

    /**
     * 原因
     **/
    private String reason;

    /**
     * db开始时间
     **/
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss SSS") // JSON格式 响应给浏览器
    @JSONField(format="yyyy-MM-dd HH:mm:ss SSS") // fastjson 中用 @JSONField 格式化日期格式/指定日期属性的格式
    private Date dbStartDate;

    /**
     * db结束时间
     **/
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss SSS") // JSON格式 响应给浏览器
    @JSONField(format="yyyy-MM-dd HH:mm:ss SSS") // fastjson 中用 @JSONField 格式化日期格式/指定日期属性的格式
    private Date dbEndDate;

    /**
     * db时间消费
     **/
    private long dbTimeCost;

    /**
     * excel写开始时间
     **/
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss SSS") // JSON格式 响应给浏览器
    @JSONField(format="yyyy-MM-dd HH:mm:ss SSS") // fastjson 中用 @JSONField 格式化日期格式/指定日期属性的格式
    private Date excelWriteStartDate;

    /**
     * excel写结束时间
     **/
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss SSS") // JSON格式 响应给浏览器
    @JSONField(format="yyyy-MM-dd HH:mm:ss SSS") // fastjson 中用 @JSONField 格式化日期格式/指定日期属性的格式
    private Date excelWriteEndDate;

    /**
     * excel写时间消费
     **/
    private long excelWriteTimeCost;
}
