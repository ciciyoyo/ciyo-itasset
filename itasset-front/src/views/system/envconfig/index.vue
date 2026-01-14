<template>
  <el-card>
    <el-tabs class="=tabs" v-model="activeName" @tab-change="handleTabClick">
      <el-tab-pane :label="$t('system.systemConfig.title')" name="systemInfoConfig">
        <system-info-config v-if="activeName === 'systemInfoConfig'" />
      </el-tab-pane>
      <el-tab-pane :label="$t('system.mailConfig.title')" name="emailEnvConfig">
        <el-form ref="emailEnvConfig" :model="form" label-width="148px">
          <el-form-item
            label="host"
            prop="host"
            :rules="[{ required: true, message: $t('system.mailConfig.host'), trigger: 'blur' }]"
          >
            <el-input v-model="form.host" placeholder="smtp.88.com" />
          </el-form-item>
          <el-form-item
            label="port"
            prop="port"
            :rules="[{ required: true, message: $t('system.mailConfig.port'), trigger: 'blur' }]"
          >
            <el-input v-model="form.port" placeholder="465" />
          </el-form-item>
          <el-form-item
            :label="$t('system.mailConfig.username')"
            prop="port"
            :rules="[{ required: true, message: $t('system.mailConfig.enterUsername'), trigger: 'blur' }]"
          >
            <el-input v-model="form.username" :placeholder="$t('system.mailConfig.emailAccount')" />
          </el-form-item>
          <el-form-item
            :label="$t('system.mailConfig.authorizationCode')"
            prop="password"
            :rules="[{ required: true, message: $t('system.mailConfig.enterAuthorizationCode'), trigger: 'blur' }]"
          >
            <el-input v-model="form.password" :placeholder="$t('system.mailConfig.authorizationCodeNote')" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSubmit(emailEnvConfig)">
              {{ $t('system.systemConfig.save') }}
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      <el-tab-pane :label="$t('system.smsConfig.title')" name="smsEnvConfig">
        <el-form ref="smsEnvConfig" :model="form" label-width="140px">
          <el-form-item
            :label="$t('system.smsConfig.channel')"
            prop="type"
            :rules="[{ required: true, message: $t('system.smsConfig.selectChannel'), trigger: 'blur' }]"
          >
            <el-select class="select-width" v-model="form.type" :placeholder="$t('system.smsConfig.selectChannel')">
              <el-option :label="$t('system.smsConfig.aliyun')" value="ALIYUN" />
              <el-option :label="$t('system.smsConfig.tencentCloud')" value="TENCENT_CLOUD" />
              <el-option :label="$t('system.smsConfig.zywx')" value="VEESING" />
            </el-select>
          </el-form-item>
          <el-form-item
            v-if="form.type == 'VEESING'"
            :label="$t('system.smsConfig.appId')"
            prop="appId"
            :rules="[{ required: true, message: $t('system.smsConfig.appId'), trigger: 'blur' }]"
          >
            <el-input v-model="form.appId" :placeholder="$t('system.smsConfig.appId')" />
          </el-form-item>
          <el-form-item
            v-if="form.type == 'VEESING'"
            :label="$t('system.smsConfig.appKey')"
            prop="secretKey"
            :rules="[{ required: true, message: $t('system.smsConfig.appKey'), trigger: 'blur' }]"
          >
            <el-input label-width="140px" v-model="form.secretKey" :placeholder="$t('system.smsConfig.appKey')" show-password />
          </el-form-item>
          <el-form-item
            label-width="140px"
            v-if="form.type == 'ALIYUN'"
            :label="$t('system.smsConfig.accessKeyId')"
            prop="secretId"
            :rules="[{ required: true, message: $t('system.smsConfig.enterAccessKeyId'), trigger: 'blur' }]"
          >
            <el-input v-model="form.secretId" :placeholder="$t('system.smsConfig.enterAccessKeyId')" />
          </el-form-item>
          <el-form-item
            label-width="140px"
            v-if="form.type == 'ALIYUN'"
            :label="$t('system.smsConfig.accessKeySecret')"
            prop="secretKey"
            :rules="[{ required: true, message: $t('system.smsConfig.enterAccessKeySecret'), trigger: 'blur' }]"
          >
            <el-input v-model="form.secretKey" :placeholder="$t('system.smsConfig.enterAccessKeySecret')" show-password />
          </el-form-item>
          <el-form-item
            label-width="140px"
            v-if="form.type == 'ALIYUN'"
            :label="$t('system.smsConfig.smsSignature')"
            prop="sign"
            :rules="[{ required: true, message: $t('system.smsConfig.enterSmsSignature'), trigger: 'blur' }]"
          >
            <el-input v-model="form.sign" :placeholder="$t('system.smsConfig.enterSmsSignature')" />
          </el-form-item>
          <el-form-item
            v-if="form.type == 'TENCENT_CLOUD'"
            :label="$t('system.smsConfig.secretId')"
            prop="secretId"
            :rules="[{ required: true, message: $t('system.smsConfig.enterSecretId'), trigger: 'blur' }]"
          >
            <el-input v-model="form.secretId" :placeholder="$t('system.smsConfig.enterSecretId')" />
          </el-form-item>
          <el-form-item
            v-if="form.type == 'TENCENT_CLOUD'"
            :label="$t('system.smsConfig.secretKey')"
            prop="secretKey"
            :rules="[{ required: true, message: $t('system.smsConfig.enterSecretKey'), trigger: 'blur' }]"
          >
            <el-input v-model="form.secretKey" :placeholder="$t('system.smsConfig.enterSecretKey')" />
          </el-form-item>
          <el-form-item
            v-if="form.type == 'TENCENT_CLOUD'"
            :label="$t('system.smsConfig.smsSignature')"
            prop="sign"
            :rules="[{ required: true, message: $t('system.smsConfig.enterSmsSignature'), trigger: 'blur' }]"
          >
            <el-input v-model="form.sign" :placeholder="$t('system.smsConfig.enterSmsSignature')" />
          </el-form-item>
          <el-form-item
            v-if="form.type == 'TENCENT_CLOUD'"
            :label="$t('system.smsConfig.appId')"
            prop="appId"
            :rules="[{ required: true, message: $t('system.smsConfig.enterAppId'), trigger: 'blur' }]"
          >
            <el-input v-model="form.appId" :placeholder="$t('system.smsConfig.enterAppId')" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSubmit(smsEnvConfig)">
              {{ $t('system.systemConfig.save') }}
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane :label="$t('system.fileConfig.title')" name="fileEnvConfig">
        <oss-config v-if="activeName === 'fileEnvConfig'" />
      </el-tab-pane>
      <el-tab-pane :label="$t('system.fileStorage.wechatOfficialAccount')" name="wxMpEnvConfig">
        <el-form ref="wxMpEnvConfig" :model="form" label-width="120px">
          <el-form-item
            label="appId"
            prop="appId"
            :rules="[{ required: true, message: $t('system.fileStorage.appId'), trigger: 'blur' }]"
          >
            <el-input v-model="form.appId" :placeholder="$t('system.fileStorage.appId')" />
          </el-form-item>
          <el-form-item
            label="appSecret"
            prop="secret"
            :rules="[{ required: true, message: $t('system.fileStorage.appSecret'), trigger: 'blur' }]"
          >
            <el-input v-model="form.secret" :placeholder="$t('system.fileStorage.appSecret')" show-password />
          </el-form-item>
          <el-form-item
            label="token"
            prop="token"
            :rules="[{ required: true, message: $t('system.fileStorage.token'), trigger: 'blur' }]"
          >
            <el-input v-model="form.token" :placeholder="$t('system.fileStorage.token')" show-password />
          </el-form-item>
          <el-form-item
            label="aesKey"
            prop="aesKey"
            :rules="[{ required: true, message: $t('system.fileStorage.aesKey'), trigger: 'blur' }]"
          >
            <el-input v-model="form.aesKey" :placeholder="$t('system.fileStorage.aesKey')" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSubmit(wxMpEnvConfig)">
              {{ $t('common.save') }}
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      <el-tab-pane :label="$t('system.faceVerification.wechatMiniProgram')" name="wxMaEnvConfig">
        <el-form ref="wxMaEnvConfig" :model="form" label-width="120px">
          <el-form-item
            label="Appid"
            prop="appid"
            :rules="[
              {
                required: true,
                message: $t('system.faceVerification.miniProgramAppId'),
                trigger: 'blur'
              }
            ]"
          >
            <el-input v-model="form.appid" :placeholder="$t('system.faceVerification.miniProgramAppId')" />
          </el-form-item>
          <el-form-item
            label="secret"
            prop="secret"
            :rules="[
              {
                required: true,
                message: $t('system.faceVerification.miniProgramSecret'),
                trigger: 'blur'
              }
            ]"
          >
            <el-input v-model="form.secret" :placeholder="$t('system.faceVerification.miniProgramSecret')" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSubmit(wxMaEnvConfig)">
              {{ $t('common.save') }}
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup lang="ts">
  import { ref } from 'vue'
  import { useConfig } from './sub/useConfig'
  import SystemInfoConfig from './sub/SystemInfoConfig.vue'
  import OssConfig from './sub/OssConfig.vue'
  import type { FormInstance } from 'element-plus'

  defineOptions({
    name: 'ConfigForm'
  })

  const { activeName, form, handleClick, handleSubmit } = useConfig()

  // Form refs
  const emailEnvConfig = ref<FormInstance>()
  const smsEnvConfig = ref<FormInstance>()
  const wxMpEnvConfig = ref<FormInstance>()
  const wxMaEnvConfig = ref<FormInstance>()

  const handleTabClick = () => {
    if (!['systemInfoConfig', 'fileEnvConfig', 'thirdpartyLoginConfig', 'faceEnvConfig'].includes(activeName.value)) {
      handleClick()
    }
  }
</script>

<style scoped>
  .el-input {
    width: 35%;
  }
</style>
