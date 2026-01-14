<template>
  <div class="dept-page art-full-height">
    <!-- 搜索表单 -->
    <DeptSearch v-show="showSearch" v-model="searchParams" @search="handleSearch" @reset="resetSearchParams" />

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
            <ElButton @click="handleAdd" icon="ele-Plus" type="primary" v-hasPermi="['system:dept:add']" v-ripple>
              {{ $t('system.deptManagement.addDepartment') }}
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        class="flex-1"
        :loading="loading"
        :data="treeData"
        :columns="columns"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :lazy="isLazyLoading"
        :load="isLazyLoading ? loadChildren : undefined"
        :default-expand-all="!isLazyLoading"
        row-key="id"
        :show-table-header="false"
      >
        <template #status="{ row }">
          {{ formatStatus(row) }}
        </template>

        <template #createTime="{ row }">
          {{ parseTime(row.createTime) }}
        </template>

        <template #operation="{ row }">
          <div class="flex gap-2">
            <el-button link type="primary" @click="handlePost(row)" v-hasPermi="['system:deptpost:list']">
              {{ $t('system.deptManagement.position') }}
            </el-button>
            <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['system:dept:edit']">
              {{ $t('common.update') }}
            </el-button>
            <el-button link type="primary" @click="handleAdd(row)" v-hasPermi="['system:dept:add']">
              {{ $t('common.add') }}
            </el-button>
            <el-button v-if="row.parentId != 0" link type="danger" @click="handleDelete(row)" v-hasPermi="['system:dept:remove']">
              {{ $t('common.delete') }}
            </el-button>
          </div>
        </template>
      </ArtTable>
    </ElCard>

    <!-- 添加或修改部门对话框 -->
    <el-dialog :title="title" v-model="open" append-to-body width="50%" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="150px">
        <el-row>
          <el-col v-if="form.parentId !== 0" :span="24">
            <el-form-item :label="$t('system.deptManagement.parentDepartment')" prop="parentId">
              <el-tree-select
                style="width: 310px !important"
                v-model="form.parentId"
                :data="deptOptions"
                :props="{ value: 'id', label: 'deptName', children: 'children' }"
                value-key="id"
                check-strictly
                :placeholder="$t('system.deptManagement.selectParentDepartment')"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('system.deptManagement.departmentName')" prop="deptName">
              <el-input v-model="form.deptName" :placeholder="$t('system.deptManagement.enterDepartmentName')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('system.deptManagement.showOrder')" prop="orderNum">
              <el-input-number v-model="form.orderNum" :min="0" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('system.deptManagement.leader')" prop="leader">
              <el-input v-model="form.leader" maxlength="20" :placeholder="$t('system.deptManagement.enterLeader')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('system.deptManagement.contactPhone')" prop="phone">
              <el-input v-model="form.phone" maxlength="11" :placeholder="$t('system.deptManagement.enterContactPhone')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('system.deptManagement.email')" prop="email">
              <el-input v-model="form.email" maxlength="50" :placeholder="$t('system.deptManagement.enterEmail')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('system.deptManagement.departmentState')">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in statusOptions" :key="dict.dictValue" :value="dict.dictValue">
                  {{ dict.dictLabel }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="submitForm">
            {{ $t('common.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 部门岗位管理弹窗 -->
    <el-dialog
      v-model="deptPostDialogOpen"
      :title="$t('system.deptPost.deptPostSetting')"
      width="90%"
      append-to-body
      :close-on-click-modal="false"
      destroy-on-close
    >
      <DeptPost v-if="deptPostDialogOpen" :dept-id="selectedDeptId" />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="deptPostDialogOpen = false">{{ $t('common.cancel') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
  import { computed, nextTick, onMounted, reactive, ref } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElMessageBox } from 'element-plus'
  import { addDept, delDept, getDept, listDeptExcludeChild, pageDept, updateDept, DeptEntity } from '@/api/system/dept'
  import { getDicts } from '@/api/system/dict/data'
  import { parseTime, selectDictLabel, handleTree } from '@/utils/business'
  import type { FormInstance } from 'element-plus'
  import { MessageUtil } from '@/utils/messageUtil'
  import { useI18n } from 'vue-i18n'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import DeptSearch from './modules/dept-search.vue'
  import DeptPost from './modules/dept-post.vue'

  defineOptions({ name: 'DeptManagement' })

  const { t } = useI18n()
  const router = useRouter()

  // 状态和引用
  const loading = ref(true)
  const showSearch = ref(true)
  const deptList = ref<DeptEntity[]>([])
  const deptOptions = ref<any[]>([])
  const title = ref('')
  const open = ref(false)
  const statusOptions = ref<any[]>([])

  // 部门岗位弹窗相关
  const deptPostDialogOpen = ref(false)
  const selectedDeptId = ref<number | null>(null)

  // 懒加载相关数据
  const isLazyLoading = ref(false)
  const allDeptData = ref<DeptEntity[]>([]) // 存储所有部门数据
  const lazyLoadThreshold = 200 // 懒加载阈值

  // 搜索参数
  const searchParams = ref({
    deptName: undefined,
    status: undefined
  })

  // 表单数据
  const form = reactive<any>({
    id: undefined,
    parentId: undefined,
    deptName: undefined,
    orderNum: undefined,
    leader: undefined,
    phone: undefined,
    email: undefined,
    status: '0'
  })

  const formRef = ref<FormInstance | null>(null)

  // 表单校验规则
  const rules = {
    parentId: [
      {
        required: true,
        message: t('system.deptManagement.parentDepartmentNotEmpty'),
        trigger: 'blur'
      }
    ],
    deptName: [
      {
        required: true,
        message: t('system.deptManagement.departmentNameNotEmpty'),
        trigger: 'blur'
      }
    ],
    orderNum: [
      {
        required: true,
        message: t('system.deptManagement.showOrderNotEmpty'),
        trigger: 'blur'
      }
    ],
    email: [
      {
        type: 'email' as const,
        message: t('system.deptManagement.enterEmail'),
        trigger: ['blur', 'change']
      }
    ],
    phone: [
      {
        pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
        message: t('system.deptManagement.enterCorrectPhoneNumber'),
        trigger: 'blur'
      }
    ]
  }

  // 表格列配置 - 使用 useTableColumns hook 支持自定义列显示
  const { columnChecks, columns } = useTableColumns(() => [
    {
      prop: 'deptName',
      label: t('system.deptManagement.departmentName'),
      showOverflowTooltip: true,
      minWidth: 260
    },
    {
      prop: 'id',
      label: t('system.deptManagement.departmentId'),
      minWidth: 260
    },
    {
      prop: 'orderNum',
      label: t('system.deptManagement.order'),
      minWidth: 200
    },
    {
      prop: 'status',
      label: t('system.deptManagement.status'),
      minWidth: 100,
      useSlot: true
    },
    {
      prop: 'createTime',
      label: t('system.deptManagement.createTime'),
      align: 'center' as const,
      minWidth: 200,
      useSlot: true
    },
    {
      prop: 'operation',
      label: t('common.operation'),
      align: 'center' as const,
      minWidth: 380,
      fixed: 'right' as const,
      useSlot: true
    }
  ])

  // 计算树形数据
  const treeData = computed(() => {
    if (isLazyLoading.value) {
      return deptList.value
    }
    return deptList.value
  })

  // 生命周期钩子
  onMounted(() => {
    getList()
    getDicts('sys_normal_disable')
      .then((response: any) => {
        statusOptions.value = response || []
      })
      .catch((error) => {
        console.error('获取状态字典失败:', error)
        statusOptions.value = []
      })
  })

  // 方法定义

  // 重置搜索参数
  const resetSearchParams = () => {
    searchParams.value = {
      deptName: undefined,
      status: undefined
    }
    getList()
  }

  /** 查询部门列表 */
  const getList = async () => {
    loading.value = true
    try {
      const response = await pageDept(searchParams.value)
      const allData = response
      allDeptData.value = allData // 保存所有数据

      // 判断数据量是否超过阈值
      if (allData.length > lazyLoadThreshold) {
        isLazyLoading.value = true
        // 懒加载模式下，只显示根节点，并设置hasChildren属性
        const rootNodes = allData
          .filter((item) => item.parentId === 0)
          .map((node) => {
            const hasChildren = allData.some((item) => item.parentId === node.id)
            return {
              ...node,
              hasChildren
            }
          })
        deptList.value = rootNodes
      } else {
        isLazyLoading.value = false
        // 普通模式下，显示所有数据
        deptList.value = handleTree(allData) as DeptEntity[]
      }
    } catch (error) {
      console.error('获取部门列表失败:', error)
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
    getList()
  }

  // 格式化状态
  const formatStatus = (row: any) => {
    if (!statusOptions.value || statusOptions.value.length === 0) {
      return row.status
    }
    return selectDictLabel(statusOptions.value, row.status)
  }

  /** 懒加载子节点 */
  const loadChildren = (tree: any, treeNode: any, resolve: any) => {
    // 从所有数据中查找当前节点的子节点
    let children = allDeptData.value.filter((item) => item.parentId === tree.id)

    // 如果当前有搜索条件，需要过滤子节点
    if (searchParams.value.deptName || searchParams.value.status) {
      children = children.filter((item) => {
        const matchName = !searchParams.value.deptName || item.deptName?.includes(searchParams.value.deptName)
        const matchStatus = !searchParams.value.status || item.status === searchParams.value.status
        return matchName && matchStatus
      })
    }

    // 为每个子节点设置hasChildren属性
    const processedChildren = children.map((child) => {
      let childNodes = allDeptData.value.filter((item) => item.parentId === child.id)

      // 如果有搜索条件，也需要过滤子节点的子节点
      if (searchParams.value.deptName || searchParams.value.status) {
        childNodes = childNodes.filter((item) => {
          const matchName = !searchParams.value.deptName || item.deptName?.includes(searchParams.value.deptName)
          const matchStatus = !searchParams.value.status || item.status === searchParams.value.status
          return matchName && matchStatus
        })
      }

      const hasChildren = childNodes.length > 0
      return {
        ...child,
        hasChildren
      }
    })

    resolve(processedChildren)
  }

  const cancel = () => {
    open.value = false
    reset()
  }

  const reset = () => {
    Object.assign(form, {
      id: undefined,
      parentId: undefined,
      deptName: undefined,
      orderNum: undefined,
      leader: undefined,
      phone: undefined,
      email: undefined,
      status: '0'
    })
    formRef.value?.resetFields()
  }

  const handleAdd = (row?: any) => {
    reset()
    open.value = true
    nextTick(() => {
      if (row && row.id) {
        form.parentId = row.id
      } else {
        form.parentId = undefined
      }
    })
    title.value = t('system.deptManagement.addDepartment')

    // 使用已保存的所有数据来构建部门选项
    if (allDeptData.value.length > 0) {
      deptOptions.value = handleTree(allDeptData.value, 'id') as any[]
    } else {
      // 如果没有数据，则重新获取
      pageDept({}).then((response) => {
        deptOptions.value = handleTree(response, 'id') as any[]
      })
    }
  }

  const handlePost = (row: any) => {
    selectedDeptId.value = row.id
    deptPostDialogOpen.value = true
  }

  const handleUpdate = (row: any) => {
    reset()
    getDept(row.id)
      .then((response) => {
        if (!response) {
          MessageUtil.error('获取部门信息失败')
          return
        }
        open.value = true
        nextTick(() => {
          Object.assign(form, response)
        })
        title.value = t('system.deptManagement.editDepartment')
      })
      .catch((error) => {
        console.error('获取部门详情失败:', error)
        MessageUtil.error('获取部门信息失败')
      })

    listDeptExcludeChild(row.id).then((response) => {
      deptOptions.value = handleTree(response, 'id') as any[]
    })
  }

  const submitForm = () => {
    formRef.value!.validate((valid: boolean) => {
      if (valid) {
        if (form.id !== undefined) {
          updateDept(form).then(() => {
            MessageUtil.success(t('common.saveSuccess'))
            open.value = false
            getList()
          })
        } else {
          addDept(form).then(() => {
            MessageUtil.success(t('common.saveSuccess'))
            open.value = false
            getList()
          })
        }
      }
    })
  }

  const handleDelete = (row: any) => {
    ElMessageBox.confirm(t('system.deptManagement.confirmDeleteData', { deptName: row.deptName }), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
      .then(() => {
        return delDept(row.id)
      })
      .then(() => {
        getList()
        MessageUtil.success(t('common.saveSuccess'))
      })
      .catch(() => {})
  }
</script>

<style scoped lang="scss">
  .dept-page {
    padding: 16px;
    height: 100%;
    display: flex;
    flex-direction: column;
  }
</style>
