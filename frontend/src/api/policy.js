import request from './request'

export const getPolicyPage = (params) => request.get('/policy/page', { params })
export const getPolicyDetail = (id) => request.get(`/policy/${id}`)
