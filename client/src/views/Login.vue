<template>
  <!-- 登录页：渐变背景 + 浮动光斑 + 毛玻璃卡片 -->
  <div class="login-page">
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>
    <div class="orb orb-3"></div>

    <div class="login-card">
      <!-- 左侧品牌展示区 -->
      <div class="brand-panel">
        <div class="brand-logo">
          <el-icon :size="34"><Shop /></el-icon>
        </div>
        <h1>ZYR SHOP</h1>
        <h2>商城管理后台</h2>
        <p>基于 Spring AI 2.0 的智能客服商城系统</p>
        <ul class="brand-features">
          <li><el-icon><Goods /></el-icon> 商品订单一站式管理</li>
          <li><el-icon><Wallet /></el-icon> 支付宝沙箱在线支付</li>
          <li><el-icon><ChatDotRound /></el-icon> RAG 智能客服</li>
        </ul>
      </div>

      <!-- 右侧登录表单区 -->
      <div class="form-panel">
        <h3>欢迎回来 👋</h3>
        <p class="form-sub">请使用管理员账号登录</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          size="large"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form>

        <el-alert
          class="tip"
          type="info"
          :closable="false"
          title="管理员账号：admin / 密码：123456（普通用户请到前台登录）"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 登录页
 * @author zyr
 */
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Shop, Goods, Wallet, ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'admin', password: '123456' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

/** 执行登录 */
const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功，欢迎使用 ZYR SHOP')
    if (userStore.role === 'ADMIN') {
      router.push('/dashboard')
    } else {
      ElMessage.warning('当前账号非管理员，已跳转登录页')
      userStore.logout()
      router.push('/login')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  height: 100vh;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e1b4b 0%, #3730a3 45%, #6d28d9 100%);
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.55;
  animation: float 9s ease-in-out infinite;
}
.orb-1 { width: 360px; height: 360px; background: #06b6d4; top: -90px; left: -80px; }
.orb-2 { width: 300px; height: 300px; background: #ec4899; bottom: -80px; right: -60px; animation-delay: 2.5s; }
.orb-3 { width: 240px; height: 240px; background: #8b5cf6; top: 40%; left: 62%; animation-delay: 4.5s; }

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-28px) scale(1.08); }
}

.login-card {
  position: relative;
  z-index: 2;
  width: 880px;
  min-height: 480px;
  display: flex;
  border-radius: 22px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.35);
}

.brand-panel {
  flex: 1.1;
  padding: 48px 40px;
  color: #fff;
  background: linear-gradient(160deg, #4f46e5, #7c3aed 55%, #9333ea);
  display: flex;
  flex-direction: column;
}
.brand-logo {
  width: 62px;
  height: 62px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 26px;
}
.brand-panel h1 { font-size: 34px; letter-spacing: 2px; font-weight: 800; }
.brand-panel h2 { font-size: 20px; font-weight: 600; margin-top: 6px; }
.brand-panel > p { margin-top: 10px; font-size: 13px; opacity: 0.82; }
.brand-features { margin-top: auto; list-style: none; }
.brand-features li {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  margin: 14px 0;
  opacity: 0.95;
}

.form-panel { flex: 1; padding: 54px 46px; }
.form-panel h3 { font-size: 24px; font-weight: 700; }
.form-sub { color: #9ca3af; font-size: 13px; margin: 8px 0 30px; }
.login-btn { width: 100%; height: 44px; font-size: 16px; letter-spacing: 6px; border-radius: 10px; }
.tip { margin-top: 18px; }
</style>
