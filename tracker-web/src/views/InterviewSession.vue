<template>
  <div class="page">
    <el-button @click="$router.back()">← 返回</el-button>
    <h3 style="margin:16px 0">{{ session?.title || '模拟面试' }}</h3>
    <div ref="chatBox" style="height:400px;overflow-y:auto;border:1px solid #eee;padding:16px;margin-bottom:16px;background:#fff;border-radius:8px">
      <div v-for="m in messages" :key="m.id" :style="{ textAlign: m.role === 'interviewer' ? 'left' : 'right', marginBottom: '12px' }">
        <div :style="{ background: m.role === 'interviewer' ? '#e8f4ff' : '#dcfce6', padding:'10px 14px', borderRadius:'8px', display:'inline-block', maxWidth:'80%', textAlign:'left' }">
          {{ m.content }}
          <el-tag v-if="m.score" size="small" :type="m.score >= 7 ? 'success' : m.score >= 4 ? 'warning' : 'danger'" style="margin-top:6px;display:block">
            评分 {{ m.score }} · {{ m.feedback }}
          </el-tag>
        </div>
      </div>
      <el-empty v-if="!messages.length" description="等待面试开始..." />
    </div>
    <el-input v-model="answer" type="textarea" :rows="3" placeholder="输入你的回答..." :disabled="session?.status === 1" />
    <div style="margin-top:8px;display:flex;gap:8px">
      <el-button type="primary" @click="submitAnswer" :disabled="!answer || session?.status === 1">提交回答</el-button>
      <el-button @click="skip" :disabled="session?.status === 1">跳过</el-button>
      <el-button type="danger" @click="end" :disabled="session?.status === 1">结束面试</el-button>
    </div>
    <!-- 面试报告 -->
    <el-dialog v-model="showReport" title="面试报告" width="700px">
      <div v-if="report">
        <p><strong>总体评价：</strong>{{ report.overall }}</p>
        <p><strong>预估等级：</strong>{{ report.estimate }}</p>
        <el-divider />
        <h4>建议提升</h4>
        <p>{{ report.suggestions }}</p>
        <h4 style="margin-top:16px">薄弱知识点</h4>
        <el-tag v-for="w in report.weakTopics" :key="w" type="danger" style="margin:2px">{{ w }}</el-tag>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api'

const route = useRoute()
const session = ref(null)
const messages = ref([])
const answer = ref('')
const showReport = ref(false)
const report = ref(null)
const chatBox = ref(null)

onMounted(async () => {
  // 获取消息列表（如果已有的话）
  const res = await api.get(`/interview/sessions/${route.params.id}/messages`).catch(() => ({ data: [] }))
  messages.value = res.data || []
  if (!messages.value.length) {
    // 新会话：创建
    const sessionRes = await api.post('/interview/sessions', {
      title: 'Java 后端模拟面试',
      moduleIds: '1,2,3',
      difficulty: 2
    })
    session.value = sessionRes.data
    const msgs = await api.get(`/interview/sessions/${session.value.id}/messages`)
    messages.value = msgs.data || []
  }
})

async function submitAnswer() {
  if (!answer.value) return
  const res = await api.post(`/interview/sessions/${route.params.id}/answer`, { answer: answer.value })
  messages.value.push({
    role: 'candidate', content: answer.value,
    score: res.data?.score, feedback: res.data?.feedback
  })
  answer.value = ''
  // 拉取 AI 追问
  const msgs = await api.get(`/interview/sessions/${route.params.id}/messages`)
  messages.value = msgs.data || []
  await nextTick()
  if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight
}

async function skip() {
  await api.post(`/interview/sessions/${route.params.id}/skip`)
  const msgs = await api.get(`/interview/sessions/${route.params.id}/messages`)
  messages.value = msgs.data || []
}

async function end() {
  const res = await api.post(`/interview/sessions/${route.params.id}/end`)
  report.value = res.data
  showReport.value = true
}
</script>
