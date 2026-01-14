<template>
  <div class="gen-page art-full-height">
    <!-- 搜索表单 -->
    <GenSearch v-show="showSearch" v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

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
            <ElButton @click="handleGenTable()" icon="ele-Download" type="primary" plain v-hasPermi="['tool:gen:code']" v-ripple>
              {{ $t('system.tool.gen.generate') }}
            </ElButton>
            <ElButton @click="openImportTable" icon="ele-Upload" type="info" plain v-hasPermi="['tool:gen:import']">
              {{ $t('system.tool.gen.import') }}
            </ElButton>
            <ElButton
              @click="handleEditTable()"
              icon="ele-Edit"
              type="success"
              plain
              :disabled="single"
              v-hasPermi="['tool:gen:edit']"
            >
              {{ $t('system.tool.gen.modify') }}
            </ElButton>
            <ElButton
              @click="handleDelete()"
              icon="ele-Delete"
              type="danger"
              plain
              :disabled="multiple"
              v-hasPermi="['tool:gen:remove']"
            >
              {{ $t('system.tool.gen.delete') }}
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        class="flex-1"
        :loading="loading"
        :data="tableList"
        :columns="columns"
        :pagination="pagination"
        row-key="id"
        :show-table-header="false"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
        <template #index="{ $index }">
          {{ (pagination.current - 1) * pagination.size + $index + 1 }}
        </template>

        <template #operation="{ row }">
          <div class="flex gap-2">
            <el-button link type="primary" @click="handlePreview(row)" v-hasPermi="['tool:gen:preview']">
              {{ $t('system.tool.gen.preview') }}
            </el-button>
            <el-button link type="primary" @click="handleEditTable(row)" v-hasPermi="['tool:gen:edit']">
              {{ $t('system.tool.gen.edit') }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['tool:gen:remove']">
              {{ $t('system.tool.gen.delete') }}
            </el-button>
            <el-button link type="primary" @click="handleSynchDb(row)" v-hasPermi="['tool:gen:edit']">
              {{ $t('system.tool.gen.sync') }}
            </el-button>
            <el-button link type="primary" @click="handleGenTable(row)" v-hasPermi="['tool:gen:code']">
              {{ $t('system.tool.gen.generateCode') }}
            </el-button>
          </div>
        </template>
      </ArtTable>
    </ElCard>

    <!-- 预览界面 -->
    <el-dialog
      v-model="preview.open"
      :title="$t('system.tool.gen.codePreview')"
      append-to-body
      class="scrollbar"
      top="5vh"
      width="80%"
    >
      <el-tabs v-model="preview.activeName">
        <el-tab-pane
          v-for="(value, key) in preview.data"
          :key="value"
          :label="String(key).substring(String(key).lastIndexOf('/') + 1, String(key).indexOf('.vm'))"
          :name="String(key).substring(String(key).lastIndexOf('/') + 1, String(key).indexOf('.vm'))"
        >
          <el-link v-copyText="value" underline="never" icon="ele-DocumentCopy" style="float: right">
            &nbsp;{{ $t('system.tool.gen.copy') }}
          </el-link>
          <el-scrollbar max-height="80vh">
            <pre>{{ value }}</pre>
          </el-scrollbar>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <import-table ref="importRef" @ok="handleQuery" />

    <!-- 编辑表弹窗 -->
    <el-dialog
      v-model="editTableVisible"
      fullscreen
      :title="$t('system.tool.gen.editTable')"
      append-to-body
      destroy-on-close
      width="90%"
    >
      <edit-table ref="editTableRef" :table-id="editTableId" @close="handleEditTableClose" @success="handleEditTableSuccess" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
  import { onActivated, reactive, ref } from 'vue'
  import { useRoute } from 'vue-router'
  import { ElMessageBox } from 'element-plus'
  import { delTable, genCode, listTable, previewTable, synchDb } from '@/api/tool/gen'
  import type { GenTableItem } from '@/api/tool/gen'
  import importTable from './importTable.vue'
  import editTable from './editTable.vue'
  import GenSearch from './modules/gen-search.vue'
  import { downLoadZip } from './zipdownload'
  import { baseUrl, joinUrl } from '@/utils/auth'
  import { addDateRange } from '@/utils/business'
  import { MessageUtil } from '@/utils/messageUtil'
  import { useTable } from '@/hooks/core/useTable'
  import { useI18n } from 'vue-i18n'

  defineOptions({ name: 'GenCode' })

  const { t } = useI18n()
  const route = useRoute()

  // 显示搜索栏
  const showSearch = ref(true)

  // 引用
  const importRef = ref()
  const editTableRef = ref()
  const editTableVisible = ref(false)
  const editTableId = ref<number | null>(null)

  // 选择相关
  const ids = ref<number[]>([])
  const tableNames = ref<string[]>([])
  const single = ref(true)
  const multiple = ref(true)
  const uniqueId = ref('')

  // 搜索参数
  const searchForm = ref({
    tableName: undefined as any,
    tableComment: undefined as any,
    dateRange: null as any
  })

  // 预览数据
  const preview = reactive({
    open: false,
    title: t('system.tool.gen.codePreview'),
    data: {} as Record<string, any>,
    activeName: 'entity.java'
  })

  // useTable hook
  const {
    columns,
    columnChecks,
    data: tableList,
    loading,
    pagination,
    searchParams,
    resetSearchParams: resetTableSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: listTable,
      apiParams: {
        current: 1,
        size: 10,
        ...searchForm.value
      },
      immediate: true,
      columnsFactory: () => [
        {
          type: 'selection',
          minWidth: 55,
          align: 'center' as const
        },
        {
          prop: 'index',
          label: t('system.tool.gen.serialNo'),
          align: 'center' as const,
          minWidth: 80,
          useSlot: true
        },
        {
          prop: 'tableName',
          label: t('system.tool.gen.tableName'),
          align: 'center' as const,
          showOverflowTooltip: true,
          minWidth: 150
        },
        {
          prop: 'tableComment',
          label: t('system.tool.gen.tableComment'),
          align: 'center' as const,
          showOverflowTooltip: true,
          minWidth: 150
        },
        {
          prop: 'className',
          label: t('system.tool.gen.className'),
          align: 'center' as const,
          showOverflowTooltip: true,
          minWidth: 150
        },
        {
          prop: 'createTime',
          label: t('system.tool.gen.createTime'),
          align: 'center' as const,
          minWidth: 160
        },
        {
          prop: 'updateTime',
          label: t('system.tool.gen.updateTime'),
          align: 'center' as const,
          minWidth: 160
        },
        {
          prop: 'operation',
          label: t('system.tool.gen.operation'),
          align: 'center' as const,
          minWidth: 330,
          fixed: 'right' as const,
          useSlot: true
        }
      ]
    }
  })

  // 路由激活时检查
  onActivated(() => {
    const time = route.query.t as string
    if (time != null && time !== uniqueId.value) {
      uniqueId.value = time
      searchForm.value = {
        tableName: undefined,
        tableComment: undefined,
        dateRange: null
      }
      refreshData()
    }
  })

  // 搜索处理
  const handleSearch = (params: Record<string, any>) => {
    // 处理日期范围
    const searchParamsWithDate = addDateRange(
      {
        tableName: params.tableName,
        tableComment: params.tableComment
      },
      params.dateRange
    )
    Object.assign(searchParams, searchParamsWithDate)
    refreshData()
  }

  // 重置搜索参数
  const resetSearchParams = () => {
    searchForm.value = {
      tableName: undefined,
      tableComment: undefined,
      dateRange: null
    }
    resetTableSearchParams()
  }

  // 查询按钮
  const handleQuery = () => {
    refreshData()
  }

  /** 生成代码操作 */
  const handleGenTable = (row?: GenTableItem) => {
    const tbNames = row?.tableName || tableNames.value.join(',')
    if (tbNames === '') {
      MessageUtil.error(t('system.tool.gen.selectData'))
      return
    }
    if (row?.genType === '1') {
      genCode(row.tableName).then(() => {
        MessageUtil.success(t('system.tool.gen.generateSuccess') + row.genPath)
      })
    } else {
      downLoadZip(joinUrl(baseUrl, `/tool/gen/batchGenCode?tables=${tbNames}`), 'ciyo')
    }
  }

  /** 同步数据库操作 */
  const handleSynchDb = (row: GenTableItem) => {
    const tableName = row.tableName
    ElMessageBox.confirm(t('system.tool.gen.confirmSync') + `"${tableName}"` + t('system.tool.gen.tableStructure'))
      .then(() => synchDb(tableName))
      .then(() => {
        MessageUtil.success(t('system.tool.gen.syncSuccess'))
      })
      .catch(() => {})
  }

  /** 打开导入表弹窗 */
  const openImportTable = () => {
    importRef.value?.show()
  }

  /** 预览按钮 */
  const handlePreview = (row: GenTableItem) => {
    previewTable(row.id).then((response: any) => {
      preview.data = response
      preview.open = true
      preview.activeName = 'entity.java'
    })
  }

  // 多选框选中数据
  const handleSelectionChange = (selection: GenTableItem[]) => {
    ids.value = selection.map((item) => item.id)
    tableNames.value = selection.map((item) => item.tableName)
    single.value = selection.length !== 1
    multiple.value = !selection.length
  }

  /** 修改按钮操作 */
  const handleEditTable = (row?: GenTableItem) => {
    editTableId.value = row?.id || ids.value[0]
    editTableVisible.value = true
  }

  /** 编辑表弹窗关闭 */
  const handleEditTableClose = () => {
    editTableVisible.value = false
  }

  /** 编辑表保存成功 */
  const handleEditTableSuccess = () => {
    editTableVisible.value = false
    refreshData()
  }

  /** 删除按钮操作 */
  const handleDelete = (row?: GenTableItem) => {
    const selectIds = row?.id ? [row.id] : ids.value
    ElMessageBox.confirm(
      t('system.tool.gen.confirmDeleteTable') + '"' + selectIds.join(',') + '"' + t('system.tool.gen.dataItem')
    )
      .then(() => {
        return delTable(selectIds)
      })
      .then(() => {
        refreshData()
        MessageUtil.success(t('system.tool.gen.deleteSuccess'))
      })
      .catch(() => {})
  }
</script>

<style scoped lang="scss">
  .gen-page {
    padding: 16px;
    height: 100%;
    display: flex;
    flex-direction: column;
  }
</style>
