<template>
  <el-form ref="systemInfoConfig" :model="form" label-width="168px" v-if="!loading">
    <el-form-item
      :label="$t('system.systemConfig.version')"
      prop="name"
      :rules="[{ required: true, message: $t('system.systemConfig.enterSystemName'), trigger: 'blur' }]"
    >
      <el-tag size="default" type="success" effect="dark"> V{{ appVersion }} </el-tag>
    </el-form-item>
    <el-form-item
      :label="$t('system.systemConfig.systemName')"
      prop="name"
      :rules="[{ required: true, message: $t('system.systemConfig.enterSystemName'), trigger: 'blur' }]"
    >
      <el-input style="width: 400px" v-model="form.name" />
    </el-form-item>
    <el-form-item
      :label="$t('system.systemConfig.uploadLogo')"
      prop="logoImg"
      :rules="[{ required: true, message: $t('system.systemConfig.enterLogo'), trigger: 'blur' }]"
    >
      <div>
        <div>
          <image-upload v-model:value="form.logoImg" :limit="1" />
        </div>
        <div class="el-upload__tip">{{ $t('system.systemConfig.uploadLogoDesc') }}</div>
      </div>
    </el-form-item>
    <el-form-item
      :label="$t('system.systemConfig.loginBackground')"
      prop="backgroundImage"
      :rules="[{ required: false, message: $t('system.systemConfig.uploadLoginBackground'), trigger: 'blur' }]"
    >
      <image-upload v-model:value="form.backgroundImage" :limit="1" />
    </el-form-item>
    <el-form-item
      :label="$t('system.systemConfig.domain')"
      prop="webBaseUrl"
      :rules="[{ required: true, message: $t('system.systemConfig.enterDomain'), trigger: 'blur' }]"
    >
      <el-input style="width: 400px" v-model="form.webBaseUrl" placeholder="https://pro.xxxx.com/" />
    </el-form-item>
    <el-form-item
      :label="$t('system.systemConfig.copyright')"
      prop="copyright"
      :rules="[{ required: true, message: $t('system.systemConfig.enterCopyright'), trigger: 'blur' }]"
    >
      <tinymce
        v-model:value="form.copyright"
        :height="192"
        toolbar="undo redo | blocks fontsize | bold italic underline strikethrough | link image media table mergetags | addcomment showcomments | spellcheckdialog a11ycheck typography | align lineheight | checklist numlist bullist indent outdent | emoticons charmap | removeformat | forecolor | backcolor "
        placeholder="Copyright © 2018-2024 tduckcloud.com All Rights Reserved."
      />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="handleSubmit">
        {{ $t('system.systemConfig.save') }}
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { useConfig } from './useConfig'
  import Tinymce from '@/components/business/tinymce/index.vue'
  import ImageUpload from '@/components/business/image-upload/index.vue'
  import type { FormInstance } from 'element-plus'

  const { t } = useI18n()
  const { form, handleClick, handleSubmit: submitForm } = useConfig()

  const loading = ref(true)
  const systemInfoConfig = ref<FormInstance>()

  const thirdPartyLoginTypeList = computed(() => [
    {
      label: t('system.systemConfig.wechat'),
      value: 'wechatOpen'
    },
    {
      label: t('system.systemConfig.enterpriseWeChat'),
      value: 'wechatEnterprise'
    },
    {
      label: t('system.otherLogin.dingtalk'),
      value: 'dingTalk'
    },
    {
      label: t('system.otherLogin.feishu'),
      value: 'lark'
    }
  ])

  const appVersion = computed(() => __APP_VERSION__)

  const handleSubmit = () => {
    submitForm(systemInfoConfig.value)
  }

  onMounted(async () => {
    await handleClick()
    // Check if thirdPartyLoginTypeList exists, if not add it (logic from previous version)
    if (!form.value.thirdPartyLoginTypeList) {
      form.value['thirdPartyLoginTypeList'] = []
      form.value['allowThirdPartyLogin'] = false
    }
    loading.value = false
  })
</script>
