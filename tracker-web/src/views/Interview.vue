<template>
  <div class="page">
    <h2>AI 模拟面试</h2>
    <el-button type="primary" style="margin:16px 0" @click="$router.push('/interview/' + new Date().getTime())">创建面试</el-button>
    <el-card v-for="s in sessions" :key="s.id" style="margin-bottom:8px;cursor:pointer" @click="$router.push(`/interview/${s.id}`)">
      <div style="display:flex;justify-content:space-between">
        <div>
          <strong>{{ s.title }}</strong>
          <span style="color:#999;margin-left:12px">{{ s.questionCount }} 题</span>
        </div>
        <el-tag :type="s.status === 1 ? 'info' : 'success'">{{ s.status === 1 ? '已结束' : '进行中' }}</el-tag>
      </div>
    </el-card>
    <el-empty v-if="!sessions.length" description="暂无面试记录，开始一场模拟面试吧" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'

const sessions = ref([])
onMounted(async () => {
  const res = await api.get('/interview/sessions')
  sessions.value = res.data || []
})
</script>
