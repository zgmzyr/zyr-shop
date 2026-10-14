<template>
  <!-- 知识库管理：上传 + 列表 + 分块查看 + 删除 -->
  <div class="page-card">
    <div class="page-card-title">
      <span>知识库管理</span>
      <el-button type="primary" @click="uploadVisible = true">
        <el-icon><UploadFilled /></el-icon>上传知识文件
      </el-button>
    </div>

    <div style="display:flex;gap:12px;margin-bottom:16px;flex-wrap:wrap;">
      <el-input v-model="query.keyword" placeholder="文件名" clearable style="width:220px" @keyup.enter="reload" @clear="reload" />
      <el-select v-model="query.fileType" placeholder="全部类型" clearable style="width:140px" @change="reload">
        <el-option label="TXT" value="TXT" />
        <el-option label="DOC" value="DOC" />
        <el-option label="PDF" value="PDF" />
        <el-option label="Markdown" value="MD" />
      </el-select>
      <el-button type="primary" @click="reload">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column label="文件" min-width="280">
        <template #default="{ row }">
          <div style="display:flex;align-items:center;gap:8px;">
            <span :style="{ background: typeColor(row.fileType) }" style="color:#fff;padding:2px 8px;border-radius:4px;font-size:12px;">
              {{ row.fileType }}
            </span>
            <a v-if="row.fileUrl" :href="row.fileUrl" target="_blank">{{ row.fileName }}</a>
            <span v-else>{{ row.fileName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="大小" width="100">
        <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
      </el-table-column>
      <el-table-column label="分块数" width="90">
        <template #default="{ row }">
          <el-tag type="info" round>{{ row.chunkCount }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" round>{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="上传时间" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="viewChunks(row)">查看分块</el-button>
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

    <!-- 上传弹窗 -->
    <el-dialog v-model="uploadVisible" title="上传知识文件" width="540px">
      <el-upload
        drag
        action="/api/knowledge/upload"
        :headers="uploadHeaders"
        :data="{ fileType: formType }"
        :before-upload="beforeUpload"
        :on-success="onUploadSuccess"
        :on-error="onUploadError"
        :on-progress="onUploadProgress"
        :show-file-list="true"
        accept=".txt,.doc,.docx,.pdf,.md,.markdown"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽文件到此处，或<em>点击选择</em></div>
        <template #tip>
          <div style="font-size:12px;color:#909399;line-height:1.8;">
            支持 TXT、DOC、PDF、Markdown 四种格式<br>
            文件会被自动解析、切分、向量化入库到 Redis
          </div>
        </template>
      </el-upload>
      <el-progress v-if="uploading" :percentage="percent" style="margin-top:12px" />
    </el-dialog>

    <!-- 分块查看 -->
    <el-drawer v-model="chunkVisible" :title="current?.fileName || '分块列表'" size="50%">
      <el-table :data="chunks" v-loading="chunkLoading" border size="small">
        <el-table-column prop="chunkNo" label="序号" width="60" />
        <el-table-column prop="content" label="分块内容" min-width="400" show-overflow-tooltip />
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
/**
 * 知识库管理：上传/列表/分块/删除
 * @author zyr
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { pageDocuments, listChunks, deleteDocument } from '@/api/knowledge'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, keyword: '', fileType: '' })

const reload = async () => {
  loading.value = true
  try {
    const { data } = await pageDocuments(query)
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const formatSize = (b) => {
  if (!b) return '-'
  if (b < 1024) return b + ' B'
  if (b < 1024 * 1024) return (b / 1024).toFixed(1) + ' KB'
  return (b / 1024 / 1024).toFixed(2) + ' MB'
}

const typeColor = (t) => ({
  TXT: '#6366f1', DOC: '#2563eb', PDF: '#dc2626', MD: '#10b981'
}[t] || '#909399')

const statusText = (s) => ({ PENDING: '处理中', SUCCESS: '已入库', FAILED: '入库失败' }[s] || s)
const statusType = (s) => ({ PENDING: 'warning', SUCCESS: 'success', FAILED: 'danger' }[s] || 'info')

// 上传
const uploadVisible = ref(false)
const uploading = ref(false)
const percent = ref(0)
const formType = ref('')

// 上传请求头：携带 JWT
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + userStore.token
}))

const beforeUpload = (file) => {
  const name = file.name.toLowerCase()
  const ok = ['.txt', '.doc', '.docx', '.pdf', '.md', '.markdown'].some(ext => name.endsWith(ext))
  if (!ok) {
    ElMessage.warning('仅支持 txt、doc、pdf、markdown 四种格式')
    return false
  }
  // 推断类型
  if (name.endsWith('.txt')) formType.value = 'TXT'
  else if (name.endsWith('.pdf')) formType.value = 'PDF'
  else if (name.endsWith('.md') || name.endsWith('.markdown')) formType.value = 'MD'
  else formType.value = 'DOC'
  uploading.value = true
  percent.value = 0
  return true
}

const onUploadProgress = (e) => {
  percent.value = Math.round((e.loaded / e.total) * 100)
}

const onUploadSuccess = (res) => {
  uploading.value = false
  if (res.code === 200) {
    ElMessage.success('上传成功，已开始向量化')
    uploadVisible.value = false
    reload()
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

const onUploadError = () => {
  uploading.value = false
  ElMessage.error('上传失败，请检查网络或文件格式')
}

// 分块查看
const chunkVisible = ref(false)
const chunkLoading = ref(false)
const chunks = ref([])
const current = ref(null)

const viewChunks = async (row) => {
  current.value = row
  chunkVisible.value = true
  chunkLoading.value = true
  try {
    const { data } = await listChunks(row.id)
    chunks.value = data || []
  } finally {
    chunkLoading.value = false
  }
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除知识文件「${row.fileName}」？相关向量也会一并清理`, '提示', { type: 'warning' })
  await deleteDocument(row.id)
  ElMessage.success('已删除')
  reload()
}

onMounted(reload)
</script>
