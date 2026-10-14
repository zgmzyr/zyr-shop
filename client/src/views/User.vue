<template>
  <!-- 用户管理：列表 + 新增 + 编辑 + 重置密码 + 启停 + 删除 -->
  <div class="page-card">
    <div class="page-card-title">
      <span>用户管理</span>
      <el-button type="primary" @click="openForm()">新增用户</el-button>
    </div>

    <div style="display:flex;gap:12px;margin-bottom:16px;flex-wrap:wrap;">
      <el-input v-model="query.keyword" placeholder="用户名/昵称" clearable style="width:240px" @keyup.enter="reload" @clear="reload" />
      <el-select v-model="query.role" placeholder="全部角色" clearable style="width:140px" @change="reload">
        <el-option label="管理员" value="ADMIN" />
        <el-option label="普通用户" value="USER" />
      </el-select>
      <el-button type="primary" @click="reload">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'warning' : 'success'" round>
            {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" round>
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="170" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openForm(row)">编辑</el-button>
          <el-button link type="warning" @click="resetPwd(row)">重置密码</el-button>
          <el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
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

    <!-- 用户表单 -->
    <el-dialog v-model="formVisible" :title="form.id ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名" v-if="!form.id">
          <el-input v-model="form.username" placeholder="登录用户名" />
        </el-form-item>
        <el-form-item label="用户名" v-else>
          <el-input :value="form.username" disabled />
        </el-form-item>
        <el-form-item label="密码" v-if="!form.id">
          <el-input v-model="form.password" type="password" placeholder="默认密码 123456" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width:100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 用户管理
 * @author zyr
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageUsers, createUser, updateUser, resetPassword, deleteUser } from '@/api/user'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, keyword: '', role: '' })

const reload = async () => {
  loading.value = true
  try {
    const { data } = await pageUsers(query)
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const formVisible = ref(false)
const submitting = ref(false)
const form = reactive({ id: null, username: '', password: '123456', role: 'USER', nickname: '', phone: '', status: 1 })

const openForm = (row) => {
  Object.assign(form, { id: null, username: '', password: '123456', role: 'USER', nickname: '', phone: '', status: 1 })
  if (row) {
    Object.assign(form, { ...row, password: '' })
  }
  formVisible.value = true
}

const submit = async () => {
  if (!form.username) { ElMessage.warning('请填写用户名'); return }
  if (!form.id && !form.password) { ElMessage.warning('请填写密码'); return }
  submitting.value = true
  try {
    if (form.id) {
      await updateUser(form.id, form)
    } else {
      await createUser(form)
    }
    ElMessage.success('保存成功')
    formVisible.value = false
    reload()
  } finally {
    submitting.value = false
  }
}

const resetPwd = async (row) => {
  await ElMessageBox.confirm(`确认将「${row.username}」的密码重置为 123456？`, '提示', { type: 'warning' })
  await resetPassword(row.id)
  ElMessage.success('密码已重置为 123456')
}

const toggleStatus = async (row) => {
  const next = row.status === 1 ? 0 : 1
  await updateUser(row.id, { status: next })
  ElMessage.success(next === 1 ? '已启用' : '已禁用')
  reload()
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除用户「${row.username}」？`, '提示', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('已删除')
  reload()
}

onMounted(reload)
</script>
