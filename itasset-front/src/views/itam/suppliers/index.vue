<template>
  <div class="log-page art-full-height">
    <div class="flex flex-col h-full">
      <ArtSearchBar
        v-if="showSearch"
        ref="searchBarRef"
        v-model="searchForm"
        :items="searchFormItems"
        @reset="resetSearchParams"
        @search="handleSearch"
      >
      </ArtSearchBar>
      <ElCard
        class="art-table-card flex flex-col flex-1 min-h-0"
        shadow="never"
        :style="{ 'margin-top': showSearch ? '12px' : '0' }"
      >
        <ArtTableHeader
          v-model:columns="columnChecks"
          v-model:showSearchBar="showSearch"
          :loading="loading"
          @refresh="refreshData"
        >
          <template #left>
            <ElSpace wrap>
              <el-button type="primary" icon="ele-Plus" @click="handleAdd" v-ripple v-hasPermi="['itam:suppliers:add']">
                新增
              </el-button>
              <el-button
                type="danger"
                v-ripple
                icon="ele-Delete"
                :disabled="!selectedRows.length"
                @click="handleDelete"
                v-hasPermi="['itam:suppliers:delete']"
              >
                删除
              </el-button>

              <ExcelImport
                url="/itam/suppliers/importData"
                title="供应商数据导入"
                templateCode="supplier"
                v-hasPermi="['itam:suppliers:import']"
                @success="refreshData"
              >
                <template #trigger>
                  <el-button type="success" icon="ele-Upload" v-ripple> 导入 </el-button>
                </template>
              </ExcelImport>

              <el-button v-hasPermi="['itam:suppliers:export']" icon="ele-Download" v-ripple @click="handleExport">
                导出
              </el-button>
            </ElSpace>
          </template>
        </ArtTableHeader>

        <!-- Table -->
        <ArtTable
          class="flex-1"
          :loading="loading"
          :data="data"
          :columns="columns"
          :pagination="pagination"
          @selection-change="handleSelectionChange"
          @pagination:size-change="handleSizeChange"
          @pagination:current-change="handleCurrentChange"
        >
          <template #operation="{ row }">
            <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['itam:suppliers:update']"> 编辑 </el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['itam:suppliers:delete']"> 删除 </el-button>
          </template>
        </ArtTable>
      </ElCard>
    </div>
    <!-- 添加或修改供应商对话框 -->
    <el-dialog :title="dialogTitle" v-model="open" width="500px" append-to-body>
      <el-form ref="suppliersRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="供应商名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">取消</el-button>
          <el-button :loading="saveLoading" type="primary" @click="submitForm">提交</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref, useTemplateRef } from 'vue'
  import {
    addSuppliers,
    delSuppliers,
    exportSuppliers,
    getSuppliers,
    pageSuppliers,
    SuppliersEntity,
    updateSuppliers
  } from '@/api/itam/suppliers'
  import { ElMessageBox } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import { MessageUtil } from '@/utils/messageUtil'
  import { download, resetFormRef } from '@/utils/business'
  import ExcelImport from '@/components/business/excel-import/index.vue'

  defineOptions({
    name: 'suppliers'
  })

  const showSearch = ref(true)

  const selectedRows = ref<SuppliersEntity[]>([])

  const searchForm = ref({
    name: undefined
  })

  // 表单配置
  const searchFormItems = computed(() => [
    {
      label: '供应商名称',
      key: 'name',
      type: 'input',
      placeholder: '请输入供应商名称',
      clearable: true,
      labelWidth: '90px'
    }
  ])

  const {
    columns,
    columnChecks,
    data,
    loading,
    getData,
    pagination,
    searchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: pageSuppliers,
      apiParams: {
        current: 1,
        size: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        { prop: 'id', label: 'Id', minWidth: 120 },
        { prop: 'name', label: '供应商名称', minWidth: 120 },
        { prop: 'contactName', label: '联系人', minWidth: 120 },
        { prop: 'phone', label: '联系电话', minWidth: 120 },
        { prop: 'remark', label: '备注', minWidth: 120 },
        {
          prop: 'operation',
          label: '操作',
          useSlot: true,
          minWidth: 100,
          fixed: 'right'
        }
      ]
    }
  })

  const searchBarRef = ref()

  const handleSearch = () => {
    searchBarRef.value.validate().then(() => {
      Object.assign(searchParams, searchForm.value)
      getData()
    })
  }

  const handleSelectionChange = (selection: any[]): void => {
    selectedRows.value = selection
  }

  const open = ref(false)

  const form = ref<SuppliersEntity>({
    id: 0,
    name: '',
    contactName: '',
    phone: '',
    remark: '',
    createTime: '',
    updateTime: ''
  })

  const formRules = ref({
    name: [
      {
        required: true,
        message: '供应商名称不能为空',
        trigger: 'blur'
      }
    ]
  })

  const suppliersRef = useTemplateRef('suppliersRef')
  const dialogTitle = ref('')
  const saveLoading = ref(false)

  // 表单重置
  const reset = () => {
    form.value = {
      id: undefined,
      name: '',
      contactName: '',
      phone: '',
      remark: '',
      createTime: '',
      updateTime: ''
    }
    resetFormRef(suppliersRef)
  }

  /** 新增按钮操作 */
  const handleAdd = () => {
    reset()
    open.value = true
    dialogTitle.value = '添加供应商'
  }

  /** 修改按钮操作 */
  const handleUpdate = (row: SuppliersEntity) => {
    reset()
    if (row.id) {
      getSuppliers(row.id).then((data: SuppliersEntity) => {
        form.value = data
        open.value = true
        dialogTitle.value = '修改供应商'
      })
    }
  }

  /** 提交按钮 */
  const submitForm = () => {
    suppliersRef.value!.validate((valid: boolean) => {
      if (valid) {
        saveLoading.value = true
        if (form.value.id != null) {
          updateSuppliers(form.value)
            .then(() => {
              MessageUtil.success('保存成功')
              open.value = false
              refreshData()
            })
            .finally(() => {
              saveLoading.value = false
            })
        } else {
          addSuppliers(form.value)
            .then(() => {
              MessageUtil.success('添加成功')
              open.value = false
              refreshData()
            })
            .finally(() => {
              saveLoading.value = false
            })
        }
      }
    })
  }

  const handleDelete = (row?: any) => {
    const ids = row?.id || selectedRows.value.map((item) => item.id).join(',')
    ElMessageBox.confirm('是否确认删除编号为' + ids + '的数据项?', '警告')
      .then(() => delSuppliers(ids))
      .then(() => {
        refreshData()
        MessageUtil.success('删除成功')
      })
      .catch(() => {})
  }

  // 取消按钮
  const cancel = () => {
    open.value = false
    reset()
  }

  const exportLoading = ref(false)
  const handleExport = () => {
    ElMessageBox.confirm('是否确认导出所有数据项?', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(async () => {
        try {
          exportLoading.value = true
          const response = await exportSuppliers(searchParams)
          download(response, '供应商数据')
        } catch (error) {
          console.error('导出失败:', error)
        } finally {
          exportLoading.value = false
        }
      })
      .catch(() => {})
  }
</script>

<style scoped lang="scss">
  .log-page {
    padding: 16px;
  }
</style>
