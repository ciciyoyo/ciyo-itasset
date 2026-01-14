package com.ciyocloud.system.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.IDictEnum;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author codeck
 * @Description: 系统通告表
 */
@Data
@ExcelIgnoreUnannotated
@TableName("sys_announcement")
public class SysAnnounceEntity extends SysBaseEntity {
    private static final long serialVersionUID = 1L;
    /**
     * 标题
     */
    @ExcelProperty(value = "标题")
    private String title;
    /**
     * 内容
     */
    @ExcelProperty(value = "内容")
    private String msgContent;
    /**
     * 开始时间
     */
    @ExcelProperty(value = "开始时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    @ExcelProperty(value = "结束时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    /**
     * 发布人
     */
    @ExcelProperty(value = "发布人")
    private String sender;
    /**
     * 优先级（L低，M中，H高）
     */
    @ExcelProperty(value = "优先级")
    private PriorityEnum priority;

    /**
     * 消息类型1:通知公告2:系统消息
     */
    @ExcelProperty(value = "消息类型")
    private MsgCategoryEnum msgCategory;
    /**
     * 通告对象类型（USER:指定用户，ALL:全体用户）
     */
    @ExcelProperty(value = "通告对象类型")
    private MsgTypeEnum msgType;
    /**
     * 发布状态（0未发布，1已发布，2已撤销）
     */
    @ExcelProperty(value = "发布状态")
    private SendStatusEnum sendStatus;
    /**
     * 发布时间
     */
    @ExcelProperty(value = "发布时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;
    /**
     * 撤销时间
     */
    @ExcelProperty(value = "撤销时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;
    /**
     * 删除状态（0，正常，1已删除）
     */
    private String delFlag;
    /**
     * 指定用户
     **/
    private String userIds;
    /**
     * 业务类型(email:邮件 bpm:流程 system:系统)
     */
    private String busType;
    /**
     * 业务id
     */
    private String busId;
    /**
     * 打开方式 默认：default 组件：component 路由：url
     */
    private String openType;
    /**
     * 组件/路由 地址
     */
    private String openPage;
    /**
     * 摘要
     */
    private String msgAbstract;

    @AllArgsConstructor
    public enum MsgCategoryEnum implements IDictEnum<String> {
        NOTICE("1", "通知公告"),
        SYSTEM("2", "系统消息");

        @EnumValue
        private String value;
        private String desc;

        @Override
        public String getValue() {
            return value;
        }


        @Override
        public String getDesc() {
            return desc;
        }
    }

    @AllArgsConstructor
    public enum MsgTypeEnum implements IDictEnum<String> {
        USER("1", "指定用户"),
        ALL("2", "全部用户");

        @EnumValue
        private String value;
        private String desc;

        @Override
        public String getValue() {
            return value;
        }


        @Override
        public String getDesc() {
            return desc;
        }
    }


    @AllArgsConstructor
    public enum SendStatusEnum implements IDictEnum<String> {
        NO_SEND("1", "未发布"),
        HAS_SEND("2", "已发布"),
        HAS_CANCLE("3", "已撤销");

        @EnumValue
        private String value;
        private String desc;

        @Override
        public String getValue() {
            return value;
        }


        @Override
        public String getDesc() {
            return desc;
        }
    }

    @AllArgsConstructor
    public enum PriorityEnum implements IDictEnum<String> {
        PRIORITY_L("L", "低"),
        PRIORITY_M("M", "中"),
        PRIORITY_H("H", "高");

        @EnumValue
        private String value;
        private String desc;

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String getDesc() {
            return desc;
        }


    }
}
