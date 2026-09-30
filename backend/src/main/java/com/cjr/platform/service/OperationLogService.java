package com.cjr.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.entity.OperationLog;

public interface OperationLogService extends IService<OperationLog> {

    PageResult<OperationLog> page(PageQuery query, String keyword, Long userId, String startTime, String endTime);

    void clear(String before);
}
