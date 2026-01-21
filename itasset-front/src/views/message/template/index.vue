<!-- Message template management page -->
<template>
  <div class="template-page art-full-height">
    <div class="flex flex-col h-full">
      <el-alert :title="$t('system.noticeTemplate.tip')" type="warning" class="mb-3" />

      <!-- Search bar -->
      <TemplateSearch
        v-show="showSearch"
        v-model="searchForm"
        :msgTypeList="msgTypeList"
        @search="handleSearch"
        @reset="resetSearchParams"
      />

      <ElCard
        class="art-table-card flex flex-col flex-1 min-h-0"
        shadow="never"
        :style="{ 'margin-top': showSearch ? '12px' : '0' }"
      >
        <!-- Table header -->
        <ArtTableHeader
          v-model:columns="columnChecks"
          v-model:showSearchBar="showSearch"
          :loading="loading"
          @refresh="refreshData"
        >
          <template #left>
            <ElSpace wrap>
              <ElButton @click="handleAdd" icon="ele-Plus" type="primary" v-hasPermi="['sys:msgtemplate:save']" v-ripple>
                {{ $t('common.add') }}
              </ElButton>
              <ElButton
                @click="handleDelete"
                icon="ele-Delete"
                type="danger"
                :disabled="!selectedRows.length"
                v-hasPermi="['sys:msgtemplate:delete']"
                v-ripple
              >
                {{ $t('common.delete') }}
              </ElButton>
              <ElButton @click="handleSyncWxMpTemplate" icon="ele-Refresh" v-hasPermi="['sys:msgtemplate:save']" v-ripple>
                {{ $t('system.noticeTemplate.syncWeChatTemplate') }}
              </ElButton>
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
          <template #templateContent="{ row }">
            <el-button link type="primary" @click="handleDesc(row.templateContent)">
              {{ $t('system.noticeTemplate.details') }}
            </el-button>
          </template>
          <template #operation="{ row }">
            <div class="flex gap-2">
              <el-button link type="success" @click="handleOpenSend(row)">
                {{ $t('system.noticeTemplate.send') }}
              </el-button>
              <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['sys:msgtemplate:update']">
                {{ $t('common.modify') }}
              </el-button>
              <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['sys:msgtemplate:delete']">
                {{ $t('common.delete') }}
              </el-button>
            </div>
          </template>
        </ArtTable>
      </ElCard>
    </div>

    <!-- Add or edit dialog -->
    <el-dialog v-if="open" :title="title" v-model="open" width="800px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="$t('system.noticeTemplate.templateTitle')" prop="templateName">
          <el-input v-model="form.templateName" :placeholder="$t('system.noticeTemplate.enterTemplateTitle')" />
        </el-form-item>
        <el-form-item :label="$t('system.noticeTemplate.templateCode')" prop="templateCode">
          <el-input v-model="form.templateCode" :placeholder="$t('system.noticeTemplate.enterTemplateCode')">
            <template #append>
              <el-button @click="handleRandCode">{{ $t('system.noticeTemplate.generateRandom') }}</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item :label="$t('system.noticeTemplate.templateType')" prop="templateType">
          <el-select v-model="form.templateType" :placeholder="$t('system.noticeTemplate.chooseTemplateType')" clearable>
            <el-option v-for="item in msgTypeList" :label="item.label" :value="item.value" :key="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="[1, 3].includes(form.templateType)"
          :label="$t('system.noticeTemplate.thirdPartyTemplateId')"
          prop="thirdTemplateId"
        >
          <el-input v-model="form.thirdTemplateId" :placeholder="$t('system.noticeTemplate.enterThirdPartyTemplateId')" />
          <p class="tips-text">{{ $t('system.noticeTemplate.wechatTemplateIdNote') }}</p>
        </el-form-item>
        <el-form-item v-if="[6].includes(form.templateType)" label="" prop="thirdTemplateId">
          <el-input v-model="form.thirdTemplateId" :placeholder="$t('system.noticeTemplate.webHookUrl')" />
          <p class="tips-text">{{ $t('system.noticeTemplate.webhookTips') }}</p>
        </el-form-item>
        <el-form-item :label="$t('system.noticeTemplate.templateContent')" prop="templateContent">
          <div>
            <div v-if="form.templateType != 3">
              <el-button size="small" type="primary" @click="handleInsertVariable">
                {{ $t('system.noticeTemplate.insertVariables') }}
              </el-button>
              <form-tinymce
                style="width: 600px; margin-top: 5px"
                ref="tinymceRef"
                v-model:value="form.templateContent"
                :toolbar="getToolbar"
                :key="form.templateType"
              />
            </div>
            <div v-else class="mt10">
              {{ form.templateContent }}
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">
            {{ $t('common.confirm') }}
          </el-button>
          <el-button @click="cancel">
            {{ $t('common.cancel') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Send test dialog -->
    <el-dialog v-if="sendOpen" :title="$t('system.noticeTemplate.sendMessage')" v-model="sendOpen" width="800px" append-to-body>
      <el-form ref="sendFormRef" :model="sendForm" label-width="130px">
        <el-form-item
          :label="$t('system.noticeTemplate.receiver')"
          prop="receiver"
          :rules="[{ required: true, message: $t('system.noticeTemplate.enterReceiver'), trigger: 'blur' }]"
        >
          <el-input v-model="sendForm.receiver" :placeholder="$t('system.noticeTemplate.enterReceiver')" />
        </el-form-item>
        <el-form-item
          :label="$t('system.noticeTemplate.templateCode')"
          prop="templateCode"
          :rules="[
            {
              required: true,
              message: $t('system.noticeTemplate.enterTemplateCode'),
              trigger: 'blur'
            }
          ]"
        >
          <el-input v-model="sendForm.templateCode" :placeholder="$t('system.noticeTemplate.enterTemplateCode')" />
        </el-form-item>
        <el-form-item
          :label="$t('system.noticeTemplate.messageType')"
          prop="msgType"
          :rules="[
            {
              required: true,
              message: $t('system.noticeTemplate.selectTemplateType'),
              trigger: 'blur'
            }
          ]"
        >
          <el-select v-model="sendForm.msgType" :placeholder="$t('system.noticeTemplate.chooseTemplateType')" clearable>
            <el-option v-for="item in msgTypeList" :label="item.label" :value="item.value" :key="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('system.noticeTemplate.params')" prop="testData">
          <el-input
            :rows="8"
            v-model="sendForm.testData"
            type="textarea"
            :placeholder="$t('system.noticeTemplate.templateParams')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="handleSendMsg">
            {{ $t('common.confirm') }}
          </el-button>
          <el-button @click="sendOpen = false">{{ $t('common.cancel') }}</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Details dialog -->
    <el-dialog append-to-body :title="$t('system.noticeTemplate.details')" v-model="dialogVisible" width="40%">
      <span>{{ dialogContent }}</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="dialogVisible = false">
            {{ $t('common.confirm') }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script name="MsgTemplate" setup lang="ts">
  import { computed, ref } from 'vue'
  import { ElMessageBox } from 'element-plus'
  import type { FormInstance } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { useTable } from '@/hooks/core/useTable'
  import {
    addMsgTemplate,
    delMsgTemplate,
    getMsgTemplate,
    pageMsgTemplate,
    sendTemplateMsg,
    syncWxTemplateMsg,
    updateMsgTemplate,
    type MsgTemplate
  } from '@/api/system/msgtemplate'
  import FormTinymce from '@/components/business/tinymce/index.vue'
  import { customAlphabet } from 'nanoid'
  import { resetFormRef } from '@/utils/business'
  import { MessageUtil } from '@/utils/messageUtil'
  import TemplateSearch from './modules/template-search.vue'

  const nanoid = customAlphabet('0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ', 6)

  // i18n support
  const { t } = useI18n()

  // Define state
  const dialogContent = ref('')
  const dialogVisible = ref(false)
  const title = ref('')
  const open = ref(false)
  const sendOpen = ref(false)
  const showSearch = ref(true)

  // Selected rows
  const selectedRows = ref<MsgTemplate[]>([])

  // Search form
  const searchForm = ref({
    templateName: undefined,
    templateCode: undefined,
    templateType: undefined
  })

  const form = ref<Partial<MsgTemplate>>({})
  const sendForm = ref({
    templateCode: undefined,
    receiver: undefined,
    msgType: undefined,
    testData: undefined
  })

  const rules = {
    templateName: [
      {
        required: true,
        message: t('system.noticeTemplate.templateName'),
        trigger: 'blur'
      }
    ],
    templateCode: [
      {
        required: true,
        message: t('system.noticeTemplate.templateCodeRequired'),
        trigger: 'blur'
      }
    ],
    templateType: [
      {
        required: true,
        message: t('system.noticeTemplate.templateTypeRequired'),
        trigger: 'change'
      }
    ],
    templateContent: [
      {
        required: true,
        message: t('system.noticeTemplate.templateContentRequired'),
        trigger: 'blur'
      }
    ],
    thirdTemplateId: [
      {
        required: true,
        message: t('system.noticeTemplate.inputRequired'),
        trigger: 'blur'
      }
    ]
  }

  const msgTypeList = [
    { label: t('system.noticeTemplate.sms'), value: 1 },
    { label: t('system.noticeTemplate.email'), value: 2 },
    { label: t('system.noticeTemplate.wechat'), value: 3 },
    { label: t('system.noticeTemplate.inbox'), value: 4 },
    { label: t('system.noticeTemplate.cpWechat'), value: 5 },
    { label: t('system.noticeTemplate.chatHook'), value: 6 },
    { label: t('system.noticeTemplate.dingTalk'), value: 7 },
    { label: t('system.noticeTemplate.lark'), value: 8 }
  ]

  // Define computed properties
  const getToolbar = computed(() => {
    // Different toolbar for email type
    if (form.value.templateType == 2) {
      return 'undo redo | accordion accordionremove | blocks  fontsize | bold italic underline strikethrough | align numlist bullist | link image | table media | lineheight outdent indent| forecolor backcolor removeformat | charmap emoticons | code fullscreen preview | save print | pagebreak anchor codesample | ltr rtl '
    }
    return '  '
  })

  // useTable hook
  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    searchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: pageMsgTemplate,
      apiParams: {
        current: 1,
        size: 20,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        { prop: 'id', label: 'ID', minWidth: 80 },
        { prop: 'templateName', label: t('system.noticeTemplate.templateTitle'), minWidth: 180 },
        { prop: 'templateCode', label: t('system.noticeTemplate.templateCode'), minWidth: 180 },
        {
          prop: 'templateTypeDesc',
          label: t('system.noticeTemplate.templateType'),
          minWidth: 120
        },
        {
          prop: 'templateContent',
          label: t('system.noticeTemplate.templateContent'),
          minWidth: 120,
          useSlot: true
        },
        {
          prop: 'thirdTemplateId',
          label: t('system.noticeTemplate.thirdPartyTemplateId'),
          minWidth: 300
        },
        {
          prop: 'operation',
          label: t('system.noticeTemplate.action'),
          useSlot: true,
          minWidth: 200,
          fixed: 'right'
        }
      ]
    },
    debug: {
      enableLog: true
    }
  })

  const tinymceRef = ref<InstanceType<typeof FormTinymce> | null>(null)
  const formRef = ref<FormInstance | null>(null)
  const sendFormRef = ref<FormInstance | null>(null)

  // Search handling
  const handleSearch = (params: Record<string, any>) => {
    Object.assign(searchParams, params)
    refreshData()
  }

  const handleInsertVariable = () => {
    tinymceRef.value?.handleInsertContent('${' + nanoid() + '}')
  }

  const cancel = () => {
    open.value = false
    reset()
  }

  const reset = () => {
    form.value = {
      id: undefined,
      templateName: undefined,
      templateCode: undefined,
      templateType: undefined,
      templateContent: undefined,
      thirdTemplateId: undefined,
      createTime: undefined,
      createBy: undefined,
      updateTime: undefined,
      updateBy: undefined
    }
    resetFormRef(formRef.value)
  }

  const handleSelectionChange = (selection: any[]): void => {
    selectedRows.value = selection
  }

  const handleAdd = () => {
    reset()
    open.value = true
    title.value = t('system.noticeTemplate.addTemplate')
  }

  const handleUpdate = (row?: any) => {
    reset()
    const id = row?.id || selectedRows.value[0]?.id
    getMsgTemplate(id).then((response) => {
      form.value = response as MsgTemplate
      open.value = true
      title.value = t('system.noticeTemplate.modifyTemplate')
    })
  }

  const handleOpenSend = (row: any) => {
    sendOpen.value = true
    sendForm.value.templateCode = row.templateCode
    sendForm.value.msgType = row.templateType
  }

  const handleSendMsg = () => {
    sendFormRef.value?.validate((valid: boolean) => {
      if (valid) {
        sendTemplateMsg(sendForm.value).then(() => {
          MessageUtil.success(t('common.success'))
          sendOpen.value = false
        })
      }
    })
  }

  const submitForm = () => {
    formRef.value?.validate((valid: boolean) => {
      if (valid) {
        if (form.value.id != null) {
          updateMsgTemplate(form.value).then(() => {
            MessageUtil.success(t('common.success'))
            open.value = false
            refreshData()
          })
        } else {
          addMsgTemplate(form.value).then(() => {
            MessageUtil.success(t('common.success'))
            open.value = false
            refreshData()
          })
        }
      }
    })
  }

  const handleDelete = (row?: any) => {
    const idsToDelete = row?.id || selectedRows.value.map((item) => item.id).join(',')
    ElMessageBox.confirm(t('system.noticeTemplate.isDelete'), t('common.warning'))
      .then(() => delMsgTemplate(idsToDelete))
      .then(() => {
        refreshData()
        MessageUtil.success(t('common.success'))
      })
      .catch(() => {})
  }

  const handleDesc = (content: string) => {
    dialogVisible.value = true
    dialogContent.value = content
  }

  const handleRandCode = () => {
    form.value.templateCode = nanoid()
  }

  const handleSyncWxMpTemplate = () => {
    ElMessageBox.confirm(t('system.noticeTemplate.syncWxMpTemplate'), t('common.warning'))
      .then(() => syncWxTemplateMsg({}))
      .then(() => {
        refreshData()
        MessageUtil.success(t('common.success'))
      })
      .catch(() => {})
  }
</script>

<style scoped lang="scss">
  .template-page {
    padding: 16px;
  }

  .tips-text {
    color: #909399;
    font-size: 12px;
  }
</style>
