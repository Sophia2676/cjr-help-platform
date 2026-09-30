package com.cjr.platform.controller.admin;

import com.cjr.platform.aspect.OperationLog;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.common.Result;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.dto.PolicyDTO;
import com.cjr.platform.dto.StatusDTO;
import com.cjr.platform.entity.Policy;
import com.cjr.platform.security.UserContext;
import com.cjr.platform.security.annotation.RequireAdmin;
import com.cjr.platform.service.PolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/policy")
@RequiredArgsConstructor
@RequireAdmin
public class AdminPolicyController {

    private final PolicyService policyService;

    @GetMapping("/page")
    public Result<PageResult<Policy>> page(PageQuery query,
                                           @RequestParam(required = false) String keyword) {
        return Result.ok(policyService.adminPage(query, keyword));
    }

    @PostMapping
    @OperationLog("发布政策资讯")
    public Result<Long> create(@Valid @RequestBody PolicyDTO dto) {
        return Result.ok("发布成功", policyService.create(UserContext.getUserId(), dto));
    }

    @PutMapping("/{id}")
    @OperationLog("修改政策资讯")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PolicyDTO dto) {
        policyService.update(id, dto);
        return Result.ok("保存成功", null);
    }

    /** 下架/上架 */
    @PutMapping("/{id}/status")
    @OperationLog("政策上下架")
    public Result<Void> status(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        policyService.status(id, dto.getStatus());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog("删除政策资讯")
    public Result<Void> delete(@PathVariable Long id) {
        policyService.adminDelete(id);
        return Result.ok("删除成功", null);
    }
}
