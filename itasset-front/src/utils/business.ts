import { MessageUtil } from '@/utils/messageUtil'

// 日期格式化
export function parseTime(time: any, pattern?: string): any {
  if (arguments.length === 0 || !time) {
    return null
  }
  const format = pattern || '{y}-{m}-{d} {h}:{i}:{s}'
  let date
  if (typeof time === 'object') {
    date = time
  } else {
    if (typeof time === 'string' && /^[0-9]+$/.test(time)) {
      time = parseInt(time)
    } else if (typeof time === 'string') {
      time = time.replace(new RegExp(/-/gm), '/')
    }
    if (typeof time === 'number' && time.toString().length === 10) {
      time = time * 1000
    }
    date = new Date(time)
  }
  const formatObj: any = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay()
  }
  const time_str = format.replace(/{(y|m|d|h|i|s|a)+}/g, (result, key) => {
    let value = formatObj[key]
    // Note: getDay() returns 0 on Sunday
    if (key === 'a') {
      return ['日', '一', '二', '三', '四', '五', '六'][value]
    }
    if (result.length > 0 && value < 10) {
      value = '0' + value
    }
    return value || 0
  })
  return time_str
}

// 表单重置
export function resetForm(refName: string): void {
  // @ts-ignore
  if (this.$refs[refName]) {
    // @ts-ignore
    this.$refs[refName].resetFields()
  }
}

/**
 * 表单重置
 * @param formEl
 */
export const resetFormRef = (formEl: any) => {
  if (!formEl || !formEl.value) return
  formEl.value!.resetFields()
}

// 添加日期范围
export function addDateRange(params: any, dateRange: any[], propName?: string): any {
  let search = params
  if (!search.params) {
    search.params = {}
  }
  if (dateRange != null) {
    if (typeof propName === 'undefined') {
      search.params['beginTime'] = dateRange[0]
      search.params['endTime'] = dateRange[1]
    } else {
      search.params['begin' + propName] = dateRange[0]
      search.params['end' + propName] = dateRange[1]
    }
  } else if (Object.keys(search.params)) {
    if (typeof propName === 'undefined') {
      delete search.params['beginTime']
      delete search.params['endTime']
    } else {
      delete search.params['begin' + propName]
      delete search.params['end' + propName]
    }
  }
  return search
}

// 回显数据字典
export function selectDictLabel(datas: any, value: string): string {
  let actions: string[] = []
  Object.keys(datas).some((key) => {
    if (datas[key].dictValue === '' + value) {
      actions.push(datas[key].dictLabel)
      return true
    }
  })
  return actions.join('')
}

// 回显数据字典（字符串数组）
export function selectDictLabels(datas: any, value: string, separator?: string): string {
  let actions: string[] = []
  let currentSeparator = separator === undefined ? ',' : separator
  let temp: any = value.split(currentSeparator)
  Object.keys(value.split(currentSeparator)).some((val) => {
    Object.keys(datas).some((key) => {
      if (datas[key].dictValue === '' + temp[val]) {
        actions.push(datas[key].dictLabel + currentSeparator)
      }
    })
  })
  return actions.join('').substring(0, actions.join('').length - 1)
}

// 通用下载方法
export function download(data: any, fileName: string, type: string = '.xlsx'): void {
  if (!data) {
    MessageUtil.warning('文件下载失败')
    return
  }
  // @ts-ignore
  if (typeof window.navigator.msSaveBlob !== 'undefined') {
    // @ts-ignore
    window.navigator.msSaveBlob(
      new Blob([data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
      fileName + type
    )
  } else {
    let url = window.URL.createObjectURL(
      new Blob([data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    )
    let link = document.createElement('a')
    link.style.display = 'none'
    link.href = url
    link.setAttribute('download', fileName + type)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link) // 下载完成移除元素
    window.URL.revokeObjectURL(url) // 释放掉blob对象
  }
}

// 字符串格式化(%s )
export function sprintf(str: string, ...args: any[]): string {
  let flag = true,
    i = 0
  str = str.replace(/%s/g, function () {
    let arg = args[i++]
    if (typeof arg === 'undefined') {
      flag = false
      return ''
    }
    return arg
  })
  return flag ? str : ''
}

// 转换字符串，undefined,null等转化为""
export function praseStrEmpty(str: any): string {
  if (!str || str == 'undefined' || str == 'null') {
    return ''
  }
  return str
}

/**
 * 构造树型结构数据
 * @param {*} data 数据源
 * @param {*} id id字段 默认 'id'
 * @param {*} parentId 父节点字段 默认 'parentId'
 * @param {*} children 孩子节点字段 默认 'children'
 */
export function handleTree(data: any[], id?: string, parentId?: string, children?: string): any[] {
  let config = {
    id: id || 'id',
    parentId: parentId || 'parentId',
    childrenList: children || 'children'
  }

  let childrenListMap: { [key: string]: any[] } = {}
  let nodeIds: { [key: string]: any } = {}
  let tree: any[] = []

  for (let d of data) {
    let parentId = d[config.parentId]
    if (childrenListMap[parentId] == null) {
      childrenListMap[parentId] = []
    }
    nodeIds[d[config.id]] = d
    childrenListMap[parentId].push(d)
  }

  for (let d of data) {
    let parentId = d[config.parentId]
    if (nodeIds[parentId] == null) {
      tree.push(d)
    }
  }

  for (let t of tree) {
    adaptToChildrenList(t)
  }

  function adaptToChildrenList(o: any) {
    if (childrenListMap[o[config.id]] !== null) {
      o[config.childrenList] = childrenListMap[o[config.id]]
    }
    if (o[config.childrenList]) {
      for (let c of o[config.childrenList]) {
        adaptToChildrenList(c)
      }
    }
  }

  return tree
}

/**
 * 格式化字符串
 * @param template - 包含占位符的模板字符串
 * @param values - 用于替换占位符的值
 * @returns 替换后的字符串
 */
export function formatString(template: string, ...values: any[]): string {
  console.log(template)
  return template.replace(/{(\d+)}/g, (match: string, index: string) => {
    // 返回对应的值，如果没有提供则返回原始占位符
    console.log(values)
    const numIndex = parseInt(index, 10)
    return typeof values[numIndex] !== 'undefined' ? values[numIndex] : match
  })
}

/**
 * 将 base64 编码的图片数据转换为 Blob 对象
 * @param code - base64 编码的图片数据，格式如 "data:image/png;base64,xxx"
 * @returns Blob 对象
 */
export function base64ToBlob(code: string): Blob {
  const parts = code.split(';base64,')
  const contentType = parts[0].split(':')[1]
  const raw = window.atob(parts[1])
  const rawLength = raw.length
  const uInt8Array = new Uint8Array(rawLength)
  for (let i = 0; i < rawLength; ++i) {
    uInt8Array[i] = raw.charCodeAt(i)
  }
  return new Blob([uInt8Array], { type: contentType })
}

/**
 * 下载图片文件
 * @param fileName - 下载的文件名
 * @param content - 图片内容，可以是 base64 编码的数据URL 或者 blob URL
 */
export function downloadImage(fileName: string, content: string): void {
  const aLink = document.createElement('a')
  let blob: Blob

  // 判断是否为 base64 编码的数据
  if (content.startsWith('data:')) {
    blob = base64ToBlob(content)
  } else {
    // 如果是 URL，直接使用
    aLink.href = content
    aLink.download = fileName
    aLink.style.display = 'none'
    document.body.appendChild(aLink)
    aLink.click()
    document.body.removeChild(aLink)
    return
  }

  const evt = document.createEvent('HTMLEvents')
  evt.initEvent('click', true, true)
  aLink.download = fileName
  aLink.href = URL.createObjectURL(blob)
  aLink.style.display = 'none'
  document.body.appendChild(aLink)
  aLink.click()
  document.body.removeChild(aLink)
  // 释放 blob URL
  URL.revokeObjectURL(aLink.href)
}

export function downloadPdf(data: any, fileName: string): void {
  if (!data) {
    MessageUtil.warning('文件下载失败')
    return
  }
  const mimeType = 'application/pdf'
  // @ts-ignore
  if (typeof window.navigator.msSaveBlob !== 'undefined') {
    // @ts-ignore
    window.navigator.msSaveBlob(new Blob([data], { type: mimeType }), fileName)
  } else {
    let url = window.URL.createObjectURL(new Blob([data], { type: mimeType }))
    let link = document.createElement('a')
    link.style.display = 'none'
    link.href = url
    link.setAttribute('download', fileName)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  }
}
