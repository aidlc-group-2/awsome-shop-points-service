package com.awsome.shop.point.repository.mysql.mapper.points;

import com.awsome.shop.point.repository.mysql.po.points.PointsAccountPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 积分账户 Mapper 接口
 */
@Mapper
public interface PointsAccountMapper extends BaseMapper<PointsAccountPO> {

    /**
     * 悲观锁查询账户（SELECT ... FOR UPDATE）。
     *
     * <p>必须在事务内调用，用于串行化同一用户的余额/批次变动，杜绝丢失更新与超扣。</p>
     */
    @Select("SELECT id, user_id, balance, created_at, updated_at, created_by, updated_by, deleted, version "
            + "FROM points_account WHERE user_id = #{userId} AND deleted = 0 FOR UPDATE")
    PointsAccountPO selectByUserIdForUpdate(@Param("userId") Long userId);
}
