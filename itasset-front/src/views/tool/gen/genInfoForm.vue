<template>
  <el-form ref="genInfoForm" :model="info" :rules="rules" label-width="150px">
    <el-row>
      <el-col :span="12">
        <el-form-item prop="tplCategory">
          <template #label>{{ $t('system.tool.gen.genTemplate') }}</template>
          <el-select v-model="info.tplCategory" @change="tplSelectChange">
            <el-option :label="$t('system.tool.gen.singleTable')" value="crud" />
          </el-select>
        </el-form-item>
      </el-col>

      <el-col :span="12">
        <el-form-item prop="packageName">
          <template #label>
            {{ $t('system.tool.gen.packagePath') }}
            <el-tooltip :content="$t('system.tool.gen.packagePathTip')" placement="top">
              <el-icon>
                <ele-QuestionFilled />
              </el-icon>
            </el-tooltip>
          </template>
          <el-input v-model="info.packageName" />
        </el-form-item>
      </el-col>

      <el-col :span="12">
        <el-form-item prop="moduleName">
          <template #label>
            {{ $t('system.tool.gen.moduleName') }}
            <el-tooltip :content="$t('system.tool.gen.moduleNameTip')" placement="top">
              <el-icon>
                <ele-QuestionFilled />
              </el-icon>
            </el-tooltip>
          </template>
          <el-input v-model="info.moduleName" />
        </el-form-item>
      </el-col>

      <el-col :span="12">
        <el-form-item prop="businessName">
          <template #label>
            {{ $t('system.tool.gen.businessName') }}
            <el-tooltip :content="$t('system.tool.gen.businessNameTip')" placement="top">
              <el-icon>
                <ele-QuestionFilled />
              </el-icon>
            </el-tooltip>
          </template>
          <el-input v-model="info.businessName" />
        </el-form-item>
      </el-col>

      <el-col :span="12">
        <el-form-item prop="functionName">
          <template #label>
            {{ $t('system.tool.gen.functionName') }}
            <el-tooltip :content="$t('system.tool.gen.functionNameTip')" placement="top">
              <el-icon>
                <ele-QuestionFilled />
              </el-icon>
            </el-tooltip>
          </template>
          <el-input v-model="info.functionName" />
        </el-form-item>
      </el-col>

      <el-col :span="12">
        <el-form-item>
          <template #label>
            {{ $t('system.tool.gen.parentMenu') }}
            <el-tooltip :content="$t('system.tool.gen.parentMenuTip')" placement="top">
              <el-icon>
                <ele-QuestionFilled />
              </el-icon>
            </el-tooltip>
          </template>
          <el-tree-select
            v-model="info.parentMenuId"
            :data="menuOptions"
            :props="{ value: 'id', label: 'menuName', children: 'children' }"
            value-key="id"
            :placeholder="$t('system.tool.gen.selectSystemMenu')"
            check-strictly
          />
        </el-form-item>
      </el-col>

      <el-col :span="12">
        <el-form-item prop="genType">
          <template #label>
            {{ $t('system.tool.gen.codeGenMethod') }}
            <el-tooltip :content="$t('system.tool.gen.codeGenMethodTip')" placement="top">
              <el-icon>
                <ele-QuestionFilled />
              </el-icon>
            </el-tooltip>
          </template>
          <el-radio v-model="info.genType" value="0"> {{ $t('system.tool.gen.zipPackage') }} </el-radio>
          <!--					<el-radio v-model="info.genType" label="1">{{ $t('system.tool.gen.customPath') }}</el-radio>-->
        </el-form-item>
      </el-col>

      <el-col :span="24" v-if="info.genType == '1'">
        <el-form-item prop="genPath">
          <template #label>
            {{ $t('system.tool.gen.customPath') }}
            <el-tooltip :content="$t('system.tool.gen.customPathTip')" placement="top">
              <el-icon>
                <ele-QuestionFilled />
              </el-icon>
            </el-tooltip>
          </template>
          <el-input v-model="info.genPath">
            <template #append>
              <el-dropdown>
                <el-button type="primary">
                  {{ $t('system.tool.gen.recentPathSelect') }}
                  <i class="el-icon-arrow-down el-icon--right"></i>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="info.genPath = '/'">{{
                      $t('system.tool.gen.restoreDefaultPath')
                    }}</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-input>
        </el-form-item>
      </el-col>
    </el-row>

    <template v-if="info.tplCategory == 'tree'">
      <h4 class="form-header">{{ $t('system.tool.gen.otherInfo') }}</h4>
      <el-row v-show="info.tplCategory == 'tree'">
        <el-col :span="12">
          <el-form-item>
            <template #label>
              {{ $t('system.tool.gen.treeCodeField') }}
              <el-tooltip :content="$t('system.tool.gen.treeCodeFieldTip')" placement="top">
                <el-icon>
                  <ele-QuestionFilled />
                </el-icon>
              </el-tooltip>
            </template>
            <el-select v-model="info.treeCode" :placeholder="$t('system.tool.gen.pleaseSelect')">
              <el-option
                v-for="(column, index) in info.columns"
                :key="index"
                :label="column.columnName + '：' + column.columnComment"
                :value="column.columnName"
              ></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item>
            <template #label>
              {{ $t('system.tool.gen.treeParentCodeField') }}
              <el-tooltip :content="$t('system.tool.gen.treeParentCodeFieldTip')" placement="top">
                <el-icon>
                  <ele-QuestionFilled />
                </el-icon>
              </el-tooltip>
            </template>
            <el-select v-model="info.treeParentCode" :placeholder="$t('system.tool.gen.pleaseSelect')">
              <el-option
                v-for="(column, index) in info.columns"
                :key="index"
                :label="column.columnName + '：' + column.columnComment"
                :value="column.columnName"
              ></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item>
            <template #label>
              {{ $t('system.tool.gen.treeNameField') }}
              <el-tooltip :content="$t('system.tool.gen.treeNameFieldTip')" placement="top">
                <el-icon>
                  <ele-QuestionFilled />
                </el-icon>
              </el-tooltip>
            </template>
            <el-select v-model="info.treeName" :placeholder="$t('system.tool.gen.pleaseSelect')">
              <el-option
                v-for="(column, index) in info.columns"
                :key="index"
                :label="column.columnName + '：' + column.columnComment"
                :value="column.columnName"
              ></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
    </template>

    <template v-if="info.tplCategory == 'sub'">
      <h4 class="form-header">{{ $t('system.tool.gen.relatedInfo') }}</h4>
      <el-row>
        <el-col :span="12">
          <el-form-item>
            <template #label>
              {{ $t('system.tool.gen.subTableName') }}
              <el-tooltip :content="$t('system.tool.gen.subTableNameTip')" placement="top">
                <el-icon>
                  <ele-QuestionFilled />
                </el-icon>
              </el-tooltip>
            </template>
            <el-select v-model="info.subTableName" :placeholder="$t('system.tool.gen.pleaseSelect')" @change="subSelectChange">
              <el-option
                v-for="(table, index) in tables"
                :key="index"
                :label="table.tableName + '：' + table.tableComment"
                :value="table.tableName"
              ></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item>
            <template #label>
              {{ $t('system.tool.gen.subTableFkName') }}
              <el-tooltip :content="$t('system.tool.gen.subTableFkNameTip')" placement="top">
                <el-icon>
                  <ele-QuestionFilled />
                </el-icon>
              </el-tooltip>
            </template>
            <el-select v-model="info.subTableFkName" :placeholder="$t('system.tool.gen.pleaseSelect')">
              <el-option
                v-for="(column, index) in subColumns"
                :key="index"
                :label="column.columnName + '：' + column.columnComment"
                :value="column.columnName"
              ></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
    </template>
  </el-form>
</template>

<script setup lang="ts">
  import { listMenu } from '@/api/system/menu'
  import { ref, watch, computed, type PropType } from 'vue'
  import { handleTree } from '@/utils/business'
  import { useI18n } from 'vue-i18n'
  import type { GenTableItem } from '@/api/tool/gen'

  const { t } = useI18n()

  const subColumns = ref<any[]>([])
  const menuOptions = ref<any[]>([])

  const props = defineProps({
    info: {
      type: Object,
      default: null
    },
    tables: {
      type: Array as PropType<GenTableItem[]>,
      default: null
    }
  })

  // 表单校验
  const rules = computed(() => ({
    tplCategory: [{ required: true, message: t('system.tool.gen.genTemplateRequired'), trigger: 'blur' }],
    packageName: [{ required: true, message: t('system.tool.gen.packagePathRequired'), trigger: 'blur' }],
    moduleName: [{ required: true, message: t('system.tool.gen.moduleNameRequired'), trigger: 'blur' }],
    businessName: [{ required: true, message: t('system.tool.gen.businessNameRequired'), trigger: 'blur' }],
    functionName: [{ required: true, message: t('system.tool.gen.functionNameRequired'), trigger: 'blur' }]
  }))

  const subSelectChange = () => {
    ;(props.info as any).subTableFkName = ''
  }

  const tplSelectChange = (value: string) => {
    if (value !== 'sub') {
      ;(props.info as any).subTableName = ''
      ;(props.info as any).subTableFkName = ''
    }
  }

  const setSubTableColumns = (value: string) => {
    const table = (props.tables as any[])?.find((t: any) => t.tableName === value)
    if (table) {
      subColumns.value = table.columns
    }
  }

  /** 查询菜单下拉树结构 */
  function getMenuTreeselect() {
    listMenu().then((response: any) => {
      menuOptions.value = handleTree(response, 'id')
    })
  }

  watch(
    () => (props.info as any)?.subTableName,
    (val) => {
      setSubTableColumns(val)
    }
  )

  getMenuTreeselect()
</script>
