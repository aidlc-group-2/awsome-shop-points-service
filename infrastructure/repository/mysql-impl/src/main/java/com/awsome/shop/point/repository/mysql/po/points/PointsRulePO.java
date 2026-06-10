package com.awsome.shop.point.repository.mysql.po.points;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分规则配置持久化对象
 */
@Data
@TableName("points_rule")
public class PointsRulePO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long onboardingBonus;

    private Long periodicAmount;

    private String periodicCycle;

    private Integer validityDays;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;
}
