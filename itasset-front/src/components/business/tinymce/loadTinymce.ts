import loadScript from './loadScript'
import { ElLoading } from 'element-plus'
import { getStaticBaseUrl } from '@/utils/auth'

let tinymceObj: any

export default function loadTinymce(cb: Function) {
  const tinymceUrl = getStaticBaseUrl() + '/tinymce/tinymce.min.js'

  if (tinymceObj) {
    cb(tinymceObj)
    return
  }

  const loading = ElLoading.service({
    fullscreen: true,
    lock: true,
    text: '资源加载中...',
    background: 'rgba(255, 255, 255, 0.5)'
  })

  loadScript(tinymceUrl, () => {
    loading.close()
    // @ts-ignore
    tinymceObj = tinymce
    cb(tinymceObj)
  })
}
