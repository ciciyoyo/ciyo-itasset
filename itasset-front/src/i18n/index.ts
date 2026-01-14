/**
 * 国际化配置
 *
 * 基于 vue-i18n 实现的多语言国际化解决方案。
 * 支持中文和英文切换，自动从本地存储恢复用户的语言偏好。
 *
 * ## 主要功能
 *
 * - 多语言支持 - 支持中文（简体）和英文两种语言
 * - 语言切换 - 运行时动态切换语言，无需刷新页面
 * - 持久化存储 - 自动保存和恢复用户的语言偏好
 * - 全局注入 - 在任何组件中都可以使用 $t 函数进行翻译
 * - 类型安全 - 提供 TypeScript 类型支持
 *
 * ## 支持的语言
 *
 * - zh: 简体中文
 * - en: English
 *
 * @module locales
 * @author Art Design Pro Team
 */

import type {I18nOptions} from 'vue-i18n'
import {createI18n} from 'vue-i18n'
import {LanguageEnum} from '@/enums/appEnum'
import {getSystemStorage} from '@/utils/storage'
import {StorageKeyManager} from '@/utils/storage/storage-key-manager'

import {App} from 'vue'

const modules = import.meta.glob('./langs/*.ts')

/**
 * 存储键管理器实例
 */
const storageKeyManager = new StorageKeyManager()

/**
 * 语言选项列表
 * 用于语言切换下拉框
 */
export const languageOptions = [
    {value: LanguageEnum.ZH, label: '简体中文'},
    {value: LanguageEnum.EN, label: 'English'}
]

/**
 * 从存储中获取语言设置
 * @returns 语言设置，如果获取失败则返回默认语言
 */
const getDefaultLanguage = (): LanguageEnum => {
    // 尝试从版本化的存储中获取语言设置
    try {
        const storageKey = storageKeyManager.getStorageKey('user')
        const userStore = localStorage.getItem(storageKey)

        if (userStore) {
            const {language} = JSON.parse(userStore)
            if (language && Object.values(LanguageEnum).includes(language)) {
                return language
            }
        }
    } catch (error) {
        console.warn('[i18n] 从版本化存储获取语言设置失败:', error)
    }

    // 尝试从系统存储中获取语言设置
    try {
        const sys = getSystemStorage()
        if (sys) {
            const {user} = JSON.parse(sys)
            if (user?.language && Object.values(LanguageEnum).includes(user.language)) {
                return user.language
            }
        }
    } catch (error) {
        console.warn('[i18n] 从系统存储获取语言设置失败:', error)
    }

    // 返回默认语言
    console.debug('[i18n] 使用默认语言:', LanguageEnum.ZH)
    return LanguageEnum.ZH
}

export const changeLocale = async (locale: string) => {
    try {
        const loader = modules[`./langs/${locale}.ts`]
        if (!loader) return
        const langModule = ((await loader()) as any).default
        if (!langModule) return
        const {message} = langModule
        const globalI18n = i18n.global as any
        globalI18n.setLocaleMessage(locale, message)
        ;(i18n.global.locale as any).value = locale
    } catch (e) {
        console.error('[i18n] 切换语言失败:', e)
    }
}

async function createI18nOptions(): Promise<I18nOptions> {
    const locale = getDefaultLanguage() || 'zh-cn'
    const loader = modules[`./langs/${locale}.ts`]
    let message = {}
    if (loader) {
        const defaultLocal = (await loader()) as any
        message = defaultLocal.default?.message ?? {}
    }

    console.log(message)

    return {
        legacy: false,
        locale,
        fallbackLocale: LanguageEnum.ZH,
        messages: {
            [locale]: message
        },
        sync: true, //If you don’t want to inherit locale from global scope, you need to set sync of i18n component option to false.
        silentTranslationWarn: true, // true - warning off
        warnHtmlMessage: false, // 关闭html警告
        missingWarn: false,
        silentFallbackWarn: true
    }
}

/**
 * i18n 实例
 */
export let i18n: ReturnType<typeof createI18n>

// setup i18n instance with glob
export async function setupI18n(app: App) {
    const options = await createI18nOptions()
    i18n = createI18n(options)
    app.use(i18n)
}
