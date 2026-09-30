package com.cjr.platform.controller;

import com.cjr.platform.aspect.OperationLog;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.common.Result;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.dto.PostDTO;
import com.cjr.platform.security.UserContext;
import com.cjr.platform.security.annotation.RequireLogin;
import com.cjr.platform.service.PostService;
import com.cjr.platform.vo.PostDetailVO;
import com.cjr.platform.vo.PostVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /** 公开帖子分页(仅已通过, 置顶优先; sort=latest/hot) */
    @GetMapping("/page")
    public Result<PageResult<PostVO>> page(PageQuery query,
                                           @RequestParam(required = false) String category,
                                           @RequestParam(required = false) String sort) {
        return Result.ok(postService.page(query, category, sort));
    }

    /** 智能推荐(登录用户按高频搜索词推送相关帖子) */
    @GetMapping("/recommend")
    public Result<PageResult<PostVO>> recommend(PageQuery query) {
        return Result.ok(postService.recommendPage(query));
    }

    @GetMapping("/{id}")
    public Result<PostDetailVO> detail(@PathVariable Long id) {
        return Result.ok(postService.detail(id));
    }

    @PostMapping
    @RequireLogin
    @OperationLog("发布帖子")
    public Result<Long> create(@Valid @RequestBody PostDTO dto) {
        Long id = postService.create(UserContext.getUserId(), dto);
        return Result.ok("发布成功，等待审核", id);
    }

    @PutMapping("/{id}")
    @RequireLogin
    @OperationLog("修改帖子")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PostDTO dto) {
        postService.update(UserContext.getUserId(), id, dto);
        return Result.ok("修改成功，重新进入审核", null);
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    @OperationLog("删除帖子")
    public Result<Void> delete(@PathVariable Long id) {
        postService.delete(UserContext.getUserId(), id);
        return Result.ok("删除成功", null);
    }

    /** 点赞切换 */
    @PostMapping("/{id}/like")
    @RequireLogin
    public Result<Map<String, Object>> like(@PathVariable Long id) {
        return Result.ok(postService.toggleLike(UserContext.getUserId(), id));
    }

    /** 我的帖子 */
    @GetMapping("/my")
    @RequireLogin
    public Result<PageResult<PostVO>> my(PageQuery query, @RequestParam(required = false) Integer status) {
        return Result.ok(postService.myPage(UserContext.getUserId(), query, status));
    }

    /** 某用户已通过的帖子 */
    @GetMapping("/user/{userId}")
    public Result<PageResult<PostVO>> userPosts(@PathVariable Long userId, PageQuery query) {
        return Result.ok(postService.userPage(userId, query));
    }
}
