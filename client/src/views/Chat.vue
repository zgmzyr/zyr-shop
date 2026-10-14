<template>
  <!-- 智能客服：SSE 流式问答 -->
  <div class="chat-wrapper">
    <div class="chat-header">
      <span class="dot"></span>
      <span>ZYR 智能客服（RAG 增强检索）</span>
    </div>
    <div class="chat-messages" ref="msgBoxRef">
      <div class="chat-msg assistant">
        <div class="avatar">客</div>
        <div class="bubble">您好，我是 zyr_shop 商城智能客服，可以咨询商品、订单、支付、售后等问题。知识库已接入，回答会基于商城知识库内容。</div>
      </div>
    </div>
    <div class="chat-input">
      <textarea
        v-model="prompt"
        class="chat-textarea"
        placeholder="请输入您的问题，回车发送（Shift+Enter 换行）"
        @keydown.enter="send($event)"
      ></textarea>
      <el-button type="primary" :loading="loading" @click="send()" style="align-self:stretch;">发送</el-button>
    </div>
  </div>
</template>

<script setup>
/**
 * 智能客服聊天窗（SSE 流式）
 * @author zyr
 */
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const prompt = ref('')
const loading = ref(false)
const msgBoxRef = ref()
let es = null

const send = async (e) => {
  // 回车发送，Shift+Enter 换行
  if (e && e.shiftKey) return
  if (e) e.preventDefault()
  if (loading.value) { ElMessage.info('请等待当前回复完成'); return }
  const text = prompt.value.trim()
  if (!text) return

  appendMsg('user', text)
  prompt.value = ''
  const bubble = appendMsg('assistant', '')
  loading.value = true

  // EventSource 不支持自定义请求头，token 走 URL 参数（后端 JwtInterceptor 已支持）
  const url = `/api/ai/chat?prompt=${encodeURIComponent(text)}&token=${encodeURIComponent(userStore.token)}`
  es = new EventSource(url)

  let buffer = ''
  es.onmessage = (ev) => {
    if (ev.data === '[DONE]') {
      es.close()
      es = null
      loading.value = false
      return
    }
    buffer += ev.data
    bubble.textContent = buffer
    scrollBottom()
  }
  es.onerror = () => {
    if (es) { es.close(); es = null }
    if (!bubble.textContent) bubble.textContent = '（回复失败，请稍后重试）'
    loading.value = false
  }
}

const appendMsg = (role, content) => {
  const box = msgBoxRef.value
  const wrap = document.createElement('div')
  wrap.className = 'chat-msg ' + role
  const avatar = document.createElement('div')
  avatar.className = 'avatar'
  avatar.textContent = role === 'user' ? '我' : '客'
  const bubble = document.createElement('div')
  bubble.className = 'bubble'
  bubble.textContent = content
  wrap.appendChild(avatar)
  wrap.appendChild(bubble)
  box.appendChild(wrap)
  scrollBottom()
  return bubble
}

const scrollBottom = () => {
  nextTick(() => {
    if (msgBoxRef.value) msgBoxRef.value.scrollTop = msgBoxRef.value.scrollHeight
  })
}
</script>

<style scoped>
.chat-wrapper {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(31, 41, 55, 0.06);
  height: calc(100vh - 140px);
  min-height: 500px;
  display: flex;
  flex-direction: column;
}
.chat-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--zyr-border);
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}
.chat-header .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #67c23a;
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: var(--zyr-bg);
}
.chat-msg {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}
.chat-msg.user {
  flex-direction: row-reverse;
}
.chat-msg .avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
}
.chat-msg.user .avatar { background: #6366f1; }
.chat-msg.assistant .avatar { background: #67c23a; }
.chat-msg .bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 8px;
  background: #fff;
  color: #303133;
  font-size: 14px;
  line-height: 1.6;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  white-space: pre-wrap;
  word-break: break-word;
}
.chat-msg.user .bubble { background: #6366f1; color: #fff; }
.chat-input {
  border-top: 1px solid var(--zyr-border);
  padding: 16px 20px;
  display: flex;
  gap: 12px;
  background: #fff;
}
.chat-textarea {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  resize: none;
  height: 60px;
  font-family: inherit;
  outline: none;
}
.chat-textarea:focus { border-color: var(--zyr-primary); }
</style>
