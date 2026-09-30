package com.cjr.platform.controller.admin;

import com.cjr.platform.aspect.OperationLog;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.common.Result;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.security.annotation.RequireAdmin;
import com.cjr.platform.service.DonationService;
import com.cjr.platform.vo.DonationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/donation")
@RequiredArgsConstructor
@RequireAdmin
public class AdminDonationController {

    private final DonationService donationService;

    @GetMapping("/page")
    public Result<PageResult<DonationVO>> page(PageQuery query,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) Long helpRequestId) {
        return Result.ok(donationService.adminPage(query, keyword, helpRequestId));
    }

    /** 删除异常捐助并回滚金额 */
    @DeleteMapping("/{id}")
    @OperationLog("管理端撤销捐助")
    public Result<Void> delete(@PathVariable Long id) {
        donationService.adminDelete(id);
        return Result.ok("已撤销并回滚金额", null);
    }
}
