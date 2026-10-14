<template>
  <!-- 管理员数据看板 -->
  <div>
    <!-- 核心指标卡片 -->
    <div class="stat-grid">
      <div v-for="card in cards" :key="card.label" class="stat-card">
        <el-icon class="stat-icon"><component :is="card.icon" /></el-icon>
        <div class="stat-label">{{ card.label }}</div>
        <div class="stat-value">{{ card.value }}</div>
        <div class="stat-foot">{{ card.foot }}</div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-grid">
      <div class="chart-card">
        <div class="chart-card-title">近 7 天订单趋势</div>
        <div ref="orderRef" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <div class="chart-card-title">近 7 天销售额</div>
        <div ref="salesRef" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <div class="chart-card-title">商品分类分布</div>
        <div ref="catRef" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <div class="chart-card-title">用户角色分布</div>
        <div ref="roleRef" class="chart-box"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 数据看板：4 指标卡片 + 4 ECharts 图
 * @author zyr
 */
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import {
  overview,
  orderTrend,
  salesTrend,
  productCategoryDistribution,
  userRoleDistribution
} from '@/api/stats'

const cards = ref([
  { label: '总用户数', value: '-', foot: '商城注册用户', icon: 'User' },
  { label: '商品总数', value: '-', foot: '在售商品', icon: 'Goods' },
  { label: '订单总数', value: '-', foot: '累计订单', icon: 'List' },
  { label: '累计销售额', value: '-', foot: '已支付订单金额', icon: 'Wallet' }
])

const orderRef = ref()
const salesRef = ref()
const catRef = ref()
const roleRef = ref()
const charts = []

/**
 * 加载全部看板数据
 */
const loadAll = async () => {
  const { data: ov } = await overview()
  cards.value[0].value = ov.userCount ?? 0
  cards.value[1].value = ov.productCount ?? 0
  cards.value[2].value = ov.orderCount ?? 0
  cards.value[3].value = '¥' + Number(ov.totalSales ?? 0).toFixed(2)

  const { data: ot } = await orderTrend(7)
  renderLine(orderRef.value, ot, '#6366f1', '订单数')

  const { data: st } = await salesTrend(7)
  renderBar(salesRef.value, st)

  const { data: cd } = await productCategoryDistribution()
  renderPie(catRef.value, cd, false)

  const { data: ur } = await userRoleDistribution()
  renderPie(roleRef.value, ur, true)
}

/** 折线图 */
const renderLine = (el, data, color, name) => {
  const chart = echarts.init(el)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: {
      type: 'category',
      data: (data || []).map((d) => d.date),
      axisLine: { lineStyle: { color: '#d1d5db' } }
    },
    yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { type: 'dashed' } } },
    series: [{
      name,
      type: 'line',
      smooth: true,
      symbolSize: 8,
      data: (data || []).map((d) => d.value),
      lineStyle: { width: 3, color },
      itemStyle: { color },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: color + '55' },
          { offset: 1, color: color + '05' }
        ])
      }
    }]
  })
  charts.push(chart)
}

/** 柱状图 */
const renderBar = (el, data) => {
  const chart = echarts.init(el)
  chart.setOption({
    tooltip: { trigger: 'axis', formatter: (p) => `${p[0].name}<br/>销售额：¥${p[0].value}` },
    grid: { left: 50, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: (data || []).map((d) => d.date) },
    yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed' } } },
    series: [{
      name: '销售额',
      type: 'bar',
      barWidth: '45%',
      data: (data || []).map((d) => d.value),
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#10b981' },
          { offset: 1, color: '#6ee7b7' }
        ])
      }
    }]
  })
  charts.push(chart)
}

/** 饼图 / 环形图 */
const renderPie = (el, data, donut) => {
  const chart = echarts.init(el)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}：{c}（{d}%）' },
    legend: { bottom: 0, icon: 'circle' },
    color: ['#6366f1', '#06b6d4', '#f59e0b', '#ef4444', '#10b981', '#8b5cf6'],
    series: [{
      type: 'pie',
      radius: donut ? ['45%', '70%'] : '65%',
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { formatter: '{b}\n{c}' },
      data: data || []
    }]
  })
  charts.push(chart)
}

/** 窗口缩放时重绘图表 */
const resizeAll = () => charts.forEach((c) => c.resize())

onMounted(() => {
  loadAll()
  window.addEventListener('resize', resizeAll)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeAll)
  charts.forEach((c) => c.dispose())
})
</script>
