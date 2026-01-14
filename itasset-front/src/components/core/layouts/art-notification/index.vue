<template>
  <div class="layout-navbars-breadcrumb-user-news">
    <el-tabs v-model="activeName">
      <el-tab-pane :label="$t('notice.newTitle') + `(${anntMsgTotal})`" name="default">
        <div class="content-box">
          <template v-if="anntMsgList.length > 0">
            <el-scrollbar style="height: 240px">
              <div v-for="(v, k) in anntMsgList" :key="k" class="content-box-item" @click="onGoToClick">
                <div>{{ v.title }}</div>
                <div class="content-box-msg" />
                <div class="content-box-time">
                  {{ v.sendTime }}
                </div>
              </div>
            </el-scrollbar>
          </template>
          <el-empty :description="$t('notice.newDesc')" v-else></el-empty>
        </div>
        <div v-if="anntMsgList.length > 0" class="foot-box" @click="onGoToClick">
          {{ $t('notice.newGo') }}
        </div>
      </el-tab-pane>
      <el-tab-pane :label="$t('notice.sysNoticeTitle') + `(${sysMsgTotal})`" name="sys">
        <div class="content-box">
          <template v-if="sysMsgTotal > 0">
            <el-scrollbar style="height: 240px">
              <div v-for="(v, k) in sysMsgList" :key="k" class="content-box-item" @click="onGoToClick">
                <div>{{ v.title }}</div>
                <div class="content-box-time">
                  {{ v.sendTime }}
                </div>
              </div>
            </el-scrollbar>
          </template>
          <el-empty :description="$t('notice.newDesc')" v-else></el-empty>
        </div>
        <div v-if="sysMsgTotal > 0" class="foot-box" @click="onGoToClick">
          {{ $t('notice.newGo') }}
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts" name="layoutBreadcrumbUserNews">
  import { onMounted, ref } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElNotification } from 'element-plus'
  import { getMyAnnouncementNotice } from '@/api/system/announcement'
  import mittBus from '@/utils/sys/mittBus'

  defineProps({
    value: {
      type: Boolean,
      default: false
    }
  })

  const emit = defineEmits(['update:value'])

  const activeName = ref('default')
  const anntMsgList = ref([])
  const sysMsgList = ref([])
  const sysMsgTotal = ref(0)
  const anntMsgTotal = ref(0)

  onMounted(() => {
    // 获取消息列表
    getUserNews()
    mittBus.on('sysMsgNotice', (res: any) => {
      getUserNews()
      try {
        const { title, msgContent } = res
        if (!msgContent) {
          return
        }
        ElNotification({
          position: 'top-right',
          title: title,
          offset: 50,
          dangerouslyUseHTMLString: true,
          message: msgContent
        })
      } catch (e) {
        console.log(e)
      }
    })
  })

  const getUserNews = () => {
    getMyAnnouncementNotice({}).then((res: any) => {
      anntMsgList.value = res.anntMsgList
      sysMsgList.value = res.sysMsgList
      anntMsgTotal.value = res.anntMsgTotal
      sysMsgTotal.value = res.sysMsgTotal
      emit('update:value', !!(sysMsgTotal.value || anntMsgTotal.value))
    })
  }
  const router = useRouter()

  // 前往通知中心点击
  const onGoToClick = () => {
    router.push({
      path: '/msg/my'
    })
  }
</script>

<style scoped lang="scss">
  .layout-navbars-breadcrumb-user-news {
    .head-box {
      display: flex;
      border-bottom: 1px solid var(--el-border-color-lighter);
      box-sizing: border-box;
      color: var(--el-text-color-primary);
      justify-content: space-between;
      height: 35px;
      align-items: center;

      .head-box-btn {
        color: var(--el-color-primary);
        font-size: 13px;
        cursor: pointer;
        opacity: 0.8;

        &:hover {
          opacity: 1;
        }
      }
    }

    .content-box {
      font-size: 13px;

      .content-box-item {
        padding-top: 12px;

        &:last-of-type {
          padding-bottom: 12px;
        }

        .content-box-msg {
          color: var(--el-text-color-secondary);
          margin-top: 5px;
          margin-bottom: 5px;
        }

        .content-box-time {
          color: var(--el-text-color-secondary);
        }
      }
    }

    .foot-box {
      height: 35px;
      color: var(--el-color-primary);
      font-size: 13px;
      cursor: pointer;
      opacity: 0.8;
      display: flex;
      align-items: center;
      justify-content: center;
      border-top: 1px solid var(--el-border-color-lighter);

      &:hover {
        opacity: 1;
      }
    }

    :deep(.el-empty__description p) {
      font-size: 13px;
    }
  }
</style>
