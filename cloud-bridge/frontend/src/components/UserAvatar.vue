<template>
  <div class="user-avatar" :style="style">
    <span class="initials">{{ initials }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  username?: string
  size?: number
}>()

const colors = [
  '#f56a00', '#7265e6', '#ffbf00', '#00a2ae', '#1890ff', 
  '#eb2f96', '#52c41a', '#722ed1', '#fa8c16', '#eb2f96'
]

const initials = computed(() => {
  if (!props.username) return 'U'
  return props.username.substring(0, 1).toUpperCase()
})

const style = computed(() => {
  const size = props.size || 40
  const name = props.username || 'User'
  
  // Simple hash for color
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  const color = colors[Math.abs(hash) % colors.length]
  
  return {
    width: `${size}px`,
    height: `${size}px`,
    lineHeight: `${size}px`,
    fontSize: `${size * 0.5}px`,
    backgroundColor: color,
    color: '#fff',
    borderRadius: '50%',
    textAlign: 'center' as const,
    fontWeight: 'bold',
    display: 'inline-block',
    verticalAlign: 'middle'
  }
})
</script>

<style scoped>
.user-avatar {
  user-select: none;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
</style>