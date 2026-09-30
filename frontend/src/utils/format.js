import dayjs from 'dayjs'

/** 相对时间(今天/昨天/具体日期) */
export const formatTime = (time) => {
  if (!time) return ''
  const date = dayjs(time)
  if (date.isSame(dayjs(), 'day')) {
    return date.format('HH:mm')
  }
  if (date.isSame(dayjs().subtract(1, 'day'), 'day')) {
    return '昨天 ' + date.format('HH:mm')
  }
  if (date.isSame(dayjs(), 'year')) {
    return date.format('MM-DD HH:mm')
  }
  return date.format('YYYY-MM-DD')
}

/** 完整时间 */
export const formatDateTime = (time) => (time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '')

/** 金额格式化 */
export const formatMoney = (amount) => {
  if (amount === null || amount === undefined) return '0.00'
  return Number(amount).toFixed(2)
}
