import {defineStore} from 'pinia'
import {ref} from 'vue'
import {getSystemInfoConfig} from '@/api/system/config'
import {SystemBasicConfig} from '@/types/config'

export const useSystemStore = defineStore(
    'systemStore',
    () => {
        const systemInfo = ref<SystemBasicConfig>({
            name: '',
            description: '',
            logoImg: '',
            favicon: '',
            copyright: '',
            backgroundImage: ''
        })

        const initSystemConfig = async () => {
            try {
                const res: any = await getSystemInfoConfig()
                if (typeof res === 'string') {
                    systemInfo.value = JSON.parse(decodeURIComponent(escape(atob(res))))
                } else {
                    systemInfo.value = res
                }
            } catch (error) {
                console.error('Failed to fetch system config:', error)
            }
        }

        return {
            systemInfo,
            initSystemConfig
        }
    },
    {
        persist: {
            key: 'systemInfoConfig',
            storage: sessionStorage
        }
    }
)
