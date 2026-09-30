package com.cjr.platform.controller;

import com.cjr.platform.aspect.OperationLog;
import com.cjr.platform.common.Result;
import com.cjr.platform.dto.CommentDTO;
import com.cjr.platform.security.UserContext;
import com.cjr.platform.security.annotation.RequireLogin;
import com.cjr.platform.service.CommentService;
import com.cjr.platform.vo.CommentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** 帖子评论列表(一级评论含回复) */
    @GetMapping("/post/{postId}")
    public Result<List<CommentVO>> list(@PathVariable Long postId) {
        return Result.ok(commentService.listByPost(postId));
    }

    @PostMapping
    @RequireLogin
    @OperationLog("发表评论")
    public Result<Long> create(@Valid @RequestBody CommentDTO dto) {
        return Result.ok("评论成功", commentService.create(UserContext.getUserId(), dto));
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    @OperationLog("删除评论")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.delete(UserContext.getUserId(), id);
        return Result.ok("删除成功", null);
    }
}
