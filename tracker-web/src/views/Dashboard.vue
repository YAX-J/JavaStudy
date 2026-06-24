<template>
  <div class="dashboard">
    <h2 style="margin-bottom:20px">学习看板</h2>

    <el-row :gutter="20" style="margin-bottom:20px">
      <el-col :span="6" v-for="c in cards" :key="c.label">
        <el-card shadow="hover">
          <div class="card-val">{{ c.value }}</div>
          <div class="card-label">{{ c.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-bottom:20px">
      <el-col :span="12">
        <el-card header="各模块掌握进度">
          <div ref="radarChart" style="height:350px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="本周回顾">
          <p v-if="weekly"><strong>{{ weekly.checkDays }}</strong> 天共学习 <strong>{{ weekly.weeklyMinutes }}</strong> 分钟</p>
          <p style="color:#999;margin-top:8px">{{ weekly?.summary }}</p>
          <h4 style="margin-top:16px">薄弱知识点</h4>
          <el-tag v-for="w in weakTopics" :key="w.topicId" type="danger" style="margin:4px">
            {{ w.title }} (反复{{ w.changeCount }}次)
          </el-tag>
          <p v-if="!weakTopics.length" style="color:#999">暂无薄弱点，继续加油！</p>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card header="近半年学习趋势">
          <div ref="trendChart" style="height:300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import api from '@/api'

const cards = ref([
  { label: '总知识点', value: 0 },
  { label: '已掌握', value: 0 },
  { label: '连续打卡', value: 0 },
  { label: '本周学习时长', value: '0 分钟' }
])
const weakTopics = ref([])
const weekly = ref(null)
const radarChart = ref(null)
const trendChart = ref(null)

onMounted(async () => {
  const [overview, modules, trend, weak, wr] = await Promise.all([
    api.get('/stats/overview'),
    api.get('/stats/module-progress'),
    api.get('/stats/trend'),
    api.get('/stats/weak-topics'),
    api.get('/stats/weekly-report')
  ])
  if (overview.data) {
    const o = overview.data
    cards.value = [
      { label: '总知识点', value: o.totalTopics },
      { label: '已掌握', value: o.mastered },
      { label: '连续打卡', value: o.streak + ' 天' },
      { label: '本周学习时长', value: o.weeklyMinutes + ' 分钟' }
    ]
  }
  weakTopics.value = weak.data || []
  weekly.value = wr.data

  await nextTick()
  renderRadar(modules.data || [])
  renderTrend(trend.data || [])
})

function renderRadar(data) {
  if (!radarChart.value) return
  const chart = echarts.init(radarChart.value)
  chart.setOption({
    radar: {
      indicator: data.map(d => ({ name: d.moduleName, max: 100 })),
      center: ['50%', '55%'], radius: '65%'
    },
    series: [{ type: 'radar', data: [{ value: data.map(d => d.percent), name: '掌握度 %' }],
      areaStyle: { color: 'rgba(64,158,255,0.2)' }, lineStyle: { color: '#409eff' } }]
  })
}

function renderTrend(data) {
  if (!trendChart.value) return
  const chart = echarts.init(trendChart.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.map(d => d.date) },
    yAxis: { type: 'value', name: '分钟' },
    series: [{ type: 'bar', data: data.map(d => d.minutes), itemStyle: { color: '#409eff' } }]
  })
}
</script>

<style scoped>
.card-val { font-size: 32px; font-weight: 700; color: #409eff; }
.card-label { font-size: 13px; color: #999; margin-top: 4px; }
</style>
