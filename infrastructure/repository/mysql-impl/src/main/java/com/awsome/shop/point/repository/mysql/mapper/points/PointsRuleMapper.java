package com.awsome.shop.point.repository.mysql.mapper.points;

import com.awsome.shop.point.repository.mysql.po.points.PointsRulePO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分规则配置 Mapper 接口
 */
@Mapper
public interface PointsRuleMapper extends BaseMapper<PointsRulePO> {
}
