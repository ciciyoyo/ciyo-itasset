<template>
  <el-drawer
    v-model="visible"
    size="100%"
    direction="rtl"
    :show-close="true"
    :destroy-on-close="true"
    class="device-detail-drawer"
  >
    <template #header>
      <div class="drawer-header">
        <div class="header-title">
          <el-icon :size="24" class="mr-2">
            <ele-Monitor />
          </el-icon>
          <span class="title-text">设备详情</span>
          <el-tag v-if="deviceData" :type="getStatusType(deviceData.assetsStatus)" class="ml-3">
            {{ deviceData.assetsStatusDesc || getStatusText(deviceData.assetsStatus) }}
          </el-tag>
        </div>
      </div>
    </template>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <div v-else-if="deviceData" class="detail-content">
      <!-- 顶部概览卡片 -->
      <div class="overview-card">
        <div class="device-image">
          <el-image
            v-if="deviceData.imageUrl"
            :src="deviceData.imageUrl"
            :preview-src-list="[deviceData.imageUrl]"
            fit="cover"
            class="device-img"
            preview-teleported
          />
          <div v-else class="no-image">
            <el-icon :size="48" color="#c0c4cc">
              <ele-Picture />
            </el-icon>
            <span>暂无图片</span>
          </div>
        </div>
        <div class="device-info">
          <h2 class="device-name">{{ deviceData.name }}</h2>
          <div class="info-row">
            <span class="label">设备编号：</span>
            <span class="value">{{ deviceData.deviceNo || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="label">序列号：</span>
            <span class="value">{{ deviceData.serial || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="label">使用人：</span>
            <el-tag v-if="deviceData.assignedToName" type="success" size="small">
              {{ deviceData.assignedToName }}
            </el-tag>
            <el-tag v-else type="info" size="small">未分配</el-tag>
          </div>
        </div>
      </div>

      <!-- 详细信息区域 -->
      <el-tabs v-model="activeTab" class="detail-tabs">
        <el-tab-pane label="详细信息" name="detail">
          <div class="info-section">
            <el-descriptions :column="descriptionColumn" border>
              <el-descriptions-item label="设备编号">
                {{ deviceData.deviceNo || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="序列号">
                {{ deviceData.serial || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="设备名称">
                {{ deviceData.name || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="设备型号">
                {{ deviceData.modelName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="使用人">
                <el-tag v-if="deviceData.assignedToName" type="success" size="small">
                  {{ deviceData.assignedToName }}
                </el-tag>
                <span v-else class="text-gray-400">未分配</span>
              </el-descriptions-item>
              <el-descriptions-item label="设备状态">
                <el-tag :type="getStatusType(deviceData.assetsStatus)">
                  {{ deviceData.assetsStatusDesc || getStatusText(deviceData.assetsStatus) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="存放位置">
                {{ deviceData.locationName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="供应商">
                {{ deviceData.supplierName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="折旧规则">
                {{ deviceData.depreciationName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="购买成本">
                <span v-if="deviceData.purchaseCost" class="cost-value"> ¥{{ deviceData.purchaseCost }} </span>
                <span v-else>-</span>
              </el-descriptions-item>
              <el-descriptions-item label="购买日期">
                {{ deviceData.purchaseDate || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="过保日期">
                <span :class="{ 'text-danger': isExpired }">
                  {{ deviceData.warrantyDate || '-' }}
                  <el-tag v-if="isExpired" type="danger" size="small" class="ml-2">已过保</el-tag>
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="描述" :span="2">
                {{ deviceData.description || '暂无描述' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-tab-pane>

        <el-tab-pane label="型号信息" name="model" v-if="deviceData.model">
          <div class="info-section">
            <el-descriptions :column="descriptionColumn" border>
              <el-descriptions-item label="型号名称">
                {{ deviceData.model.name || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="型号编号">
                {{ deviceData.model.modelNumber || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="制造商">
                {{ deviceData.model.manufacturerName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="分类">
                {{ deviceData.model.categoryName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="折旧规则">
                {{ deviceData.model.depreciationName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="EOL (月)">
                {{ deviceData.model.eol || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-tab-pane>

        <el-tab-pane label="关联资产" name="related">
          <div class="info-section">
            <!-- 配件 -->
            <div class="related-section">
              <h4 class="section-title">
                <el-icon><ele-Box /></el-icon>
                配件
                <el-tag size="small" class="ml-2">{{ deviceData.accessories?.length || 0 }}</el-tag>
              </h4>
              <el-table v-if="deviceData.accessories?.length" :data="deviceData.accessories" stripe border size="small">
                <el-table-column prop="name" label="名称" min-width="150" />
                <el-table-column prop="assetNumber" label="资产编号" min-width="140" />
                <el-table-column prop="serialNumber" label="序列号" min-width="140" />
                <el-table-column prop="specifications" label="规格" min-width="150" />
                <el-table-column prop="quantity" label="数量" width="80" />
                <el-table-column prop="categoryName" label="分类" min-width="100" />
              </el-table>
              <el-empty v-else description="暂无配件" :image-size="60" />
            </div>

            <!-- 许可证 -->
            <div class="related-section">
              <h4 class="section-title">
                <el-icon><ele-Key /></el-icon>
                软件许可
                <el-tag size="small" class="ml-2">{{ deviceData.licenses?.length || 0 }}</el-tag>
              </h4>
              <el-table v-if="deviceData.licenses?.length" :data="deviceData.licenses" stripe border size="small">
                <el-table-column prop="name" label="名称" min-width="150" />
                <el-table-column prop="licenseKey" label="许可证密钥" min-width="200" />
                <el-table-column prop="totalSeats" label="席位数" width="80" />
                <el-table-column prop="expirationDate" label="过期日期" min-width="120" />
                <el-table-column prop="licensedToName" label="授权给" min-width="120" />
              </el-table>
              <el-empty v-else description="暂无许可证" :image-size="60" />
            </div>

            <!-- 服务 -->
            <div class="related-section">
              <h4 class="section-title">
                <el-icon><ele-Service /></el-icon>
                服务
                <el-tag size="small" class="ml-2">{{ deviceData.services?.length || 0 }}</el-tag>
              </h4>
              <el-table v-if="deviceData.services?.length" :data="deviceData.services" stripe border size="small">
                <el-table-column prop="name" label="名称" min-width="150" />
                <el-table-column prop="serviceNumber" label="服务编号" min-width="140" />
                <el-table-column prop="offeringStatusDesc" label="状态" width="80">
                  <template #default="{ row }">
                    <el-tag :type="row.offeringStatus === 'normal' ? 'success' : 'warning'" size="small">
                      {{ row.offeringStatusDesc }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="startDate" label="开始日期" min-width="120" />
                <el-table-column prop="endDate" label="结束日期" min-width="120" />
                <el-table-column prop="supplierName" label="供应商" min-width="120" />
              </el-table>
              <el-empty v-else description="暂无服务" :image-size="60" />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <template #footer>
      <div class="drawer-footer">
        <el-button @click="visible = false">关闭</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
  import { ref, computed } from 'vue'
  import { useWindowSize } from '@vueuse/core'
  import { getDeviceDetail, DeviceEntity, DeviceDetailVO } from '@/api/itam/device'

  const visible = ref(false)
  const loading = ref(false)
  const deviceData = ref<DeviceDetailVO | null>(null)
  const activeTab = ref('detail')

  // 响应式窗口尺寸
  const { width } = useWindowSize()
  const isMobile = computed(() => width.value < 768)

  // 响应式描述列表列数
  const descriptionColumn = computed(() => (isMobile.value ? 1 : 2))

  // 判断是否已过保
  const isExpired = computed(() => {
    if (!deviceData.value?.warrantyDate) return false
    return new Date(deviceData.value.warrantyDate) < new Date()
  })

  // 获取状态类型
  const getStatusType = (status?: number) => {
    switch (status) {
      case 1:
        return 'success'
      case 2:
        return 'primary'
      case 4:
        return 'warning'
      case 10:
        return 'danger'
      default:
        return 'info'
    }
  }

  // 获取状态文本
  const getStatusText = (status?: number) => {
    switch (status) {
      case 1:
        return '闲置'
      case 2:
        return '在用'
      case 4:
        return '故障'
      case 10:
        return '报废'
      default:
        return '未知'
    }
  }

  // 加载设备详情
  const loadDeviceDetail = async (id: number) => {
    loading.value = true
    try {
      const data = await getDeviceDetail(id)
      deviceData.value = data
    } catch (error) {
      console.error('加载设备详情失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 打开弹窗
  const open = (row: DeviceEntity) => {
    visible.value = true
    activeTab.value = 'detail'
    if (row.id) {
      loadDeviceDetail(row.id)
    }
  }

  defineExpose({
    open
  })
</script>

<style scoped lang="scss">
  .device-detail-drawer {
    :deep(.el-drawer__header) {
      margin-bottom: 0;
      padding: 16px 20px;
      border-bottom: 1px solid var(--el-border-color-light);
    }

    :deep(.el-drawer__body) {
      padding: 0;
    }

    :deep(.el-drawer__footer) {
      padding: 12px 20px;
      border-top: 1px solid var(--el-border-color-light);
    }
  }

  .drawer-header {
    .header-title {
      display: flex;
      align-items: center;

      .title-text {
        font-size: 18px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }
    }
  }

  .loading-container {
    padding: 24px;
  }

  .detail-content {
    padding: 24px;
  }

  .overview-card {
    display: flex;
    gap: 24px;
    padding: 24px;
    background: linear-gradient(135deg, #3b82f6 0%, #1e40af 100%);
    border-radius: 12px;
    margin-bottom: 24px;
    color: #fff;

    .device-image {
      flex-shrink: 0;

      .device-img {
        width: 160px;
        height: 160px;
        border-radius: 8px;
        background: rgba(255, 255, 255, 0.1);
      }

      .no-image {
        width: 160px;
        height: 160px;
        border-radius: 8px;
        background: rgba(255, 255, 255, 0.1);
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 8px;
        color: rgba(255, 255, 255, 0.6);
      }
    }

    .device-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: center;

      .device-name {
        font-size: 28px;
        font-weight: 600;
        margin: 0 0 16px 0;
      }

      .info-row {
        display: flex;
        align-items: center;
        margin-bottom: 8px;
        font-size: 14px;

        .label {
          color: rgba(255, 255, 255, 0.8);
          margin-right: 8px;
        }

        .value {
          color: #fff;
          font-weight: 500;
        }
      }
    }
  }

  .detail-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 16px;
    }

    :deep(.el-tabs__item) {
      font-size: 15px;
    }
  }

  .info-section {
    background: var(--el-bg-color);
    border-radius: 8px;
    padding: 16px;

    :deep(.el-descriptions__label) {
      width: 120px;
      font-weight: 500;
    }

    .cost-value {
      color: var(--el-color-danger);
      font-weight: 600;
    }
  }

  .related-section {
    margin-bottom: 24px;

    &:last-child {
      margin-bottom: 0;
    }

    .section-title {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0 0 12px 0;
      font-size: 15px;
      font-weight: 500;
      color: var(--el-text-color-primary);

      .el-icon {
        color: var(--el-color-primary);
      }
    }
  }

  .drawer-footer {
    display: flex;
    justify-content: flex-end;
  }

  .text-danger {
    color: var(--el-color-danger);
  }

  .text-gray-400 {
    color: #9ca3af;
  }

  .ml-2 {
    margin-left: 8px;
  }

  .ml-3 {
    margin-left: 12px;
  }

  .mr-2 {
    margin-right: 8px;
  }

  // 移动端适配
  @media (max-width: 768px) {
    :deep(.el-drawer__header) {
      padding: 12px 15px !important;
    }

    :deep(.el-drawer__body) {
      padding: 0 !important;
    }

    :deep(.el-drawer__footer) {
      padding: 10px 15px !important;
    }

    .drawer-header .header-title {
      .title-text {
        font-size: 16px;
      }
    }

    .detail-content {
      padding: 15px;
    }

    .overview-card {
      flex-direction: column;
      gap: 16px;
      padding: 16px;
      margin-bottom: 16px;

      .device-image {
        align-self: center;

        .device-img,
        .no-image {
          width: 120px;
          height: 120px;
        }
      }

      .device-info {
        .device-name {
          font-size: 20px;
          margin-bottom: 12px;
        }

        .info-row {
          font-size: 13px;
        }
      }
    }

    .detail-tabs {
      :deep(.el-tabs__item) {
        font-size: 14px;
        padding: 0 12px;
      }
    }

    .info-section {
      padding: 12px;

      :deep(.el-descriptions__label) {
        font-size: 13px;
      }

      :deep(.el-descriptions__content) {
        font-size: 13px;
      }
    }

    .related-section {
      margin-bottom: 20px;

      .section-title {
        font-size: 14px;
        margin-bottom: 10px;
      }

      // 表格横向滚动
      :deep(.el-table) {
        font-size: 12px;

        .el-table__cell {
          padding: 6px 4px;
        }

        .cell {
          font-size: 12px;
        }
      }

      // 将表格包裹在可滚动容器中
      .el-table {
        overflow-x: auto;
        -webkit-overflow-scrolling: touch;
      }
    }
  }

  @media (max-width: 640px) {
    .detail-content {
      padding: 12px;
    }

    .overview-card {
      padding: 12px;
      gap: 12px;
      margin-bottom: 12px;

      .device-image {
        .device-img,
        .no-image {
          width: 100px;
          height: 100px;
        }
      }

      .device-info {
        .device-name {
          font-size: 18px;
          margin-bottom: 10px;
        }

        .info-row {
          font-size: 12px;
        }
      }
    }

    .info-section {
      padding: 10px;

      :deep(.el-descriptions__label) {
        font-size: 12px;
        padding: 6px 10px;
      }

      :deep(.el-descriptions__content) {
        font-size: 12px;
        padding: 6px 10px;
      }
    }

    .related-section {
      .section-title {
        font-size: 13px;
      }

      :deep(.el-table) {
        font-size: 11px;

        .cell {
          font-size: 11px;
        }
      }
    }
  }
</style>
