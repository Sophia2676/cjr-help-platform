package com.cjr.platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjr.platform.common.BusinessException;
import com.cjr.platform.common.PageResult;
import com.cjr.platform.dto.PageQuery;
import com.cjr.platform.entity.Message;
import com.cjr.platform.mapper.MessageMapper;
import com.cjr.platform.service.MessageService;
import com.cjr.platform.vo.MessageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Override
    public PageResult<MessageVO> page(Long userId, PageQuery query, String type) {
        Page<Message> page = new Page<>(
                query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum(),
                query.getPageSize() == null || query.getPageSize() < 1 ? 10 : Math.min(query.getPageSize(), 100));
        lambdaQuery()
                .eq(Message::getReceiverId, userId)
                .eq(type != null && !type.isBlank(), Message::getType, type)
                .orderByDesc(Message::getCreateTime)
                .page(page);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public long unreadCount(Long userId) {
        return lambdaQuery().eq(Message::getReceiverId, userId).eq(Message::getIsRead, 0).count();
    }

    @Override
    public void markRead(Long userId, Long id) {
        Message msg = getById(id);
        if (msg == null || !msg.getReceiverId().equals(userId)) {
            throw new BusinessException("消息不存在");
        }
        if (msg.getIsRead() == 0) {
            Message update = new Message();
            update.setId(id);
            update.setIsRead(1);
            updateById(update);
        }
    }

    @Override
    public void markAllRead(Long userId, String type) {
        lambdaUpdate()
                .eq(Message::getReceiverId, userId)
                .eq(type != null && !type.isBlank(), Message::getType, type)
                .set(Message::getIsRead, 1)
                .update();
    }

    @Override
    public void delete(Long userId, Long id) {
        Message msg = getById(id);
        if (msg == null || !msg.getReceiverId().equals(userId)) {
            throw new BusinessException("消息不存在");
        }
        removeById(id);
    }

    @Override
    public void send(Long receiverId, String type, String title, String content, Long relatedId) {
        if (receiverId == null) {
            return;
        }
        Message msg = new Message();
        msg.setReceiverId(receiverId);
        msg.setType(type);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setRelatedId(relatedId);
        msg.setIsRead(0);
        save(msg);
    }

    private MessageVO toVO(Message m) {
        MessageVO vo = new MessageVO();
        BeanUtils.copyProperties(m, vo);
        return vo;
    }
}
