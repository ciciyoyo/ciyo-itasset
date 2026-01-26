import { ref } from 'vue'
import { getEnvConfig, saveEnvConfig } from '@/api/system/config'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'

export function useConfig() {
  const activeName = ref<string>('systemInfoConfig')
  const form = ref<Record<string, any>>({})
  const config = ref<Record<string, any>>({})

  async function handleClick() {
    const res = await getEnvConfig(activeName.value)
    if (res) {
      config.value = res
      form.value = res?.envValue
    } else {
      config.value = {}
      form.value = {}
    }
  }

  function handleSubmit(formRef: FormInstance | undefined) {
    if (!formRef) return
    formRef.validate((valid: boolean) => {
      if (valid) {
        config.value.envKey = activeName.value
        config.value.envValue = form.value
        saveEnvConfig(config.value).then(() => {
          ElMessage.success('保存成功')
        })
      } else {
        console.log('error submit!!')
        return false
      }
    })
  }

  return {
    activeName,
    form,
    config,
    handleClick,
    handleSubmit
  }
}
