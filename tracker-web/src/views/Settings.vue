<template>
  <div class="page">
    <h2>系统设置</h2>
    <el-card header="AI 配置" style="margin-top:16px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="提供商">
          <el-select v-model="form.provider">
            <el-option value="openai" label="OpenAI" />
            <el-option value="claude" label="Claude" />
            <el-option value="deepseek" label="DeepSeek" />
          </el-select>
        </el-form-item>
        <el-form-item label="API Key">
          <el-input v-model="form.apiKey" type="password" show-password placeholder="sk-..." />
        </el-form-item>
        <el-form-item label="模型">
          <el-input v-model="form.model" placeholder="gpt-4o / claude-opus-4-8 / deepseek-chat" />
        </el-form-item>
        <el-form-item label="Base URL" v-if="form.provider !== 'claude'">
          <el-input v-model="form.baseUrl" placeholder="https://api.openai.com" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save">保存</el-button>
        </el-form-item>
      </el-form>
      <div v-if="saved" style="margin-top:12px">当前启用: <el-tag>{{ saved.provider }}</el-tag> · {{ saved.model }} · Key: {{ saved.apiKey }}</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '@/api'

const form = reactive({ provider: 'openai', apiKey: '', model: '', baseUrl: '' })
const saved = ref(null)

onMounted(async () => {
  const res = await api.get('/ai/config')
  saved.value = res.data
})

async function save() {
  await api.post('/ai/config', form)
  const res = await api.get('/ai/config')
  saved.value = res.data
}
</script>
