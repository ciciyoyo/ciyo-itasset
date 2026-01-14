<template>
  <el-form ref="formRef" :model="localUser" :rules="rules" label-width="80px">
    <el-form-item :label="t('system.userProfile.nickName')" prop="nickName">
      <el-input v-model="localUser.nickName" maxlength="30" />
    </el-form-item>
    <el-form-item :label="t('system.userProfile.phoneNumber')" prop="phonenumber">
      <el-input v-model="localUser.phonenumber" maxlength="11" />
    </el-form-item>
    <el-form-item :label="t('system.userProfile.email')" prop="email">
      <el-input v-model="localUser.email" maxlength="50" />
    </el-form-item>
    <el-form-item :label="t('system.userProfile.gender')">
      <el-radio-group v-model="localUser.sex">
        <el-radio value="0">{{ t('system.userProfile.male') }}</el-radio>
        <el-radio value="1">{{ t('system.userProfile.female') }}</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item>
      <el-button size="default" type="primary" @click="submit">
        {{ t('system.userProfile.save') }}
      </el-button>
      <el-button size="default" type="danger" @click="close">
        {{ t('system.userProfile.close') }}
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script lang="ts" setup>
  import { computed, reactive, ref, watch } from 'vue'
  import { updateUserProfile } from '@/api/system/user'
  import { useI18n } from 'vue-i18n'
  import { useRoute, useRouter } from 'vue-router'
  import type { FormInstance, FormRules } from 'element-plus'
  import { cloneDeep } from 'lodash-es'
  import { useWorktabStore } from '@/store/modules/worktab'
  import { MessageUtil } from '@/utils/messageUtil'

  interface User {
    nickName: string
    phonenumber: string
    email: string
    sex: string
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
