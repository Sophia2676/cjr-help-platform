import request from './request'

export const confirmDonation = (id) => request.post(`/donation/${id}/confirm`)
export const getMyDonations = (params) => request.get('/donation/my', { params })
export const getReceivedDonations = (params) => request.get('/donation/received', { params })
