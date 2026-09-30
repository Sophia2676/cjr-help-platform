import request from './request'

export const getHelpPage = (params) => request.get('/help/page', { params })
export const getHelpDetail = (id) => request.get(`/help/${id}`)
export const createHelp = (data) => request.post('/help', data)
export const updateHelp = (id, data) => request.put(`/help/${id}`, data)
export const deleteHelp = (id) => request.delete(`/help/${id}`)
export const getMyHelps = (params) => request.get('/help/my', { params })
export const donate = (id, data) => request.post(`/help/${id}/donate`, data)
export const completeHelp = (id) => request.post(`/help/${id}/complete`)

/** 社区工作者面板：全部求助列表(含求助人手机号) */
export const getWorkerHelpPage = (params) => request.get('/help/worker-page', { params })
