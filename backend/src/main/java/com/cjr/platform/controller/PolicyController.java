package com.cjr.platform.controller;

import com.cjr.platform.common.PageResult;
import com.cjr.platform.common.Result;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.service.PolicyService;
import com.cjr.platform.vo.PolicyDetailVO;
import com.cjr.platform.vo.PolicyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policy")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping("/page")
    public Result<PageResult<PolicyVO>> page(PageQuery query) {
        return Result.ok(policyService.page(query));
    }

    @GetMapping("/{id}")
    public Result<PolicyDetailVO> detail(@PathVariable Long id) {
        return Result.ok(policyService.detail(id));
    }
}
