<template>
  <div class="iconfont-container">
    <div class="icon-body">
      <el-input
        v-model="name"
        style="position: relative"
        clearable
        :placeholder="$t('system.menu.enterIconName')"
        @input="filterIcons"
      >
        <template #suffix>
          <el-icon class="el-input__icon">
            <ele-Search />
          </el-icon>
        </template>
      </el-input>
      <div class="icon-list">
        <div v-for="(v, index) in tempIconList" :key="index" @click="selectedIcon(v)" class="icon-item">
          <ArtSvgIcon :icon="v" class="icon-display" />
          <span class="icon-name">{{ v }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="IocnfontSelect">
  import { onMounted, ref } from 'vue'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'

  const name = ref('')
  const iconList = ref([])
  const tempIconList = ref([])

  // Remix Icon 图标列表 (项目使用的图标格式) - 350+个常用图标
  const icons = [
    // 系统图标
    'ri:home-line',
    'ri:home-fill',
    'ri:settings-line',
    'ri:settings-fill',
    'ri:settings-2-line',
    'ri:settings-3-line',
    'ri:menu-line',
    'ri:menu-fill',
    'ri:dashboard-line',
    'ri:dashboard-fill',
    'ri:apps-line',
    'ri:apps-fill',
    'ri:function-line',
    'ri:layout-line',
    'ri:layout-grid-line',

    // 用户图标
    'ri:user-line',
    'ri:user-fill',
    'ri:user-3-line',
    'ri:user-3-fill',
    'ri:user-add-line',
    'ri:user-settings-line',
    'ri:team-line',
    'ri:team-fill',
    'ri:group-line',
    'ri:group-fill',
    'ri:admin-line',
    'ri:admin-fill',
    'ri:account-circle-line',
    'ri:contacts-line',

    // 文件图标
    'ri:file-line',
    'ri:file-fill',
    'ri:file-list-line',
    'ri:file-list-fill',
    'ri:file-text-line',
    'ri:file-copy-line',
    'ri:file-add-line',
    'ri:folder-line',
    'ri:folder-fill',
    'ri:folder-open-line',
    'ri:folder-open-fill',
    'ri:folder-add-line',
    'ri:save-line',
    'ri:save-fill',
    'ri:draft-line',

    // 编辑图标
    'ri:edit-line',
    'ri:edit-fill',
    'ri:edit-2-line',
    'ri:edit-box-line',
    'ri:pencil-line',
    'ri:pencil-fill',
    'ri:delete-bin-line',
    'ri:delete-bin-fill',
    'ri:delete-bin-4-line',
    'ri:delete-bin-5-line',
    'ri:eraser-line',
    'ri:mark-pen-line',
    'ri:ball-pen-line',

    // 操作图标
    'ri:add-line',
    'ri:add-fill',
    'ri:add-circle-line',
    'ri:add-circle-fill',
    'ri:subtract-line',
    'ri:close-line',
    'ri:close-fill',
    'ri:close-circle-line',
    'ri:close-circle-fill',
    'ri:check-line',
    'ri:check-fill',
    'ri:checkbox-line',
    'ri:checkbox-circle-line',
    'ri:search-line',
    'ri:search-fill',
    'ri:search-eye-line',
    'ri:refresh-line',
    'ri:refresh-fill',
    'ri:loop-left-line',
    'ri:loop-right-line',
    'ri:restart-line',

    // 箭头图标
    'ri:arrow-up-line',
    'ri:arrow-down-line',
    'ri:arrow-left-line',
    'ri:arrow-right-line',
    'ri:arrow-up-s-line',
    'ri:arrow-down-s-line',
    'ri:arrow-left-s-line',
    'ri:arrow-right-s-line',
    'ri:arrow-drop-down-line',
    'ri:arrow-drop-up-line',
    'ri:expand-up-down-line',
    'ri:corner-down-left-line',
    'ri:corner-down-right-line',
    'ri:arrow-go-back-line',
    'ri:arrow-go-forward-line',

    // 导航图标
    'ri:more-line',
    'ri:more-fill',
    'ri:more-2-line',
    'ri:more-2-fill',
    'ri:filter-line',
    'ri:filter-fill',
    'ri:list-check',
    'ri:list-unordered',
    'ri:list-ordered',
    'ri:menu-2-line',
    'ri:menu-3-line',
    'ri:menu-4-line',
    'ri:grid-line',
    'ri:layout-masonry-line',

    // 状态图标
    'ri:error-warning-line',
    'ri:error-warning-fill',
    'ri:information-line',
    'ri:information-fill',
    'ri:check-circle-line',
    'ri:check-circle-fill',
    'ri:close-circle-line',
    'ri:close-circle-fill',
    'ri:question-line',
    'ri:question-fill',
    'ri:alert-line',
    'ri:alert-fill',
    'ri:checkbox-blank-circle-line',
    'ri:checkbox-circle-fill',
    'ri:radio-button-line',

    // 安全图标
    'ri:lock-line',
    'ri:lock-fill',
    'ri:lock-unlock-line',
    'ri:lock-unlock-fill',
    'ri:key-line',
    'ri:key-fill',
    'ri:shield-line',
    'ri:shield-fill',
    'ri:shield-check-line',
    'ri:eye-line',
    'ri:eye-off-line',
    'ri:fingerprint-line',
    'ri:scan-line',

    // 通讯图标
    'ri:mail-line',
    'ri:mail-fill',
    'ri:mail-send-line',
    'ri:message-line',
    'ri:message-fill',
    'ri:chat-1-line',
    'ri:chat-3-line',
    'ri:phone-line',
    'ri:phone-fill',
    'ri:notification-line',
    'ri:notification-fill',
    'ri:notification-badge-line',
    'ri:discuss-line',
    'ri:question-answer-line',

    // 图表图标
    'ri:pie-chart-line',
    'ri:pie-chart-fill',
    'ri:bar-chart-line',
    'ri:bar-chart-fill',
    'ri:bar-chart-2-line',
    'ri:line-chart-line',
    'ri:line-chart-fill',
    'ri:donut-chart-line',
    'ri:stock-line',
    'ri:progress-1-line',
    'ri:progress-2-line',
    'ri:progress-3-line',
    'ri:fund-line',

    // 工具图标
    'ri:tools-line',
    'ri:tools-fill',
    'ri:hammer-line',
    'ri:hammer-fill',
    'ri:bug-line',
    'ri:bug-fill',
    'ri:code-line',
    'ri:code-box-line',
    'ri:terminal-line',
    'ri:command-line',
    'ri:braces-line',
    'ri:palette-line',

    // 媒体图标
    'ri:image-line',
    'ri:image-fill',
    'ri:image-add-line',
    'ri:gallery-line',
    'ri:video-line',
    'ri:video-fill',
    'ri:film-line',
    'ri:movie-line',
    'ri:music-line',
    'ri:music-fill',
    'ri:headphone-line',
    'ri:play-line',
    'ri:play-fill',
    'ri:pause-line',
    'ri:stop-line',
    'ri:volume-up-line',

    // 商务图标
    'ri:shopping-cart-line',
    'ri:shopping-cart-fill',
    'ri:shopping-bag-line',
    'ri:store-line',
    'ri:store-fill',
    'ri:money-dollar-circle-line',
    'ri:money-dollar-circle-fill',
    'ri:bank-card-line',
    'ri:bank-card-fill',
    'ri:wallet-line',
    'ri:wallet-fill',
    'ri:coupon-line',
    'ri:price-tag-line',
    'ri:coin-line',

    // 办公图标
    'ri:briefcase-line',
    'ri:briefcase-fill',
    'ri:book-line',
    'ri:book-fill',
    'ri:book-open-line',
    'ri:book-read-line',
    'ri:newspaper-line',
    'ri:pencil-ruler-line',
    'ri:printer-line',
    'ri:scanner-line',
    'ri:archive-line',
    'ri:inbox-line',

    // 设备图标
    'ri:computer-line',
    'ri:smartphone-line',
    'ri:tablet-line',
    'ri:keyboard-line',
    'ri:mouse-line',
    'ri:database-line',
    'ri:database-fill',
    'ri:server-line',
    'ri:hard-drive-line',
    'ri:cpu-line',
    'ri:device-line',

    // 网络图标
    'ri:global-line',
    'ri:global-fill',
    'ri:wifi-line',
    'ri:wifi-fill',
    'ri:signal-wifi-line',
    'ri:cloud-line',
    'ri:cloud-fill',
    'ri:download-cloud-line',
    'ri:upload-cloud-line',
    'ri:broadcast-line',
    'ri:radar-line',

    // 地图位置
    'ri:map-line',
    'ri:map-fill',
    'ri:map-pin-line',
    'ri:map-pin-fill',
    'ri:map-pin-2-line',
    'ri:navigation-line',
    'ri:compass-line',
    'ri:route-line',
    'ri:direction-line',
    'ri:car-line',

    // 时间日期
    'ri:calendar-line',
    'ri:calendar-fill',
    'ri:calendar-event-line',
    'ri:calendar-check-line',
    'ri:time-line',
    'ri:time-fill',
    'ri:timer-line',
    'ri:hourglass-line',
    'ri:history-line',
    'ri:alarm-line',
    'ri:countdown-line',

    // 天气图标
    'ri:sun-line',
    'ri:sun-fill',
    'ri:moon-line',
    'ri:moon-fill',
    'ri:cloudy-line',
    'ri:rainy-line',
    'ri:snowy-line',
    'ri:thunderstorms-line',
    'ri:windy-line',
    'ri:temp-hot-line',

    // 表情图标
    'ri:emotion-line',
    'ri:emotion-fill',
    'ri:emotion-happy-line',
    'ri:emotion-sad-line',
    'ri:emotion-laugh-line',
    'ri:thumb-up-line',
    'ri:thumb-down-line',
    'ri:hand-heart-line',

    // 其他常用图标
    'ri:star-line',
    'ri:star-fill',
    'ri:star-half-line',
    'ri:heart-line',
    'ri:heart-fill',
    'ri:gift-line',
    'ri:gift-fill',
    'ri:link',
    'ri:link-m',
    'ri:link-unlink',
    'ri:external-link-line',
    'ri:external-link-fill',
    'ri:download-line',
    'ri:download-fill',
    'ri:upload-line',
    'ri:upload-fill',
    'ri:share-line',
    'ri:share-fill',
    'ri:fire-line',
    'ri:fire-fill',
    'ri:flashlight-line',
    'ri:lightbulb-line',
    'ri:lightbulb-fill',
    'ri:trophy-line',
    'ri:award-line',
    'ri:medal-line',
    'ri:flag-line',
    'ri:bookmark-line',

    // 社交图标
    'ri:wechat-line',
    'ri:wechat-fill',
    'ri:qq-line',
    'ri:qq-fill',
    'ri:github-line',
    'ri:github-fill',
    'ri:twitter-line',
    'ri:twitter-fill',
    'ri:facebook-line',
    'ri:linkedin-line',
    'ri:dribbble-line',
    'ri:dribbble-fill',
    'ri:instagram-line',
    'ri:youtube-line'
  ]

  onMounted(() => {
    iconList.value = icons
    tempIconList.value = icons
  })

  function filterIcons() {
    tempIconList.value = iconList.value.filter((item) => item.includes(name.value))
  }

  // 定义props
  const emit = defineEmits(['selected'])

  const selectedIcon = (iconName) => {
    emit('selected', iconName)
    document.body.click()
  }

  const reset = () => {
    name.value = ''
    tempIconList.value = iconList.value
  }

  defineExpose({
    reset
  })
</script>

<style lang="scss" scoped>
  .iconfont-container {
    .icon-body {
      width: 100%;
      padding: 10px;

      .icon-list {
        height: 300px;
        overflow-y: scroll;
        display: flex;
        flex-wrap: wrap;
        gap: 8px;

        .icon-item {
          width: calc(33.33% - 6px);
          height: 70px;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          padding: 8px;
          border-radius: 4px;
          transition: all 0.3s;

          &:hover {
            background-color: var(--el-fill-color-light);
          }

          .icon-display {
            font-size: 24px;
            margin-bottom: 4px;
          }

          .icon-name {
            font-size: 12px;
            text-align: center;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            width: 100%;
          }
        }
      }
    }
  }
</style>
