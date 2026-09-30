package com.cjr.platform.controller;

import com.cjr.platform.aspect.OperationLog;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.common.Result;
import com.cjr.platform.dto.ExperienceDTO;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.security.UserContext;
import com.cjr.platform.security.annotation.RequireLogin;
import com.cjr.platform.service.ExperienceService;
import com.cjr.platform.vo.ExperienceDetailVO;
import com.cjr.platform.vo.ExperienceVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/experience")
@RequiredArgsConstructor
public class ExperienceController {

    private final ExperienceService experienceService;

    @GetMapping("/page")
    public Result<PageResult<ExperienceVO>> page(PageQuery query,
                                                 @RequestParam(required = false) String category) {
        return Result.ok(experienceService.page(query, category));
    }

    @GetMapping("/{id}")
    public Result<ExperienceDetailVO> detail(@PathVariable Long id) {
        return Result.ok(experienceService.detail(id));
    }

    @PostMapping
    @RequireLogin
    @OperationLog("发布康复经验")
    public Result<Long> create(@Valid @RequestBody ExperienceDTO dto) {
        return Result.ok("发布成功，等待审核", experienceService.create(UserContext.getUserId(), dto));
    }

    @PutMapping("/{id}")
    @RequireLogin
    @OperationLog("修改康复经验")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ExperienceDTO dto) {
        experienceService.update(UserContext.getUserId(), id, dto);
        return Result.ok("修改成功，重新进入审核", null);
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    @OperationLog("删除康复经验")
    public Result<Void> delete(@PathVariable Long id) {
        experienceService.delete(UserContext.getUserId(), id);
        return Result.ok("删除成功", null);
    }

    @GetMapping("/my")
    @RequireLogin
    public Result<PageResult<ExperienceVO>> my(PageQuery query, @RequestParam(required = false) Integer status) {
        return Result.ok(experienceService.myPage(UserContext.getUserId(), query, status));
    }
}
