package com.awsome.shop.point.application.impl.service.test;

import com.awsome.shop.point.application.api.dto.test.TestDTO;
import com.awsome.shop.point.application.api.dto.test.request.*;
import com.awsome.shop.point.application.api.service.test.TestApplicationService;
import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.domain.model.test.TestEntity;
import com.awsome.shop.point.domain.service.test.TestDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Test 应用服务实现
 *
 * <p>只依赖 Domain Service，不直接依赖 Repository</p>
 */
@Service
@RequiredArgsConstructor
public class TestApplicationServiceImpl implements TestApplicationService {

    private final TestDomainService testDomainService;

    @Override
    public TestDTO get(GetTestRequest request) {
        return toDTO(testDomainService.getById(request.getId()));
    }

    @Override
    public PageResult<TestDTO> list(ListTestRequest request) {
        PageResult<TestEntity> page = testDomainService.page(
                request.getPage(), request.getSize(), request.getName());
        return page.convert(this::toDTO);
    }

    @Override
    public TestDTO create(CreateTestRequest request) {
        TestEntity entity = testDomainService.create(request.getName(), request.getDescription());
        return toDTO(entity);
    }

    @Override
    public TestDTO update(UpdateTestRequest request) {
        TestEntity entity = testDomainService.update(
                request.getId(), request.getName(), request.getDescription());
        return toDTO(entity);
    }

    @Override
    public void delete(DeleteTestRequest request) {
        testDomainService.delete(request.getId());
    }

    private TestDTO toDTO(TestEntity entity) {
        TestDTO dto = new TestDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
