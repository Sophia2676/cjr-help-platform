import request from './request'

export const getMessagePage = (params) => request.get('/message/page', { params })
export const getUnreadCount = () => request.get('/message/unread/count')
export const markRead = (id) => request.put(`/message/${id}/read`)
export const markAllRead = (type) => request.put('/message/read/all', null, { params: type ? { type } : {} })
export const deleteMessage = (id) => request.delete(`/message/${id}`)
