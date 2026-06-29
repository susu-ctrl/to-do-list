<template>
  <div class="login-page">
    <!-- ========== 左侧：卡通角色区 ========== -->
    <div class="login-visual">
      <div class="visual-header">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="none" width="20" height="20">
            <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
          </svg>
        </div>
        <span>待办清单</span>
      </div>

      <div class="characters-container">
        <div class="characters-wrapper">
          <!-- 紫色角色 -->
          <div ref="purpleRef" class="character purple"
               :style="{
              height: isTypingPassword ? '440px' : '400px',
              transform: showPassword ? 'skewX(0deg)' : isTyping
                ? `skewX(${(purplePos.skew||0)-12}deg) translateX(40px)`
                : `skewX(${purplePos.skew||0}deg)`
            }">
            <div class="eyes-group"
                 :style="{
                left: showPassword ? '20px' : isLookingAtEachOther ? '55px' : (45+purplePos.faceX)+'px',
                top: showPassword ? '35px' : isLookingAtEachOther ? '65px' : (40+purplePos.faceY)+'px'
              }">
              <EyeBall :size="18" :pupil-size="7" :max-distance="5"
                       :is-blinking="isPurpleBlinking"
                       :force-look-x="purpleLookX" :force-look-y="purpleLookY" />
              <EyeBall :size="18" :pupil-size="7" :max-distance="5"
                       :is-blinking="isPurpleBlinking"
                       :force-look-x="purpleLookX" :force-look-y="purpleLookY" />
            </div>
          </div>

          <!-- 黑色角色 -->
          <div ref="blackRef" class="character black"
               :style="{
              transform: showPassword ? 'skewX(0deg)' : isLookingAtEachOther
                ? `skewX(${(blackPos.skew||0)*1.5+10}deg) translateX(20px)`
                : isTyping
                  ? `skewX(${(blackPos.skew||0)*1.5}deg)`
                  : `skewX(${blackPos.skew||0}deg)`
            }">
            <div class="eyes-group"
                 :style="{
                left: showPassword ? '10px' : isLookingAtEachOther ? '32px' : (26+blackPos.faceX)+'px',
                top: showPassword ? '28px' : isLookingAtEachOther ? '12px' : (32+blackPos.faceY)+'px'
              }">
              <EyeBall :size="16" :pupil-size="6" :max-distance="4"
                       :is-blinking="isBlackBlinking"
                       :force-look-x="blackLookX" :force-look-y="blackLookY" />
              <EyeBall :size="16" :pupil-size="6" :max-distance="4"
                       :is-blinking="isBlackBlinking"
                       :force-look-x="blackLookX" :force-look-y="blackLookY" />
            </div>
          </div>

          <!-- 橙色角色 -->
          <div ref="orangeRef" class="character orange"
               :style="{ transform: showPassword ? 'skewX(0deg)' : `skewX(${orangePos.skew||0}deg)` }">
            <div class="pupils-group"
                 :style="{
                left: showPassword ? '50px' : (82+orangePos.faceX)+'px',
                top: showPassword ? '85px' : (90+orangePos.faceY)+'px'
              }">
              <PupilDot :size="12" :max-distance="5"
                        :force-look-x="showPassword?-5:undefined"
                        :force-look-y="showPassword?-4:undefined" />
              <PupilDot :size="12" :max-distance="5"
                        :force-look-x="showPassword?-5:undefined"
                        :force-look-y="showPassword?-4:undefined" />
            </div>
          </div>

          <!-- 黄色角色 -->
          <div ref="yellowRef" class="character yellow"
               :style="{ transform: showPassword ? 'skewX(0deg)' : `skewX(${yellowPos.skew||0}deg)` }">
            <div class="pupils-group"
                 :style="{
                left: showPassword ? '20px' : (52+yellowPos.faceX)+'px',
                top: showPassword ? '35px' : (40+yellowPos.faceY)+'px'
              }">
              <PupilDot :size="12" :max-distance="5"
                        :force-look-x="showPassword?-5:undefined"
                        :force-look-y="showPassword?-4:undefined" />
              <PupilDot :size="12" :max-distance="5"
                        :force-look-x="showPassword?-5:undefined"
                        :force-look-y="showPassword?-4:undefined" />
            </div>
            <div class="mouth-line"
                 :style="{
                left: showPassword ? '10px' : (40+yellowPos.faceX)+'px',
                top: showPassword ? '88px' : (88+yellowPos.faceY)+'px'
              }" />
          </div>
        </div>
      </div>

      <div class="visual-footer">
        <a href="#">隐私政策</a>
        <a href="#">服务条款</a>
        <a href="#">联系我们</a>
      </div>

      <div class="visual-glow glow-1"></div>
      <div class="visual-glow glow-2"></div>
      <div class="visual-grid"></div>
    </div>

    <!-- ========== 右侧：登录表单 ========== -->
    <div class="form-side">
      <div class="form-wrapper">
        <div class="form-header">
          <h1>欢迎回来！</h1>
          <p>请输入你的账号信息</p>
        </div>

        <form @submit.prevent="handleLogin" class="auth-form">
          <div class="form-group">
            <label>用户名</label>
            <input v-model="form.username" type="text" placeholder="请输入用户名"
                   autocomplete="off" @focus="isTyping=true" @blur="isTyping=false" required />
          </div>

          <div class="form-group">
            <label>密码</label>
            <div class="input-with-icon">
              <input
                  v-model="form.password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="••••••••"
                  autocomplete="new-password"
                  required
              />
              <button type="button" class="eye-btn" @click="showPassword = !showPassword" tabindex="-1">
                <svg v-if="!showPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                </svg>
                <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                  <line x1="1" y1="1" x2="23" y2="23"/>
                </svg>
              </button>
            </div>
          </div>

          <div class="form-options">
            <label class="checkbox-label">
              <input type="checkbox" v-model="rememberMe" />
              <span>记住我30天</span>
            </label>
            <a href="#" class="forgot-link">忘记密码？</a>
          </div>

          <div v-if="error" class="form-error">{{ error }}</div>

          <button type="submit" class="btn-primary" :disabled="loading">
            <span v-if="loading" class="spinner"></span>
            {{ loading ? '登录中...' : '登 录' }}
          </button>
        </form>

        <div class="switch-link">
          还没有账号？<a href="#" @click.prevent="showRegister = true">立即注册</a>
        </div>
      </div>
    </div>

    <!-- 注册弹窗 -->
    <el-dialog v-model="showRegister" title="注册" width="400px">
      <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-width="0"
      >
        <el-form-item prop="username">
          <el-input v-model="registerForm.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="密码" size="large" />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" size="large" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegister = false">取消</el-button>
        <el-button type="primary" :loading="registerLoading" @click="handleRegister">注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import EyeBall from '@/components/EyeBall.vue'
import PupilDot from '@/components/PupilDot.vue'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({ username: '', password: '' })
const showPassword = ref(false)
const rememberMe = ref(false)
const error = ref('')
const loading = ref(false)
const showRegister = ref(false)
const registerForm = reactive({ username: '', password: '', confirmPassword: '' })
const registerLoading = ref(false)

const mouseX = ref(0)
const mouseY = ref(0)
const handleMouseMove = (e) => { mouseX.value = e.clientX; mouseY.value = e.clientY }

const purpleRef = ref(null)
const blackRef = ref(null)
const orangeRef = ref(null)
const yellowRef = ref(null)

const isPurpleBlinking = ref(false)
const isBlackBlinking = ref(false)
const isTyping = ref(false)
const isLookingAtEachOther = ref(false)
const isPurplePeeking = ref(false)

function calcPos(el) {
  if (!el) return { faceX: 0, faceY: 0, skew: 0 }
  const r = el.getBoundingClientRect()
  const cx = r.left + r.width / 2
  const cy = r.top + r.height / 3
  const dx = mouseX.value - cx
  const dy = mouseY.value - cy
  return {
    faceX: Math.max(-15, Math.min(15, dx / 20)),
    faceY: Math.max(-10, Math.min(10, dy / 30)),
    skew: Math.max(-6, Math.min(6, -dx / 120))
  }
}

const purplePos = computed(() => calcPos(purpleRef.value))
const blackPos = computed(() => calcPos(blackRef.value))
const orangePos = computed(() => calcPos(orangeRef.value))
const yellowPos = computed(() => calcPos(yellowRef.value))
const isTypingPassword = computed(() => form.password.length > 0 && !showPassword.value)

const purpleLookX = computed(() => {
  if (form.password.length > 0 && showPassword.value) return isPurplePeeking.value ? 4 : -4
  if (isLookingAtEachOther.value) return 3
  return undefined
})
const purpleLookY = computed(() => {
  if (form.password.length > 0 && showPassword.value) return isPurplePeeking.value ? 5 : -4
  if (isLookingAtEachOther.value) return 4
  return undefined
})
const blackLookX = computed(() => {
  if (form.password.length > 0 && showPassword.value) return -4
  if (isLookingAtEachOther.value) return 0
  return undefined
})
const blackLookY = computed(() => {
  if (form.password.length > 0 && showPassword.value) return -4
  if (isLookingAtEachOther.value) return -4
  return undefined
})

let blinkTimers = []
let peekTimer = null

function scheduleBlink(setFn) {
  const run = () => {
    const timer = setTimeout(() => {
      setFn(true)
      setTimeout(() => { setFn(false); run() }, 150)
    }, Math.random() * 4000 + 3000)
    blinkTimers.push(timer)
  }
  run()
}

onMounted(() => {
  window.addEventListener('mousemove', handleMouseMove)
  scheduleBlink((v) => { isPurpleBlinking.value = v })
  scheduleBlink((v) => { isBlackBlinking.value = v })
})

onUnmounted(() => {
  window.removeEventListener('mousemove', handleMouseMove)
  blinkTimers.forEach(clearTimeout)
  if (peekTimer) clearTimeout(peekTimer)
})

watch(isTyping, (val) => {
  if (val) {
    isLookingAtEachOther.value = true
    setTimeout(() => { isLookingAtEachOther.value = false }, 800)
  }
})

watch([() => form.password, showPassword], ([pwd, visible]) => {
  if (peekTimer) { clearTimeout(peekTimer); peekTimer = null }
  if (pwd.length > 0 && visible) {
    const schedulePeek = () => {
      peekTimer = setTimeout(() => {
        isPurplePeeking.value = true
        setTimeout(() => { isPurplePeeking.value = false }, 800)
        schedulePeek()
      }, Math.random() * 3000 + 2000)
    }
    schedulePeek()
  } else {
    isPurplePeeking.value = false
  }
})

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次密码输入不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

async function handleLogin() {
  error.value = ''
  if (!form.username || !form.password) {
    error.value = '请填写完整信息'
    return
  }
  loading.value = true
  try {
    const res = await request.post('/api/auth/login', form)
    if (res.code === 200) {
      userStore.setUser(res.data)
      ElMessage.success('登录成功')
      router.push('/tasks')
    } else {
      error.value = res.message || '登录失败'
    }
  } catch (e) {
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  registerLoading.value = true
  try {
    const res = await request.post('/api/auth/register', registerForm)
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      showRegister.value = false
      form.username = registerForm.username
      registerForm.username = ''
      registerForm.password = ''
      registerForm.confirmPassword = ''
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('注册失败，请稍后再试')
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 100vh;
  overflow: hidden;
}

/* ===== 左侧视觉区 ===== */
.login-visual {
  position: relative;
  background: linear-gradient(135deg, #6C3FF5 0%, #4B2DBB 50%, #3A1FA0 100%);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 48px;
  overflow: hidden;
}
.visual-header {
  display: flex; align-items: center; gap: 10px;
  color: rgba(255,255,255,0.9); font-size: 18px; font-weight: 600; z-index: 10;
}
.logo-icon {
  width: 32px; height: 32px;
  background: rgba(255,255,255,0.15); backdrop-filter: blur(8px);
  border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #fff;
}
.characters-container { flex: 1; display: flex; align-items: flex-end; justify-content: center; z-index: 5; }
.characters-wrapper { position: relative; width: 550px; height: 450px; }

.character { position: absolute; bottom: 0; transition: all 0.7s ease-in-out; transform-origin: bottom center; }
.character.purple { left: 70px; width: 180px; height: 400px; background: #6C3FF5; border-radius: 10px 10px 0 0; z-index: 1; }
.character.black { left: 240px; width: 120px; height: 310px; background: #2D2D2D; border-radius: 8px 8px 0 0; z-index: 2; }
.character.orange { left: 0; width: 240px; height: 200px; background: #FF9B6B; border-radius: 120px 120px 0 0; z-index: 3; }
.character.yellow { left: 310px; width: 140px; height: 230px; background: #E8D754; border-radius: 70px 70px 0 0; z-index: 4; }

.eyes-group { position: absolute; display: flex; gap: 24px; transition: all 0.7s ease-in-out; }
.character.black .eyes-group { gap: 18px; }
.pupils-group { position: absolute; display: flex; gap: 24px; transition: all 0.2s ease-out; }
.character.yellow .pupils-group { gap: 18px; }
.mouth-line { position: absolute; width: 80px; height: 4px; background: #2D2D2D; border-radius: 2px; transition: all 0.2s ease-out; }

.visual-footer { display: flex; gap: 32px; z-index: 10; }
.visual-footer a { color: rgba(255,255,255,0.5); text-decoration: none; font-size: 13px; transition: color 0.2s; }
.visual-footer a:hover { color: #fff; }
.visual-glow { position: absolute; border-radius: 50%; pointer-events: none; }
.glow-1 { top: 25%; right: 25%; width: 256px; height: 256px; background: rgba(255,255,255,0.1); filter: blur(60px); }
.glow-2 { bottom: 25%; left: 25%; width: 384px; height: 384px; background: rgba(255,255,255,0.05); filter: blur(60px); }
.visual-grid {
  position: absolute; inset: 0;
  background-image: linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px),
  linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px);
  background-size: 20px 20px; pointer-events: none;
}

/* ===== 右侧表单区 ===== */
.form-side {
  display: flex; align-items: center; justify-content: center;
  padding: 32px; background: #f8f9fc;
}
.form-wrapper { width: 100%; max-width: 400px; }
.form-header { text-align: center; margin-bottom: 36px; }
.form-header h1 { font-size: 28px; font-weight: 700; color: #1a1a2e; margin-bottom: 8px; }
.form-header p { font-size: 14px; color: #888; }

.auth-form { display: flex; flex-direction: column; gap: 20px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-group label { font-size: 13px; font-weight: 600; color: #1a1a2e; }

.form-group input {
  height: 48px; padding: 0 16px;
  border: 1px solid #e0e0e0; border-radius: 8px;
  font-size: 14px; background: #fff; color: #1a1a2e;
  outline: none; transition: all 0.25s; font-family: inherit; width: 100%;
}
.form-group input:focus { border-color: #6C3FF5; box-shadow: 0 0 0 3px rgba(108,63,245,0.15); }
.form-group input::placeholder { color: #bbb; }

.input-with-icon { position: relative; }
.input-with-icon input { padding-right: 44px; }
.eye-btn {
  position: absolute; right: 4px; top: 50%; transform: translateY(-50%);
  width: 36px; height: 36px;
  background: none; border: none; cursor: pointer;
  color: #bbb; display: flex; align-items: center; justify-content: center;
  border-radius: 6px; transition: all 0.2s;
}
.eye-btn:hover { color: #1a1a2e; background: rgba(0,0,0,0.04); }

.form-options { display: flex; justify-content: space-between; align-items: center; }
.checkbox-label { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #888; cursor: pointer; }
.checkbox-label input[type="checkbox"] { width: 16px; height: 16px; accent-color: #6C3FF5; }
.forgot-link { font-size: 13px; color: #6C3FF5; text-decoration: none; font-weight: 500; }
.forgot-link:hover { text-decoration: underline; }

.form-error {
  padding: 10px 14px; font-size: 13px;
  color: #FF3B30; background: rgba(255,59,48,0.08);
  border: 1px solid rgba(255,59,48,0.2); border-radius: 8px;
}

.btn-primary {
  height: 48px; border: none; border-radius: 8px;
  background: linear-gradient(135deg, #6C3FF5, #8B6BFF);
  color: #fff; font-size: 15px; font-weight: 600; cursor: pointer;
  display: flex; align-items: center; justify-content: center; gap: 8px;
  font-family: inherit; transition: all 0.25s;
}
.btn-primary:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 8px 25px rgba(108,63,245,0.35); }
.btn-primary:disabled { opacity: 0.7; cursor: not-allowed; }

.spinner {
  width: 18px; height: 18px;
  border: 2px solid rgba(255,255,255,0.3); border-top-color: #fff;
  border-radius: 50%; animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.switch-link { text-align: center; font-size: 13px; color: #888; margin-top: 28px; }
.switch-link a { color: #6C3FF5; font-weight: 600; text-decoration: none; cursor: pointer; }
.switch-link a:hover { text-decoration: underline; }

@media (max-width: 1024px) {
  .login-page { grid-template-columns: 1fr; }
  .login-visual { display: none; }
  .form-side { min-height: 100vh; }
}
</style>