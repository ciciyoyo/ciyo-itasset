interface ScriptCallback {
  (error: Error | null, script?: HTMLScriptElement): void
}

const callbacks: { [src: string]: ScriptCallback[] } = {}

/**
 * 加载一个远程脚本
 * @param {String} src 一个远程脚本
 * @param {Function} callback 回调
 */
function loadScript(src: string, callback?: ScriptCallback): void {
  const existingScript = document.getElementById(src)
  const cb: ScriptCallback = callback || (() => {})

  if (!existingScript) {
    callbacks[src] = []
    const $script = document.createElement('script')
    $script.src = src
    $script.id = src
    $script.async = true
    document.body.appendChild($script)

    const onEnd = 'onload' in $script ? stdOnEnd.bind($script) : ieOnEnd.bind($script)
    onEnd($script)
  }

  callbacks[src].push(cb)

  function stdOnEnd(this: HTMLScriptElement, script: HTMLScriptElement): void {
    script.onload = () => {
      this.onerror = this.onload = null
      callbacks[src].forEach((item: ScriptCallback) => {
        item(null, script)
      })
      delete callbacks[src]
    }

    script.onerror = () => {
      this.onerror = this.onload = null
      cb(new Error(`Failed to load ${src}`), script)
    }
  }

  function ieOnEnd(this: HTMLScriptElement, script: HTMLScriptElement): void {
    // @ts-ignore
    script.onreadystatechange = () => {
      // @ts-ignore
      if (this.readyState !== 'complete' && this.readyState !== 'loaded') return
      // @ts-ignore
      this.onreadystatechange = null
      callbacks[src].forEach((item: ScriptCallback) => {
        item(null, script)
      })
      delete callbacks[src]
    }
  }
}

/**
 * 顺序加载一组远程脚本
 * @param {Array} list 一组远程脚本
 * @param {Function} cb 回调
 */
export function loadScriptQueue(list: string[], cb: ScriptCallback): void {
  const first: string = list.shift() || ''
  if (first) {
    loadScript(first, () => loadScriptQueue(list, cb))
  } else {
    loadScript(first, cb)
  }
}

export default loadScript
