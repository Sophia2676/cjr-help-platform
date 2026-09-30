package com.cjr.platform.controller.admin;

import com.cjr.platform.aspect.OperationLog;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.common.Result;
import com.cjr.platform.dto.AuditDTO;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.dto.StatusDTO;
import com.cjr.platform.security.annotation.RequireAdmin;
import com.cjr.platform.service.PostService;
import com.cjr.platform.vo.PostVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/post")
@RequiredArgsConstructor
@RequireAdmin
public class AdminPostController {

    private final PostService postService;

    /** 全状态帖子分页 */
    @GetMapping("/page")
    public Result<PageResult<PostVO>> page(PageQuery query,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(required = false) String category) {
        return Result.ok(postService.adminPage(query, keyword, status, category));
    }

    /** 审核(1通过 2驳回) */
    @PutMapping("/{id}/audit")
    @OperationLog("审核帖子")
    public Result<Void> audit(@PathVariable Long id, @Valid @RequestBody AuditDTO dto) {
        postService.audit(id, dto);
        return Result.ok("审核完成", null);
    }

    /** 置顶/取消置顶 */
    @PutMapping("/{id}/top")
    @OperationLog("帖子置顶操作")
    public Result<Void> top(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        postService.top(id, dto.getIsTop());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog("管理端删除帖子")
    public Result<Void> delete(@PathVariable Long id) {
        postService.adminDelete(id);
        return Result.ok("删除成功", null);
    }
}
