<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="6" :xs="24">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>{{ $t('system.userCenter.personalInfo') }}</span>
            </div>
          </template>
          <div>
            <div class="text-center">
              <UserAvatar :user="user" @upload-success="handleUploadSuccess" />
            </div>
            <ul class="list-group list-group-striped">
              <li class="list-group-item">
                <i class="iconfont icon-gerenzhongxin" />
                {{ $t('system.userCenter.userName') }}
                <div class="pull-right">
                  {{ user.userName }}
                </div>
              </li>
              <li class="list-group-item">
                <i class="iconfont icon-gerenzhongxin" />
                {{ $t('system.userCenter.phoneNumber') }}
                <div class="pull-right">
                  {{ user.phonenumber }}
                </div>
              </li>
              <li class="list-group-item">
                <i class="iconfont icon-shouye" />
                {{ $t('system.userCenter.userEmail') }}
                <div class="pull-right">
                  {{ user.email }}
                </div>
              </li>
              <li class="list-group-item">
                <i class="iconfont icon-bumenguanli" />
                {{ $t('system.userCenter.dept') }}
                <div v-if="user.dept" class="pull-right"> {{ user.dept.deptName }} / {{ postGroup }} </div>
              </li>
              <li class="list-group-item">
                <i class="iconfont icon-jiaoseguanli" />
                {{ $t('system.userCenter.role') }}
                <div class="pull-right">
                  {{ roleGroup }}
                </div>
              </li>
              <li class="list-group-item">
                <i class="iconfont icon-gerenzhongxin" />
                {{ $t('system.userCenter.createDate') }}
                <div class="pull-right">
                  {{ user.createTime }}
                </div>
              </li>
            </ul>
          </div>
        </el-card>
      </el-col>
      <el-col :span="18" :xs="24">
        <el-card>
          <template #header>
            <div class="clearfix">
              <span>{{ $t('system.userCenter.basicInfo') }}</span>
            </div>
          </template>
          <el-tabs v-model="activeTab">
            <el-tab-pane :label="$t('system.userCenter.basicInfo')" name="userinfo">
              <UserInfo :user="user" @update-user="getUser" />
            </el-tab-pane>

            <el-tab-pane :label="$t('system.userCenter.resetPassword')" name="resetPwd">
              <ResetPwd :user="user" />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
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
    phonenumber?: string
    email?: string
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
  }

  onMounted(() => {
    const { active } = route.query
    if (active && typeof active === 'string') {
      activeTab.value = active
    }
    getUser()
  })
</script>

<style scoped>
  .list-group-striped > .list-group-item {
    border-left: 0;
    border-right: 0;
    border-radius: 0;
    padding-left: 0;
    padding-right: 0;
  }

  .list-group {
    padding-left: 0px;
    list-style: none;
  }

  .list-group-item {
    border-bottom: 1px solid #e7eaec;
    border-top: 1px solid #e7eaec;
    margin-bottom: -1px;
    padding: 11px 0px;
    font-size: 13px;
  }
</style>
