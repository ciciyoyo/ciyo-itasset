<template>
  <div class="reset-pwd-form">
    <el-form ref="formRef" :model="passwordForm" :rules="rules" label-position="top">
      <el-form-item :label="$t('system.changePassword.oldPassword')" prop="oldPassword">
        <el-input
          v-model="passwordForm.oldPassword"
          :placeholder="$t('system.changePassword.oldPasswordRequired')"
          type="password"
          show-password
        />
      </el-form-item>
      <el-form-item :label="$t('system.changePassword.newPassword')" prop="newPassword">
        <el-input
          v-model="passwordForm.newPassword"
          :placeholder="$t('system.changePassword.newPasswordRequired')"
          type="password"
          show-password
        />
      </el-form-item>
      <el-form-item :label="$t('system.changePassword.confirmPassword')" prop="confirmPassword">
        <el-input
          v-model="passwordForm.confirmPassword"
          :placeholder="$t('system.changePassword.confirmPasswordRequired')"
          type="password"
          show-password
        />
      </el-form-item>
      <div class="form-actions mt-8">
        <el-button size="large" type="primary" class="submit-btn" @click="submit">
          <i class="i-ri-shield-check-line mr-2" />
          {{ $t('system.changePassword.save') }}
        </el-button>
        <el-button size="large" plain class="close-btn" @click="close">
          {{ $t('system.changePassword.close') }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script lang="ts" setup>
  import { reactive, ref } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { useI18n } from 'vue-i18n'
  import { updateUserPwd } from '@/api/system/user'
  import { encrypt } from '@/utils/sys/jsencrypt'
  import { useWorktabStore } from '@/store/modules/worktab'
  import { MessageUtil } from '@/utils/messageUtil'
  import type { FormInstance, FormRules } from 'element-plus'

  const { t } = useI18n()
  const router = useRouter()
  const route = useRoute()
  const worktabStore = useWorktabStore()

  const formRef = ref<FormInstance>()

  const passwordForm = reactive({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  })

  const equalToPassword = (rule: any, value: string, callback: any) => {
    if (passwordForm.newPassword !== value) {
      callback(new Error(t('system.changePassword.confirmPasswordMatch')))
    } else {
      callback()
    }
  }

  const rules = reactive<FormRules>({
    oldPassword: [
      {
        required: true,
        message: t('system.changePassword.oldPasswordRequired'),
        trigger: 'blur'
      }
    ],
    newPassword: [
      {
        required: true,
        message: t('system.changePassword.newPasswordRequired'),
        trigger: 'blur'
      },
      {
        min: 6,
        max: 20,
        message: t('system.changePassword.newPasswordLength'),
        trigger: 'blur'
      }
    ],
    confirmPassword: [
      {
        required: true,
        message: t('system.changePassword.confirmPasswordRequired'),
        trigger: 'blur'
      },
      { required: true, validator: equalToPassword, trigger: 'blur' }
    ]
  })

  const submit = () => {
    formRef.value?.validate((valid) => {
      if (valid) {
        updateUserPwd(encrypt(passwordForm.oldPassword), encrypt(passwordForm.newPassword)).then(() => {
          MessageUtil.success(t('system.changePassword.updateSuccess'))
          // Clear form after success
          passwordForm.oldPassword = ''
          passwordForm.newPassword = ''
          passwordForm.confirmPassword = ''
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
  .reset-pwd-form {
    padding: 10px;
    max-width: 600px;

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
