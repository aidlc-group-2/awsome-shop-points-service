package com.awsome.shop.point.repository.mysql.mapper.points;

import com.awsome.shop.point.repository.mysql.po.points.PointsBatchPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分批次 Mapper 接口
 */
@Mapper
public interface PointsBatchMapper extends BaseMapper<PointsBatchPO> {
}
