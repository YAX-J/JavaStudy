<template>
  <div class="page">
    <el-button @click="$router.back()">← 返回</el-button>
    <h3 style="margin:16px 0">今日任务</h3>
    <div v-if="todayTask?.items?.length">
      <el-card v-for="item in todayTask.items" :key="item.topicId" style="margin-bottom:8px">
        <div style="display:flex;align-items:center;justify-content:space-between">
          <div>
            <el-checkbox :model-value="item.checked" :disabled="item.checked" @change="doCheckIn(item)">
              {{ item.topicTitle }}
            </el-checkbox>
          </div>
          <el-tag v-if="item.checked" type="success">已打卡</el-tag>
        </div>
      </el-card>
    </div>
    <el-empty v-else description="今天没有学习任务，或计划已完成" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api'

const route = useRoute()
const todayTask = ref(null)

onMounted(async () => {
  const res = await api.get(`/plans/${route.params.id}/today`)
  todayTask.value = res.data
})

async function doCheckIn(item) {
  await api.post('/check-in', {
    planId: parseInt(route.params.id),
    topicId: item.topicId,
    checkDate: new Date().toISOString().slice(0, 10),
    durationMinutes: 30,
    feeling: 4
  })
  const res = await api.get(`/plans/${route.params.id}/today`)
  todayTask.value = res.data
}
</script>
