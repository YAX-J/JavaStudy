<template>
  <div id="app-shell">
    <el-container>
      <el-aside width="220px">
        <div class="logo">📚 学迹</div>
        <el-menu :router="true" :default-active="route.path" class="side-menu">
          <el-menu-item index="/dashboard">
            <el-icon><DataAnalysis /></el-icon> 学习看板
          </el-menu-item>
          <el-menu-item index="/topics">
            <el-icon><Collection /></el-icon> 知识点
          </el-menu-item>
          <el-menu-item index="/plans">
            <el-icon><Calendar /></el-icon> 学习计划
          </el-menu-item>
          <el-menu-item index="/calendar">
            <el-icon><Clock /></el-icon> 打卡日历
          </el-menu-item>
          <el-menu-item index="/interview">
            <el-icon><ChatDotRound /></el-icon> AI 面试
          </el-menu-item>
          <el-menu-item index="/settings">
            <el-icon><Setting /></el-icon> 设置
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
#app-shell { height: 100vh; overflow: hidden; }
.logo { padding: 20px; font-size: 22px; font-weight: 700; text-align: center; color: #409eff; border-bottom: 1px solid #eee; }
.side-menu { border-right: none; }
.el-main { background: #f5f7fa; overflow-y: auto; height: 100vh; padding: 24px; }
</style>
