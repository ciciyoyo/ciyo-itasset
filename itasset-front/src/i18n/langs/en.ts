import { genMessage } from '../helper'
import enLocale from 'element-plus/es/locale/lang/en'

const modules = import.meta.glob('./en/**/*.json', { eager: true })
export default {
  message: {
    ...genMessage(modules as any, 'en'),
    ...enLocale
  },
  dateLocale: null,
  dateLocaleName: 'en'
}
