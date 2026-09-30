package com.cjr.platform.controller.admin;

import com.cjr.platform.aspect.OperationLog;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.common.Result;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.security.annotation.RequireAdmin;
import com.cjr.platform.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/log")
@RequiredArgsConstructor
@RequireAdmin
public class AdminLogController {

    private final OperationLogService operationLogService;

    @GetMapping("/page")
    public Result<PageResult<com.cjr.platform.entity.OperationLog>> page(PageQuery query,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) Long userId,
                                                 @RequestParam(required = false) String startTime,
                                                 @RequestParam(required = false) String endTime) {
        return Result.ok(operationLogService.page(query, keyword, userId, startTime, endTime));
    }

    /** 清理指定日期之前的日志 */
    @DeleteMapping("/clear")
    @OperationLog("清理操作日志")
    public Result<Void> clear(@RequestParam(required = false) String before) {
        operationLogService.clear(before);
        return Result.ok("清理完成", null);
    }
}
