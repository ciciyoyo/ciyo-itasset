<template>
  <div class="job-log-page art-full-height">
    <!-- 搜索表单 -->
    <JobLogSearch v-show="showSearch" v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

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
            <ElButton @click="handleClean" icon="ele-Delete" type="warning" v-hasPermi="['system:job:remove']" v-ripple>
              {{ $t('system.tool.task.clean') }}
            </ElButton>
            <ElButton
              @click="handleDelete()"
              icon="ele-Delete"
              type="danger"
              :disabled="!selectedRows.length"
              v-hasPermi="['system:job:remove']"
            >
              {{ $t('system.tool.task.delete') }}
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        class="flex-1"
        :loading="loading"
        :data="jobLogList"
        :columns="columns"
        :pagination="pagination"
        row-key="id"
        :show-table-header="false"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
        <template #jobGroup="{ row }">
          <el-tag>
            {{ sysJobGroupOptions.find((item: any) => item.dictValue === row.jobGroup)?.dictLabel || row.jobGroup }}
          </el-tag>
        </template>

        <template #status="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'">
            {{ sysJobStatusOptions.find((item: any) => item.dictValue === `${row.status}`)?.dictLabel || row.status }}
          </el-tag>
        </template>

        <template #createTime="{ row }">
          {{ parseTime(row.createTime) }}
        </template>

        <template #operation="{ row }">
          <div class="flex gap-2">
            <el-button link type="primary" @click="handleView(row)" v-hasPermi="['system:job:query']">
              {{ $t('system.tool.task.view') }}
            </el-button>
          </div>
        </template>
      </ArtTable>
    </ElCard>

    <!-- 调度日志详细 -->
    <el-dialog v-model="open" append-to-body :title="$t('system.tool.task.scheduleLogDetails')" width="700px">
      <el-form ref="viewFormRef" :model="form" label-width="120px">
        <el-row>
          <el-col :span="12">
            <el-form-item :label="$t('system.tool.task.logId')">{{ form.id }}</el-form-item>
            <el-form-item :label="$t('system.tool.task.taskName')">{{ form.jobName }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('system.tool.task.taskGroupLabelInfo')">{{ form.jobGroup }}</el-form-item>
            <el-form-item :label="$t('system.tool.task.executionTimeLabel')">{{ form.createTime }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('system.tool.task.invokeMethod')">{{ form.invokeTarget }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('system.tool.task.logMessage')">{{ form.jobMessage }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('system.tool.task.executionStatus')">
              <div v-if="form.status == 0">{{ $t('system.tool.task.normal') }}</div>
              <div v-else-if="form.status == 1">{{ $t('system.tool.task.failed') }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item v-if="form.status == 1" :label="$t('system.tool.task.exceptionInfo')">
              {{ form.exceptionInfo }}
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="open = false">{{ $t('system.tool.task.close') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
  import { onMounted, reactive, ref, watch } from 'vue'
  import { ElMessageBox } from 'element-plus'
  import { useRoute } from 'vue-router'
  import { getJob } from '@/api/tool/job'
  import { cleanJobLog, delJobLog, listJobLog } from '@/api/tool/jobLog'
  import { getDicts } from '@/api/system/dict/data'
  import type { FormInstance } from 'element-plus'
  import { MessageUtil } from '@/utils/messageUtil'
  import { useI18n } from 'vue-i18n'
  import { useTable } from '@/hooks/core/useTable'
  import { parseTime, addDateRange } from '@/utils/business'
  import JobLogSearch from './modules/job-log-search.vue'

  defineOptions({ name: 'JobLog' })

  // Props - 支持作为弹窗组件使用
  const props = defineProps<{
    jobId?: string | number
    jobName?: string
    jobGroup?: string
  }>()

  const { t } = useI18n()
  const route = useRoute()

  // 显示搜索栏
  const showSearch = ref(true)

  // 状态和引用
  const open = ref(false)
  const sysJobGroupOptions = ref<any[]>([])
  const sysJobStatusOptions = ref<any[]>([])

  // 选择相关
  const selectedRows = ref<any[]>([])

  // 搜索参数
  const searchForm = ref({
    jobName: undefined as any,
    jobGroup: undefined as any,
    status: undefined as any,
    dateRange: null as any
  })

  // 表单数据
  const form = reactive<any>({
    id: undefined,
    jobName: undefined,
    jobGroup: undefined,
    invokeTarget: undefined,
    jobMessage: undefined,
    status: undefined,
    exceptionInfo: undefined,
    createTime: undefined
  })

  const viewFormRef = ref<FormInstance | null>(null)

  // useTable hook
  const {
    columns,
    columnChecks,
    data: jobLogList,
    loading,
    pagination,
    searchParams,
    resetSearchParams: resetTableSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: listJobLog,
      apiParams: {
        current: 1,
        size: 10,
        ...searchForm.value
      },
      immediate: false, // 禁用自动加载，手动控制
      columnsFactory: () => [
        {
          type: 'selection',
          minWidth: 55,
          align: 'center' as const
        },
        {
          prop: 'id',
          label: 'ID',
          align: 'center' as const,
          minWidth: 80
        },
        {
          prop: 'jobName',
          label: t('system.tool.task.taskName'),
          align: 'center' as const,
          showOverflowTooltip: true,
          minWidth: 150
        },
        {
          prop: 'jobGroup',
          label: t('system.tool.task.taskGroupName'),
          align: 'center' as const,
          minWidth: 120,
          useSlot: true
        },
        {
          prop: 'invokeTarget',
          label: t('system.tool.task.invokeTargetString'),
          align: 'center' as const,
          showOverflowTooltip: true,
          minWidth: 200
        },
        {
          prop: 'jobMessage',
          label: t('system.tool.task.logMessage'),
          align: 'center' as const,
          showOverflowTooltip: true,
          minWidth: 150
        },
        {
          prop: 'status',
          label: t('system.tool.task.executionStatus'),
          align: 'center' as const,
          minWidth: 100,
          useSlot: true
        },
        {
          prop: 'createTime',
          label: t('system.tool.task.executionTime'),
          align: 'center' as const,
          minWidth: 180,
          useSlot: true
        },
        {
          prop: 'operation',
          label: t('system.tool.task.operation'),
          align: 'center' as const,
          minWidth: 100,
          fixed: 'right' as const,
          useSlot: true
        }
      ]
    }
  })

  // 初始化数据加载
  const initData = async () => {
    // 获取字典数据
    try {
      const groupResponse = await getDicts('sys_job_group')
      sysJobGroupOptions.value = groupResponse || []
    } catch (error) {
      console.error('获取任务组字典失败:', error)
      sysJobGroupOptions.value = []
    }

    try {
      const statusResponse = await getDicts('sys_job_status')
      sysJobStatusOptions.value = statusResponse || []
    } catch (error) {
      console.error('获取任务状态字典失败:', error)
      sysJobStatusOptions.value = []
    }

    // 优先使用 props（弹窗模式），其次使用路由参数（页面模式）
    if (props.jobName || props.jobGroup) {
      // 弹窗模式 - 使用 props
      searchForm.value.jobName = props.jobName
      searchForm.value.jobGroup = props.jobGroup
      Object.assign(searchParams, {
        jobName: props.jobName,
        jobGroup: props.jobGroup
      })
    } else {
      // 页面模式 - 检查路由参数
      const jobId = route.params?.jobId as string
      if (jobId && jobId !== '0') {
        try {
          const response = await getJob(Number(jobId))
          if (response) {
            searchForm.value.jobName = response.jobName
            searchForm.value.jobGroup = response.jobGroup
            Object.assign(searchParams, {
              jobName: response.jobName,
              jobGroup: response.jobGroup
            })
          }
        } catch (error) {
          console.error('获取任务信息失败:', error)
        }
      }
    }

    // 加载数据
    refreshData()
  }

  // 生命周期钩子
  onMounted(() => {
    initData()
  })

  // 监听 props 变化（弹窗重新打开时）
  watch(
    () => [props.jobId, props.jobName, props.jobGroup],
    () => {
      if (props.jobName || props.jobGroup) {
        searchForm.value.jobName = props.jobName
        searchForm.value.jobGroup = props.jobGroup
        Object.assign(searchParams, {
          jobName: props.jobName,
          jobGroup: props.jobGroup
        })
        refreshData()
      }
    }
  )

  // 方法定义

  // 搜索处理
  const handleSearch = (params: Record<string, any>) => {
    // 处理日期范围
    const searchParamsWithDate = addDateRange(
      {
        jobName: params.jobName,
        jobGroup: params.jobGroup,
        status: params.status
      },
      params.dateRange
    )
    Object.assign(searchParams, searchParamsWithDate)
    refreshData()
  }

  // 重置搜索参数
  const resetSearchParams = () => {
    searchForm.value = {
      jobName: undefined,
      jobGroup: undefined,
      status: undefined,
      dateRange: null
    }
    resetTableSearchParams()
  }

  // 多选框选中数据
  const handleSelectionChange = (selection: any[]) => {
    selectedRows.value = selection
  }

  /** 详细按钮操作 */
  const handleView = (row: any) => {
    Object.assign(form, row)
    open.value = true
  }

  /** 删除按钮操作 */
  const handleDelete = () => {
    const jobLogIds = selectedRows.value.map((item) => item.id).join(',')
    ElMessageBox.confirm(
      t('system.tool.task.confirmDelteLogNumer') + '"' + jobLogIds + '"' + t('system.tool.task.dataItem'),
      t('common.warning'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
      .then(() => {
        return delJobLog(jobLogIds as any)
      })
      .then(() => {
        refreshData()
        MessageUtil.success(t('common.success'))
      })
      .catch(() => {})
  }

  /** 清空按钮操作 */
  const handleClean = () => {
    ElMessageBox.confirm(t('system.tool.task.confirmDeleteAllLog'), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
      .then(() => {
        return cleanJobLog()
      })
      .then(() => {
        refreshData()
        MessageUtil.success(t('common.success'))
      })
      .catch(() => {})
  }
</script>

<style scoped lang="scss">
  .job-log-page {
    padding: 16px;
    height: 100%;
    display: flex;
    flex-direction: column;
  }
</style>
