<!-- 登录页面 -->
<template>
  <div
    class="login-page"
    :class="{ 'has-bg-image': !!systemInfo.backgroundImage }"
    :style="{ '--login-bg-image': systemInfo.backgroundImage ? `url(${systemInfo.backgroundImage})` : 'none' }"
  >
    <AuthTopBar />

    <!-- 统一登录卡片 -->
    <div class="login-card-container">
      <div class="login-card">
        <!-- 左侧品牌区域 -->
        <div class="card-left">
          <div class="brand-content">
            <!-- Logo -->
            <div class="brand-logo">
              <div class="logo-pulse-ring ring-1"></div>
              <div class="logo-circle">
                <span>ITAM</span>
              </div>
            </div>
            <!-- 标题 -->
            <h1 class="brand-title">{{ systemInfo.name }}</h1>
            <p class="brand-subtitle">{{ $t('login.leftView.systemDesc') }}</p>

            <!-- 功能特点列表 -->
            <ul class="feature-list">
              <li v-for="(item, index) in features" :key="index" :style="{ '--index': index }">
                <div class="feature-icon-wrapper">
                  <ElIcon class="feature-icon">
                    <ele-CircleCheckFilled />
                  </ElIcon>
                </div>
                <div class="feature-content">
                  <div class="feature-title">{{ item.title }}</div>
                  <div class="feature-desc">{{ item.desc }}</div>
                </div>
              </li>
            </ul>
          </div>
        </div>

        <!-- 右侧登录区域 -->
        <div class="card-right">
          <!-- 标题区域 -->
          <div class="login-header">
            <h3 class="login-title">{{ $t('login.title') }}</h3>
            <p class="login-subtitle">{{ $t('login.subTitle') }}</p>
          </div>

          <!-- 表单区域 -->
          <ElForm ref="formRef" :model="formData" :rules="rules" :key="formKey" @keyup.enter="handleSubmit" class="login-form">
            <div class="form-group">
              <label class="form-label">{{ $t('login.label.username') }}</label>
              <ElFormItem prop="username">
                <ElInput class="form-input" :placeholder="$t('login.placeholder.username')" v-model.trim="formData.username" />
              </ElFormItem>
            </div>

            <div class="form-group">
              <label class="form-label">{{ $t('login.label.password') }}</label>
              <ElFormItem prop="password">
                <ElInput
                  class="form-input"
                  :placeholder="$t('login.placeholder.password')"
                  v-model.trim="formData.password"
                  type="password"
                  autocomplete="off"
                  show-password
                />
              </ElFormItem>
            </div>

            <div class="form-options">
              <ElCheckbox v-model="formData.rememberPassword">{{ $t('login.rememberPwd') }}</ElCheckbox>
            </div>

            <ElButton class="login-btn" type="primary" @click="handleSubmit" :loading="loading" v-ripple>
              {{ $t('login.btnText') }}
            </ElButton>
          </ElForm>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useUserStore } from '@/store/modules/user'
  import { useI18n } from 'vue-i18n'
  import { HttpError } from '@/utils/http/error'
  import { fetchLogin } from '@/api/auth'
  import { ElNotification, type FormInstance, type FormRules } from 'element-plus'
  import { useSettingStore } from '@/store/modules/setting'
  import { encrypt } from '@utils/sys/jsencrypt'
  import { useSystemStore } from '@/store/modules/system'

  defineOptions({ name: 'Login' })

  const settingStore = useSettingStore()
  const { t, locale } = useI18n()
  const formKey = ref(0)

  // 功能特点列表
  const features = computed(() => [
    {
      title: t('login.leftView.features.customerManagement'),
      desc: '从采购到报度,全流程追踪管理'
    },
    {
      title: t('login.leftView.features.salesTracking'),
      desc: '自动化盘点实时监控资产状态'
    },
    {
      title: t('login.leftView.features.taskReminder'),
      desc: '可现化报表，助力决策优化'
    },
    {
      title: t('login.leftView.features.teamCollaboration'),
      desc: '灵活的角色权慢体系，保险数据安全'
    }
  ])

  // 监听语言切换，重置表单
  watch(locale, () => {
    formKey.value++
  })

  const userStore = useUserStore()
  const router = useRouter()
  const route = useRoute()

  const formRef = ref<FormInstance>()

  const formData = reactive({
    username: '',
    password: '',
    rememberPassword: true
  })

  const rules = computed<FormRules>(() => ({
    username: [{ required: true, message: t('login.placeholder.username'), trigger: 'blur' }],
    password: [{ required: true, message: t('login.placeholder.password'), trigger: 'blur' }]
  }))

  const loading = ref(false)

  // 登录
  const handleSubmit = async () => {
    if (!formRef.value) return

    try {
      // 表单验证
      const valid = await formRef.value.validate()
      if (!valid) return

      loading.value = true

      // 登录请求
      const { username, password } = formData

      const { token } = await fetchLogin({
        username,
        password: encrypt(password) as string
      })

      // 验证token
      if (!token) {
        throw new Error('Login failed - no token received')
      }

      // 存储 token 和登录状态
      userStore.setToken(token)
      userStore.setLoginStatus(true)

      // 登录成功处理
      showLoginSuccessNotice()

      // 获取 redirect 参数，如果存在则跳转到指定页面，否则跳转到首页
      const redirect = route.query.redirect as string
      router.push(redirect || '/')
    } catch (error) {
      // 处理 HttpError
      if (error instanceof HttpError) {
        // console.log(error.code)
      } else {
        // 处理非 HttpError
        // ElMessage.error('登录失败，请稍后重试')
        console.error('[Login] Unexpected error:', error)
      }
    } finally {
      loading.value = false
    }
  }

  // 登录成功提示
  const showLoginSuccessNotice = () => {
    setTimeout(() => {
      ElNotification({
        title: t('login.success.title'),
        type: 'success',
        duration: 2500,
        zIndex: 10000,
        message: `${t('login.success.message')}!`
      })
    }, 1000)
  }

  const systemStore = useSystemStore()

  const { systemInfo } = storeToRefs(systemStore)
</script>

<style lang="scss" scoped>
  /* 登录页面 */
  .login-page {
    width: 100%;
    height: 100vh;
    position: relative;
    display: flex;
    flex-direction: column;
    background-color: #f7f9fc;
    overflow: hidden;

    /* 背景模糊层 */
    &::before {
      content: '';
      position: absolute;
      top: -50%;
      left: -50%;
      width: 200%;
      height: 200%;
      background-image:
        radial-gradient(at 40% 20%, hsla(215, 100%, 94%, 0.5) 0px, transparent 50%),
        radial-gradient(at 80% 0%, hsla(189, 100%, 96%, 0.5) 0px, transparent 50%),
        radial-gradient(at 0% 50%, hsla(215, 100%, 96%, 0.5) 0px, transparent 50%),
        radial-gradient(at 80% 50%, hsla(215, 100%, 94%, 0.5) 0px, transparent 50%),
        radial-gradient(at 0% 100%, hsla(189, 100%, 96%, 0.5) 0px, transparent 50%),
        radial-gradient(at 80% 100%, hsla(215, 100%, 94%, 0.5) 0px, transparent 50%),
        radial-gradient(at 0% 0%, hsla(215, 100%, 96%, 0.5) 0px, transparent 50%);
      filter: blur(100px);
      z-index: 0;
      animation: gradientMove 15s ease infinite alternate;
    }

    &.has-bg-image::before {
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background-image: var(--login-bg-image);
      background-size: cover;
      background-position: center;
      background-repeat: no-repeat;
      filter: none;
      animation: none;
    }
  }

  /* 确保内容在背景之上 */
  .login-card-container {
    position: relative;
    z-index: 1;
  }

  @keyframes gradientMove {
    0% {
      transform: translate(-2%, -2%) rotate(0deg);
    }
    100% {
      transform: translate(2%, 2%) rotate(2deg);
    }
  }

  /* 卡片容器 */
  .login-card-container {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
  }

  /* 统一登录卡片 */
  .login-card {
    display: flex;
    width: 900px;
    max-width: 100%;
    min-height: 560px;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 8px 40px rgb(0 0 0 / 12%);
    animation: slideInUp 0.6s ease-out;
  }

  /* 左侧品牌区域 */
  .card-left {
    width: 400px;
    background: linear-gradient(315deg, #6699ff 0%, #165dff 50%, #003399 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 50px 40px;
  }

  .brand-content {
    color: #fff;
    text-align: center;
  }

  .brand-logo {
    position: relative;
    display: inline-block;
    margin-bottom: 28px;
  }

  .logo-pulse-ring {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    border-radius: 50%;
    border: 2px solid rgba(255, 255, 255, 0.4);

    &.ring-1 {
      width: 85px;
      height: 85px;
      animation: breathe 2s ease-in-out infinite;
    }

    &.ring-2 {
      width: 100px;
      height: 100px;
      animation: breathe 2s ease-in-out infinite 0.3s;
    }
  }

  @keyframes breathe {
    0%,
    100% {
      transform: translate(-50%, -50%) scale(1);
      opacity: 0.6;
    }

    50% {
      transform: translate(-50%, -50%) scale(1.08);
      opacity: 0.3;
    }
  }

  .logo-circle {
    position: relative;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 70px;
    height: 70px;
    border: 2px solid rgba(255, 255, 255, 0.6);
    border-radius: 50%;
    font-size: 18px;
    font-weight: 600;
    color: #fff;
    background: rgba(255, 255, 255, 0.1);
    animation: breatheCenter 2s ease-in-out infinite;
  }

  @keyframes breatheCenter {
    0%,
    100% {
      transform: scale(1);
    }

    50% {
      transform: scale(1.06);
    }
  }

  .brand-title {
    font-size: 36px;
    font-weight: 700;
    margin: 0 0 10px;
    letter-spacing: 1px;
  }

  .brand-subtitle {
    font-size: 16px;
    opacity: 0.9;
    margin: 0 0 40px;
  }

  .feature-list {
    list-style: none;
    padding: 0;
    margin: 0;
    text-align: left;

    li {
      display: flex;
      align-items: center;
      font-size: 15px;
      font-weight: 500;
      padding: 12px 24px;
      border-radius: 12px;
      color: #fff;
      opacity: 0;
      transition: all 0.3s ease;
      animation: slideInLeft 0.5s ease-out forwards;
      animation-delay: calc(0.3s + var(--index) * 0.1s);

      &:last-child {
        margin-bottom: 0;
      }
    }

    .feature-icon-wrapper {
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
    }

    .feature-icon {
      font-size: 24px;
      color: #fff;
    }

    .feature-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: center;
    }

    .feature-title {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 4px;
      line-height: 1.4;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }

    .feature-desc {
      font-size: 13px;
      color: rgba(255, 255, 255, 0.85);
      line-height: 1.4;
      font-weight: normal;
    }
  }

  @keyframes slideInLeft {
    from {
      opacity: 0;
      transform: translateX(-30px);
    }

    to {
      opacity: 1;
      transform: translateX(0);
    }
  }

  /* 右侧登录区域 */
  .card-right {
    flex: 1;
    background: var(--el-bg-color);
    padding: 50px 45px;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }

  /* 标题区域 */
  .login-header {
    text-align: center;
    margin-bottom: 32px;
  }

  .login-title {
    font-size: 28px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    margin: 0 0 8px;
  }

  .login-subtitle {
    font-size: 14px;
    color: var(--el-text-color-secondary);
    margin: 0;
  }

  /* 表单区域 */
  .login-form {
    .form-group {
      margin-bottom: 20px;
    }

    .form-label {
      display: block;
      font-size: 14px;
      font-weight: 500;
      color: var(--el-text-color-primary);
      margin-bottom: 8px;
    }

    :deep(.el-form-item) {
      margin-bottom: 0;
    }

    :deep(.el-input__wrapper) {
      height: 44px;
      border-radius: 8px;
    }
  }

  .login-btn {
    width: 100%;
    height: 44px !important;
    margin-top: 8px;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 500;
  }

  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    font-size: 14px;

    .forget-link {
      color: var(--el-color-primary);
      text-decoration: none;

      &:hover {
        text-decoration: underline;
      }
    }
  }

  .register-link {
    margin-top: 16px;
    text-align: center;
    font-size: 14px;
    color: var(--el-text-color-secondary);

    .text-theme {
      color: var(--el-color-primary);
      text-decoration: none;
      margin-left: 4px;

      &:hover {
        text-decoration: underline;
      }
    }
  }

  /* 测试账号区域 */
  .test-account-section {
    margin-top: 28px;
    padding: 16px;
    background: var(--el-fill-color-light);
    border-radius: 12px;
    border: 1px dashed var(--el-border-color);
  }

  .test-account-header {
    font-size: 13px;
    font-weight: 500;
    color: var(--el-text-color-secondary);
    text-align: center;
    margin-bottom: 12px;
  }

  .test-account-item {
    display: flex;
    align-items: center;
    padding: 8px 12px;
    background: var(--el-bg-color);
    border-radius: 8px;
    margin-bottom: 8px;
    font-size: 13px;

    .el-icon {
      color: var(--el-color-primary);
      margin-right: 8px;
    }

    .test-label {
      color: var(--el-text-color-secondary);
      margin-right: 8px;
    }

    .test-value {
      flex: 1;
      font-family: 'Consolas', 'Monaco', monospace;
      color: var(--el-text-color-primary);
    }

    .copy-icon {
      cursor: pointer;
      color: var(--el-text-color-placeholder);
      transition: color 0.2s;

      &:hover {
        color: var(--el-color-primary);
      }
    }
  }

  .auto-fill-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    margin-top: 4px;
    font-size: 13px;

    .el-icon {
      margin-right: 4px;
    }
  }

  /* 动画 */
  @keyframes slideInUp {
    from {
      opacity: 0;
      transform: translateY(40px);
    }

    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  /* 响应式适配 */
  @media (max-width: 900px) {
    .login-card {
      flex-direction: column;
      width: 100%;
      max-width: 420px;
      min-height: auto;
    }

    .card-left {
      display: none;
    }

    .card-right {
      padding: 40px 30px;
    }
  }

  @media (max-width: 480px) {
    .login-card-container {
      padding: 16px;
    }

    .card-right {
      padding: 30px 24px;
    }

    .login-title {
      font-size: 24px;
    }
  }
</style>
