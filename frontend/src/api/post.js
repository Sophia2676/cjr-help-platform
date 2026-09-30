import request from './request'

export const getPostPage = (params) => request.get('/post/page', { params })
export const getPostDetail = (id) => request.get(`/post/${id}`)
export const createPost = (data) => request.post('/post', data)
export const updatePost = (id, data) => request.put(`/post/${id}`, data)
export const deletePost = (id) => request.delete(`/post/${id}`)
export const toggleLike = (id) => request.post(`/post/${id}/like`)
export const getMyPosts = (params) => request.get('/post/my', { params })
export const getUserPosts = (userId, params) => request.get(`/post/user/${userId}`, { params })

/** 智能推荐：按用户高频搜索词(>20次)推送相关帖子 */
export const getRecommendPostPage = (params) => request.get('/post/recommend', { params })
