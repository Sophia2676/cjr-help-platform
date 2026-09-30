package com.cjr.platform.controller;

import com.cjr.platform.aspect.OperationLog;
import com.cjr.platform.common.Result;
import com.cjr.platform.dto.UpdatePasswordDTO;
import com.cjr.platform.dto.UpdateProfileDTO;
import com.cjr.platform.security.UserContext;
import com.cjr.platform.security.annotation.RequireLogin;
import com.cjr.platform.service.UserService;
import com.cjr.platform.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 用户公开信息 */
    @GetMapping("/{id}")
    public Result<UserVO> getUser(@PathVariable Long id) {
        return Result.ok(userService.getUserById(id));
    }

    /** 修改个人资料(仅本人) */
    @PutMapping("/profile")
    @RequireLogin
    @OperationLog("修改个人资料")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        userService.updateProfile(UserContext.getUserId(), dto);
        return Result.ok("保存成功", null);
    }

    /** 修改密码 */
    @PutMapping("/password")
    @RequireLogin
    @OperationLog("修改密码")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordDTO dto) {
        userService.updatePassword(UserContext.getUserId(), dto);
        return Result.ok("密码修改成功", null);
    }
}
