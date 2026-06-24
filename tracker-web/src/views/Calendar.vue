<template>
  <div class="page">
    <h2>打卡日历</h2>
    <el-select v-model="month" @change="loadCalendar" style="margin:16px 0">
      <el-option v-for="m in months" :key="m" :value="m" :label="m" />
    </el-select>
    <div style="display:flex;gap:20px;margin-bottom:20px">
      <el-statistic title="当前连续" :value="calendar?.currentStreak || 0" suffix="天" />
      <el-statistic title="最长连续" :value="calendar?.longestStreak || 0" suffix="天" />
    </div>
    <el-calendar v-if="calendar">
      <template #date-cell="{ data }">
        <div :style="{ background: checkedSet.has(data.date) ? '#67c23a55' : '', borderRadius:'4px', padding:'4px' }">
          {{ data.day.split('-')[2] }}
        </div>
      </template>
    </el-calendar>
    <el-empty v-else description="请先选择一个计划" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import api from '@/api'

const month = ref(new Date().toISOString().slice(0, 7))
const calendar = ref(null)
const months = computed(() => {
  const ms = []
  for (let i = 0; i < 12; i++) {
    const d = new Date(); d.setMonth(d.getMonth() - i)
    ms.push(d.toISOString().slice(0, 7))
  }
  return ms
})
const checkedSet = computed(() => {
  if (!calendar.value?.checkedDates) return new Set()
  return new Set(calendar.value.checkedDates.map(d => d.substring(0, 10)))
})

onMounted(loadCalendar)

async function loadCalendar() {
  const plans = await api.get('/plans')
  if (plans.data?.length) {
    // 简化：查第一个计划的日历
    const res = await api.get(`/check-in/calendar/${plans.data[0].id}`, { params: { month: month.value } })
    calendar.value = res.data
  }
}
</script>
