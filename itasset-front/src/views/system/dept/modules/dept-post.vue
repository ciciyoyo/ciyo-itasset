<template>
  <div class="dept-post-page art-full-height">
    <!-- 搜索表单 -->
    <DeptPostSearch v-show="showSearch" v-model="searchParams" @search="handleSearch" @reset="resetSearchParams" />

    <!-- 表格卡片 -->
    <ElCard
      class="art-table-card flex flex-col flex-1 min-h-0"
      shadow="never"
      :style="{ 'margin-top': showSearch ? '12px' : '0' }"
    >
      <!-- 表格头部工具栏 -->
      <ArtTableHeader v-model:columns="columnChecks" v-model:showSearchBar="showSearch" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton @click="handleSetting" icon="ele-Plus" type="primary" v-hasPermi="['system:deptpost:setting']" v-ripple>
              {{ $t('system.deptPost.postSetting') }}
            </ElButton>
            <ElButton
              @click="handleDelete()"
              icon="ele-Delete"
              type="danger"
              plain
              :disabled="multiple"
              v-hasPermi="['system:deptpost:delete']"
            >
              {{ $t('system.deptPost.delete') }}
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        class="flex-1"
        :loading="loading"
        :data="deptPostList"
        :columns="columns"
        :pagination="{
          current: queryParams.current,
          size: queryParams.size,
          total: deptPostTotal
        }"
        row-key="id"
        :show-table-header="false"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
        <template #leaderPost="{ row }">
          <el-tag v-if="row.leaderPost === 1" type="success">
            {{ $t('system.deptPost.yes') }}
          </el-tag>
          <el-tag v-else type="danger">
            {{ $t('system.deptPost.no') }}
          </el-tag>
        </template>

        <template #operation="{ row }">
          <div class="flex gap-2">
            <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['system:deptpost:update']">
              {{ $t('system.deptPost.editAlias') }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['system:deptpost:delete']">
              {{ $t('common.delete') }}
            </el-button>
          </div>
        </template>
      </ArtTable>
    </ElCard>

    <!-- 添加或修改部门岗位关系对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="$t('system.deptPost.postShowName')" prop="postShowName">
          <el-input v-model="form.postShowName" :placeholder="$t('system.deptPost.enterPostShowName')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">
            {{ $t('common.confirm') }}
          </el-button>
          <el-button @click="cancel">{{ $t('common.cancel') }}</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 设置岗位对话框 -->
    <el-dialog
      :title="$t('system.deptPost.deptPostSetting')"
      v-model="settingOpen"
      width="800px"
      append-to-body
      :close-on-click-modal="false"
    >
      <el-form :inline="true" :model="postQueryParams">
        <el-form-item :label="$t('system.deptPost.postName')">
          <el-input
            v-model="postQueryParams.postShowName"
            :placeholder="$t('system.deptPost.enterPostName')"
            clearable
            @keyup.enter="handlePostQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="ele-Search" @click="handlePostQuery">
            {{ $t('system.deptPost.search') }}
          </el-button>
        </el-form-item>
      </el-form>

      <ArtTable
        ref="postTableRef"
        :loading="postLoading"
        :data="postList"
        :columns="postColumns"
        :pagination="{
          current: postQueryParams.current,
          size: postQueryParams.size,
          total: postTotal
        }"
        row-key="id"
        @selection-change="handlePostSelectionChange"
        @pagination:size-change="handlePostSizeChange"
        @pagination:current-change="handlePostCurrentChange"
      >
        <template #leaderPost="{ row }">
          <el-tag v-if="row.leaderPost === 1" type="success">
            {{ $t('system.deptPost.yes') }}
          </el-tag>
          <el-tag v-else type="danger">
            {{ $t('system.deptPost.no') }}
          </el-tag>
        </template>
      </ArtTable>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="settingOpen = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleSettingDeptPost">
            {{ $t('common.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
  import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
  import { useRoute } from 'vue-router'
  import { ElMessageBox } from 'element-plus'
  import { delDeptPost, pageDeptPost, queryDeptNotInPost, settingDeptPost, updateDeptPost } from '@/api/system/dept'
  import { i18n } from '@/i18n'
  import type { FormInstance } from 'element-plus'
  import type { ColumnOption } from '@/types/component'
  import { MessageUtil } from '@/utils/messageUtil'
  import { useI18n } from 'vue-i18n'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import DeptPostSearch from './dept-post-search.vue'

  defineOptions({ name: 'DeptPostManagement' })

  // 定义 props
  const props = defineProps<{
    deptId: number | string | null
  }>()

  const { t } = useI18n()

  // 状态和引用
  const loading = ref(true)
  const postLoading = ref(false)
  const showSearch = ref(true)
  const deptPostList = ref<any[]>([])
  const postList = ref<any[]>([])
  const title = ref('')
  const open = ref(false)
  const settingOpen = ref(false)
  const deptPostTotal = ref(0)
  const postTotal = ref(0)

  // 选择相关
  const ids = ref<number[]>([])
  const multiple = ref(true)
  const selectedPosts = ref<any[]>([]) // 岗位对话框选中的岗位

  // 搜索参数
  const searchParams = ref({
    postShowName: undefined
  })

  // 查询参数
  const queryParams = reactive({
    current: 1,
    size: 10,
    deptId: null as any,
    postShowName: undefined as any
  })

  // 岗位查询参数
  const postQueryParams = reactive({
    current: 1,
    size: 10,
    deptId: null as any,
    postShowName: ''
  })

  // 表单数据
  const form = reactive<any>({
    id: null,
    deptId: null,
    postId: null,
    postShowName: null
  })

  const formRef = ref<FormInstance | null>(null)
  const postTableRef = ref<any>(null)

  // 表单校验规则
  const rules = {
    postShowName: [
      {
        required: true,
        message: i18n.global.t('system.deptPost.postShowNameNotEmpty'),
        trigger: 'blur'
      }
    ]
  }

  // 表格列配置 - 主表格
  const { columnChecks, columns } = useTableColumns(() => [
    {
      type: 'selection',
      width: 55,
      align: 'center' as const
    },
    {
      prop: 'id',
      label: t('system.deptPost.postCode'),
      align: 'center' as const,
      width: 100
    },
    {
      prop: 'postName',
      label: t('system.deptPost.postName'),
      align: 'center' as const,
      width: 150
    },
    {
      prop: 'postShowName',
      label: t('system.deptPost.postShowName'),
      align: 'center' as const,
      minWidth: 150
    },
    {
      prop: 'leaderPost',
      label: t('system.deptPost.leaderPost'),
      align: 'center' as const,
      width: 120,
      useSlot: true
    },
    {
      prop: 'operation',
      label: t('common.operation'),
      align: 'center' as const,
      width: 200,
      fixed: 'right' as const,
      useSlot: true
    }
  ])

  // 表格列配置 - 岗位选择对话框表格
  const postColumns = computed<ColumnOption[]>(() => [
    {
      type: 'selection' as const,
      width: 55,
      align: 'center' as const,
      reserveSelection: true
    },
    {
      prop: 'id',
      label: t('system.deptPost.deptPostCode'),
      align: 'center' as const
    },
    {
      prop: 'postCode',
      label: t('system.deptPost.postCode'),
      align: 'center' as const
    },
    {
      prop: 'postName',
      label: t('system.deptPost.postName'),
      align: 'center' as const
    },
    {
      prop: 'leaderPost',
      label: t('system.deptPost.leaderPost'),
      align: 'center' as const,
      width: 120,
      useSlot: true
    }
  ])

  // 生命周期钩子
  onMounted(() => {
    queryParams.deptId = props.deptId || null
    getList()
  })

  // 监听 deptId prop 变化
  watch(
    () => props.deptId,
    (newId) => {
      if (newId !== undefined && newId !== null) {
        queryParams.deptId = newId
        queryParams.current = 1 // 重置为第一页
        getList()
      }
    }
  )

  // 方法定义

  // 重置搜索参数
  const resetSearchParams = () => {
    searchParams.value = {
      postShowName: undefined
    }
    queryParams.postShowName = undefined
    getList()
  }

  /** 查询部门岗位关系列表 */
  const getList = async () => {
    loading.value = true
    // 合并搜索参数
    Object.assign(queryParams, searchParams.value)
    try {
      const response = await pageDeptPost(queryParams)
      deptPostList.value = response.records
      deptPostTotal.value = response.total
    } catch (error) {
      console.error('获取部门岗位列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 刷新数据
  const refreshData = () => {
    getList()
  }

  // 搜索处理
  const handleSearch = () => {
    queryParams.current = 1
    getList()
  }

  // 多选框选中数据
  const handleSelectionChange = (selection: any[]) => {
    ids.value = selection.map((item) => item.id)
    multiple.value = !selection.length
  }

  // 分页大小变化
  const handleSizeChange = (val: number) => {
    queryParams.size = val
    getList()
  }

  // 分页当前页变化
  const handleCurrentChange = (val: number) => {
    queryParams.current = val
    getList()
  }

  const cancel = () => {
    open.value = false
    reset()
  }

  const reset = () => {
    Object.assign(form, {
      id: null,
      deptId: null,
      postId: null,
      postShowName: null
    })
    formRef.value?.resetFields()
  }

  /** 修改按钮操作 */
  const handleUpdate = (row: any) => {
    reset()
    nextTick(() => {
      Object.assign(form, row)
    })
    open.value = true
    title.value = i18n.global.t('system.deptPost.editAlias')
  }

  /** 提交按钮 */
  const submitForm = () => {
    formRef.value!.validate((valid: boolean) => {
      if (valid) {
        updateDeptPost(form).then(() => {
          MessageUtil.success(i18n.global.t('system.deptPost.editSuccess'))
          open.value = false
          getList()
        })
      }
    })
  }

  /** 查询岗位列表 */
  const getPostList = async () => {
    postLoading.value = true
    postQueryParams.deptId = queryParams.deptId
    try {
      const response = await queryDeptNotInPost(postQueryParams)
      postList.value = response.records
      postTotal.value = response.total
    } catch (error) {
      console.error('获取岗位列表失败:', error)
    } finally {
      postLoading.value = false
    }
  }

  /** 搜索岗位 */
  const handlePostQuery = () => {
    postQueryParams.current = 1
    getPostList()
  }

  // 岗位表格分页处理
  const handlePostSizeChange = (val: number) => {
    postQueryParams.size = val
    getPostList()
  }

  const handlePostCurrentChange = (val: number) => {
    postQueryParams.current = val
    getPostList()
  }

  const handlePostSelectionChange = (selection: any[]) => {
    // 存储选中的岗位数据
    selectedPosts.value = selection
  }

  /** 设置岗位 */
  const handleSetting = () => {
    settingOpen.value = true
    postQueryParams.postShowName = ''
    getPostList()
  }

  /** 删除按钮操作 */
  const handleDelete = (row?: any) => {
    const deleteIds = row?.id || ids.value.join(',')
    ElMessageBox.confirm(
      i18n.global.t('system.deptPost.confirmDelete') + '"' + deleteIds + '"' + i18n.global.t('system.deptPost.confirmDeleteData'),
      i18n.global.t('common.warning'),
      {
        confirmButtonText: i18n.global.t('common.confirm'),
        cancelButtonText: i18n.global.t('common.cancel'),
        type: 'warning'
      }
    )
      .then(() => {
        return delDeptPost(deleteIds)
      })
      .then(() => {
        getList()
        MessageUtil.success(i18n.global.t('system.deptPost.editSuccess'))
      })
      .catch(() => {})
  }

  /** 设置部门岗位 */
  const handleSettingDeptPost = () => {
    if (selectedPosts.value.length === 0) {
      MessageUtil.error(i18n.global.t('system.deptPost.selectPost'))
      return
    }

    const deptId = queryParams.deptId
    settingDeptPost({
      deptId,
      ids: selectedPosts.value.map((item: any) => item.id)
    }).then(() => {
      MessageUtil.success(i18n.global.t('system.deptPost.editSuccess'))
      settingOpen.value = false
      selectedPosts.value = [] // 清空选中的岗位
      getList()
    })
  }
</script>

<style scoped lang="scss">
  .dept-post-page {
    height: 100%;
    display: flex;
    flex-direction: column;
  }
</style>
