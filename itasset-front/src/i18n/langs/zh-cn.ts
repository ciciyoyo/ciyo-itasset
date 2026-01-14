import {genMessage} from '../helper'
import zhcnLocale from 'element-plus/es/locale/lang/zh-cn'

const modules = import.meta.glob('./zh-cn/**/*.json', {eager: true})
export default {
    message: {
        ...genMessage(modules as any, 'zh-cn'),
        ...zhcnLocale
    }
}

