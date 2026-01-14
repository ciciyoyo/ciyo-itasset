<template>
  <!-- 导入表 -->
  <el-dialog :title="$t('system.tool.gen.importTable')" v-model="visible" width="800px" top="5vh" append-to-body>
    <el-form :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item :label="$t('system.tool.gen.tableName')" prop="tableName">
        <el-input
          v-model="queryParams.tableName"
          :placeholder="$t('system.tool.gen.enterTableName')"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="$t('system.tool.gen.tableComment')" prop="tableComment">
        <el-input
          v-model="queryParams.tableComment"
          :placeholder="$t('system.tool.gen.enterTableComment')"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="ele-Search" @click="handleQuery"> {{ $t('system.tool.gen.search') }} </el-button>
        <el-button icon="ele-Refresh" @click="resetQuery"> {{ $t('system.tool.gen.reset') }}</el-button>
      </el-form-item>
    </el-form>
    <el-table @row-click="clickRow" ref="tableRef" :data="dbTableList" @selection-change="handleSelectionChange" height="260px">
      <el-table-column type="selection" width="55"></el-table-column>
      <el-table-column prop="tableName" :label="$t('system.tool.gen.tableName')" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column
        prop="tableComment"
        :label="$t('system.tool.gen.tableComment')"
        :show-overflow-tooltip="true"
      ></el-table-column>
      <el-table-column prop="createTime" :label="$t('system.tool.gen.createTime')"></el-table-column>
      <el-table-column prop="updateTime" :label="$t('system.tool.gen.updateTime')"></el-table-column>
    </el-table>
    <el-pagination
      v-show="total > 0"
      :total="total"
      :current-page="queryParams.current"
      :page-size="queryParams.size"
      class="mt-10px ml-4px"
      :page-sizes="[10, 20, 30, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">{{ $t('system.tool.gen.cancel') }}</el-button>
        <el-button type="primary" @click="handleImportTable"> {{ $t('system.tool.gen.confirm') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
  import { importTable, listDbTable } from '@/api/tool/gen'
  import { reactive, ref } from 'vue'
  import { ElMessage } from 'element-plus'
  import { useI18n } from 'vue-i18n'

  const { t } = useI18n()

  const total = ref(0)
  const visible = ref(false)
  const tables = ref<string[]>([])
  const dbTableList = ref<any[]>([])
  const tableRef = ref<any>(null)
  const queryFormRef = ref<any>(null)

  const queryParams = reactive({
    current: 1,
    size: 10,
    tableName: undefined as any,
    tableComment: undefined as any
  })

  const emit = defineEmits(['ok'])

  /** 查询参数列表 */
  const show = () => {
    getList()
    visible.value = true
  }

  /** 单击选择行 */
  const clickRow = (row: any) => {
    tableRef.value.toggleRowSelection(row)
  }

  /** 多选框选中数据 */
  const handleSelectionChange = (selection: any[]) => {
    tables.value = selection.map((item) => item.tableName)
  }

  /** 查询表数据 */
  const getList = () => {
    listDbTable(queryParams).then((res: any) => {
      dbTableList.value = res.records
      total.value = res.total
    })
  }

  /** 搜索按钮操作 */
  const handleQuery = () => {
    queryParams.current = 1
    getList()
  }

  /** 重置按钮操作 */
  const resetQuery = () => {
    queryFormRef.value.resetFields()
    handleQuery()
  }

  /** 分页大小改变 */
  const handleSizeChange = (val: number) => {
    queryParams.size = val
    getList()
  }

  /** 当前页改变 */
  const handleCurrentChange = (val: number) => {
    queryParams.current = val
    getList()
  }

  /** 导入按钮操作 */
  const handleImportTable = () => {
    const tableNames = tables.value.join(',')
    if (tableNames == '') {
      ElMessage.error(t('system.tool.gen.selectTableToImport'))
      return
    }
    importTable({ tables: tableNames }).then(() => {
      ElMessage.success(t('system.tool.gen.importSuccess'))
      visible.value = false
      emit('ok')
    })
  }

  defineExpose({
    show
  })
</script>
