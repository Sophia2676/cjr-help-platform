import request from './request'

export const getExperiencePage = (params) => request.get('/experience/page', { params })
export const getExperienceDetail = (id) => request.get(`/experience/${id}`)
export const createExperience = (data) => request.post('/experience', data)
export const updateExperience = (id, data) => request.put(`/experience/${id}`, data)
export const deleteExperience = (id) => request.delete(`/experience/${id}`)
export const getMyExperiences = (params) => request.get('/experience/my', { params })
