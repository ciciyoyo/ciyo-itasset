<template>
  <div class="user-info-form">
    <el-form ref="formRef" :model="localUser" :rules="rules" label-position="top">
      <el-row :gutter="30">
        <el-col :span="12" :xs="24">
          <el-form-item :label="t('system.userProfile.nickName')" prop="nickName">
            <el-input v-model="localUser.nickName" maxlength="30" placeholder="请输入昵称" />
          </el-form-item>
        </el-col>
        <el-col :span="12" :xs="24">
          <el-form-item :label="t('system.userProfile.phoneNumber')" prop="phonenumber">
            <el-input v-model="localUser.phonenumber" maxlength="11" placeholder="请输入手机号码" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="30">
        <el-col :span="12" :xs="24">
          <el-form-item :label="t('system.userProfile.email')" prop="email">
            <el-input v-model="localUser.email" maxlength="50" placeholder="请输入邮箱" />
          </el-form-item>
        </el-col>
        <el-col :span="12" :xs="24">
          <el-form-item :label="t('system.userProfile.gender')">
            <el-radio-group v-model="localUser.sex" class="custom-radio-group">
              <el-radio-button value="0">{{ t('system.userProfile.male') }}</el-radio-button>
              <el-radio-button value="1">{{ t('system.userProfile.female') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <div class="form-actions mt-8">
        <el-button size="large" type="primary" class="submit-btn" @click="submit">
          <i class="i-ri-save-line mr-2" />
          {{ t('system.userProfile.save') }}
        </el-button>
        <el-button size="large" plain class="close-btn" @click="close">
          {{ t('system.userProfile.close') }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script lang="ts" setup>
  import { reactive, ref, watch } from 'vue'
  import { updateUserProfile } from '@/api/system/user'
  import { useI18n } from 'vue-i18n'
  import { useRoute, useRouter } from 'vue-router'
  import type { FormInstance, FormRules } from 'element-plus'
  import { cloneDeep } from 'lodash-es'
  import { useWorktabStore } from '@/store/modules/worktab'
  import { MessageUtil } from '@/utils/messageUtil'

  interface User {
    nickName?: string
    phonenumber?: string
    email?: string
    sex?: string
  }

  const props = defineProps<{
    user: User
  }>()

  const { t } = useI18n()
  const router = useRouter()
  const route = useRoute()
  const worktabStore = useWorktabStore()

  const formRef = ref<FormInstance>()

  const localUser = reactive<User>(cloneDeep(props.user))

  watch(
    () => props.user,
    (newUser) => {
      Object.assign(localUser, cloneDeep(newUser))
    },
    { deep: true }
  )

  // 表单校验
  const rules: FormRules = {
    nickName: [
      {
        required: true,
        message: () => t('system.userProfile.nickNameRequired'),
        trigger: 'blur'
      }
    ],
    email: [
      {
        required: true,
        message: () => t('system.userProfile.emailRequired'),
        trigger: 'blur'
      },
      {
        type: 'email',
        message: () => t('system.userProfile.validEmail'),
        trigger: ['blur', 'change']
      }
    ],
    phonenumber: [
      {
        required: true,
        message: () => t('system.userProfile.phoneNumberRequired'),
        trigger: 'blur'
      },
      {
        pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
        message: () => t('system.userProfile.validPhoneNumber'),
        trigger: 'blur'
      }
    ]
  }

  const emit = defineEmits<{
    (e: 'updateUser', user: User): void
  }>()

  const submit = () => {
    formRef.value?.validate((valid) => {
      if (valid) {
        updateUserProfile(localUser).then((res: any) => {
          MessageUtil.success(t('system.userProfile.updateSuccess'))
          emit('updateUser', res)
        })
      }
    })
  }

  const close = () => {
    worktabStore.removeTab(route.path)
    router.go(-1)
  }
</script>

<style lang="scss" scoped>
  .user-info-form {
    padding: 10px;

    .submit-btn {
      padding: 0 32px;
      font-weight: 600;
      border-radius: 10px;
      height: 44px;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(var(--el-color-primary-rgb), 0.3);
      }
    }

    .close-btn {
      border-radius: 10px;
      height: 44px;
    }
  }

  :deep(.el-form-item__label) {
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  :deep(.el-input__wrapper) {
    border-radius: 10px;
    box-shadow: 0 0 0 1px var(--el-border-color-lighter) inset;
    transition: all 0.3s ease;

    &.is-focus {
      box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
    }
  }
</style>
