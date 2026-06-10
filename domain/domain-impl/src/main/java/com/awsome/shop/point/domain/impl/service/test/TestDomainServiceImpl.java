package com.awsome.shop.point.domain.impl.service.test;

import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.common.enums.SampleErrorCode;
import com.awsome.shop.point.common.exception.BusinessException;
import com.awsome.shop.point.domain.model.test.TestEntity;
import com.awsome.shop.point.domain.service.test.TestDomainService;
import com.awsome.shop.point.repository.test.TestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Test 领域服务实现
 */
@Service
@RequiredArgsConstructor
public class TestDomainServiceImpl implements TestDomainService {

    private final TestRepository testRepository;

    @Override
    public TestEntity getById(Long id) {
        TestEntity entity = testRepository.getById(id);
        if (entity == null) {
            throw new BusinessException(SampleErrorCode.RESOURCE_NOT_FOUND);
        }
        return entity;
    }

    @Override
    public PageResult<TestEntity> page(int page, int size, String name) {
        return testRepository.page(page, size, name);
    }

    @Override
    public TestEntity create(String name, String description) {
        TestEntity entity = new TestEntity();
        entity.setName(name);
        entity.setDescription(description);
        testRepository.save(entity);
        return testRepository.getById(entity.getId());
    }

    @Override
    public TestEntity update(Long id, String name, String description) {
        TestEntity entity = getById(id);
        entity.updateInfo(name, description);
        testRepository.update(entity);
        return testRepository.getById(id);
    }

    @Override
    public void delete(Long id) {
        getById(id); // 确认存在
        testRepository.deleteById(id);
    }
}
