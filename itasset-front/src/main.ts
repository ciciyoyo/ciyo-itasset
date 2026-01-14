import App from './App.vue'
import {createApp} from 'vue'
import {initStore} from './store' // Store
import {initRouter} from './router' // Router
import {setupI18n} from './i18n' // 国际化
import 'virtual:uno.css'
import '@styles/core/unocss.css' // unocss
import '@styles/index.scss' // 样式
import '@utils/sys/console.ts' // 控制台输出内容
import '@utils/ui/iconify-loader' // 离线图标加载
import {setupGlobDirectives} from './directives'
import {setupErrorHandle} from '@utils/sys/error-handle'

import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import {useSystemStore} from '@/store/modules/system'


document.addEventListener(
    'touchstart',
    function () {
    },
    {passive: false}
)


async function bootstrap() {
    const app = createApp(App)
    await setupI18n(app)
    initStore(app)
// 初始化系统配置
    const systemStore = useSystemStore()
    await systemStore.initSystemConfig()

    initRouter(app)
    setupGlobDirectives(app)
    setupErrorHandle(app)

    const icons = ElementPlusIconsVue as any;
    for (const i in icons) {
        app.component(`ele-${icons[i].name}`, icons[i]);
    }
    app.mount('#app')
}

bootstrap();
