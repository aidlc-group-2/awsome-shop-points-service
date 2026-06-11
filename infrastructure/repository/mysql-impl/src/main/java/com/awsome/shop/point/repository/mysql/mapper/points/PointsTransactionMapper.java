package com.awsome.shop.point.repository.mysql.mapper.points;

import com.awsome.shop.point.repository.mysql.po.points.PointsTransactionPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 积分变动流水 Mapper 接口
 */
@Mapper
public interface PointsTransactionMapper extends BaseMapper<PointsTransactionPO> {

    /**
     * 按订单号 + 类型统计流水条数（幂等去重判断）。
     */
    @Select("SELECT COUNT(*) FROM points_transaction WHERE order_ref = #{orderRef} AND type = #{type} AND deleted = 0")
    int countByOrderRefAndType(@Param("orderRef") String orderRef, @Param("type") String type);
}
