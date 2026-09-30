import request from './request'

export const getPostComments = (postId) => request.get(`/comment/post/${postId}`)
export const createComment = (data) => request.post('/comment', data)
export const deleteComment = (id) => request.delete(`/comment/${id}`)
