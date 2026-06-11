package com.awsome.shop.point.repository.mysql.mapper.points;

import com.awsome.shop.point.repository.mysql.po.points.PointsAccountPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分账户 Mapper 接口
 */
@Mapper
public interface PointsAccountMapper extends BaseMapper<PointsAccountPO> {
}
