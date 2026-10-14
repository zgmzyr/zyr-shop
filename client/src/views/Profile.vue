<template>
  <!-- 个人中心：修改昵称/手机/收货地址 + 修改密码 -->
  <div class="page-card">
    <div class="page-card-title">个人中心</div>

    <el-tabs v-model="active">
      <!-- 基本资料 -->
      <el-tab-pane label="基本资料" name="profile">
        <el-form :model="profile" label-width="100px" style="max-width: 480px">
          <el-form-item label="用户名">
            <el-input :value="userStore.username" disabled />
          </el-form-item>
          <el-form-item label="角色">
            <el-tag type="warning">管理员</el-tag>
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="profile.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="profile.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="收货地址">
            <el-input v-model="profile.address" type="textarea" :rows="2" placeholder="请输入收货地址" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 修改密码 -->
      <el-tab-pane label="修改密码" name="password">
        <el-form :model="pwd" label-width="100px" style="max-width: 480px">
          <el-form-item label="原密码">
            <el-input v-model="pwd.oldPassword" type="password" show-password placeholder="请输入原密码" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="pwd.newPassword" type="password" show-password placeholder="请输入新密码" />
          </el-form-item>
          <el-form-item label="确认新密码">
            <el-input v-model="pwd.confirm" type="password" show-password placeholder="请再次输入新密码" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="savingPwd" @click="savePwd">修改密码</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
/**
 * 个人中心：基本资料 + 修改密码
 * @author zyr
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { updateProfile, changePassword } from '@/api/auth'

const userStore = useUserStore()
const active = ref('profile')

const profile = reactive({
  nickname: '',
  phone: '',
  address: ''
})

const pwd = reactive({ oldPassword: '', newPassword: '', confirm: '' })

const savingProfile = ref(false)
const savingPwd = ref(false)

/** 保存基本资料 */
const saveProfile = async () => {
  savingProfile.value = true
  try {
    const { data } = await updateProfile({
      nickname: profile.nickname,
      phone: profile.phone,
      address: profile.address
    })
    userStore.setUserInfo(data)
    ElMessage.success('保存成功')
  } finally {
    savingProfile.value = false
  }
}

/** 修改密码 */
const savePwd = async () => {
  if (!pwd.oldPassword || !pwd.newPassword) {
    ElMessage.warning('请填写原密码与新密码')
    return
  }
  if (pwd.newPassword !== pwd.confirm) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  savingPwd.value = true
  try {
    await changePassword({
      oldPassword: pwd.oldPassword,
      newPassword: pwd.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    location.href = '/login'
  } finally {
    savingPwd.value = false
  }
}

onMounted(() => {
  const u = userStore.userInfo || {}
  profile.nickname = u.nickname || ''
  profile.phone = u.phone || ''
  profile.address = u.address || ''
})
</script>
