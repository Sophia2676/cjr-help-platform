package com.cjr.platform.controller.admin;

import com.cjr.platform.aspect.OperationLog;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.common.Result;
import com.cjr.platform.dto.AuditDTO;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.security.annotation.RequireAdmin;
import com.cjr.platform.service.ExperienceService;
import com.cjr.platform.vo.ExperienceVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/experience")
@RequiredArgsConstructor
@RequireAdmin
public class AdminExperienceController {

    private final ExperienceService experienceService;

    @GetMapping("/page")
    public Result<PageResult<ExperienceVO>> page(PageQuery query,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) Integer status) {
        return Result.ok(experienceService.adminPage(query, keyword, status));
    }

    @PutMapping("/{id}/audit")
    @OperationLog("审核康复经验")
    public Result<Void> audit(@PathVariable Long id, @Valid @RequestBody AuditDTO dto) {
        experienceService.audit(id, dto);
        return Result.ok("审核完成", null);
    }

    @DeleteMapping("/{id}")
    @OperationLog("管理端删除康复经验")
    public Result<Void> delete(@PathVariable Long id) {
        experienceService.adminDelete(id);
        return Result.ok("删除成功", null);
    }
}
