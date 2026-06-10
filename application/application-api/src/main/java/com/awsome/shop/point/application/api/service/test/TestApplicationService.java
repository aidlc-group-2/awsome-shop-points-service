package com.awsome.shop.point.application.api.service.test;

import com.awsome.shop.point.application.api.dto.test.TestDTO;
import com.awsome.shop.point.application.api.dto.test.request.*;
import com.awsome.shop.point.common.dto.PageResult;

/**
 * Test 应用服务接口
 */
public interface TestApplicationService {

    TestDTO get(GetTestRequest request);

    PageResult<TestDTO> list(ListTestRequest request);

    TestDTO create(CreateTestRequest request);

    TestDTO update(UpdateTestRequest request);

    void delete(DeleteTestRequest request);
}
