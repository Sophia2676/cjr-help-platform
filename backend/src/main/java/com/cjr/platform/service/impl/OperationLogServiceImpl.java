package com.cjr.platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjr.platform.common.BusinessException;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.entity.OperationLog;
import com.cjr.platform.mapper.OperationLogMapper;
import com.cjr.platform.service.OperationLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public PageResult<OperationLog> page(PageQuery query, String keyword, Long userId, String startTime, String endTime) {
        Page<OperationLog> page = new Page<>(
                query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum(),
                query.getPageSize() == null || query.getPageSize() < 1 ? 10 : Math.min(query.getPageSize(), 100));
        lambdaQuery()
                .and(keyword != null && !keyword.isBlank(),
                        w -> w.like(OperationLog::getOperation, keyword).or().like(OperationLog::getUsername, keyword)
                                .or().like(OperationLog::getRequestUri, keyword))
                .eq(userId != null, OperationLog::getUserId, userId)
                .ge(startTime != null && !startTime.isBlank(), OperationLog::getCreateTime,
                        startTime == null || startTime.isBlank() ? null : parseStart(startTime))
                .le(endTime != null && !endTime.isBlank(), OperationLog::getCreateTime,
                        endTime == null || endTime.isBlank() ? null : parseEnd(endTime))
                .orderByDesc(OperationLog::getCreateTime)
                .page(page);
        return PageResult.of(page);
    }

    @Override
    public void clear(String before) {
        LocalDate date;
        try {
            date = before == null || before.isBlank() ? LocalDate.now().minusDays(30) : LocalDate.parse(before);
        } catch (DateTimeParseException e) {
            throw new BusinessException("日期格式不正确(应为 yyyy-MM-dd)");
        }
        lambdaUpdate().lt(OperationLog::getCreateTime, date.atStartOfDay()).remove();
    }

    private LocalDateTime parseStart(String s) {
        return LocalDate.parse(s).atStartOfDay();
    }

    private LocalDateTime parseEnd(String s) {
        return LocalDate.parse(s).atTime(LocalTime.MAX);
    }
}
