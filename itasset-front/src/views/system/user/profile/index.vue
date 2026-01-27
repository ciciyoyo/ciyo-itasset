<template>
  <div class="profile-page">
    <!-- Header Section: Banner + Integrated Profile Meta -->
    <div class="profile-header animate__animated animate__fadeIn">
      <div class="banner">
        <div class="banner-overlay"></div>
        <div class="banner-decoration"></div>
      </div>
      <div class="header-main">
        <div class="meta-wrapper">
          <div class="avatar-container">
            <UserAvatar :user="user" @upload-success="handleUploadSuccess" />
          </div>
          <div class="user-meta">
            <h1 class="nickname">{{ user.nickName || user.userName }}</h1>
            <div class="badges">
              <el-tag effect="light" round size="small" type="primary" class="role-tag">
                {{ roleGroup || '用户' }}
              </el-tag>
              <span class="dept-text" v-if="user.dept">
                <i class="i-ri-community-line mr-1" />
                {{ user.dept.deptName }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Content Section -->
    <div class="profile-body">
      <el-row :gutter="20">
        <!-- Sidebar: Details -->
        <el-col :span="8" :xs="24">
          <div class="glass-card detail-card animate__animated animate__fadeInUp">
            <div class="card-header">
              <i class="i-ri-user-info-line mr-2" />
              <span>{{ $t('system.userCenter.personalInfo') }}</span>
            </div>
            <div class="info-list">
              <div class="info-item">
                <span class="label">
                  <i class="i-ri-id-card-line" />
                  {{ $t('system.userCenter.userName') }}
                </span>
                <span class="value">{{ user.userName }}</span>
              </div>
              <div class="info-item">
                <span class="label">
                  <i class="i-ri-phone-line" />
                  {{ $t('system.userCenter.phoneNumber') }}
                </span>
                <span class="value">{{ user.phonenumber }}</span>
              </div>
              <div class="info-item">
                <span class="label">
                  <i class="i-ri-mail-open-line" />
                  {{ $t('system.userCenter.userEmail') }}
                </span>
                <span class="value text-ellipsis" :title="user.email">{{ user.email }}</span>
              </div>
              <div class="info-item">
                <span class="label">
                  <i class="i-ri-briefcase-line" />
                  {{ $t('system.userCenter.post') }}
                </span>
                <span class="value">{{ postGroup || '—' }}</span>
              </div>
              <div class="info-item">
                <span class="label">
                  <i class="i-ri-time-line" />
                  {{ $t('system.userCenter.createDate') }}
                </span>
                <span class="value">{{ user.createTime }}</span>
              </div>
            </div>
          </div>
        </el-col>

        <!-- Main: Tabs -->
        <el-col :span="16" :xs="24">
          <div class="glass-card tabs-card animate__animated animate__fadeInUp" style="animation-delay: 0.1s">
            <el-tabs v-model="activeTab" class="modern-tabs">
              <el-tab-pane name="userinfo">
                <template #label>
                  <span class="tab-label">
                    <i class="i-ri-user-settings-line" />
                    {{ $t('system.userCenter.updateInfo') }}
                  </span>
                </template>
                <div class="tab-content">
                  <UserInfo :user="user" @update-user="getUser" />
                </div>
              </el-tab-pane>

              <el-tab-pane name="resetPwd">
                <template #label>
                  <span class="tab-label">
                    <i class="i-ri-lock-password-line" />
                    {{ $t('system.userCenter.resetPassword') }}
                  </span>
                </template>
                <div class="tab-content">
                  <ResetPwd :user="user" />
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { onMounted, ref } from 'vue'
  import { useRoute } from 'vue-router'
  import UserAvatar from './UserAvatar.vue'
  import UserInfo from './UserInfo.vue'
  import ResetPwd from './ResetPwd.vue'
  import { getUserProfile } from '@/api/system/user'

  interface Dept {
    deptName?: string
  }

  interface User {
    userName?: string
    nickName?: string
    phonenumber?: string
    email?: string
    sex?: string
    dept?: Dept
    createTime?: string
    avatar?: string

    [key: string]: any
  }

  const user = ref<User>({})
  const roleGroup = ref<string>('')
  const postGroup = ref<string>('')
  const activeTab = ref<string>('userinfo')

  const route = useRoute()

  const getUser = () => {
    getUserProfile().then((response) => {
      user.value = response.user
      roleGroup.value = response.user.roles?.map((item: any) => item.roleName).join(',') ?? ''
      postGroup.value = response.postGroup
    })
  }

  const handleUploadSuccess = (url: string) => {
    user.value.avatar = url
    getUser() // Refresh user data to sync
  }

  onMounted(() => {
    const { active } = route.query
    if (active && typeof active === 'string') {
      activeTab.value = active
    }
    getUser()
  })
</script>

<style lang="scss" scoped>
  .profile-page {
    padding: 12px;
    min-height: 100%;
    overflow-x: hidden;
  }

  /* Header Section */
  .profile-header {
    background: var(--el-bg-color);
    border-radius: 16px;
    overflow: hidden;
    margin-bottom: 24px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    border: 1px solid var(--el-border-color-lighter);
    position: relative;
  }

  .banner {
    height: 140px;
    position: relative;
    background: linear-gradient(135deg, #f0f7ff 0%, #e5eefb 100%);
    overflow: hidden;

    .banner-overlay {
      position: absolute;
      inset: 0;
      background: radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.6) 0%, transparent 100%);
    }

    .banner-decoration {
      position: absolute;
      top: -20px;
      right: -20px;
      width: 150px;
      height: 150px;
      background: rgba(64, 158, 255, 0.08);
      border-radius: 50%;
      filter: blur(20px);
    }
  }

  .header-main {
    padding: 0 32px 24px;
    margin-top: -40px;
    position: relative;
    z-index: 10;
  }

  .meta-wrapper {
    display: flex;
    align-items: flex-end;
    gap: 24px;

    .avatar-container {
      padding: 4px;
      background: var(--el-bg-color);
      border-radius: 50%;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

      :deep(.user-info-head) {
        width: 100px;
        height: 100px;
        margin-bottom: 0 !important;
      }
    }

    .user-meta {
      padding-bottom: 8px;

      .nickname {
        font-size: 24px;
        font-weight: 700;
        margin: 0 0 8px;
        color: var(--el-text-color-primary);
      }

      .badges {
        display: flex;
        align-items: center;
        gap: 12px;

        .role-tag {
          font-weight: 600;
        }

        .dept-text {
          font-size: 13px;
          color: var(--el-text-color-secondary);
          display: flex;
          align-items: center;
        }
      }
    }
  }

  /* Content Cards */
  .glass-card {
    background: var(--el-bg-color);
    border-radius: 16px;
    padding: 24px;
    border: 1px solid var(--el-border-color-lighter);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    height: 100%;
    min-height: 480px;
  }

  .card-header {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 20px;
    color: var(--el-text-color-primary);
    display: flex;
    align-items: center;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--el-border-color-extra-light);
  }

  .info-list {
    .info-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 14px 0;
      border-bottom: 1px dashed var(--el-border-color-lighter);

      &:last-child {
        border-bottom: none;
      }

      .label {
        color: var(--el-text-color-secondary);
        font-size: 14px;
        display: flex;
        align-items: center;
        gap: 8px;

        i {
          font-size: 16px;
          color: var(--el-color-primary);
        }
      }

      .value {
        color: var(--el-text-color-primary);
        font-weight: 500;
        font-size: 14px;
        max-width: 60%;
        text-align: right;
      }

      .text-ellipsis {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }

  /* Tabs Styling */
  .modern-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 30px;
    }

    :deep(.el-tabs__nav-wrap::after) {
      display: none;
    }

    :deep(.el-tabs__item) {
      font-size: 15px;
      font-weight: 500;
      height: 44px;

      &.is-active {
        font-weight: 600;
      }
    }

    .tab-label {
      display: flex;
      align-items: center;
      gap: 8px;

      i {
        font-size: 18px;
      }
    }

    .tab-content {
      padding: 4px;
    }
  }

  /* Mobile Enhancements */
  @media (max-width: 768px) {
    .profile-page {
      padding: 12px;
    }

    .header-main {
      padding: 0 16px 20px;
      margin-top: -30px;
    }

    .meta-wrapper {
      flex-direction: column;
      align-items: center;
      text-align: center;
      gap: 12px;

      .user-meta {
        .badges {
          justify-content: center;
        }
      }
    }

    .detail-card {
      margin-bottom: 20px;
      min-height: auto;
    }
  }

  /* Animations */
  .animate__animated {
    animation-duration: 0.6s;
    animation-fill-mode: both;
  }
</style>
