/** 帖子分类 */
export const POST_CATEGORIES = ['求医问药', '生活求助', '出行交流', '心理互助', '求职就业', '其他']

/** 康复经验分类 */
export const EXPERIENCE_CATEGORIES = ['康复训练', '日常护理', '心理疏导', '辅助器具', '饮食营养', '其他']

/** 残疾类别 */
export const DISABILITY_TYPES = ['视力残疾', '听力残疾', '言语残疾', '肢体残疾', '智力残疾', '精神残疾', '多重残疾']

/** 帖子/经验审核状态 */
export const POST_STATUS = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已驳回', type: 'danger' }
}

/** 求助状态 */
export const HELP_STATUS = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '募捐中', type: 'success' },
  2: { label: '已完成', type: 'info' },
  3: { label: '已驳回', type: 'danger' }
}

/** 消息类型 */
export const MESSAGE_TYPES = {
  AUDIT: '审核通知',
  DONATION: '捐助动态',
  SYSTEM: '系统通知'
}

/** 性别 */
export const GENDERS = {
  0: '未设置',
  1: '男',
  2: '女'
}
