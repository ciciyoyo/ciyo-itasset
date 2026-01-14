<template>
  <div class="user-select">
    <el-select
      v-model="selectedUserNames"
      :collapse-tags="true"
      :multiple="multiple"
      :placeholder="$t('system.userManagement.chooseUser')"
      class="user-select__input"
      size="default"
      @change="handleSelectChange"
      @remove-tag="handleRemoveUser"
    >
      <template #header>
        <div class="user-select__header">
          <el-button bg size="default" text @click="handleChooseUser">
            <el-icon>
              <ele-Plus />
            </el-icon>
            {{ $t('system.userManagement.chooseUserButton') }}
          </el-button>
          <el-button bg size="default" text @click="handleAddCurrentUser">
            <el-icon>
              <ele-User />
            </el-icon>
            {{ $t('system.userManagement.currentUser') }}
          </el-button>
        </div>
      </template>
      <el-option v-for="item in selectedUsers" :key="item.id" :label="item.nickName || item.userName" :value="item.userName">
        <span>{{ item.nickName || item.userName }}</span>
      </el-option>
    </el-select>

    <choose-table ref="chooseTableRef" :multiple="multiple" @submit="handleSubmit" />
  </div>
</template>
<script lang="ts" setup>
  import { UserEntity } from '@/api/system/user'
  import { PropType, ref, watch } from 'vue'
  import ChooseTable from './chooseTable.vue'
  import { i18n } from '@/i18n'

  const props = defineProps({
    modelValue: {
      type: Array as PropType<string[] | string>,
      default: () => []
    },
    // 是否多选
    multiple: {
      type: Boolean,
      default: true
    }
  })

  const emit = defineEmits<{
    'update:modelValue': [users: string[]]
  }>()

  const selectedUserNames = ref<string[]>(Array.isArray(props.modelValue) ? props.modelValue : [])
  const selectedUsers = ref<UserEntity[]>([])
  const chooseTableRef = ref<InstanceType<typeof ChooseTable>>()

  // 监听父组件传入的值变化
  watch(
    () => props.modelValue,
    (newVal) => {
      selectedUserNames.value = Array.isArray(newVal) ? newVal : []
    }
  )

  // 打开选择用户弹窗
  const handleChooseUser = () => {
    chooseTableRef.value?.showDialog(selectedUsers.value.filter((user) => user.id != 0))
  }

  const currentUser = {
    deptId: '',
    id: 0,
    userName: '0',
    nickName: i18n.global.t('system.userManagement.currentUser')
  }

  // 选择用户后的回调
  const handleSubmit = (users: UserEntity[]) => {
    // 如果有当前用户 不要清除了 放到最后面
    const hasCurrentUser = selectedUsers.value.find((item) => item.id == 0)
    selectedUsers.value = users
    if (hasCurrentUser) {
      selectedUsers.value.push(currentUser)
    }
    selectedUserNames.value = selectedUsers.value.map((user) => user.userName)
    emit('update:modelValue', selectedUserNames.value)
  }

  // 移除选中用户
  const handleRemoveUser = (userName: string) => {
    const index = selectedUsers.value?.findIndex((item) => item.userName === userName)
    if (index !== -1) {
      const newUsers = [...selectedUsers.value]
      newUsers.splice(index, 1)
      selectedUsers.value = newUsers
      emit(
        'update:modelValue',
        newUsers.map((user) => user.userName)
      )
    }
  }

  const handleSelectChange = (val: string[]) => {
    selectedUsers.value = selectedUsers.value.filter((user) => val.includes(user.userName))
  }

  // 添加当前用户
  const handleAddCurrentUser = () => {
    // 检查是否已经存在
    if (selectedUsers.value.find((item) => item.id == 0)) {
      return
    }
    selectedUsers.value.push(currentUser)
    selectedUserNames.value = selectedUsers.value.map((user: UserEntity) => user.userName)
    emit('update:modelValue', selectedUserNames.value)
  }
</script>

<style lang="scss" scoped>
  .user-select {
    display: flex;
    gap: 8px;

    &__input {
      flex: 1;
    }

    &__header {
      display: flex;
      gap: 8px;
      padding: 8px;
    }
  }
</style>
