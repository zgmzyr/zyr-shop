<template>
  <!-- 商品管理：列表 + 搜索 + 分类筛选 + 新增/编辑/上下架/删除 -->
  <div class="page-card">
    <div class="page-card-title">
      <span>商品管理</span>
      <div>
        <el-button type="primary" @click="openCategory">分类管理</el-button>
        <el-button type="primary" @click="openForm()">新增商品</el-button>
      </div>
    </div>

    <!-- 工具栏 -->
    <div style="display:flex;gap:12px;margin-bottom:16px;flex-wrap:wrap;">
      <el-input v-model="query.keyword" placeholder="商品名称" clearable style="width:220px" @keyup.enter="reload" @clear="reload" />
      <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width:160px" @change="reload">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-button type="primary" @click="reload">查询</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="封面" width="80">
        <template #default="{ row }">
          <el-image v-if="row.coverImage" :src="row.coverImage" style="width:50px;height:50px" fit="cover" />
          <span v-else style="color:#c0c4cc;font-size:30px">📦</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column label="价格" width="100">
        <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" round>
            {{ row.status === 1 ? '在售' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openForm(row)">编辑</el-button>
          <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
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

    <!-- 商品表单弹窗 -->
    <el-dialog v-model="formVisible" :title="form.id ? '编辑商品' : '新增商品'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="商品名称">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width:100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" :step="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="form.coverImage" placeholder="图片 URL，如 /uploads/202610/xxx.jpg" />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">在售</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分类管理弹窗 -->
    <el-dialog v-model="catVisible" title="分类管理" width="560px">
      <div style="display:flex;gap:8px;margin-bottom:12px;">
        <el-input v-model="catForm.name" placeholder="分类名称" style="width:200px" />
        <el-input-number v-model="catForm.sort" :min="0" :step="1" style="width:120px" />
        <el-select v-model="catForm.status" style="width:120px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="saveCategory">添加</el-button>
      </div>
      <el-table :data="categories" size="small">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="editCategory(row)">编辑</el-button>
            <el-button link type="danger" @click="removeCategory(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 商品与分类管理
 * @author zyr
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageProducts, getProduct, createProduct, updateProduct,
  changeProductStatus, deleteProduct,
  listCategories, createCategory, updateCategory, deleteCategory
} from '@/api/product'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const categories = ref([])

const query = reactive({ page: 1, size: 10, keyword: '', categoryId: '' })

/** 加载商品列表 */
const reload = async () => {
  loading.value = true
  try {
    const { data } = await pageProducts(query)
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

/** 加载分类 */
const loadCategories = async () => {
  const { data } = await listCategories()
  categories.value = data || []
}

// ===== 商品表单 =====
const formVisible = ref(false)
const submitting = ref(false)
const form = reactive({ id: null, categoryId: '', name: '', description: '', price: 0, stock: 0, coverImage: '', status: 1 })

/** 打开表单（编辑时拉详情） */
const openForm = async (row) => {
  Object.assign(form, { id: null, categoryId: '', name: '', description: '', price: 0, stock: 0, coverImage: '', status: 1 })
  if (row) {
    const { data } = await getProduct(row.id)
    Object.assign(form, data)
  }
  formVisible.value = true
}

/** 提交保存 */
const submit = async () => {
  if (!form.name) { ElMessage.warning('请填写商品名称'); return }
  if (!form.categoryId) { ElMessage.warning('请选择分类'); return }
  submitting.value = true
  try {
    if (form.id) {
      await updateProduct(form.id, form)
    } else {
      await createProduct(form)
    }
    ElMessage.success('保存成功')
    formVisible.value = false
    reload()
  } finally {
    submitting.value = false
  }
}

/** 上下架切换 */
const toggleStatus = async (row) => {
  const next = row.status === 1 ? 0 : 1
  await changeProductStatus(row.id, next)
  ElMessage.success(next === 1 ? '已上架' : '已下架')
  reload()
}

/** 删除商品 */
const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除商品「${row.name}」？`, '提示', { type: 'warning' })
  await deleteProduct(row.id)
  ElMessage.success('已删除')
  reload()
}

// ===== 分类管理 =====
const catVisible = ref(false)
const catForm = reactive({ id: null, name: '', sort: 0, status: 1 })

const openCategory = () => {
  Object.assign(catForm, { id: null, name: '', sort: 0, status: 1 })
  catVisible.value = true
}

const saveCategory = async () => {
  if (!catForm.name) { ElMessage.warning('请填写分类名称'); return }
  if (catForm.id) {
    await updateCategory(catForm.id, catForm)
  } else {
    await createCategory(catForm)
  }
  ElMessage.success('保存成功')
  Object.assign(catForm, { id: null, name: '', sort: 0, status: 1 })
  loadCategories()
}

const editCategory = (row) => {
  Object.assign(catForm, row)
}

const removeCategory = async (row) => {
  await ElMessageBox.confirm(`确认删除分类「${row.name}」？`, '提示', { type: 'warning' })
  await deleteCategory(row.id)
  ElMessage.success('已删除')
  loadCategories()
}

onMounted(() => {
  loadCategories()
  reload()
})
</script>
