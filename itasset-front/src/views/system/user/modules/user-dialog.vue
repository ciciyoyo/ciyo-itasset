<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? $t('system.userManagement.addUser') : $t('system.userManagement.editUser')"
    width="50%"
    align-center
    append-to-body
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="110px">
      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem :label="$t('system.userManagement.nickName')" prop="nickName">
            <ElInput v-model="formData.nickName" maxlength="30" :placeholder="$t('system.userManagement.enterNickName')" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem :label="$t('system.userManagement.dept')" prop="deptId">
            <ElTreeSelect
              v-model="formData.deptId"
              :data="deptOptions"
              check-strictly
              :props="{ value: 'id', label: 'label' }"
              :placeholder="$t('system.userManagement.selectDept')"
              @change="handleDeptChange"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem :label="$t('system.userManagement.phoneNumber')" prop="phonenumber">
            <ElInput v-model="formData.phonenumber" maxlength="11" :placeholder="$t('system.userManagement.enterPhoneNumber')" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem :label="$t('system.userManagement.email')" prop="email">
            <ElInput v-model="formData.email" maxlength="50" :placeholder="$t('system.userManagement.enterEmail')" />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow :gutter="20" v-if="dialogType === 'add'">
        <ElCol :span="12">
          <ElFormItem :label="$t('system.userManagement.username')" prop="userName">
            <ElInput v-model="formData.userName" maxlength="30" :placeholder="$t('system.userManagement.enterUsername')" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem :label="$t('system.userManagement.password')" prop="password">
            <ElInput
              v-model="formData.password"
              maxlength="20"
              show-password
              :placeholder="$t('system.userManagement.enterPassword')"
              type="password"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem :label="$t('system.userManagement.sex')">
            <ElSelect v-model="formData.sex" :placeholder="$t('system.userManagement.selectSex')">
              <ElOption v-for="dict in sexOptions" :key="dict.dictValue" :label="dict.dictLabel" :value="dict.dictValue" />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem :label="$t('system.userManagement.status')">
            <ElRadioGroup v-model="formData.status">
              <ElRadio v-for="dict in statusOptions" :key="dict.dictValue" :value="dict.dictValue">
                {{ dict.dictLabel }}
              </ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem :label="$t('system.userManagement.post')">
            <ElSelect v-model="formData.postIds" multiple :placeholder="$t('system.userManagement.selectPost')">
              <ElOption
                v-for="item in postOptions"
                :key="item.postId || item.id"
                :label="item.postName"
                :value="item.postId || item.id"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem :label="$t('system.userManagement.role')">
            <ElSelect v-model="formData.roleIds" multiple :placeholder="$t('system.userManagement.selectRole')">
              <ElOption
                v-for="item in roleOptions"
                :key="item.id"
                :disabled="item.status == '1'"
                :label="item.roleName"
                :value="item.id"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow>
        <ElCol :span="24">
          <ElFormItem :label="$t('system.userManagement.remark')">
            <ElInput v-model="formData.remark" type="textarea" :placeholder="$t('system.userManagement.enterRemark')" />
          </ElFormItem>
        </ElCol>
      </ElRow>
    </ElForm>

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="dialogVisible = false">{{ $t('system.userManagement.cancel') }}</ElButton>
        <ElButton type="primary" @click="handleSubmit">{{ $t('system.userManagement.confirm') }}</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { DictDataEntity, getDicts } from '@/api/system/dict/data'
  import { getUser, addUser, updateUser } from '@/api/system/user'
  import { ElMessage } from 'element-plus'
  import { getConfigValue } from '@/api/system/config'
  import { ref } from 'vue'
  import { DeptPostEntity } from '@/api/system/dept'
  import { SysRoleEntity } from '@/api/system/role'
  import { useI18n } from 'vue-i18n'

  const { t } = useI18n()

  interface Props {
    visible: boolean
    type: string
    userData?: any
    deptOptions: any[]
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // 对话框显示控制
  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const dialogType = computed(() => props.type)

  // 表单实例
  const formRef = ref<FormInstance>()

  // 下拉选项
  const statusOptions = ref<DictDataEntity[]>([])
  const sexOptions = ref<DictDataEntity[]>([])
  const postOptions = ref<DeptPostEntity[]>([])
  const roleOptions = ref<SysRoleEntity[]>([])
  const roleList = ref<SysRoleEntity[]>([])
  const initPassword = ref('')

  // 表单数据
  const formData = reactive({
    id: undefined as any,
    deptId: undefined as any,
    userName: '',
    nickName: '',
    password: '',
    phonenumber: '',
    email: '',
    sex: undefined as any,
    status: '0',
    remark: '',
    postIds: [] as any[],
    roleIds: [] as any[]
  })

  // 表单验证规则
  const rules: FormRules = {
    userName: [{ required: true, message: t('system.userManagement.usernameRequired'), trigger: 'blur' }],
    nickName: [{ required: true, message: t('system.userManagement.nickNameRequired'), trigger: 'blur' }],
    password: [
      { required: true, message: t('system.userManagement.passwordRequired'), trigger: 'blur' },
      { min: 5, max: 20, message: t('system.userManagement.passwordLength'), trigger: 'blur' }
    ],
    email: [{ type: 'email', message: t('system.userManagement.validEmail'), trigger: ['blur', 'change'] }],
    phonenumber: [
      {
        pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
        message: t('system.userManagement.validPhoneNumber'),
        trigger: 'blur'
      }
    ]
  }

  // 加载基础数据
  const loadBaseData = async () => {
    try {
      // 加载字典数据
      const sexRes = await getDicts('sys_user_sex')
      sexOptions.value = sexRes || []

      const statusRes = await getDicts('sys_normal_disable')
      statusOptions.value = statusRes || []

      // 设置初始密码
      getConfigValue('sys.user.initPassword').then((response) => {
        initPassword.value = response
      })
    } catch (error) {
      console.error('加载基础数据失败:', error)
    }
  }

  // 部门改变时获取岗位列表
  const handleDeptChange = async () => {
    // 可以在这里添加获取岗位列表的逻辑
    formData.postIds = []
  }

  // 初始化表单数据
  const initFormData = async () => {
    console.log(props.userData)
    const isEdit = props.type === 'edit' && props.userData && props.userData.id

    if (isEdit) {
      // 编辑模式：加载用户数据
      try {
        const response = await getUser(props.userData.id)
        Object.assign(formData, {
          id: response.user.id,
          deptId: response.user.deptId,
          userName: response.user.userName,
          nickName: response.user.nickName,
          password: undefined,
          phonenumber: response.user.phonenumber,
          email: response.user.email,
          sex: response.user.sex,
          status: response.user.status,
          remark: response.user.remark,
          postIds: response?.postIds || [],
          roleIds: response?.roleIds || []
        })
        roleOptions.value = response.roles || []

        // 加载岗位列表（简化版）
        postOptions.value = []
        if (response?.postIds) {
          formData.postIds = response?.postIds || []
        }
      } catch (error) {
        console.error('加载用户数据失败:', error)
      }
    } else {
      // 新增模式：加载角色列表并重置表单
      try {
        const response = await getUser('')
        roleOptions.value = response.roles || []

        Object.assign(formData, {
          id: undefined,
          deptId: undefined,
          userName: '',
          nickName: '',
          password: initPassword.value,
          phonenumber: '',
          email: '',
          sex: undefined,
          status: '0',
          remark: '',
          postIds: [],
          roleIds: []
        })
        postOptions.value = []
      } catch (error) {
        console.error('加载角色列表失败:', error)
      }
    }
  }

  // 监听对话框状态变化
  watch(
    () => [props.visible, props.type, props.userData],
    async ([visible]) => {
      if (visible) {
        await loadBaseData()
        await initFormData()
        nextTick(() => {
          formRef.value?.clearValidate()
        })
      }
    },
    { immediate: true }
  )

  // 提交表单
  const handleSubmit = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid) => {
      if (valid) {
        try {
          if (formData.id) {
            const submitData: any = { ...formData }
            await updateUser(submitData)
            ElMessage.success(t('system.userManagement.updateUser'))
          } else {
            await addUser(formData)
            ElMessage.success(t('system.userManagement.addUser'))
          }
          dialogVisible.value = false
          emit('submit')
        } catch (error) {
          console.error('提交失败:', error)
        }
      }
    })
  }
</script>
