<template>
  <div class="page">
    <h2>学习计划</h2>
    <el-button type="primary" style="margin:16px 0" @click="showCreate = true">创建计划</el-button>
    <el-row :gutter="16">
      <el-col :span="8" v-for="p in plans" :key="p.id">
        <el-card shadow="hover" style="margin-bottom:12px;cursor:pointer" @click="$router.push(`/plans/${p.id}`)">
          <h3>{{ p.title }}</h3>
          <el-progress :percentage="p.totalTopics ? Math.round(p.completedTopics / p.totalTopics * 100) : 0" style="margin:8px 0" />
          <div style="font-size:13px;color:#999">
            第 {{ p.currentDay }} / {{ p.totalDays }} 天 · 目标 {{ p.targetDate }}
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!plans.length" description="暂无计划，创建一个开始学习吧" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'

const plans = ref([])

onMounted(async () => {
  const res = await api.get('/plans')
  plans.value = res.data || []
})
</script>
