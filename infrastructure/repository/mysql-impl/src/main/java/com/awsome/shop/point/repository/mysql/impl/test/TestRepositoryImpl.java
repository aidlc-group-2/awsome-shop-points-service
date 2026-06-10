package com.awsome.shop.point.repository.mysql.impl.test;

import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.domain.model.test.TestEntity;
import com.awsome.shop.point.repository.mysql.mapper.test.TestMapper;
import com.awsome.shop.point.repository.mysql.po.test.TestPO;
import com.awsome.shop.point.repository.test.TestRepository;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.stream.Collectors;

/**
 * Test 仓储实现
 */
@Repository
@RequiredArgsConstructor
public class TestRepositoryImpl implements TestRepository {

    private final TestMapper testMapper;

    @Override
    public TestEntity getById(Long id) {
        TestPO po = testMapper.selectById(id);
        return po == null ? null : toEntity(po);
    }

    @Override
    public PageResult<TestEntity> page(int page, int size, String name) {
        IPage<TestPO> result = testMapper.selectPage(new Page<>(page, size), name);

        PageResult<TestEntity> pageResult = new PageResult<>();
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setTotal(result.getTotal());
        pageResult.setPages(result.getPages());
        pageResult.setRecords(result.getRecords().stream().map(this::toEntity).collect(Collectors.toList()));
        return pageResult;
    }

    @Override
    public void save(TestEntity entity) {
        TestPO po = toPO(entity);
        testMapper.insert(po);
        entity.setId(po.getId());
    }

    @Override
    public void update(TestEntity entity) {
        TestPO po = toPO(entity);
        testMapper.updateById(po);
    }

    @Override
    public void deleteById(Long id) {
        testMapper.deleteById(id);
    }

    private TestEntity toEntity(TestPO po) {
        TestEntity entity = new TestEntity();
        entity.setId(po.getId());
        entity.setName(po.getName());
        entity.setDescription(po.getDescription());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }

    private TestPO toPO(TestEntity entity) {
        TestPO po = new TestPO();
        po.setId(entity.getId());
        po.setName(entity.getName());
        po.setDescription(entity.getDescription());
        return po;
    }
}
