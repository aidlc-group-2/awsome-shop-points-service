package com.awsome.shop.point.repository.mysql.mapper.test;

import com.awsome.shop.point.repository.mysql.po.test.TestPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Test Mapper 接口
 */
@Mapper
public interface TestMapper extends BaseMapper<TestPO> {

    /**
     * 分页查询
     *
     * @param page MyBatis-Plus 分页对象
     * @param name 名称模糊查询条件（可为 null）
     * @return 分页结果
     */
    IPage<TestPO> selectPage(IPage<TestPO> page, @Param("name") String name);
}
