import request from '@/utils/http'

export function importTemplateDownlaod(code: string) {
  return request.get<Blob>({
    url: `/common/excel/template/${code}?examples=1`,
    responseType: 'blob'
  })
}
