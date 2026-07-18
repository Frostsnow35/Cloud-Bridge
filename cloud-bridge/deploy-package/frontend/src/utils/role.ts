export const BACKEND_ROLE_LABELS: Record<string, string> = {
  ENTERPRISE: '企业用户',
  EXPERT: '专家用户',
  RESEARCHER: '科研人员',
  MANAGER: '技术经理人',
  ADMIN: '管理员'
}

export const DEMAND_SIDE_ROLES = ['ENTERPRISE'] as const
export const ACHIEVEMENT_SIDE_ROLES = ['EXPERT', 'RESEARCHER', 'MANAGER'] as const

export type BackendRole = keyof typeof BACKEND_ROLE_LABELS

export function normalize_role(role?: string | null): string {
  return typeof role === 'string' ? role.toUpperCase() : ''
}

export function is_demand_side_role(role?: string | null): boolean {
  return DEMAND_SIDE_ROLES.includes(normalize_role(role) as (typeof DEMAND_SIDE_ROLES)[number])
}

export function is_achievement_side_role(role?: string | null): boolean {
  return ACHIEVEMENT_SIDE_ROLES.includes(
    normalize_role(role) as (typeof ACHIEVEMENT_SIDE_ROLES)[number]
  )
}

export function get_role_display_name(role?: string | null): string {
  const normalized_role = normalize_role(role)
  return BACKEND_ROLE_LABELS[normalized_role] || normalized_role || '普通用户'
}

export function get_business_role_label(role?: string | null): string {
  if (is_demand_side_role(role)) {
    return '需求方'
  }

  if (is_achievement_side_role(role)) {
    return '成果方'
  }

  if (normalize_role(role) === 'ADMIN') {
    return '平台管理员'
  }

  return '普通用户'
}
