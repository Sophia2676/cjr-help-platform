package com.cjr.platform.controller.admin;

import com.cjr.platform.common.Result;
import com.cjr.platform.security.annotation.RequireAdmin;
import com.cjr.platform.service.DashboardService;
import com.cjr.platform.vo.DashboardStatsVO;
import com.cjr.platform.vo.NameValueVO;
import com.cjr.platform.vo.PendingItemVO;
import com.cjr.platform.vo.TrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@RequireAdmin
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public Result<DashboardStatsVO> stats() {
        return Result.ok(dashboardService.stats());
    }

    @GetMapping("/trend")
    public Result<TrendVO> trend(@RequestParam(defaultValue = "7") int days) {
        return Result.ok(dashboardService.trend(days));
    }

    @GetMapping("/category")
    public Result<List<NameValueVO>> category() {
        return Result.ok(dashboardService.category());
    }

    @GetMapping("/latest")
    public Result<List<PendingItemVO>> latest(@RequestParam(defaultValue = "5") int limit) {
        return Result.ok(dashboardService.latest(limit));
    }
}
