package com.cjr.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjr.platform.dto.CommentDTO;
import com.cjr.platform.entity.Comment;
import com.cjr.platform.vo.CommentVO;

import java.util.List;

public interface CommentService extends IService<Comment> {

    /** 帖子评论列表(一级评论含回复) */
    List<CommentVO> listByPost(Long postId);

    Long create(Long userId, CommentDTO dto);

    void delete(Long userId, Long id);
}
