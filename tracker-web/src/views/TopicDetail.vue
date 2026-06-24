<template>
  <div class="page">
    <el-button @click="$router.back()">← 返回</el-button>
    <h2 style="margin:16px 0">{{ detail?.title }}</h2>
    <el-descriptions :column="3" border style="margin-bottom:20px">
      <el-descriptions-item label="模块">{{ detail?.moduleName }}</el-descriptions-item>
      <el-descriptions-item label="状态"><el-tag :type="statusType(detail?.status)">{{ statusLabel(detail?.status) }}</el-tag></el-descriptions-item>
      <el-descriptions-item label="掌握度">{{ detail?.masteryLevel || 0 }} / 5</el-descriptions-item>
    </el-descriptions>
    <el-button type="primary" @click="showStatusDialog = true">更新状态</el-button>

    <el-tabs v-model="activeTab" style="margin-top:24px">
      <el-tab-pane label="笔记" name="notes">
        <div style="margin-bottom:12px">
          <el-button type="primary" @click="showNoteForm = true">写笔记</el-button>
        </div>
        <el-card v-for="n in notes" :key="n.id" style="margin-bottom:8px">
          <h4>{{ n.title }}</h4>
          <div v-html="md(n.content)" style="max-height:100px;overflow:hidden"></div>
          <el-button size="small" style="margin-top:8px" @click="editingNote = n">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteNote(n.id)">删除</el-button>
        </el-card>
        <el-empty v-if="!notes.length" description="暂无笔记" />
      </el-tab-pane>
      <el-tab-pane label="代码练习" name="code">
        <el-card v-for="ex in exercises" :key="ex.id" style="margin-bottom:8px">
          <h4>{{ ex.title }}</h4>
          <p style="color:#999;font-size:13px">{{ ex.description?.substring(0, 100) }}</p>
          <el-button size="small" type="primary" style="margin-top:8px" @click="openCodeEditor(ex)">开始练习</el-button>
        </el-card>
        <el-empty v-if="!exercises.length" description="暂无练习" />
      </el-tab-pane>
    </el-tabs>

    <!-- 更新状态弹窗 -->
    <el-dialog v-model="showStatusDialog" title="更新状态" width="400px">
      <el-form>
        <el-form-item label="状态">
          <el-select v-model="statusForm.status">
            <el-option :value="0" label="未开始" />
            <el-option :value="1" label="学习中" />
            <el-option :value="2" label="已掌握" />
          </el-select>
        </el-form-item>
        <el-form-item label="掌握度">
          <el-rate v-model="statusForm.masteryLevel" :max="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showStatusDialog = false">取消</el-button>
        <el-button type="primary" @click="updateStatus">确定</el-button>
      </template>
    </el-dialog>

    <!-- 笔记编辑弹窗 -->
    <el-dialog v-model="showNoteForm" :title="editingNote ? '编辑笔记' : '写笔记'" width="700px">
      <el-input v-model="noteForm.title" placeholder="标题" style="margin-bottom:12px" />
      <el-input v-model="noteForm.content" type="textarea" :rows="10" placeholder="支持 Markdown" />
      <template #footer>
        <el-button @click="showNoteForm = false;editingNote = null">取消</el-button>
        <el-button type="primary" @click="saveNote">{{ editingNote ? '更新' : '保存' }}</el-button>
      </template>
    </el-dialog>

    <!-- 代码编辑器弹窗 -->
    <el-dialog v-model="showCodeDialog" title="代码练习" width="800px" destroy-on-close>
      <div ref="editorRef" style="height:300px;border:1px solid #ddd"></div>
      <el-button type="primary" style="margin-top:12px" @click="runCode">运行</el-button>
      <pre v-if="runResult" style="background:#f5f5f5;padding:12px;margin-top:12px;max-height:200px;overflow:auto">{{ runResult }}</pre>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api'
import MarkdownIt from 'markdown-it'
import * as monaco from 'monaco-editor'

const route = useRoute()
const detail = ref(null)
const notes = ref([])
const exercises = ref([])
const activeTab = ref('notes')
const showStatusDialog = ref(false)
const showNoteForm = ref(false)
const showCodeDialog = ref(false)
const editingNote = ref(null)
const statusForm = ref({ status: 0, masteryLevel: 0 })
const noteForm = ref({ title: '', content: '' })
const runResult = ref('')
const editorRef = ref(null)
let monacoEditor = null

const md = new MarkdownIt()

onMounted(async () => {
  const [dRes, nRes, eRes] = await Promise.all([
    api.get(`/modules/topics/${route.params.id}`),
    api.get(`/topics/${route.params.id}/notes`),
    api.get(`/topics/${route.params.id}/exercises`)
  ])
  detail.value = dRes.data
  notes.value = nRes.data || []
  exercises.value = eRes.data || []
  statusForm.value = { status: detail.value?.status || 0, masteryLevel: detail.value?.masteryLevel || 0 }
})

async function updateStatus() {
  await api.put(`/modules/topics/${route.params.id}/status`, statusForm.value)
  showStatusDialog.value = false
  location.reload()
}

async function saveNote() {
  if (editingNote.value) {
    await api.put(`/notes/${editingNote.value.id}`, noteForm.value)
  } else {
    await api.post(`/topics/${route.params.id}/notes`, noteForm.value)
  }
  showNoteForm.value = false; editingNote.value = null; noteForm.value = { title: '', content: '' }
  const res = await api.get(`/topics/${route.params.id}/notes`)
  notes.value = res.data || []
}

async function deleteNote(id) {
  await api.delete(`/notes/${id}`)
  const res = await api.get(`/topics/${route.params.id}/notes`)
  notes.value = res.data || []
}

async function openCodeEditor(ex) {
  showCodeDialog.value = true
  runResult.value = ''
  await nextTick()
  if (monacoEditor) monacoEditor.dispose()
  monacoEditor = monaco.editor.create(editorRef.value, {
    value: ex.templateCode || '// 写你的代码',
    language: 'java',
    theme: 'vs-light',
    minimap: { enabled: false }
  })
}

async function runCode() {
  const code = monacoEditor?.getValue() || ''
  const res = await api.post(`/exercises/${route.params.id}/run`, { code, exerciseId: route.params.id })
  runResult.value = res.data?.output || res.data?.errorMessage || '运行完成'
}

function statusType(s) { return s === 2 ? 'success' : s === 1 ? 'warning' : 'info' }
function statusLabel(s) { return s === 2 ? '已掌握' : s === 1 ? '学习中' : '未开始' }
</script>
