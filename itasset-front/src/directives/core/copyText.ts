import type {App, DirectiveBinding} from 'vue'
import {ElMessage} from 'element-plus'

interface ElType extends HTMLElement {
    copyData: string | number
    __handleClick__: any
}

export function setupCopyTextDirective(app: App) {
    app.directive('copyText', {
        mounted(el: ElType, binding: DirectiveBinding) {
            el.copyData = binding.value
            el.addEventListener('click', handleClick)
        },
        updated(el: ElType, binding: DirectiveBinding) {
            el.copyData = binding.value
        },
        beforeUnmount(el: ElType) {
            el.removeEventListener('click', el.__handleClick__)
        }
    })
}

function handleClick(this: any) {
    const input = document.createElement('input')
    input.value = this.copyData.toLocaleString()
    document.body.appendChild(input)
    input.select()
    document.execCommand('Copy')
    document.body.removeChild(input)
    //避免点的太快 提示太多
    setTimeout(() => {
        ElMessage({
            type: 'success',
            message: '复制成功'
        })
    }, 500)
}
