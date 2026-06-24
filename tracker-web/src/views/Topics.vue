<template>
  <div class="page">
    <h2 style="margin-bottom:20px">知识点</h2>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-menu :default-active="String(activeModule)" @select="handleModuleSelect">
          <el-menu-item v-for="m in modules" :key="m.id" :index="String(m.id)">
            {{ m.name }} ({{ m.topics?.length || 0 }})
          </el-menu-item>
        </el-menu>
      </el-col>
      <el-col :span="18">
        <el-radio-group v-model="filterStatus" @change="loadModules" style="margin-bottom:12px">
          <el-radio-button :value="null">全部</el-radio-button>
          <el-radio-button :value="0">未开始</el-radio-button>
          <el-radio-button :value="1">学习中</el-radio-button>
          <el-radio-button :value="2">已掌握</el-radio-button>
        </el-radio-group>
        <div v-if="topics.length">
          <el-card v-for="t in topics" :key="t.id" style="margin-bottom:8px;cursor:pointer" @click="$router.push(`/topics/${t.id}`)">
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span>{{ t.title }}</span>
              <el-tag :type="statusType(t.status)">{{ statusLabel(t.status) }}</el-tag>
            </div>
          </el-card>
        </div>
        <el-empty v-else description="暂无知识点" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'

const modules = ref([])
const topics = ref([])
const activeModule = ref(null)
const filterStatus = ref(null)

onMounted(loadModules)

async function loadModules() {
  const res = await api.get('/modules', { params: { status: filterStatus.value } })
  modules.value = res.data || []
  if (modules.value.length && !activeModule.value) {
    activeModule.value = modules.value[0].id
    handleModuleSelect(activeModule.value)
  } else if (activeModule.value) {
    handleModuleSelect(activeModule.value)
  }
}

function handleModuleSelect(id) {
  activeModule.value = parseInt(id)
  const m = modules.value.find(m => m.id === parseInt(id))
  topics.value = m?.topics || []
}

function statusType(s) { return s === 2 ? 'success' : s === 1 ? 'warning' : 'info' }
function statusLabel(s) { return s === 2 ? '已掌握' : s === 1 ? '学习中' : '未开始' }
</script>
