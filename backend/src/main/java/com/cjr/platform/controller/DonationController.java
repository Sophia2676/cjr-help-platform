package com.cjr.platform.controller;

import com.cjr.platform.aspect.OperationLog;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.common.Result;
import com.cjr.platform.dto.DonateDTO;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.security.UserContext;
import com.cjr.platform.security.annotation.RequireLogin;
import com.cjr.platform.service.DonationService;
import com.cjr.platform.vo.DonationVO;
import com.cjr.platform.vo.HelpRequestVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    /** 发起捐助(核心事务) */
    @PostMapping("/help/{id}/donate")
    @RequireLogin
    @OperationLog("爱心捐助")
    public Result<HelpRequestVO> donate(@PathVariable Long id, @Valid @RequestBody DonateDTO dto) {
        return Result.ok("捐助成功，感谢您的爱心！", donationService.donate(UserContext.getUserId(), id, dto));
    }

    /** 求助人手动完成筹款 */
    @PostMapping("/help/{id}/complete")
    @RequireLogin
    @OperationLog("完成筹款")
    public Result<Void> complete(@PathVariable Long id) {
        donationService.complete(UserContext.getUserId(), id);
        return Result.ok("已完成筹款", null);
    }

    /** 受助人确认收到单笔捐助 */
    @PostMapping("/donation/{id}/confirm")
    @RequireLogin
    @OperationLog("确认收到捐助")
    public Result<Void> confirm(@PathVariable Long id) {
        donationService.confirm(UserContext.getUserId(), id);
        return Result.ok("已确认", null);
    }

    /** 我捐出的记录 */
    @GetMapping("/donation/my")
    @RequireLogin
    public Result<PageResult<DonationVO>> myDonations(PageQuery query) {
        return Result.ok(donationService.myDonations(UserContext.getUserId(), query));
    }

    /** 我收到的捐助 */
    @GetMapping("/donation/received")
    @RequireLogin
    public Result<PageResult<DonationVO>> received(PageQuery query,
                                                   @RequestParam(required = false) Long helpRequestId) {
        return Result.ok(donationService.received(UserContext.getUserId(), query, helpRequestId));
    }
}
