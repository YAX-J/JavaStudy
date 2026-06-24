import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/dashboard' },
    { path: '/dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue') },
    { path: '/topics', name: 'Topics', component: () => import('@/views/Topics.vue') },
    { path: '/topics/:id', name: 'TopicDetail', component: () => import('@/views/TopicDetail.vue') },
    { path: '/plans', name: 'Plans', component: () => import('@/views/Plans.vue') },
    { path: '/plans/:id', name: 'PlanDetail', component: () => import('@/views/PlanDetail.vue') },
    { path: '/calendar', name: 'Calendar', component: () => import('@/views/Calendar.vue') },
    { path: '/interview', name: 'Interview', component: () => import('@/views/Interview.vue') },
    { path: '/interview/:id', name: 'InterviewSession', component: () => import('@/views/InterviewSession.vue') },
    { path: '/settings', name: 'Settings', component: () => import('@/views/Settings.vue') },
  ]
})

export default router
