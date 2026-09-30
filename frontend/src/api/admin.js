import request from './request'

// ---------- 审核 ----------
export const adminPostPage = (params) => request.get('/admin/post/page', { params })
export const adminPostAudit = (id, data) => request.put(`/admin/post/${id}/audit`, data)
export const adminPostTop = (id, data) => request.put(`/admin/post/${id}/top`, data)
export const adminPostDelete = (id) => request.delete(`/admin/post/${id}`)

export const adminExperiencePage = (params) => request.get('/admin/experience/page', { params })
export const adminExperienceAudit = (id, data) => request.put(`/admin/experience/${id}/audit`, data)
export const adminExperienceDelete = (id) => request.delete(`/admin/experience/${id}`)

export const adminHelpPage = (params) => request.get('/admin/help/page', { params })
export const adminHelpAudit = (id, data) => request.put(`/admin/help/${id}/audit`, data)
export const adminHelpDelete = (id) => request.delete(`/admin/help/${id}`)

export const adminDonationPage = (params) => request.get('/admin/donation/page', { params })
export const adminDonationDelete = (id) => request.delete(`/admin/donation/${id}`)

// ---------- 用户管理 ----------
export const adminUserPage = (params) => request.get('/admin/user/page', { params })
export const adminUserStatus = (id, status) => request.put(`/admin/user/${id}/status`, { status })
export const adminUserRole = (id, role) => request.put(`/admin/user/${id}/role`, { role })
export const adminUserDelete = (id) => request.delete(`/admin/user/${id}`)
export const adminUserResetPassword = (id, password) => request.put(`/admin/user/${id}/reset-password`, { password })

// ---------- 政策管理 ----------
export const adminPolicyPage = (params) => request.get('/admin/policy/page', { params })
export const adminPolicyCreate = (data) => request.post('/admin/policy', data)
export const adminPolicyUpdate = (id, data) => request.put(`/admin/policy/${id}`, data)
export const adminPolicyStatus = (id, status) => request.put(`/admin/policy/${id}/status`, { status })
export const adminPolicyDelete = (id) => request.delete(`/admin/policy/${id}`)

// ---------- 统计 ----------
export const adminStats = () => request.get('/admin/dashboard/stats')
export const adminTrend = (days = 7) => request.get('/admin/dashboard/trend', { params: { days } })
export const adminCategory = () => request.get('/admin/dashboard/category')
export const adminLatest = (limit = 5) => request.get('/admin/dashboard/latest', { params: { limit } })

// ---------- 日志 ----------
export const adminLogPage = (params) => request.get('/admin/log/page', { params })
export const adminLogClear = (before) => request.delete('/admin/log/clear', { params: { before } })
