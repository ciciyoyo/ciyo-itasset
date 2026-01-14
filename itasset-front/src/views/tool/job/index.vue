<template>
  <div class="job-page art-full-height">
    <!-- 搜索表单 -->
    <JobSearch v-show="showSearch" v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

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
            <ElButton @click="handleAdd" icon="ele-Plus" type="primary" v-hasPermi="['system:job:add']" v-ripple>
              {{ $t('system.tool.task.add') }}
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
            <ElButton @click="handleJobLog()" icon="ele-Operation" type="info" v-hasPermi="['system:job:query']">
              {{ $t('system.tool.task.log') }}
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        class="flex-1"
        :loading="loading"
        :data="jobList"
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
          <el-switch
            v-model="row.status"
            :active-value="0"
            :inactive-value="1"
            @change="() => handleStatusChange(row)"
          ></el-switch>
        </template>

        <template #operation="{ row }">
          <div class="flex gap-2">
            <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['system:job:edit']">
              {{ $t('system.tool.task.modify') }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['system:job:remove']">
              {{ $t('system.tool.task.delete') }}
            </el-button>
            <el-button link type="primary" @click="handleRun(row)" v-hasPermi="['system:job:changeStatus']">
              {{ $t('system.tool.task.runOnce') }}
            </el-button>
            <el-button link type="primary" @click="handleView(row)" v-hasPermi="['system:job:query']">
              {{ $t('system.tool.task.taskDetails') }}
            </el-button>
            <el-button link type="primary" @click="handleJobLog(row)" v-hasPermi="['system:job:query']">
              {{ $t('system.tool.task.scheduleLog') }}
            </el-button>
          </div>
        </template>
      </ArtTable>
    </ElCard>

    <!-- 添加或修改定时任务对话框 -->
    <el-dialog :title="title" v-model="open" width="40%" append-to-body :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
        <el-form-item :label="$t('system.tool.task.taskName')" prop="jobName">
          <el-input v-model="form.jobName" :placeholder="$t('system.tool.task.enterTaskName')" />
        </el-form-item>

        <el-form-item :label="$t('system.tool.task.taskGroupName')" prop="jobGroup">
          <el-select v-model="form.jobGroup" :placeholder="$t('system.tool.task.selectTaskGroupName')">
            <el-option
              v-for="dict in sysJobGroupOptions"
              :key="dict.dictValue"
              :label="dict.dictLabel"
              :value="dict.dictValue"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="invokeTarget">
          <template #label>
            <span>
              {{ $t('system.tool.task.invokeMethod') }}
              <el-tooltip placement="top">
                <template #content>
                  <div>
                    {{ $t('system.tool.task.beanInvokeExample') }}
                    <br />
                    {{ $t('system.tool.task.classInvokeExample') }}
                    <br />
                    {{ $t('system.tool.task.parameterExplanation') }}
                  </div>
                </template>
                <el-icon><ele-QuestionFilled /></el-icon>
              </el-tooltip>
            </span>
          </template>
          <el-input v-model="form.invokeTarget" :placeholder="$t('system.tool.task.enterInvokeTargetString')" />
        </el-form-item>
        <el-form-item :label="$t('system.tool.task.cronExpression')" prop="cronExpression">
          <el-input v-model="form.cronExpression" :placeholder="$t('system.tool.task.enterCronExpression')"> </el-input>
        </el-form-item>
        <!-- <el-form-item :label="$t('system.tool.task.executionStrategy')" prop="misfirePolicy">
          <el-radio-group v-model="form.misfirePolicy">
            <el-radio-button value="1">{{ $t('system.tool.task.executeImmediately') }}</el-radio-button>
            <el-radio-button value="2">{{ $t('system.tool.task.runOnceOption') }}</el-radio-button>
            <el-radio-button value="3">{{ $t('system.tool.task.giveUpExecution') }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('system.tool.task.concurrent')" prop="concurrent">
          <el-radio-group v-model="form.concurrent">
            <el-radio-button value="0">{{ $t('system.tool.task.allow') }}</el-radio-button>
            <el-radio-button value="1">{{ $t('system.tool.task.forbid') }}</el-radio-button>
          </el-radio-group>
        </el-form-item> -->
        <el-form-item :label="$t('system.tool.task.status')">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in sysJobStatusOptions" :key="dict.dictValue" :value="dict.dictValue">
              {{ dict.dictLabel }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
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

    <!-- 任务详细信息对话框 -->
    <el-dialog v-model="openView" append-to-body :title="$t('system.tool.task.taskDetails')" width="40%">
      <el-form ref="viewFormRef" :model="form" label-width="160px">
        <el-row>
          <el-col :span="12">
            <el-form-item :label="$t('system.tool.task.taskNumberLabel')">{{ form.id }}</el-form-item>
            <el-form-item :label="$t('system.tool.task.taskNameLabelInfo')">{{ form.jobName }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('system.tool.task.taskGroupLabel')">{{ jobGroupFormat(form) }}</el-form-item>
            <el-form-item :label="$t('system.tool.task.createTimeLabelInfo')">{{ form.createTime }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('system.tool.task.cronExpressionInfo')">{{ form.cronExpression }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('system.tool.task.nextExecutionTime')">{{ parseTime(form.nextValidTime) }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('system.tool.task.invokeTargetMethodLabelInfo')">{{ form.invokeTarget }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('system.tool.task.taskStateLabelInfo')">
              <div v-if="form.status == 0">{{ $t('system.tool.task.normal') }}</div>
              <div v-else-if="form.status == 1">{{ $t('system.tool.task.failed') }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('system.tool.task.concurrentLabelInfo')">
              <div v-if="form.concurrent == 0">{{ $t('system.tool.task.allow') }}</div>
              <div v-else-if="form.concurrent == 1">{{ $t('system.tool.task.forbid') }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('system.tool.task.executionStrategyLabelInfo')">
              <div v-if="form.misfirePolicy == 0">{{ $t('system.tool.task.defaultStrategy') }}</div>
              <div v-else-if="form.misfirePolicy == 1">{{ $t('system.tool.task.executeImmediately') }}</div>
              <div v-else-if="form.misfirePolicy == 2">{{ $t('system.tool.task.runOnceOption') }}</div>
              <div v-else-if="form.misfirePolicy == 3">{{ $t('system.tool.task.giveUpExecution') }}</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="openView = false">{{ $t('system.tool.task.close') }}</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 任务日志弹窗 -->
    <JobLogDialog
      v-model="logDialogVisible"
      :job-id="selectedJobId"
      :job-name="selectedJobName"
      :job-group="selectedJobGroup"
      @close="logDialogVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
  import { nextTick, onMounted, reactive, ref } from 'vue'
  import type { FormInstance } from 'element-plus'
  import { ElMessageBox } from 'element-plus'
  import { addJob, changeJobStatus, delJob, getJob, listJob, runJob, updateJob } from '@/api/tool/job'
  import { getDicts } from '@/api/system/dict/data'
  import { MessageUtil } from '@/utils/messageUtil'
  import { useI18n } from 'vue-i18n'
  import { useTable } from '@/hooks/core/useTable'
  import { parseTime } from '@/utils/business'
  import JobSearch from './modules/job-search.vue'
  import JobLogDialog from './modules/job-log-dialog.vue'

  defineOptions({ name: 'JobManagement' })

  const { t } = useI18n()

  // 显示搜索栏
  const showSearch = ref(true)

  // 状态和引用
  const title = ref('')
  const open = ref(false)
  const openView = ref(false)
  // const openCron = ref(false)
  const expression = ref('')
  const sysJobGroupOptions = ref<any[]>([])
  const sysJobStatusOptions = ref<any[]>([])

  // 日志弹窗相关
  const logDialogVisible = ref(false)
  const selectedJobId = ref<number | undefined>()
  const selectedJobName = ref<string | undefined>()
  const selectedJobGroup = ref<string | undefined>()

  // 选择相关
  const selectedRows = ref<any[]>([])

  // 搜索参数
  const searchForm = ref({
    jobName: undefined,
    jobGroup: undefined,
    status: undefined
  })

  const {
    columns,
    columnChecks,
    data: jobList,
    loading,
    pagination,
    searchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: listJob,
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
          label: t('system.tool.task.taskNumber'),
          align: 'center' as const,
          minWidth: 110
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
          prop: 'cronExpression',
          label: t('system.tool.task.cronExpression'),
          align: 'center' as const,
          showOverflowTooltip: true,
          minWidth: 150
        },
        {
          prop: 'status',
          label: t('system.tool.task.status'),
          align: 'center' as const,
          minWidth: 100,
          useSlot: true
        },
        {
          prop: 'operation',
          label: t('system.tool.task.operation'),
          align: 'center' as const,
          minWidth: 350,
          fixed: 'right' as const,
          useSlot: true
        }
      ]
    }
  })

  // 表单数据
  const form = reactive<any>({
    id: undefined,
    jobName: undefined,
    jobGroup: undefined,
    invokeTarget: undefined,
    cronExpression: undefined,
    misfirePolicy: '1',
    concurrent: '1',
    status: '0',
    createTime: undefined,
    nextValidTime: undefined
  })

  const formRef = ref<FormInstance | null>(null)
  const viewFormRef = ref<FormInstance | null>(null)

  // 表单校验规则
  const rules = {
    jobName: [
      {
        required: true,
        message: t('system.tool.task.enterTaskName'),
        trigger: 'blur'
      }
    ],
    invokeTarget: [
      {
        required: true,
        message: t('system.tool.task.invokeTargetStringRequired'),
        trigger: 'blur'
      }
    ],
    cronExpression: [
      {
        required: true,
        message: t('system.tool.task.cronExpressionRequired'),
        trigger: 'blur'
      }
    ]
  }

  // 生命周期钩子
  onMounted(() => {
    // 手动加载数据
    refreshData()

    getDicts('sys_job_group')
      .then((response: any) => {
        sysJobGroupOptions.value = response || []
      })
      .catch((error) => {
        console.error('获取任务组字典失败:', error)
        sysJobGroupOptions.value = []
      })
    getDicts('sys_job_status')
      .then((response: any) => {
        sysJobStatusOptions.value = response || []
      })
      .catch((error) => {
        console.error('获取任务状态字典失败:', error)
        sysJobStatusOptions.value = []
      })
  })

  // 方法定义

  // 搜索处理
  const handleSearch = (params: Record<string, any>) => {
    Object.assign(searchParams, params)
    refreshData()
  }

  // 多选框选中数据
  const handleSelectionChange = (selection: any[]) => {
    selectedRows.value = selection
  }

  const cancel = () => {
    open.value = false
    reset()
  }

  const reset = () => {
    Object.assign(form, {
      id: undefined,
      jobName: undefined,
      jobGroup: undefined,
      invokeTarget: undefined,
      cronExpression: undefined,
      misfirePolicy: '1',
      concurrent: '1',
      status: '0'
    })
    formRef.value?.resetFields()
  }

  /** 新增按钮操作 */
  const handleAdd = () => {
    reset()
    open.value = true
    title.value = t('system.tool.task.addTask')
  }

  /** 修改按钮操作 */
  const handleUpdate = (row?: any) => {
    reset()
    const jobId = row?.id || selectedRows.value[0]?.id
    getJob(jobId)
      .then((response: any) => {
        if (!response) {
          MessageUtil.error('获取任务信息失败')
          return
        }
        open.value = true
        nextTick(() => {
          Object.assign(form, response)
          form.status = `${response.status}`
        })
        title.value = t('system.tool.task.modifyTask')
      })
      .catch((error) => {
        console.error('获取任务详情失败:', error)
        MessageUtil.error('获取任务信息失败')
      })
  }

  /** 提交按钮 */
  const submitForm = () => {
    formRef.value!.validate((valid: boolean) => {
      if (valid) {
        if (form.id !== undefined) {
          updateJob(form).then(() => {
            MessageUtil.success(t('common.updateSuccess'))
            open.value = false
            refreshData()
          })
        } else {
          addJob(form).then(() => {
            MessageUtil.success(t('common.addSuccess'))
            open.value = false
            refreshData()
          })
        }
      }
    })
  }

  /** 删除按钮操作 */
  const handleDelete = (row?: any) => {
    const deleteIds = row?.id || selectedRows.value.map((item) => item.id).join(',')
    ElMessageBox.confirm(
      t('system.tool.task.confirmDelete') + ' ' + deleteIds + ' ' + t('system.tool.task.dataItem') + '?',
      t('common.warning'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
      .then(() => {
        return delJob(deleteIds)
      })
      .then(() => {
        refreshData()
        MessageUtil.success(t('system.tool.task.deleteSuccess'))
      })
      .catch(() => {})
  }

  // 任务状态修改
  const handleStatusChange = (row: any) => {
    if (!row.id) {
      return
    }
    const text = row.status === 0 ? t('system.tool.task.disable') : t('system.tool.task.enable')
    ElMessageBox.confirm(`${t('common.confirm')} ${text} ${row.jobName} ${t('system.tool.task.task')}?`, t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
      .then(() => {
        return changeJobStatus(row.id, row.status)
      })
      .then(() => {
        MessageUtil.success(`${text}${t('common.success')}`)
      })
      .catch(() => {
        row.status = row.status === 0 ? 1 : 0
      })
  }

  /* 立即执行一次 */
  const handleRun = (row: any) => {
    ElMessageBox.confirm(
      `${t('system.tool.task.confirmRunOnce')} ${row.jobName} ${t('system.tool.task.task')}?`,
      t('common.warning'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
      .then(() => {
        return runJob(row.id, row.jobGroup)
      })
      .then(() => {
        MessageUtil.success(t('system.tool.task.runOnceSuccess'))
      })
      .catch(() => {})
  }

  /** 任务详细信息 */
  const handleView = (row: any) => {
    getJob(row.id).then((response) => {
      Object.assign(form, response)
      openView.value = true
    })
  }

  /** 确定后回传值 */
  const crontabFill = (value: string) => {
    form.cronExpression = value
  }

  /** 任务日志列表查询 */
  const handleJobLog = (row?: any) => {
    selectedJobId.value = row?.id
    selectedJobName.value = row?.jobName
    selectedJobGroup.value = row?.jobGroup
    logDialogVisible.value = true
  }

  // 任务组名字典翻译
  const jobGroupFormat = (row: any) => {
    return sysJobGroupOptions.value.find((item: any) => item.dictValue === row.jobGroup)?.dictLabel || row.jobGroup
  }
</script>

<style scoped lang="scss">
  .job-page {
    padding: 16px;
    height: 100%;
    display: flex;
    flex-direction: column;
  }
</style>
