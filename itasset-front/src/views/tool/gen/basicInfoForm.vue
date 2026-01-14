<template>
  <el-form ref="basicInfoForm" :model="info" :rules="rules" label-width="150px">
    <el-row>
      <el-col :span="12">
        <el-form-item :label="$t('system.tool.gen.tableName')" prop="tableName">
          <el-input :placeholder="$t('system.tool.gen.enterTableName')" v-model="info.tableName" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="$t('system.tool.gen.tableComment')" prop="tableComment">
          <el-input :placeholder="$t('system.tool.gen.enterInput')" v-model="info.tableComment" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="$t('system.tool.gen.entityClassName')" prop="className">
          <el-input :placeholder="$t('system.tool.gen.enterInput')" v-model="info.className" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="$t('system.tool.gen.author')" prop="functionAuthor">
          <el-input :placeholder="$t('system.tool.gen.enterInput')" v-model="info.functionAuthor" />
        </el-form-item>
      </el-col>
      <el-col :span="24">
        <el-form-item :label="$t('system.tool.gen.remark')" prop="remark">
          <el-input type="textarea" :rows="3" v-model="info.remark"></el-input>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script setup lang="ts">
  import { ref, computed } from 'vue'
  import { useI18n } from 'vue-i18n'

  const { t } = useI18n()

  defineProps({
    info: {
      type: Object,
      default: null
    }
  })

  // 表单校验
  const rules = computed(() => ({
    tableName: [{ required: true, message: t('system.tool.gen.tableNameRequired'), trigger: 'blur' }],
    tableComment: [{ required: true, message: t('system.tool.gen.tableCommentRequired'), trigger: 'blur' }],
    className: [{ required: true, message: t('system.tool.gen.classNameRequired'), trigger: 'blur' }],
    functionAuthor: [{ required: true, message: t('system.tool.gen.authorRequired'), trigger: 'blur' }]
  }))
</script>
