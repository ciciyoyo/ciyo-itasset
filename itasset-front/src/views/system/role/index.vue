<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <RoleSearch v-model="searchParams" @search="handleSearch" @reset="handleReset" />

    <ElCard class="art-table-card" shadow="never">
      <!-- Table header -->
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <el-button v-hasPermi="['system:role:add']" icon="ele-Plus" type="primary" @click="handleAdd">
              {{ $t('system.roleManagement.add') }}
            </el-button>
            <el-button
              v-hasPermi="['system:role:remove']"
              :disabled="multiple"
              icon="ele-Delete"
              type="danger"
              @click="handleDelete"
            >
              {{ $t('system.roleManagement.delete') }}
            </el-button>
            <el-button v-hasPermi="['system:role:export']" :loading="exportLoading" icon="ele-Download" @click="handleExport">
              {{ $t('system.roleManagement.export') }}
            </el-button>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- Table -->
      <ArtTable
        :loading="loading"
        :data="tableData"
        :columns="columns"
        :pagination="pagination"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
        <template #status="{ row }">
          <el-switch v-model="row.status" active-value="0" inactive-value="1" @change="() => handleStatusChange(row)" />
        </template>
        <template #operation="{ row }">
          <template v-if="row.id !== 1">
            <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['system:role:edit']">
              {{ $t('system.roleManagement.edit') }}
            </el-button>
            <el-button link type="primary" @click="handleDataScope(row)" v-hasPermi="['system:role:edit']">
              {{ $t('system.roleManagement.dataPermission') }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['system:role:remove']">
              {{ $t('system.roleManagement.delete') }}
            </el-button>
          </template>
        </template>
      </ArtTable>
    </ElCard>

    <!-- 添加或修改角色配置对话框 -->
    <el-dialog :title="title" v-model="open" append-to-body width="50%">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
        <el-form-item :label="$t('system.roleManagement.roleName')" prop="roleName">
          <el-input v-model="form.roleName" :placeholder="$t('system.roleManagement.enterRoleName')" />
        </el-form-item>
        <!--        <el-form-item-->
        <!--          :label="$t('system.roleManagement.permissionCode')"-->
        <!--          prop="roleKey"-->
        <!--        >-->
        <!--          <el-input-->
        <!--            v-model="form.roleKey"-->
        <!--            :placeholder="$t('system.roleManagement.enterPermissionCode')"-->
        <!--          />-->
        <!--        </el-form-item>-->
        <el-form-item :label="$t('system.roleManagement.roleOrder')" prop="roleSort">
          <el-input-number size="default" v-model="form.roleSort" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item :label="$t('system.roleManagement.status')">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in statusOptions" :key="dict.dictValue" :value="dict.dictValue">
              {{ dict.dictLabel }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('system.roleManagement.menuPermission')">
          <div>
            <div class="flex flex-row w100">
              <el-checkbox v-model="menuExpand" @change="handleCheckedTreeExpand($event, 'menu')">
                {{ $t('system.roleManagement.expand') }}/{{ $t('system.roleManagement.collapse') }}
              </el-checkbox>
              <el-checkbox v-model="menuNodeAll" @change="handleCheckedTreeNodeAll($event, 'menu')">
                {{ $t('system.roleManagement.selectAll') }}/{{ $t('system.roleManagement.deselectAll') }}
              </el-checkbox>
              <el-checkbox v-model="form.menuCheckStrictly" @change="handleCheckedTreeConnect($event, 'menu')">
                {{ $t('system.roleManagement.parentChildLinkage') }}
              </el-checkbox>
            </div>
            <div>
              <el-scrollbar height="250px">
                <el-tree
                  ref="menuRef"
                  :check-strictly="!form.menuCheckStrictly"
                  :data="menuOptions"
                  :props="defaultProps"
                  class="tree-border"
                  :empty-text="$t('system.roleManagement.loading')"
                  node-key="id"
                  show-checkbox
                />
              </el-scrollbar>
            </div>
          </div>
        </el-form-item>
        <el-form-item :label="$t('system.roleManagement.remark')">
          <el-input v-model="form.remark" :placeholder="$t('system.roleManagement.enterRemark')" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancel">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitForm">
          {{ $t('common.confirm') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 分配角色数据权限对话框 -->
    <el-dialog :title="$t('system.roleManagement.assignDataPermission')" v-model="openDataScope" append-to-body width="40%">
      <el-form :model="form" label-width="130px">
        <el-form-item :label="$t('system.roleManagement.roleName')">
          <el-input v-model="form.roleName" :disabled="true" />
        </el-form-item>
        <!--        <el-form-item :label="$t('system.roleManagement.permissionCode')">-->
        <!--          <el-input-->
        <!--            v-model="form.roleKey"-->
        <!--            :disabled="true"-->
        <!--          />-->
        <!--        </el-form-item>-->
        <el-form-item :label="$t('system.roleManagement.permissionScope')">
          <el-select style="width: 350px" v-model="form.dataScope" @change="dataScopeSelectChange">
            <el-option v-for="item in dataScopeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-show="form.dataScope == 2" :label="$t('system.roleManagement.dataPermission')">
          <div>
            <div class="flex flex-row w100">
              <el-checkbox v-model="deptExpand" @change="handleCheckedTreeExpand($event, 'dept')">
                {{ $t('system.roleManagement.expand') }}/{{ $t('system.roleManagement.collapse') }}
              </el-checkbox>
              <el-checkbox v-model="deptNodeAll" @change="handleCheckedTreeNodeAll($event, 'dept')">
                {{ $t('system.roleManagement.selectAll') }}/{{ $t('system.roleManagement.deselectAll') }}
              </el-checkbox>
              <el-checkbox v-model="form.deptCheckStrictly" @change="handleCheckedTreeConnect($event, 'dept')">
                {{ $t('system.roleManagement.parentChildLinkage') }}
              </el-checkbox>
            </div>
            <el-tree
              ref="deptRef"
              :check-strictly="!form.deptCheckStrictly"
              :data="deptOptions"
              :props="defaultProps"
              class="tree-border w100"
              default-expand-all
              :empty-text="$t('system.roleManagement.loading')"
              node-key="id"
              show-checkbox
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDataScope">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitDataScope">
          {{ $t('common.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
  import { ref, reactive, toRefs, onMounted, nextTick, h } from 'vue'
  import { addRole, changeRoleStatus, dataScope, delRole, exportRole, getRole, pageRole, updateRole } from '@/api/system/role'
  import { roleMenuTreeselect, treeselect as menuTreeselect } from '@/api/system/menu'
  import { roleDeptTreeselect, treeselect as deptTreeselect } from '@/api/system/dept'
  import { getDicts } from '@/api/system/dict/data'
  import { parseTime, resetFormRef, download } from '@/utils/business'
  import { ElMessage, ElMessageBox, ElSwitch } from 'element-plus'
  import RoleSearch from './modules/role-search.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { useI18n } from 'vue-i18n'

  const { t } = useI18n()

  // Refs
  const formRef = ref()
  const menuRef = ref()
  const deptRef = ref()

  // 导出遮罩层
  const exportLoading = ref(false)
  // 选中数组
  const ids = ref<number[]>([])
  // 非单个禁用
  const single = ref(true)
  // 非多个禁用
  const multiple = ref(true)
  // 弹出层标题
  const title = ref('')
  // 是否显示弹出层
  const open = ref(false)
  // 是否显示弹出层（数据权限）
  const openDataScope = ref(false)
  const menuExpand = ref(false)
  const menuNodeAll = ref(false)
  const deptExpand = ref(true)
  const deptNodeAll = ref(false)
  // 状态数据字典
  const statusOptions = ref<any[]>([])
  // 搜索参数
  const searchParams = ref<Record<string, any>>({})
  // 数据范围选项
  const dataScopeOptions = ref([
    {
      value: '1',
      label: t('system.roleManagement.allDataPermission')
    },
    {
      value: '2',
      label: t('system.roleManagement.customDataPermission')
    },
    {
      value: '3',
      label: t('system.roleManagement.deptDataPermission')
    },
    {
      value: '4',
      label: t('system.roleManagement.deptAndSubDataPermission')
    },
    {
      value: '5',
      label: t('system.roleManagement.personalDataPermission')
    }
  ])
  // 菜单列表
  const menuOptions = ref<any[]>([])
  // 部门列表
  const deptOptions = ref<any[]>([])

  // 分页参数
  const pageParams = reactive({
    current: 1,
    size: 10
  })

  const formData = reactive({
    form: {} as any,
    rules: {
      roleName: [
        {
          required: true,
          message: t('system.roleManagement.roleNameNotNull'),
          trigger: 'blur'
        }
      ],
      roleSort: [
        {
          required: true,
          message: t('system.roleManagement.roleOrderNotNull'),
          trigger: 'blur'
        }
      ]
    }
  })

  const { form, rules } = toRefs(formData)

  const defaultProps = {
    children: 'children',
    label: 'label'
  }

  // useTable hook
  const {
    columns,
    columnChecks,
    data: tableData,
    loading,
    pagination,
    searchParams: tableSearchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData,
    getData
  } = useTable({
    core: {
      apiFn: pageRole,
      apiParams: {
        current: 1,
        size: 10
      },
      columnsFactory: () => [
        { type: 'selection' },
        { prop: 'id', label: t('system.roleManagement.roleNumber'), minWidth: 120 },
        { prop: 'roleName', label: t('system.roleManagement.roleName') },
        { prop: 'roleSort', label: t('system.roleManagement.displayOrder'), minWidth: 130 },
        {
          prop: 'status',
          label: t('system.roleManagement.status'),
          minWidth: 100,
          useSlot: true
        },
        {
          prop: 'createTime',
          label: t('system.roleManagement.createTime'),
          formatter: (row: any) => parseTime(row.createTime)
        },
        {
          prop: 'operation',
          label: t('system.roleManagement.operation'),
          useSlot: true,
          fixed: 'right',
          minWidth: 200
        }
      ]
    }
  })

  /** 查询角色列表 */
  const getList = async () => {
    try {
      const params = {
        ...pageParams,
        ...searchParams.value
      }
      // 更新 tableSearchParams
      Object.assign(tableSearchParams, params)
      await getData(params)
    } catch (error) {
      console.error('获取角色列表失败:', error)
    }
  }

  /** 查询菜单树结构 */
  const getMenuTreeselect = async () => {
    const response: any = await menuTreeselect()
    menuOptions.value = response
  }

  /** 查询部门树结构 */
  const getDeptTreeselect = async () => {
    const response: any = await deptTreeselect()
    deptOptions.value = response
  }

  // 所有菜单节点数据
  const getMenuAllCheckedKeys = () => {
    // 目前被选中的菜单节点
    const checkedKeys = menuRef.value!.getCheckedKeys()
    // 半选中的菜单节点
    const halfCheckedKeys = menuRef.value!.getHalfCheckedKeys()
    return [...halfCheckedKeys, ...checkedKeys]
  }

  // 所有部门节点数据
  const getDeptAllCheckedKeys = () => {
    // 目前被选中的部门节点
    const checkedKeys = deptRef.value!.getCheckedKeys()
    // 半选中的部门节点
    const halfCheckedKeys = deptRef.value!.getHalfCheckedKeys()
    return [...halfCheckedKeys, ...checkedKeys]
  }

  /** 根据角色ID查询菜单树结构 */
  const getRoleMenuTreeselect = async (roleId: any) => {
    const response: any = await roleMenuTreeselect(roleId)
    menuOptions.value = response.menus
    return response
  }

  /** 根据角色ID查询部门树结构 */
  const getRoleDeptTreeselect = async (roleId: any) => {
    const response: any = await roleDeptTreeselect(roleId)
    deptOptions.value = response.depts
    return response
  }

  // 角色状态修改
  const handleStatusChange = async (row: any) => {
    if (!row.id) {
      return
    }
    const text = row.status === '0' ? t('system.roleManagement.enable') : t('system.roleManagement.disable')
    try {
      await ElMessageBox.confirm(
        t('system.roleManagement.confirmRoleAction') + text + row.roleName + t('system.roleManagement.confirmRole'),
        t('common.warning'),
        {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }
      )
      await changeRoleStatus(row.id, row.status)
      ElMessage.success(text + t('common.saveSuccess'))
      await refreshData()
    } catch {
      row.status = row.status === '0' ? '1' : '0'
    }
  }

  // 取消按钮
  const cancel = () => {
    open.value = false
    reset()
  }

  // 取消按钮（数据权限）
  const cancelDataScope = () => {
    openDataScope.value = false
    reset()
  }

  // 表单重置
  const reset = () => {
    if (menuRef.value) {
      menuRef.value.setCheckedKeys([])
    }
    menuExpand.value = false
    menuNodeAll.value = false
    deptExpand.value = true
    deptNodeAll.value = false
    form.value = {
      id: undefined,
      roleName: undefined,
      roleKey: undefined,
      roleSort: 0,
      status: '0',
      menuIds: [],
      deptIds: [],
      menuCheckStrictly: true,
      deptCheckStrictly: true,
      remark: undefined
    }
    resetFormRef(formRef)
  }

  /** 搜索操作 */
  const handleSearch = (params: Record<string, any>) => {
    searchParams.value = params
    pageParams.current = 1
    getList()
  }

  /** 重置操作 */
  const handleReset = () => {
    searchParams.value = {}
    pageParams.current = 1
    resetSearchParams()
    getList()
  }

  // 多选框选中数据
  const handleSelectionChange = (selection: any[]) => {
    ids.value = selection.map((item) => item.id)
    single.value = selection.length !== 1
    multiple.value = !selection.length
  }

  // 树权限（展开/折叠）
  const handleCheckedTreeExpand = (value: any, type: string) => {
    if (type === 'menu') {
      const treeList = menuOptions.value
      treeList.forEach((item: any) => {
        // 访问内部存储属性
        menuRef.value!.store.nodesMap[item.id].expanded = value
      })
    } else if (type === 'dept') {
      const treeList = deptOptions.value
      treeList.forEach((item: any) => {
        // 访问内部存储属性
        deptRef.value!.store.nodesMap[item.id].expanded = value
      })
    }
  }

  // 树权限（全选/全不选）
  const handleCheckedTreeNodeAll = (value: any, type: string) => {
    if (type === 'menu') {
      menuRef.value!.setCheckedNodes(value ? menuOptions.value : [])
    } else if (type === 'dept') {
      deptRef.value!.setCheckedNodes(value ? deptOptions.value : [])
    }
  }

  // 树权限（父子联动）
  const handleCheckedTreeConnect = (value: any, type: string) => {
    if (type === 'menu') {
      form.value.menuCheckStrictly = !!value
    } else if (type === 'dept') {
      form.value.deptCheckStrictly = !!value
    }
  }

  /** 新增按钮操作 */
  const handleAdd = () => {
    reset()
    getMenuTreeselect()
    open.value = true
    title.value = t('system.roleManagement.addRole')
  }

  /** 修改按钮操作 */
  const handleUpdate = async (row: any) => {
    reset()
    const id = row.id || ids.value
    const roleMenuPromise = getRoleMenuTreeselect(id)

    try {
      const response: any = await getRole(id)
      form.value = response
      open.value = true

      await nextTick()
      const res: any = await roleMenuPromise
      const checkedKeys = res.checkedKeys
      checkedKeys.forEach((v: any) => {
        nextTick(() => {
          menuRef.value!.setChecked(v, true, false)
        })
      })
      title.value = t('system.roleManagement.editRole')
    } catch (error) {
      console.error('Failed to update role:', error)
    }
  }

  /** 选择角色权限范围触发 */
  const dataScopeSelectChange = (value: any) => {
    if (value !== '2') {
      deptRef.value!.setCheckedKeys([])
    }
  }

  /** 分配数据权限操作 */
  const handleDataScope = async (row: any) => {
    reset()
    const roleDeptTreeselectPromise = getRoleDeptTreeselect(row.id)

    try {
      const response: any = await getRole(row.id)
      form.value = response
      openDataScope.value = true

      await nextTick()
      const res: any = await roleDeptTreeselectPromise
      deptRef.value!.setCheckedKeys(res.checkedKeys)
      title.value = t('system.roleManagement.assignDataPermission')
    } catch (error) {
      console.error('Failed to handle data scope:', error)
    }
  }

  /** 提交按钮 */
  const submitForm = async () => {
    const valid = await formRef.value!.validate()
    if (valid) {
      form.value.menuIds = getMenuAllCheckedKeys()
      try {
        if (form.value.id !== undefined) {
          await updateRole(form.value)
        } else {
          await addRole(form.value)
        }
        ElMessage.success(t('common.saveSuccess'))
        open.value = false
        getList()
      } catch (error) {
        console.error('Failed to submit form:', error)
      }
    }
  }

  /** 提交按钮（数据权限） */
  const submitDataScope = async () => {
    if (form.value.id !== undefined) {
      form.value.deptIds = getDeptAllCheckedKeys()
      try {
        await dataScope(form.value)
        ElMessage.success(t('system.roleManagement.dataScopeSuccess'))
        openDataScope.value = false
        getList()
      } catch (error) {
        console.error('Failed to submit data scope:', error)
      }
    }
  }

  /** 删除按钮操作 */
  const handleDelete = async (row: any) => {
    const roleIds = row.id || ids.value
    try {
      await ElMessageBox.confirm(t('system.roleManagement.isDelete'), t('common.warning'), {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      })
      await delRole(roleIds)
      getList()
      ElMessage.success(t('common.saveSuccess'))
    } catch {
      // User cancelled or error occurred
    }
  }

  /** 导出按钮操作 */
  const handleExport = async () => {
    const exportParams = { ...pageParams, ...searchParams.value }
    try {
      await ElMessageBox.confirm(t('system.roleManagement.isDelete'), t('common.warning'), {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      })
      exportLoading.value = true
      const response: any = await exportRole(exportParams)
      download(response, t('system.roleManagement.roleData'))
    } catch {
    } finally {
      exportLoading.value = false
    }
  }

  onMounted(async () => {
    getList()
    try {
      const response: any = await getDicts('sys_normal_disable')
      statusOptions.value = response
      getDeptTreeselect()
    } catch (error) {
      console.error('Failed to load status options:', error)
    }
  })
</script>

<style scoped lang="scss">
  /* 移动端适配 */
  @media (max-width: 768px) {
    /* 对话框宽度 */
    :deep(.el-dialog) {
      width: 95% !important;
      max-width: 95vw !important;
      margin: 0 auto !important;
    }

    :deep(.el-dialog__body) {
      padding: 15px;
      max-height: calc(90vh - 120px);
      overflow-y: auto;
    }

    :deep(.el-dialog__footer) {
      padding: 12px 15px;

      .el-button {
        width: 48%;
        min-width: 0 !important;
        margin: 0 !important;
      }
    }

    /* 表单垂直布局 */
    :deep(.el-form) {
      .el-form-item {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        margin-bottom: 16px;
      }

      .el-form-item__label {
        text-align: left !important;
        width: 100% !important;
        max-width: 100% !important;
        margin-bottom: 6px !important;
        padding-right: 0 !important;
        float: none !important;
        display: block !important;
        font-size: 13px;
      }

      .el-form-item__content {
        margin-left: 0 !important;
        width: 100% !important;

        .el-input,
        .el-select,
        .el-input-number,
        .el-textarea {
          width: 100% !important;
        }

        .el-radio-group {
          width: 100%;
          display: flex;
          flex-wrap: wrap;
          gap: 8px;

          .el-radio {
            margin-right: 0 !important;
          }
        }
      }
    }

    /* 菜单权限和数据权限区域 */
    :deep(.flex.flex-row) {
      flex-direction: column !important;
      align-items: flex-start !important;
      gap: 8px;

      .el-checkbox {
        width: 100%;
        margin-right: 0 !important;
        margin-bottom: 4px;
      }
    }

    /* Tree 树形控件 */
    :deep(.el-tree) {
      font-size: 13px;
    }

    :deep(.tree-border) {
      max-height: 200px;
      overflow-y: auto;
    }

    :deep(.el-scrollbar) {
      max-height: 200px !important;
    }

    /* 选择框 */
    :deep(.el-select) {
      width: 100% !important;
    }
  }

  @media (max-width: 640px) {
    :deep(.el-dialog__body) {
      padding: 12px;
    }

    :deep(.el-dialog__footer) {
      padding: 10px 12px;
      flex-direction: column;
      gap: 8px;

      .el-button {
        width: 100% !important;
      }
    }

    :deep(.el-form .el-form-item) {
      margin-bottom: 12px;
    }

    :deep(.el-form .el-form-item__label) {
      font-size: 12px !important;
    }

    :deep(.tree-border),
    :deep(.el-scrollbar) {
      max-height: 150px !important;
    }

    :deep(.flex.flex-row .el-checkbox) {
      font-size: 12px;
    }
  }
</style>
