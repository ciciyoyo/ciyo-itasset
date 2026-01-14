<template>
  <div class="log-page art-full-height">
    <div class="flex flex-col h-full">
      <ArtSearchBar v-if="showSearch" ref="searchBarRef" v-model="searchForm" :items="searchFormItems"
        @reset="resetSearchParams" @search="handleSearch">
      </ArtSearchBar>
      <ElCard class="art-table-card flex flex-col flex-1 min-h-0" shadow="never"
        :style="{ 'margin-top': showSearch ? '12px' : '0' }">
        <ArtTableHeader v-model:columns="columnChecks" v-model:showSearchBar="showSearch" :loading="loading"
          @refresh="refreshData">
          <template #left>
            <ElSpace wrap>
              <el-button type="primary" icon="ele-Plus" @click="handleAdd" v-ripple
                v-hasPermi="['itam:manufacturers:add']">
                新增
              </el-button>
              <el-button type="danger" v-ripple icon="ele-Delete" @click="handleDelete"
                v-hasPermi="['itam:manufacturers:delete']">
                删除
              </el-button>

              <el-button v-hasPermi="['itam:manufacturers:export']" icon="ele-Download" v-ripple @click="handleExport">
                导出
              </el-button>
            </ElSpace>
          </template>
        </ArtTableHeader>

        <!-- Table -->
        <ArtTable class="flex-1" :loading="loading" :data="data" :columns="columns" :pagination="pagination"
          @selection-change="handleSelectionChange" @pagination:size-change="handleSizeChange"
          @pagination:current-change="handleCurrentChange">
          <template #operation="{ row }">
            <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['itam:manufacturers:update']">
              编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['itam:manufacturers:delete']"> 删除
            </el-button>
          </template>
          <template #officialUrl="{ row }">
            <el-link v-if="row.officialUrl" type="primary" :href="row.officialUrl" target="_blank" :underline="false">
              {{ row.officialUrl }}
            </el-link>
            <span v-else>-</span>
          </template>
          <template #supportUrl="{ row }">
            <el-link v-if="row.supportUrl" type="primary" :href="row.supportUrl" target="_blank" :underline="false">
              {{ row.supportUrl }}
            </el-link>
            <span v-else>-</span>
          </template>
          <template #warrantyUrl="{ row }">
            <el-link v-if="row.warrantyUrl" type="primary" :href="row.warrantyUrl" target="_blank" :underline="false">
              {{ row.warrantyUrl }}
            </el-link>
            <span v-else>-</span>
          </template>
          <template #logoUrl="{ row }">
            <el-image v-if="row.logoUrl" :src="row.logoUrl" :preview-src-list="[row.logoUrl]" fit="contain"
              style="width: 40px; height: 40px" preview-teleported />
            <span v-else>-</span>
          </template>
          <template #supportEmail="{ row }">
            <el-link v-if="row.supportEmail" type="primary" :href="'mailto:' + row.supportEmail" :underline="false">
              {{ row.supportEmail }}
            </el-link>
            <span v-else>-</span>
          </template>
        </ArtTable>
      </ElCard>
    </div>
    <!-- 添加或修改制造商对话框 -->
    <el-dialog :title="dialogTitle" v-model="open" width="500px" append-to-body>
      <el-form ref="manufacturersRef" :model="form" :rules="formRules" label-width="120px">
        <el-form-item label="厂商名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入厂商名称" />
        </el-form-item>
        <el-form-item label="官网 URL" prop="officialUrl">
          <el-input v-model="form.officialUrl" placeholder="请输入官网 URL" />
        </el-form-item>
        <el-form-item label="网站客服支持" prop="supportUrl">
          <el-input v-model="form.supportUrl" placeholder="请输入网站客服支持" />
        </el-form-item>
        <el-form-item label="保修查询 URL" prop="warrantyUrl">
          <el-input v-model="form.warrantyUrl" placeholder="请输入保修查询 URL" />
        </el-form-item>
        <el-form-item label="电话客服支持" prop="supportPhone">
          <el-input v-model="form.supportPhone" placeholder="请输入电话客服支持" />
        </el-form-item>
        <el-form-item label="邮件客服支持" prop="supportEmail">
          <el-input v-model="form.supportEmail" placeholder="请输入邮件客服支持" />
        </el-form-item>
        <el-form-item label="厂商 Logo" prop="logoUrl">
          <ImageUpload v-model:value="form.logoUrl" />
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
  addManufacturers,
  delManufacturers,
  exportManufacturers,
  getManufacturers,
  pageManufacturers,
  ManufacturersEntity,
  updateManufacturers
} from '@/api/itam/manufacturers'
import { ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/core/useTable'
import { MessageUtil } from '@/utils/messageUtil'
import { download, resetFormRef } from '@/utils/business'
import ImageUpload from '@/components/business/image-upload/index.vue'

defineOptions({
  name: 'manufacturers'
})

const showSearch = ref(true)

const selectedRows = ref<ManufacturersEntity[]>([])

const searchForm = ref({
  name: undefined
})

// 表单配置
const searchFormItems = computed(() => [
  {
    label: '厂商名称',
    key: 'name',
    type: 'input',
    placeholder: '请输入厂商名称',
    clearable: true
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
    apiFn: pageManufacturers,
    apiParams: {
      current: 1,
      size: 10,
      ...searchForm.value
    },
    columnsFactory: () => [
      { type: 'selection' },
      { prop: 'id', label: 'Id', minWidth: 120 },
      { prop: 'name', label: '厂商名称', minWidth: 120 },
      { prop: 'logoUrl', label: '厂商 Logo', minWidth: 120, useSlot: true },
      { prop: 'officialUrl', label: '官网 URL', minWidth: 120, useSlot: true },
      { prop: 'supportUrl', label: '网站客服支持', minWidth: 120, useSlot: true },
      { prop: 'warrantyUrl', label: '保修查询 URL', minWidth: 120, useSlot: true },
      { prop: 'supportPhone', label: '电话客服支持', minWidth: 120 },
      { prop: 'supportEmail', label: '邮件客服支持', minWidth: 120, useSlot: true },
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

const form = ref<ManufacturersEntity>({
  id: 0,
  name: '',
  officialUrl: '',
  supportUrl: '',
  warrantyUrl: '',
  supportPhone: '',
  supportEmail: '',
  logoUrl: '',
  remark: '',
  createTime: '',
  updateTime: ''
})

const formRules = ref({
  name: [
    {
      required: true,
      message: '厂商名称不能为空',
      trigger: 'blur'
    }
  ]
})

const manufacturersRef = useTemplateRef('manufacturersRef')
const dialogTitle = ref('')
const saveLoading = ref(false)

// 表单重置
const reset = () => {
  form.value = {
    id: undefined,
    name: '',
    officialUrl: '',
    supportUrl: '',
    warrantyUrl: '',
    supportPhone: '',
    supportEmail: '',
    logoUrl: '',
    remark: '',
    createTime: '',
    updateTime: ''
  }
  resetFormRef(manufacturersRef)
}

/** 新增按钮操作 */
const handleAdd = () => {
  reset()
  open.value = true
  dialogTitle.value = '添加制造商'
}

/** 修改按钮操作 */
const handleUpdate = (row: ManufacturersEntity) => {
  reset()
  if (row.id) {
    getManufacturers(row.id).then((data: ManufacturersEntity) => {
      form.value = data
      open.value = true
      dialogTitle.value = '修改制造商'
    })
  }
}

/** 提交按钮 */
const submitForm = () => {
  manufacturersRef.value!.validate((valid: boolean) => {
    if (valid) {
      saveLoading.value = true
      if (form.value.id != null) {
        updateManufacturers(form.value)
          .then(() => {
            MessageUtil.success('保存成功')
            open.value = false
            refreshData()
          })
          .finally(() => {
            saveLoading.value = false
          })
      } else {
        addManufacturers(form.value)
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
    .then(() => delManufacturers(ids))
    .then(() => {
      refreshData()
      MessageUtil.success('删除成功')
    })
    .catch(() => { })
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
        const response = await exportManufacturers(searchParams)
        download(response, '制造商数据')
      } catch (error) {
        console.error('导出失败:', error)
      } finally {
        exportLoading.value = false
      }
    })
    .catch(() => { })
}
</script>

<style scoped lang="scss">
.log-page {
  padding: 16px;
}
</style>
