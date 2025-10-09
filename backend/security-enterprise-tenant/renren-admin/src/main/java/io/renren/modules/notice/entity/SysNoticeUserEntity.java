/**
 * Copyright (c) 2019 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */
package io.renren.modules.notice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 我的通知
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Accessors(chain = true)
@TableName("sys_notice_user")
public class SysNoticeUserEntity implements Serializable {
    /**
     * 通知ID
     */
    private Long noticeId;
    /**
     * 接收者ID
     */
    private Long receiverId;
    /**
     * 阅读状态  0：未读  1：已读
     */
    private Integer readStatus;
    /**
     * 阅读时间
     */
    private Date readDate;
}