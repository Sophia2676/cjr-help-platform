package com.cjr.platform.controller;

import com.cjr.platform.aspect.OperationLog;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.common.Result;
import com.cjr.platform.dto.HelpRequestDTO;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.security.UserContext;
import com.cjr.platform.security.annotation.RequireLogin;
import com.cjr.platform.security.annotation.RequireWorker;
import com.cjr.platform.service.HelpRequestService;
import com.cjr.platform.vo.HelpRequestVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/help")
@RequiredArgsConstructor
public class HelpRequestController {

    private final HelpRequestService helpRequestService;

    /** 公开求助分页(仅募捐中) */
    @GetMapping("/page")
    public Result<PageResult<HelpRequestVO>> page(PageQuery query) {
        return Result.ok(helpRequestService.page(query));
    }

    @GetMapping("/{id}")
    public Result<HelpRequestVO> detail(@PathVariable Long id) {
        return Result.ok(helpRequestService.detail(id));
    }

    @PostMapping
    @RequireLogin
    @OperationLog("发布求助")
    public Result<Long> create(@Valid @RequestBody HelpRequestDTO dto) {
        return Result.ok("发布成功，等待审核", helpRequestService.create(UserContext.getUserId(), dto));
    }

    @PutMapping("/{id}")
    @RequireLogin
    @OperationLog("修改求助")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody HelpRequestDTO dto) {
        helpRequestService.update(UserContext.getUserId(), id, dto);
        return Result.ok("修改成功，重新进入审核", null);
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    @OperationLog("删除求助")
    public Result<Void> delete(@PathVariable Long id) {
        helpRequestService.delete(UserContext.getUserId(), id);
        return Result.ok("删除成功", null);
    }

    /** 社区工作者面板：全部求助列表(含求助人手机号) */
    @GetMapping("/worker-page")
    @RequireWorker
    public Result<PageResult<HelpRequestVO>> workerPage(PageQuery query) {
        return Result.ok(helpRequestService.workerPage(query));
    }

    @GetMapping("/my")
    @RequireLogin
    public Result<PageResult<HelpRequestVO>> my(PageQuery query) {
        return Result.ok(helpRequestService.myPage(UserContext.getUserId(), query));
    }
}
