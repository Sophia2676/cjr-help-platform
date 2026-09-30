package com.cjr.platform.controller.admin;

import com.cjr.platform.aspect.OperationLog;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.common.Result;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.dto.ResetPasswordDTO;
import com.cjr.platform.dto.RoleDTO;
import com.cjr.platform.dto.StatusDTO;
import com.cjr.platform.security.annotation.RequireAdmin;
import com.cjr.platform.service.UserService;
import com.cjr.platform.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
@RequireAdmin
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/page")
    public Result<PageResult<UserVO>> page(PageQuery query,
                                           @RequestParam(required = false) String role,
                                           @RequestParam(required = false) Integer status) {
        return Result.ok(userService.adminPage(query, role, status));
    }

    /** 禁用/启用账号 */
    @PutMapping("/{id}/status")
    @OperationLog("禁用/启用用户")
    public Result<Void> status(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        userService.setStatus(id, dto.getStatus());
        return Result.ok();
    }

    /** 设置角色 USER/ADMIN */
    @PutMapping("/{id}/role")
    @OperationLog("设置用户角色")
    public Result<Void> role(@PathVariable Long id, @Valid @RequestBody RoleDTO dto) {
        userService.setRole(id, dto.getRole());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog("删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.ok("删除成功", null);
    }

    @PutMapping("/{id}/reset-password")
    @OperationLog("重置用户密码")
    public Result<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(id, dto.getPassword());
        return Result.ok("密码已重置", null);
    }
}
