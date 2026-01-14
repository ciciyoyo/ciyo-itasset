<template>
  <div>
    <el-tabs v-model="activeName">
      <el-tab-pane :label="$t('system.tool.gen.basicInfo')" name="basic">
        <basic-info-form ref="basicInfo" :info="info" />
      </el-tab-pane>
      <el-tab-pane :label="$t('system.tool.gen.columnInfo')" name="columnInfo">
        <el-table ref="dragTable" :data="columns" row-key="columnId" :max-height="tableHeight">
          <el-table-column :label="$t('system.tool.gen.serialNo')" type="index" min-width="5%" />
          <el-table-column
            :label="$t('system.tool.gen.columnName')"
            prop="columnName"
            min-width="10%"
            :show-overflow-tooltip="true"
          />
          <el-table-column :label="$t('system.tool.gen.columnComment')" min-width="10%">
            <template #default="scope">
              <el-input v-model="scope.row.columnComment"></el-input>
            </template>
          </el-table-column>
          <el-table-column
            :label="$t('system.tool.gen.columnType')"
            prop="columnType"
            min-width="10%"
            :show-overflow-tooltip="true"
          />
          <el-table-column :label="$t('system.tool.gen.javaType')" min-width="11%">
            <template #default="scope">
              <el-select v-model="scope.row.javaType">
                <el-option label="Long" value="Long" />
                <el-option label="String" value="String" />
                <el-option label="Integer" value="Integer" />
                <el-option label="Double" value="Double" />
                <el-option label="BigDecimal" value="BigDecimal" />
                <el-option label="LocalDateTime" value="LocalDateTime" />
                <el-option label="Boolean" value="Boolean" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="$t('system.tool.gen.javaField')" min-width="10%">
            <template #default="scope">
              <el-input v-model="scope.row.javaField"></el-input>
            </template>
          </el-table-column>

          <el-table-column :label="$t('system.tool.gen.insert')" min-width="5%">
            <template #default="scope">
              <el-checkbox true-label="1" false-label="0" v-model="scope.row.isInsert"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column :label="$t('system.tool.gen.edit')" min-width="5%">
            <template #default="scope">
              <el-checkbox true-label="1" false-label="0" v-model="scope.row.isEdit"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column :label="$t('system.tool.gen.list')" min-width="5%">
            <template #default="scope">
              <el-checkbox true-label="1" false-label="0" v-model="scope.row.isList"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column :label="$t('system.tool.gen.query')" min-width="5%">
            <template #default="scope">
              <el-checkbox true-label="1" false-label="0" v-model="scope.row.isQuery"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column :label="$t('system.tool.gen.queryType')" min-width="10%">
            <template #default="scope">
              <el-select v-model="scope.row.queryType">
                <el-option label="=" value="EQ" />
                <el-option label="!=" value="NE" />
                <el-option label=">" value="GT" />
                <el-option label=">=" value="GTE" />
                <el-option label="<" value="LT" />
                <el-option label="<=" value="LTE" />
                <el-option label="LIKE" value="LIKE" />
                <el-option label="BETWEEN" value="BETWEEN" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="$t('system.tool.gen.required')" min-width="5%">
            <template #default="scope">
              <el-checkbox true-label="1" false-label="0" v-model="scope.row.isRequired"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column :label="$t('system.tool.gen.displayType')" min-width="12%">
            <template #default="scope">
              <el-select v-model="scope.row.htmlType">
                <el-option :label="$t('system.tool.gen.textInput')" value="input" />
                <el-option :label="$t('system.tool.gen.textarea')" value="textarea" />
                <el-option :label="$t('system.tool.gen.select')" value="select" />
                <el-option :label="$t('system.tool.gen.radio')" value="radio" />
                <el-option :label="$t('system.tool.gen.checkbox')" value="checkbox" />
                <el-option :label="$t('system.tool.gen.datetime')" value="datetime" />
                <el-option :label="$t('system.tool.gen.imageUpload')" value="imageUpload" />
                <el-option :label="$t('system.tool.gen.fileUpload')" value="fileUpload" />
                <el-option :label="$t('system.tool.gen.editor')" value="editor" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="$t('system.tool.gen.dictType')" min-width="12%">
            <template #default="scope">
              <el-select v-model="scope.row.dictType" clearable filterable :placeholder="$t('system.tool.gen.pleaseSelect')">
                <el-option v-for="dict in dictOptions" :key="dict.dictType" :label="dict.dictName" :value="dict.dictType">
                  <span style="float: left">{{ dict.dictName }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">{{ dict.dictType }}</span>
                </el-option>
              </el-select>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane :label="$t('system.tool.gen.genInfo')" name="genInfo">
        <gen-info-form ref="genInfo" :info="info" :tables="tables" />
      </el-tab-pane>
    </el-tabs>
    <el-form label-width="100px">
      <div style="text-align: center; margin-left: -100px; margin-top: 10px">
        <el-button @click="close()">{{ $t('system.tool.gen.back') }}</el-button>
        <el-button type="primary" @click="submitForm()"> {{ $t('system.tool.gen.submit') }} </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts" name="GenEdit">
  import { getGenTable, updateGenTable } from '@/api/tool/gen'
  import { optionselect as getDictOptionselect } from '@/api/system/dict/type'
  import basicInfoForm from './basicInfoForm.vue'
  import genInfoForm from './genInfoForm.vue'
  import { getCurrentInstance, ref, watch } from 'vue'
  import { useI18n } from 'vue-i18n'

  const { t } = useI18n()

  interface DictOption {
    dictType: string
    dictName: string
  }

  const props = defineProps({
    tableId: {
      type: [String, Number],
      default: null
    }
  })

  const emit = defineEmits(['close', 'success'])

  const { proxy } = getCurrentInstance() as any

  const activeName = ref('columnInfo')
  const tableHeight = ref(document.documentElement.scrollHeight - 245 + 'px')
  const tables = ref([])
  const columns = ref([])
  const dictOptions = ref<DictOption[]>([])
  const info = ref({})

  /** 加载表数据 */
  const loadData = async (id: any) => {
    if (id) {
      const [genRes, dictRes] = await Promise.all([getGenTable(id), getDictOptionselect()])
      columns.value = (genRes as any).rows
      info.value = (genRes as any).info
      tables.value = (genRes as any).tables
      dictOptions.value = dictRes as any
    }
  }

  /** 提交按钮 */
  const submitForm = async () => {
    const basicForm = proxy.$refs.basicInfo.$refs.basicInfoForm
    const genForm = proxy.$refs.genInfo.$refs.genInfoForm
    const results = await Promise.all([basicForm, genForm].map(getFormPromise))
    const validateResult = results.every((item) => !!item)
    if (validateResult) {
      const genTable = {
        ...(info.value as any),
        columns: columns.value,
        params: {
          treeCode: (info.value as any).treeCode,
          treeName: (info.value as any).treeName,
          treeParentCode: (info.value as any).treeParentCode,
          parentMenuId: (info.value as any).parentMenuId
        }
      }
      await updateGenTable(genTable)
      ElMessage.success(t('system.tool.gen.saveSuccess'))
      emit('success')
    } else {
      ElMessage.error(t('system.tool.gen.formValidationFailed'))
    }
  }

  const getFormPromise = (form: any) => new Promise((resolve) => form.validate(resolve))

  const close = () => emit('close')

  // 监听 tableId 变化加载数据
  watch(
    () => props.tableId,
    (newId) => {
      if (newId) {
        loadData(newId)
      }
    },
    { immediate: true }
  )
</script>
