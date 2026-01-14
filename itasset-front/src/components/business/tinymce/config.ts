/* eslint-disable max-len */

import {baseUrl, getStaticBaseUrl, getToken} from '@/utils/auth'

export const plugins =
    'preview importcss searchreplace autolink autosave save directionality code visualblocks visualchars fullscreen image link media codesample table charmap pagebreak nonbreaking anchor insertdatetime advlist lists wordcount help charmap quickbars emoticons accordion download mathformula insertinput '

export const toolbar =
    'undo redo | accordion accordionremove | blocks fontfamily fontsize | bold italic underline strikethrough | align numlist bullist | link image | table media | lineheight outdent indent| forecolor backcolor removeformat | charmap emoticons | code fullscreen preview | save print | pagebreak anchor codesample | ltr rtl'

// 内联模式包含的按钮
export const inlineToolbar =
    '  fontsize  bold italic underline strikethrough undo redo   removeformat alignleft aligncenter alignright  subscript superscript  hr forecolor backcolor image  '

const token = getToken()
const uploadUrl = `${baseUrl}/user/file/upload`

/**
 * 默认编辑器配置
 */
export const tinymceEditorConfig = {
    language: 'zh_CN',
    menubar: 'false',
    icons: 'tduck',
    skin: 'tduck',
    // skin_url: "/tinymce/skins/ui/tduck",
    // content_css: "/tinymce/skins/content/tduck",
    cache_suffix: '?v=0.1.2',
    plugins,
    // entity_encoding: "row", // 所有字符都将以非实体形式保存，避免出现部分符号变成 html 编码
    toolbar_mode: 'sliding',
    //div[*] 表示允许 <div> 标签以及所有的属性。
    extended_valid_elements: 'formpopup[*],dynamictext[*],formvariable[*]', // 明确指定自定义元素
    custom_elements: '~formpopup,~dynamictext,~formvariable', // ~ 表示不移除这些元素
    valid_elements: '*[*]',
    verify_html: false, // 关闭HTML验证，防止自定义元素被清理
    content_css: getStaticBaseUrl() + '/tinymce/skins/editor.css',
    allow_mathml_annotation_encodings: ['application/x-tex'],
    entity_encoding: 'raw',
    font_size_formats: '11px 12px 14px 16px 18px 24px 36px 48px',
    branding: false,
    // 此选项允许您打开/关闭图像、表格或媒体对象的大小调整手柄。默认情况下启用此选项，并允许您调整表格和图像的大小
    object_resizing: true,
    quickbars_insert_toolbar: false,
    quickbars_selection_toolbar: false,
    end_container_on_empty_block: true,
    powerpaste_word_import: 'clean',
    code_dialog_height: 450,
    code_dialog_width: 1000,
    default_link_target: '_blank',
    // // linebreak在所有情况下都会强制执行上述 Shift+Enter 行为 不插入p 不然换行没效果
    // 这个不能开启 开启了会导致有序无序只有第一行会有序号
    // newline_behavior: "linebreak",
    link_title: false,
    statusbar: false,
    convert_urls: false, // 关闭把文件的路径转换成相对的
    relative_urls: false,
    remove_script_host: false,
    paste_data_images: true,
    file_picker_types: 'media',
    license_key: 'gpl',
    protect: [],
    promotion: false, // 禁用推广
    katex: {
        cssUrl: getStaticBaseUrl() + '/katex/katex.min.css', // 自定义 CSS 地址
        jsUrl: getStaticBaseUrl() + '/katex/katex.min.js' // 自定义 JS 地址
    },
    // image_dimensions: false, // 禁用输入图片宽高
    // content_style: "img {max-width:100%;}",
    images_upload_handler: (blobInfo: any, progress: any) =>
        new Promise((resolve, reject) => {
            const xhr = new XMLHttpRequest()
            xhr.withCredentials = false
            xhr.open('POST', uploadUrl)
            xhr.setRequestHeader('Authorization', token)

            xhr.upload.onprogress = (e) => {
                progress((e.loaded / e.total) * 100)
            }
            xhr.onload = () => {
                if (xhr.status === 403) {
                    reject({message: 'HTTP Error: ' + xhr.status, remove: true})
                    return
                }
                if (xhr.status < 200 || xhr.status >= 300) {
                    reject('HTTP Error: ' + xhr.status)
                    return
                }

                const json = JSON.parse(xhr.responseText)

                if (!json || typeof json.data != 'string') {
                    reject('Invalid JSON: ' + xhr.responseText)
                    return
                }
                resolve(json.data)
            }

            xhr.onerror = () => {
                reject('上传失败' + xhr.status)
            }

            const formData = new FormData()
            formData.append('file', blobInfo.blob(), blobInfo.filename())

            xhr.send(formData)
        }),
    file_picker_callback(cb: any, value: any, meta: any) {
        //以下是原生上传文件
        let input = document.createElement('input')
        input.setAttribute('type', 'file')
        // 限制视屏文件
        input.setAttribute('accept', '.mp4, .webm, .ogg')

        input.click()
        input.onchange = function () {
            const xhr = new XMLHttpRequest()
            xhr.withCredentials = false
            xhr.open('POST', uploadUrl)
            xhr.setRequestHeader('Authorization', token)
            xhr.onload = function () {
                if (xhr.status !== 200) {
                    return
                }
                const json = JSON.parse(xhr.responseText)
                if (!json || typeof json.data !== 'string') {
                    return
                }
                cb(json.data, {text: json.data})
            }
            const formData = new FormData()
            const file = input.files[0]
            formData.append('file', file, file.name) // 此处与源文档不一样
            xhr.send(formData)
        }
    }
}
