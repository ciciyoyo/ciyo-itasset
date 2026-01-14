<template>
  <div>
    <el-alert class="mb10" :title="$t('system.fileConfig.desc')" type="success" />
    <el-form ref="fileEnvConfig" :model="form" label-width="150px">
      <el-form-item
        :label="$t('system.fileConfig.savePlatform')"
        prop="ossType"
        :rules="[{ required: true, message: $t('system.fileConfig.selectPlatform'), trigger: 'blur' }]"
      >
        <el-select
          class="select-width"
          v-model="form.ossType"
          @change="handleOssTypeChange"
          :placeholder="$t('system.fileConfig.selectPlatform')"
        >
          <el-option :label="$t('system.fileConfig.aliyun')" value="ALIYUN" />
          <el-option :label="$t('system.fileConfig.qiniu')" value="QINIU" />
          <el-option label="腾讯" value="TENCENT" />
          <el-option :label="$t('system.fileConfig.upyun')" value="UPYUN" />
          <el-option :label="$t('system.fileConfig.local')" value="LOCAL" />
          <el-option :label="$t('system.fileConfig.genericS3')" value="S3" />
          <el-option label="Minio" value="MINIO" />
        </el-select>
      </el-form-item>
      <el-form-item
        v-if="form.ossType !== 'LOCAL'"
        label="AccessKeyID"
        prop="accessKeyId"
        :rules="[{ required: true, message: $t('formI18n.all.pleaseEnter') + ' AccessKeyID', trigger: 'blur' }]"
      >
        <el-input v-model="form.accessKeyId" show-password type="password" />
      </el-form-item>
      <el-form-item
        v-if="form.ossType !== 'LOCAL'"
        label="AccessKeySecret"
        prop="accessKeySecret"
        :rules="[{ required: true, message: $t('formI18n.all.pleaseEnter') + ' AccessKeySecret', trigger: 'blur' }]"
      >
        <el-input v-model="form.accessKeySecret" show-password type="password" />
      </el-form-item>
      <el-form-item
        v-if="form.ossType === 'ALIYUN' || form.ossType === 'S3' || form.ossType === 'MINIO'"
        label="Endpoint"
        prop="endpoint"
        :rules="[{ required: true, message: $t('formI18n.all.pleaseEnter') + ' Endpoint', trigger: 'blur' }]"
      >
        <el-input v-model="form.endpoint" />
      </el-form-item>
      <el-form-item
        v-if="form.ossType === 'TENCENT'"
        label="Region"
        prop="endpoint"
        :rules="[{ required: true, message: '请输入Region', trigger: 'blur' }]"
      >
        <el-input v-model="form.endpoint" placeholder="请输入Region" />
      </el-form-item>
      <el-form-item
        v-if="form.ossType !== 'LOCAL'"
        label="bucketName"
        prop="bucketName"
        :rules="[{ required: true, message: $t('formI18n.all.pleaseEnter') + ' bucketName', trigger: 'blur' }]"
      >
        <el-input v-model="form.bucketName" />
      </el-form-item>
      <el-form-item
        v-if="form.ossType === 'LOCAL'"
        :label="$t('system.fileConfig.storagePath')"
        prop="uploadFolder"
        :rules="[{ required: true, message: $t('system.fileConfig.enterStoragePath'), trigger: 'blur' }]"
      >
        <el-input v-model="form.uploadFolder" />
      </el-form-item>
      <el-form-item
        :label="$t('system.fileConfig.subdirectory')"
        prop="prefixPath"
        v-if="form.ossType !== 'LOCAL'"
        :rules="[
          // { required: true, message: '请输入如 image 格式 不能携带符号', trigger: 'blur' },
          {
            pattern: /^[a-zA-Z0-9]*$/,
            message: $t('system.fileConfig.enterSubdirectory'),
            trigger: 'blur'
          }
        ]"
      >
        <el-input v-model="form.prefixPath" />
      </el-form-item>
      <el-form-item
        :label="$t('system.fileConfig.accessDomain')"
        prop="domain"
        :rules="[
          {
            required: true,
            message: $t('system.fileConfig.enterAccessDomain'),
            trigger: 'blur'
          }
          // { pattern: /^(https?:\/\/[^\/]+)$/, message: '请输入域名 如 https://oss.tduckcloud.com', trigger: 'blur' }
        ]"
      >
        <el-input v-model="form.domain" placeholder="请输入域名 如 https://oss.tduckcloud.com" />
      </el-form-item>
      <el-form-item v-if="form.ossType === 'S3'" label="regionName" prop="regionName">
        <el-input v-model="form.regionName" placeholder="请输入regionName" />
      </el-form-item>
      <el-form-item v-if="form.ossType === 'S3'" label="acl" prop="acl">
        <el-select v-model="form.acl" placeholder="请选择acl">
          <el-option label="Private" value="Private"></el-option>
          <el-option label="PublicRead" value="PublicRead"></el-option>
          <el-option label="PublicReadWrite" value="PublicReadWrite"></el-option>
          <el-option label="AuthenticatedRead" value="AuthenticatedRead"></el-option>
          <el-option label="LogDeliveryWrite" value="LogDeliveryWrite"></el-option>
          <el-option label="BucketOwnerRead" value="BucketOwnerRead"></el-option>
          <el-option label="BucketOwnerFullControl" value="BucketOwnerFullControl"></el-option>
          <el-option label="AwsExecRead" value="AwsExecRead"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSubmit">
          {{ $t('common.save') }}
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
  import { onMounted, ref } from 'vue'
  import { useConfig } from './useConfig'
  import type { FormInstance } from 'element-plus'

  const { activeName, form, handleClick, handleSubmit: submitForm } = useConfig()

  const fileEnvConfig = ref<FormInstance>()

  const handleOssTypeChange = (val: string) => {
    if (val === 'LOCAL') {
      form.value.accessKeyId = ''
      form.value.accessKeySecret = ''
      form.value.endpoint = ''
      form.value.bucketName = ''
      form.value.regionName = ''
      form.value.acl = ''
      form.value.prefixPath = ''
    }
  }

  const handleSubmit = () => {
    submitForm(fileEnvConfig.value)
  }

  onMounted(async () => {
    activeName.value = 'fileEnvConfig'
    await handleClick()
  })
</script>

<style scoped>
  .el-input {
    width: 35%;
  }
</style>
