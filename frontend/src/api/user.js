import request from './request'

export const getUser = (id) => request.get(`/user/${id}`)
export const updateProfile = (data) => request.put('/user/profile', data)
export const updatePassword = (data) => request.put('/user/password', data)
