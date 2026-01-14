<template>
  <div class="menu-page art-full-height">
    <!-- 搜索表单 -->
    <MenuSearch v-show="showSearch" v-model="searchParams" @search="handleSearch" @reset="resetSearchParams" />

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
            <ElButton @click="handleAdd" icon="ele-Plus" type="primary" v-hasPermi="['system:menu:add']" v-ripple>
              {{ $t('system.menu.handleAdd') }}
            </ElButton>

            <ElButton icon="ele-Sort" v-ripple @click="toggleExpandAll">
              {{ isExpandAll ? $t('system.menu.collapseAll') : $t('system.menu.expandAll') }}
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        ref="tableRef"
        class="flex-1"
        :loading="loading"
        :data="treeData"
        :columns="columns"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        row-key="id"
        :show-table-header="false"
      >
        <template #icon="{ row }">
          <i :class="row.icon" />
        </template>

        <template #status="{ row }">
          {{ formatStatus(row) }}
        </template>

        <template #createTime="{ row }">
          {{ parseTime(row.createTime) }}
        </template>

        <template #operation="{ row }">
          <div class="flex gap-2">
            <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['system:menu:edit']">
              {{ $t('common.update') }}
            </el-button>
            <el-button link type="primary" @click="handleAdd(row)" v-hasPermi="['system:menu:add']">
              {{ $t('common.add') }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['system:menu:remove']">
              {{ $t('common.delete') }}
            </el-button>
          </div>
        </template>
      </ArtTable>
    </ElCard>

    <!-- 添加或修改菜单对话框 -->
    <el-dialog :title="title" v-model="open" append-to-body width="40%" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="24">
            <el-form-item :label="$t('system.menu.parentMenu')">
              <el-tree-select
                v-model="form.parentId"
                :data="menuOptions"
                @change="handleTreeSelect"
                :props="{ value: 'id', label: 'menuName', children: 'children' }"
                value-key="id"
                :placeholder="$t('system.menu.selectParentMenu')"
                check-strictly
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('system.menu.menuType')" prop="menuType">
              <el-radio-group v-model="form.menuType">
                <el-radio value="M" v-if="!['M', 'C', 'F'].includes(parentType)">
                  {{ $t('system.menu.catalog') }}
                </el-radio>
                <el-radio value="C" v-if="!['C', 'F'].includes(parentType)">
                  {{ $t('system.menu.menu') }}
                </el-radio>
                <el-radio value="F" v-if="!['M', 'F'].includes(parentType)">
                  {{ $t('system.menu.button') }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('system.menu.menuIcon')">
              <div class="flex gap-2">
                <el-input v-model="form.icon" :placeholder="$t('system.menu.clickSelectIcon')" readonly class="flex-1">
                  <template #prefix>
                    <ArtSvgIcon v-if="form.icon" :icon="form.icon" class="mr-1" />
                  </template>
                </el-input>
                <el-button @click="showIconDialog = true">
                  {{ $t('system.menu.clickSelectIcon') }}
                </el-button>
                <el-button v-if="form.icon" @click="form.icon = ''" type="danger" plain>
                  {{ $t('common.delete') }}
                </el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('system.menu.menuName')" prop="menuName">
              <el-input v-model="form.menuName" :placeholder="$t('system.menu.enterMenuName')" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType != 'F'">
            <el-form-item :label="$t('system.menu.langKey')" prop="langKey">
              <el-input v-model="form.langKey" :placeholder="$t('system.menu.enterLangKey')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('system.menu.sortOrder')" prop="orderNum">
              <el-input-number v-model="form.orderNum" :min="0" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType == 'C'">
            <el-form-item :label="$t('system.menu.externalLink')">
              <el-radio-group v-model="form.isFrame">
                <el-radio :value="0">{{ $t('system.menu.yes') }}</el-radio>
                <el-radio :value="1">{{ $t('system.menu.no') }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType !== 'F'">
            <el-form-item :label="$t('system.menu.routePath')" prop="path">
              <el-input v-model="form.path" :placeholder="$t('system.menu.enterRouteAddress')" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType == 'C'">
            <el-form-item label-width="150px" :label="$t('system.menu.component')" prop="component">
              <el-input v-model="form.component" :placeholder="$t('system.menu.enterComponent')" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType != 'M'">
            <el-form-item label-width="130px" :label="$t('system.menu.permission')">
              <el-input v-model="form.perms" maxlength="100" :placeholder="$t('system.menu.enterPermissionIdentifier')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('system.menu.visible')" v-if="form.menuType != 'F'">
              <el-radio-group v-model="form.visible">
                <el-radio v-for="dict in visibleOptions" :key="dict.dictValue" :value="dict.dictValue">
                  {{ dict.dictLabel }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('system.menu.menuStatus')" v-if="form.menuType != 'F'">
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

    <!-- 图标选择弹窗 -->
    <el-dialog v-model="showIconDialog" title="选择图标" width="700px" append-to-body>
      <IconSelect ref="iconSelectRef" @selected="selected" />
      <template #footer>
        <el-button @click="showIconDialog = false">{{ $t('common.close') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
  import { computed, nextTick, onMounted, ref } from 'vue'
  import { addMenu, delMenu, getMenu, listMenu, MenuEntity, updateMenu } from '@/api/system/menu'
  import IconSelect from './IconfontSelect.vue'
  import MenuSearch from './modules/menu-search.vue'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import type { FormInstance } from 'element-plus'
  import { ElMessageBox } from 'element-plus'
  import { getDicts } from '@/api/system/dict/data'
  import { handleTree, parseTime, selectDictLabel } from '@/utils/business'
  import { MessageUtil } from '@/utils/messageUtil'
  import { useI18n } from 'vue-i18n'
  import { useTableColumns } from '@/hooks/core/useTableColumns'

  defineOptions({ name: 'MenuManagement' })

  const { t } = useI18n()

  // 状态和引用
  const loading = ref(true)
  const showSearch = ref(true)
  const menuList = ref<MenuEntity[]>([])
  const menuOptions = ref<any[]>([])
  const title = ref('')
  const open = ref(false)
  const visibleOptions = ref<any[]>([])
  const statusOptions = ref<any[]>([])
  const showIconDialog = ref(false)
  const isExpandAll = ref(false)
  const tableRef = ref()

  const toggleExpandAll = () => {
    isExpandAll.value = !isExpandAll.value
    toggleRowExpansionAll(treeData.value, isExpandAll.value)
  }

  const toggleRowExpansionAll = (data: any[], isExpansion: boolean) => {
    data.forEach((item) => {
      tableRef.value.elTableRef.toggleRowExpansion(item, isExpansion)
      if (item.children && item.children.length > 0) {
        toggleRowExpansionAll(item.children, isExpansion)
      }
    })
  }

  // 搜索参数
  const searchParams = ref({
    menuName: undefined,
    visible: undefined,
    location: undefined,
    status: undefined
  })

  // 表单数据
  const form = ref<any>({
    id: undefined,
    parentId: 0,
    menuName: undefined,
    icon: undefined,
    menuType: 'M',
    orderNum: undefined,
    isFrame: 1,
    isCache: 0,
    visible: '0',
    status: '0',
    location: '1',
    langKey: ''
  })

  const parentType = ref('')

  const formRef = ref<FormInstance | null>(null)
  const queryFormRef = ref<FormInstance | null>(null)
  const iconSelectRef = ref<InstanceType<typeof IconSelect> | null>(null)

  // 表单校验规则
  const rules = {
    menuName: [{ required: true, message: t('system.menu.menuNameRequied'), trigger: 'blur' }],
    orderNum: [{ required: true, message: t('system.menu.menuOrderRequied'), trigger: 'blur' }],
    path: [{ required: true, message: t('system.menu.routeRequied'), trigger: 'blur' }]
  }

  // 表格列配置 - 使用 useTableColumns hook 支持自定义列显示
  const { columnChecks, columns } = useTableColumns(() => [
    {
      prop: 'menuName',
      label: t('system.menu.menuName'),
      showOverflowTooltip: true,
      minWidth: 200
    },
    {
      prop: 'icon',
      label: t('system.menu.icon'),
      align: 'center',
      minWidth: 80,
      useSlot: true
    },
    {
      prop: 'orderNum',
      label: t('system.menu.sortOrder'),
      minWidth: 80
    },
    {
      prop: 'perms',
      label: t('system.menu.permission'),
      showOverflowTooltip: true,
      minWidth: 150
    },
    {
      prop: 'component',
      label: t('system.menu.component'),
      showOverflowTooltip: true,
      minWidth: 180
    },
    {
      prop: 'status',
      label: t('system.menu.status'),
      minWidth: 180,
      useSlot: true
    },
    {
      prop: 'createTime',
      label: t('system.menu.createTime'),
      align: 'center',
      minWidth: 160,
      useSlot: true
    },
    {
      prop: 'operation',
      label: t('common.operation'),
      align: 'center',
      minWidth: 280,
      fixed: 'right' as const,
      useSlot: true
    }
  ])

  // 计算树形数据
  const treeData = computed(() => {
    const list = !searchParams.value.location
      ? menuList.value
      : filterMenuByTypeAndLocation(menuList.value, searchParams.value.location)
    return handleTree(list, 'id')
  })

  // 生命周期钩子
  onMounted(() => {
    getList()
    getDicts('sys_show_hide')
      .then((response: any) => {
        visibleOptions.value = response.data || response || []
      })
      .catch((error) => {
        console.error('获取显示状态字典失败:', error)
        visibleOptions.value = []
      })
    getDicts('sys_normal_disable')
      .then((response: any) => {
        statusOptions.value = response.data || response || []
      })
      .catch((error) => {
        console.error('获取菜单状态字典失败:', error)
        statusOptions.value = []
      })
  })

  // 方法定义
  const selected = (name: string) => {
    form.value.icon = name
    showIconDialog.value = false
  }

  // 重置搜索参数
  const resetSearchParams = () => {
    searchParams.value = {
      menuName: undefined,
      visible: undefined,
      location: undefined,
      status: undefined
    }
    getList()
  }

  const getList = async () => {
    loading.value = true
    try {
      const response = await listMenu(searchParams.value)
      menuList.value = response
    } catch (error) {
      console.error('获取菜单列表失败:', error)
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

  // 重置搜索
  const resetSearch = () => {
    queryFormRef.value?.resetFields()
    searchParams.value = {
      menuName: undefined,
      visible: undefined,
      location: undefined,
      status: undefined
    }
    getList()
  }

  // 过滤菜单
  function filterMenuByTypeAndLocation(menuArray: MenuEntity[], location: string | undefined): MenuEntity[] {
    function findChildren(parentId: number | undefined, menuArray: MenuEntity[], result: MenuEntity[]) {
      const children = menuArray.filter((menu) => menu.parentId == parentId)
      result.push(...children)
      for (const child of children) {
        findChildren(child.id, menuArray, result)
      }
      return children
    }

    const topLevelMenus = menuArray.filter(
      (menu) => menu.menuType === 'M' && (menu.location == location || (searchParams.value.location == '1' && !menu.location))
    )

    const result: MenuEntity[] = []
    for (const menu of topLevelMenus) {
      result.push(menu)
      findChildren(menu.id, menuArray, result)
    }

    return result
  }

  const getTreeSelect = () => {
    listMenu({}).then((response) => {
      menuOptions.value = []
      const menu = { id: 0, menuName: t('system.menu.topMenu'), children: [] }
      menu.children = handleTree(response, 'id') as []
      menuOptions.value.push(menu)
    })
  }

  const handleTreeSelect = (id: number) => {
    getMenu(id)
      .then((res) => {
        if (res) {
          parentType.value = res.menuType
        } else {
          parentType.value = ''
        }
        form.value.menuType = ''
      })
      .catch(() => {
        parentType.value = ''
      })
  }

  // 格式化状态
  const formatStatus = (row: any) => {
    if (row.menuType == 'F') {
      return ''
    }
    if (!statusOptions.value || statusOptions.value.length === 0) {
      return row.status
    }
    return selectDictLabel(statusOptions.value, row.status)
  }

  const cancel = () => {
    open.value = false
    reset()
  }

  const reset = () => {
    form.value = {
      id: undefined,
      parentId: 0,
      menuName: undefined,
      icon: undefined,
      menuType: 'M',
      orderNum: undefined,
      isFrame: 1,
      isCache: 0,
      visible: '0',
      status: '0',
      location: '1',
      langKey: ''
    }
    formRef.value?.resetFields()
  }

  const handleAdd = (row?: any) => {
    parentType.value = ''
    getTreeSelect()
    open.value = true
    nextTick(() => {
      reset()
      if (row && row.id) {
        form.value.parentId = row.id
      } else {
        form.value.parentId = 0
      }
    })
    title.value = t('system.menu.addMenu')
  }

  const handleUpdate = (row: any) => {
    getTreeSelect()
    getMenu(row.id)
      .then((response) => {
        if (!response) {
          MessageUtil.error('获取菜单信息失败')
          return
        }
        open.value = true
        nextTick(() => {
          reset()
          form.value = { ...response }
        })
        title.value = t('system.menu.modifyMenu')
      })
      .catch((error) => {
        console.error('获取菜单详情失败:', error)
        MessageUtil.error('获取菜单信息失败')
      })
  }

  const submitForm = () => {
    formRef.value!.validate((valid: boolean) => {
      if (valid) {
        if (form.value.id !== undefined) {
          updateMenu(form.value).then(() => {
            MessageUtil.success(t('common.saveSuccess'))
            open.value = false
            getList()
          })
        } else {
          addMenu(form.value).then(() => {
            MessageUtil.success(t('common.saveSuccess'))
            open.value = false
            getList()
          })
        }
      }
    })
  }

  const handleDelete = (row: any) => {
    ElMessageBox.confirm(t('system.menu.isDelete'), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
      .then(() => {
        return delMenu(row.id)
      })
      .then(() => {
        getList()
        MessageUtil.success(t('common.saveSuccess'))
      })
      .catch(() => {})
  }
</script>

<style scoped lang="scss">
  .menu-page {
    padding: 16px;
    height: 100%;
    display: flex;
    flex-direction: column;
  }
</style>
