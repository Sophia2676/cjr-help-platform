package com.cjr.platform.controller.admin;

import com.cjr.platform.aspect.OperationLog;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.common.Result;
import com.cjr.platform.dto.AuditDTO;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.security.annotation.RequireAdmin;
import com.cjr.platform.service.HelpRequestService;
import com.cjr.platform.vo.HelpRequestVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/help")
@RequiredArgsConstructor
@RequireAdmin
public class AdminHelpController {

    private final HelpRequestService helpRequestService;

    @GetMapping("/page")
    public Result<PageResult<HelpRequestVO>> page(PageQuery query,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) Integer status) {
        return Result.ok(helpRequestService.adminPage(query, keyword, status));
    }

    /** 审核(1通过进入募捐中 3驳回) */
    @PutMapping("/{id}/audit")
    @OperationLog("审核求助")
    public Result<Void> audit(@PathVariable Long id, @Valid @RequestBody AuditDTO dto) {
        helpRequestService.audit(id, dto);
        return Result.ok("审核完成", null);
    }

    @DeleteMapping("/{id}")
    @OperationLog("管理端删除求助")
    public Result<Void> delete(@PathVariable Long id) {
        helpRequestService.adminDelete(id);
        return Result.ok("删除成功", null);
    }
}
