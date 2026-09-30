package com.cjr.platform.controller;

import com.cjr.platform.common.PageResult;
import com.cjr.platform.common.Result;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.security.UserContext;
import com.cjr.platform.security.annotation.RequireLogin;
import com.cjr.platform.service.MessageService;
import com.cjr.platform.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/page")
    @RequireLogin
    public Result<PageResult<MessageVO>> page(PageQuery query, @RequestParam(required = false) String type) {
        return Result.ok(messageService.page(UserContext.getUserId(), query, type));
    }

    @GetMapping("/unread/count")
    @RequireLogin
    public Result<Map<String, Long>> unreadCount() {
        Map<String, Long> data = new HashMap<>();
        data.put("count", messageService.unreadCount(UserContext.getUserId()));
        return Result.ok(data);
    }

    @PutMapping("/{id}/read")
    @RequireLogin
    public Result<Void> markRead(@PathVariable Long id) {
        messageService.markRead(UserContext.getUserId(), id);
        return Result.ok();
    }

    @PutMapping("/read/all")
    @RequireLogin
    public Result<Void> markAllRead(@RequestParam(required = false) String type) {
        messageService.markAllRead(UserContext.getUserId(), type);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    public Result<Void> delete(@PathVariable Long id) {
        messageService.delete(UserContext.getUserId(), id);
        return Result.ok("删除成功", null);
    }
}
