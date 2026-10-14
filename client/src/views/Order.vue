<template>
  <!-- 订单管理：列表 + 状态筛选 + 详情 + 改状态 -->
  <div class="page-card">
    <div class="page-card-title">订单管理</div>

    <div style="display:flex;gap:12px;margin-bottom:16px;flex-wrap:wrap;">
      <el-input v-model="query.orderNo" placeholder="订单号" clearable style="width:240px" @keyup.enter="reload" @clear="reload" />
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width:160px" @change="reload">
        <el-option label="待支付" value="PENDING" />
        <el-option label="已支付" value="PAID" />
        <el-option label="已发货" value="SHIPPED" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
      <el-button type="primary" @click="reload">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="orderNo" label="订单号" min-width="180" />
      <el-table-column prop="username" label="用户" width="120" />
      <el-table-column label="金额" width="120">
        <template #default="{ row }">¥{{ Number(row.totalAmount).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" round>{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="receiver" label="收件人" width="100" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
          <el-button v-if="row.status === 'PENDING'" link type="success" @click="markPaid(row)">手动确认已支付</el-button>
          <el-button v-if="row.status === 'PAID'" link type="warning" @click="changeStatus(row, 'SHIPPED')">发货</el-button>
          <el-button v-if="row.status === 'SHIPPED'" link type="success" @click="changeStatus(row, 'COMPLETED')">完成</el-button>
          <el-button v-if="row.status === 'PENDING'" link type="danger" @click="changeStatus(row, 'CANCELLED')">取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.page"
      v-model:page-size="query.size"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @current-change="reload"
      @size-change="reload"
      style="margin-top:16px;justify-content:flex-end;"
    />

    <!-- 订单详情 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="640px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(detail.status)" round>{{ statusText(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户">{{ detail.username }}</el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ Number(detail.totalAmount).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="收件人">{{ detail.receiver }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ detail.phone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ detail.address }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ detail.paidAt || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="detail?.items || []" style="margin-top:12px" size="small">
        <el-table-column prop="productName" label="商品" min-width="200" />
        <el-table-column label="单价" width="100">
          <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="小计" width="100">
          <template #default="{ row }">¥{{ (row.price * row.quantity).toFixed(2) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 订单管理
 * @author zyr
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageOrders, getOrder, updateOrderStatus, markOrderPaid } from '@/api/order'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, orderNo: '', status: '' })

const reload = async () => {
  loading.value = true
  try {
    const { data } = await pageOrders(query)
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const statusText = (s) => ({ PENDING: '待支付', PAID: '已支付', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消' }[s] || s)
const statusType = (s) => ({ PENDING: 'warning', PAID: 'success', SHIPPED: 'primary', COMPLETED: 'info', CANCELLED: 'danger' }[s] || 'info')

// 订单详情
const detailVisible = ref(false)
const detail = ref(null)

const viewDetail = async (row) => {
  const { data } = await getOrder(row.orderNo)
  detail.value = data
  detailVisible.value = true
}

const changeStatus = async (row, status) => {
  await ElMessageBox.confirm(`确认将订单「${row.orderNo}」状态修改为「${statusText(status)}」？`, '提示', { type: 'warning' })
  await updateOrderStatus(row.orderNo, status)
  ElMessage.success('状态已更新')
  reload()
}

/**
 * 管理员手动确认订单已支付
 *
 * <p>本地无内网穿透时的兜底方案：管理员在支付宝沙箱付款成功后，
 * 点此按钮把订单标记为 PAID 并写入 MANUAL 支付流水</p>
 */
const markPaid = async (row) => {
  await ElMessageBox.confirm(
    `确认订单「${row.orderNo}」已收到付款？\n\n此操作会把订单标记为已支付并写入一条手动支付流水，\n通常用于支付宝异步回调不可达时的学习场景兜底。`,
    '手动确认支付',
    { type: 'warning', confirmButtonText: '已收款，确认', cancelButtonText: '取消' }
  )
  await markOrderPaid(row.orderNo)
  ElMessage.success('订单已标记为已支付')
  reload()
}

onMounted(reload)
</script>
