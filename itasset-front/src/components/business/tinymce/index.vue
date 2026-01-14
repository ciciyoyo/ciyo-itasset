<template>
  <div>
    <textarea v-if="!inline" :id="tinymceId" style="visibility: hidden" />
    <!--    内联模式-->
    <div v-else :id="`${tinymceId}inline`" />
  </div>
</template>

<script setup>
  import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
  import { inlineToolbar, tinymceEditorConfig, toolbar as defaultToolbar } from './config'
  import loadTinymce from './loadTinymce'
  import { throttle, uniqueId } from 'lodash-es'

  defineOptions({
    name: 'FormTinymce'
  })

  const props = defineProps({
    id: {
      type: String,
      default: () => {
        return `tinymce-${uniqueId()}`
      }
    },
    value: {
      type: String,
      default: ''
    },
    toolbar: {
      type: [Array, String],
      default: () => []
    },
    // 内联模式
    inline: {
      type: Boolean,
      default: false
    },
    formItemId: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: ''
    },
    // 其他配置
    config: {
      type: Object,
      default: () => ({})
    },
    // 全屏编辑配置多出的toolbar ,分割
    fullEditToolbar: {
      type: String,
      default: ''
    }
  })

  const tinymceId = ref(props.id)

  const fullEditTinymceId = ref(`tinymce-${uniqueId()}`)

  const dialogVisible = ref(false)

  onMounted(() => {
    initDefaultTinymce()
  })

  const initDefaultTinymce = () => {
    let finalToolbar = []
    if (props.inline) {
      finalToolbar = props.toolbar && props.toolbar.length ? props.toolbar : inlineToolbar
    } else {
      finalToolbar = props.toolbar && props.toolbar.length ? props.toolbar : defaultToolbar
    }
    initTinymce(`#${props.id}${props.inline ? 'inline' : ''}`, props.inline, finalToolbar, props.config)
  }

  const initTinymce = (targetTinymceId, inline, customToolbar, tinymceConf) => {
    // eslint-disable-next-line global-require
    let conf = {
      ...tinymceEditorConfig,
      selector: targetTinymceId,
      inline: inline,
      toolbar: customToolbar,
      placeholder: props.placeholder
    }
    loadTinymce((tinymce) => {
      conf = Object.assign(conf, tinymceConf)
      conf.init_instance_callback = (editor) => {
        if (props.value) editor.setContent(props.value)
        initChangeWatch(editor)
        // 加载完成事件
        emits('load')
      }
      conf.setup = (editor) => {
        // 监听点击事件
        editor.on('click', (e) => {
          const target = e.target
          console.log(target)
          if (target.tagName === 'DYNAMICTEXT') {
            handleFormulaClick(target.getAttribute('id'), target.getAttribute('formula'))
          } else if (target.tagName === 'FORMPOPUP') {
            showPopupDialog(target.getAttribute('id'))
          }
        })
        // 全屏编辑
        editor.ui.registry.addToggleButton('fulledit', {
          icon: 'fulledit',
          tooltip: '全屏编辑',
          onAction: function () {
            editorInstance.destroy()
            editorInstance = null
            dialogVisible.value = true
            nextTick(() => {
              initTinymce(`#${fullEditTinymceId.value}`, false, defaultToolbar + ' ' + props.fullEditToolbar, {
                height: 900,
                ...props.config
              })
            })
          }
        })
      }
      tinymce.init(conf)
    })
    // 监听页面滚动事件
    window.addEventListener(
      'scroll',
      throttle(() => {
        if (props.inline) {
          if (editorInstance) {
            editorInstance?.getElement()?.blur()
          }
        }
      }, 100),
      true
    )
  }

  onUnmounted(() => {
    if (!editorInstance) return
    window.addEventListener('scroll', () => {}, true)
    editorInstance.destroy()
  })

  const handleInsertContent = (content) => {
    editorInstance.insertContent(content)
  }

  const handleSetContent = (content) => {
    editorInstance.setContent(content)
    // emits("update:value", content);
    // emits("change", content);
  }

  const emits = defineEmits(['update:value', 'blur', 'change', 'load'])

  let editorInstance = null

  const initChangeWatch = (editor) => {
    editorInstance = editor

    editor.on('change keyup undo redo', () => {
      console.log('tinymce change')
      editor.save()
      emits('update:value', editor.getContent())
      emits('change', editor.getContent())
    })

    editor.on('blur', () => {
      emits('blur')
    })
  }

  // 可以直接侦听一个 ref
  watch(
    () => props.value,
    async (val, prevVal) => {
      if (editorInstance && val !== prevVal && val !== editorInstance.getContent()) {
        editorInstance.setContent(val || '')
      }
    },
    {
      immediate: true
    }
  )

  const handleCloseDialog = () => {
    editorInstance.destroy()
    editorInstance = null
    dialogVisible.value = false
    nextTick(() => {
      initDefaultTinymce()
    })
  }

  defineExpose({
    handleInsertContent
  })
</script>
<style>
  .tox-tinymce-aux {
    z-index: 99999 !important;
  }

  .tox-tinymce-inline {
    z-index: 99999 !important;
  }

  .tinymce.ui.FloatPanel {
    z-index: 99;
  }

  [contenteditable] {
    outline: none;
    border: 1px solid transparent;
  }
</style>
