<template>
  <div class="mobile-device-detail" v-loading="loading">
    <div class="content">
      <div v-if="device">
        <!-- 顶部概览卡片 -->
        <div class="device-header-card">
          <div class="header-main">
            <el-image
              :src="device.imageUrl || ''"
              :preview-src-list="device.imageUrl ? [device.imageUrl] : []"
              fit="cover"
              class="device-img"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><ele-Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="header-info">
              <div class="device-name">{{ device.name }}</div>
              <div class="device-model">{{ device.modelName }}</div>
              <el-tag :type="getStatusType(device.assetsStatus)" size="small" class="status-tag">
                {{ device.assetsStatusDesc }}
              </el-tag>
            </div>
          </div>
          <div class="header-ids">
            <div class="id-item">
              <span class="label">资产编号</span>
              <span class="value">{{ device.deviceNo }}</span>
            </div>
            <div class="id-item border-left">
              <span class="label">序列号</span>
              <span class="value">{{ device.serial }}</span>
            </div>
          </div>
        </div>

        <!-- 详细信息卡片 -->
        <div class="detail-card">
          <div class="section-title">详细信息</div>
          <div class="info-grid">
            <div class="info-row">
              <span class="label">使用人</span>
              <span class="value">{{ device.assignedToName || '未分配' }}</span>
            </div>
            <div class="info-row">
              <span class="label">存放位置</span>
              <span class="value">{{ device.locationName }}</span>
            </div>
            <div class="info-row">
              <span class="label">供应商</span>
              <span class="value">{{ device.supplierName || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="label">购买日期</span>
              <span class="value">{{ device.purchaseDate ? dayjs(device.purchaseDate).format('YYYY-MM-DD') : '-' }}</span>
            </div>
            <div class="info-row">
              <span class="label">过保日期</span>
              <span class="value">{{ device.warrantyDate ? dayjs(device.warrantyDate).format('YYYY-MM-DD') : '-' }}</span>
            </div>
            <div class="info-row">
              <span class="label">购买成本</span>
              <span class="value">{{ device.purchaseCost ? `¥${device.purchaseCost}` : '-' }}</span>
            </div>
            <div class="info-row full-width" v-if="device.description">
              <span class="label">描述</span>
              <span class="value">{{ device.description }}</span>
            </div>
          </div>
        </div>

        <!-- 关联信息 -->
        <div class="relation-card" v-if="device.accessories && device.accessories.length">
          <div class="section-title">关联配件 ({{ device.accessories.length }})</div>
          <div class="relation-list">
            <div class="relation-item" v-for="item in device.accessories" :key="item.id">
              <div class="rel-name">{{ item.name }}</div>
              <div class="rel-desc"> {{ item.categoryName }}</div>
            </div>
          </div>
        </div>

        <div class="relation-card" v-if="device.licenses && device.licenses.length">
          <div class="section-title">关联许可 ({{ device.licenses.length }})</div>
          <div class="relation-list">
            <div class="relation-item" v-for="item in device.licenses" :key="item.id">
              <div class="rel-name">{{ item.name }}</div>
              <div class="rel-desc"> 过期：{{ item.expirationDate }}</div>
            </div>
          </div>
        </div>

        <div class="relation-card" v-if="device.services && device.services.length">
          <div class="section-title">关联服务 ({{ device.services.length }})</div>
          <div class="relation-list">
            <div class="relation-item" v-for="item in device.services" :key="item.id">
              <div class="rel-name">{{ item.name }}</div>
              <div class="rel-desc">{{ item.startDate }} 至 {{ item.endDate }}</div>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && !device" description="未找到设备信息" />

      <!-- Fixed Footer for Actions -->
      <div v-if="device" class="footer-actions">
        <el-button type="warning" class="footer-btn action-warning" @click="handleException">
          <template #icon
            ><el-icon><ele-Warning /></el-icon
          ></template>
          报告异常
        </el-button>
        <el-button type="primary" class="footer-btn action-primary" @click="handleStocktake">
          <template #icon
            ><el-icon><ele-Check /></el-icon
          ></template>
          任务盘点
        </el-button>
      </div>
    </div>

    <!-- 报告异常弹窗 (Vant) -->
    <van-popup v-model:show="exceptionVisible" round position="bottom" safe-area-inset-bottom class="mobile-popup">
      <div class="popup-header">
        <div class="popup-title">报告异常</div>
        <div class="popup-close" @click="exceptionVisible = false">
          <el-icon><ele-Close /></el-icon>
        </div>
      </div>
      <div class="popup-content">
        <van-form ref="exceptionFormRef" @submit="submitException">
          <van-cell-group inset>
            <van-field
              v-model="exceptionForm.failureName"
              name="failureName"
              label="故障说明"
              placeholder="简要说明故障情况"
              :rules="[{ required: true, message: '请输入故障说明' }]"
            />
            <van-field
              v-model="exceptionForm.failureDate"
              is-link
              readonly
              name="failureDate"
              label="故障时间"
              placeholder="点击选择时间"
              @click="showDatePicker = true"
              :rules="[{ required: true, message: '请选择故障时间' }]"
            />
            <van-popup v-model:show="showDatePicker" position="bottom">
              <van-date-picker
                v-model="datePickerValue"
                title="选择日期"
                @confirm="onConfirmDate"
                @cancel="showDatePicker = false"
                type="datetime"
              />
            </van-popup>

            <van-field
              v-model="exceptionForm.failureDescription"
              name="failureDescription"
              label="详细描述"
              type="textarea"
              rows="3"
              placeholder="详细描述故障信息"
              show-word-limit
            />
          </van-cell-group>
          <div class="popup-btn-container">
            <van-button round block type="primary" native-type="submit" :loading="submitting"> 提交 </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

    <!-- 处理盘点弹窗 (Vant) -->
    <van-popup v-model:show="stocktakeVisible" round position="bottom" safe-area-inset-bottom class="mobile-popup">
      <div class="popup-header">
        <div class="popup-title">处理盘点</div>
        <div class="popup-close" @click="stocktakeVisible = false">
          <el-icon><ele-Close /></el-icon>
        </div>
      </div>
      <div class="popup-content" v-loading="stocktakeLoading">
        <el-empty v-if="stocktakeList.length === 0" description="当前设备没有相关的进行中盘点任务" />
        <div v-else class="stocktake-list-vant">
          <div v-for="item in stocktakeList" :key="item.id" class="stocktake-card">
            <div class="st-card-header">
              <div class="st-header-left">
                <div class="st-icon-container">
                  <el-icon><ele-Memo /></el-icon>
                </div>
                <div class="st-title-group">
                  <div class="st-name">{{ item.stocktakeName || '未命名任务' }}</div>
                  <div class="st-date" v-if="item.scannedAt">
                    <el-icon><ele-Clock /></el-icon>
                    <span>{{ item.scannedAt }}</span>
                  </div>
                </div>
              </div>
              <van-tag
                :type="item.status === 'pending' ? 'primary' : item.status === 'normal' ? 'success' : 'danger'"
                size="large"
                round
                class="st-status-tag"
              >
                {{ item.statusDesc }}
              </van-tag>
            </div>

            <div class="st-card-body">
              <div class="st-info-item">
                <span class="st-label">预期位置</span>
                <span class="st-value">{{ item.expectedLocationName || '-' }}</span>
              </div>
              <div class="st-info-item" v-if="item.scannedByName">
                <span class="st-label">盘点人</span>
                <span class="st-value">{{ item.scannedByName }}</span>
              </div>
              <div class="st-info-item full" v-if="item.note">
                <span class="st-label">备注说明</span>
                <span class="st-value highlight">{{ item.note }}</span>
              </div>
            </div>

            <div class="st-card-footer">
              <van-button class="process-btn" type="primary" block round @click="openProcessStocktake(item)">
                <template #icon>
                  <el-icon><ele-Edit /></el-icon>
                </template>
                立即处理盘点
              </van-button>
            </div>
          </div>
        </div>
      </div>
    </van-popup>

    <!-- 盘点处理弹窗 -->
    <van-popup v-model:show="processStocktakeVisible" round position="bottom" safe-area-inset-bottom class="mobile-popup">
      <div class="popup-header">
        <div class="popup-title">处理盘点</div>
        <div class="popup-close" @click="processStocktakeVisible = false">
          <el-icon><ele-Close /></el-icon>
        </div>
      </div>
      <div class="popup-content">
        <van-form @submit="submitProcessStocktake">
          <div class="form-section">
            <div class="section-header">
              <div class="section-title">盘点结果</div>
              <div class="section-desc">请根据实际情况选择</div>
            </div>

            <div class="status-grid">
              <div
                class="status-item"
                :class="{ active: processStocktakeForm.status === 'normal', 'is-normal': true }"
                @click="processStocktakeForm.status = 'normal'"
              >
                <div class="icon-wrapper">
                  <el-icon><ele-CircleCheck /></el-icon>
                </div>
                <div class="status-label">正常</div>
              </div>

              <div
                class="status-item"
                :class="{ active: processStocktakeForm.status === 'lost', 'is-warning': true }"
                @click="processStocktakeForm.status = 'lost'"
              >
                <div class="icon-wrapper">
                  <el-icon><ele-QuestionFilled /></el-icon>
                </div>
                <div class="status-label">丢失</div>
              </div>

              <div
                class="status-item"
                :class="{ active: processStocktakeForm.status === 'damaged', 'is-orange': true }"
                @click="processStocktakeForm.status = 'damaged'"
              >
                <div class="icon-wrapper">
                  <el-icon><ele-Tools /></el-icon>
                </div>
                <div class="status-label">损坏</div>
              </div>

              <div
                class="status-item"
                :class="{ active: processStocktakeForm.status === 'scrapped', 'is-danger': true }"
                @click="processStocktakeForm.status = 'scrapped'"
              >
                <div class="icon-wrapper">
                  <el-icon><ele-Delete /></el-icon>
                </div>
                <div class="status-label">报废</div>
              </div>
            </div>
          </div>

          <div class="form-section">
            <van-field
              v-model="processStocktakeForm.note"
              name="note"
              label="备注说明"
              type="textarea"
              rows="3"
              placeholder="请输入盘点备注信息..."
              show-word-limit
              maxlength="200"
              class="custom-field"
            />
          </div>

          <div class="popup-btn-container">
            <van-button round block type="primary" native-type="submit" :loading="processSubmitting"> 确认提交 </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
  import { getDeviceDetail, getDeviceDetailByNo, DeviceDetailVO } from '@/api/itam/device'
  import { pageStocktakeItems, editStocktakeItems, StocktakeItemsEntity } from '@/api/itam/stocktakeItems'
  import { reportException } from '@/api/itam/offering'
  import { AssetType } from '@/api/itam/enums'
  import { MessageUtil } from '@/utils/messageUtil'
  import dayjs from 'dayjs'

  const route = useRoute()
  const deviceNoParam = (route.params.number || route.query.deviceNo || route.query.number || route.query.id) as string
  const loading = ref(false)
  const device = ref<DeviceDetailVO>()

  // 异常相关
  const exceptionVisible = ref(false)
  const submitting = ref(false)
  const exceptionForm = reactive({
    failureName: '',
    failureDate: '',
    failureDescription: ''
  })
  const exceptionFormRef = ref()

  // Date Picker logic
  const showDatePicker = ref(false)
  const datePickerValue = ref<string[]>([])

  const onConfirmDate = ({ selectedValues }: { selectedValues: string[] }) => {
    exceptionForm.failureDate = selectedValues.join('-')
    showDatePicker.value = false
  }

  // 盘点相关
  const stocktakeVisible = ref(false)
  const stocktakeLoading = ref(false)
  const stocktakeList = ref<StocktakeItemsEntity[]>([])

  const getStatusType = (status: number | undefined) => {
    if (status === 1) return 'success' // 闲置
    if (status === 2) return 'primary' // 在用
    if (status === 4) return 'warning' // 故障
    if (status === 10) return 'danger' // 报废
    return 'info'
  }

  const loadData = async () => {
    if (!deviceNoParam) return
    loading.value = true
    try {
      // 先尝试解析为 ID
      const id = parseInt(deviceNoParam)
      if (!isNaN(id) && String(id) === deviceNoParam) {
        try {
          const res = await getDeviceDetailByNo(deviceNoParam)
          if (res) {
            device.value = res
            return
          }
        } catch {
          // 忽略错误，继续尝试按 ID
        }

        const res = await getDeviceDetail(id)
        device.value = res
      } else {
        // 不是纯数字，肯定是 DeviceNo
        const res = await getDeviceDetailByNo(deviceNoParam)
        if (res) {
          device.value = res
        } else {
          MessageUtil.error('未找到该资产编号对应的设备')
        }
      }
    } catch (error) {
      console.error(error)
      // 如果按各种方式都查不到，报个错
      if (!device.value) {
        MessageUtil.error('获取设备信息失败')
      }
    } finally {
      loading.value = false
    }
  }

  const handleException = () => {
    exceptionForm.failureName = ''
    exceptionForm.failureDate = dayjs().format('YYYY-MM-DD')
    datePickerValue.value = dayjs().format('YYYY-MM-DD').split('-')
    exceptionForm.failureDescription = ''
    exceptionVisible.value = true
  }

  const submitException = async () => {
    submitting.value = true
    try {
      await reportException({
        targetType: AssetType.DEVICE,
        targetId: device.value?.id as number,
        ...exceptionForm,
        // Ensure full datetime format if backend requires it
        failureDate: exceptionForm.failureDate.includes(' ') ? exceptionForm.failureDate : `${exceptionForm.failureDate} 00:00:00`
      })
      MessageUtil.success('提交成功')
      exceptionVisible.value = false
    } catch (e) {
      console.error(e)
    } finally {
      submitting.value = false
    }
  }

  const handleStocktake = async () => {
    if (!device.value?.id) return
    stocktakeVisible.value = true
    stocktakeLoading.value = true
    try {
      // 获取该设备相关的盘点明细（通常是未盘点的，或者属于进行中任务的）
      // 这里获取所有，并在前端过滤或展示
      const res = await pageStocktakeItems({
        current: 1,
        size: 50,
        assetId: device.value.id.toString()
        // status: 'pending' // 可以根据需要过滤
      } as any)

      // 简单过滤：只显示未完成的，或者属于“进行中”任务的
      // 由于API限制，假设返回的都是相关的
      stocktakeList.value = res.records.filter((item: any) => item.status === 'pending')
    } catch (e) {
      console.error(e)
    } finally {
      stocktakeLoading.value = false
    }
  }

  // Process Stocktake Logic
  const processStocktakeVisible = ref(false)
  const processSubmitting = ref(false)
  const processStocktakeForm = reactive({
    id: 0,
    status: 'normal',
    note: ''
  })

  const openProcessStocktake = (item: StocktakeItemsEntity) => {
    processStocktakeForm.id = item.id
    processStocktakeForm.status = 'normal'
    processStocktakeForm.note = ''
    processStocktakeVisible.value = true
  }

  const submitProcessStocktake = async () => {
    processSubmitting.value = true
    try {
      await editStocktakeItems({
        id: processStocktakeForm.id,
        status: processStocktakeForm.status,
        note: processStocktakeForm.note
      })
      MessageUtil.success('处理成功')
      processStocktakeVisible.value = false
      handleStocktake()
    } catch (e) {
      console.error(e)
    } finally {
      processSubmitting.value = false
    }
  }

  onMounted(() => {
    loadData()
  })
</script>

<style scoped lang="scss">
  .mobile-device-detail {
    min-height: 100vh;
    background-color: #f5f7fa;
    padding: 12px;
    padding-bottom: 80px; /* Space for footer */
    box-sizing: border-box;

    .content {
      max-width: 100%;
    }

    /* Header Card */
    .device-header-card {
      background: linear-gradient(135deg, #ffffff 0%, #f9faff 100%);
      border-radius: 12px;
      padding: 16px;
      margin-bottom: 16px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);

      .header-main {
        display: flex;
        align-items: flex-start;
        margin-bottom: 16px;

        .device-img {
          width: 80px;
          height: 80px;
          border-radius: 8px;
          flex-shrink: 0;
          margin-right: 16px;
          border: 1px solid #eee;
          background: #f8f8f8;

          .image-placeholder {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #ccc;
            font-size: 24px;
          }
        }

        .header-info {
          flex: 1;
          .device-name {
            font-size: 18px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 4px;
            line-height: 1.4;
          }
          .device-model {
            font-size: 13px;
            color: #909399;
            margin-bottom: 8px;
          }
          .status-tag {
            border-radius: 4px;
          }
        }
      }

      .header-ids {
        display: flex;
        border-top: 1px solid #f0f0f0;
        padding-top: 12px;

        .id-item {
          flex: 1;
          display: flex;
          flex-direction: column;
          align-items: center;

          &.border-left {
            border-left: 1px solid #f0f0f0;
          }

          .label {
            font-size: 11px;
            color: #909399;
            margin-bottom: 2px;
          }
          .value {
            font-size: 13px;
            font-weight: 500;
            color: #303133;
            word-break: break-all;
          }
        }
      }
    }

    /* Detail & Relation Cards */
    .detail-card,
    .relation-card {
      background: #fff;
      border-radius: 12px;
      padding: 16px;
      margin-bottom: 16px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);

      .section-title {
        font-size: 15px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 12px;
        padding-left: 8px;
        border-left: 3px solid #409eff;
        line-height: 1;
      }
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 12px 16px;

      .info-row {
        .label {
          display: block;
          font-size: 12px;
          color: #909399;
          margin-bottom: 2px;
        }
        .value {
          display: block;
          font-size: 14px;
          color: #303133;
        }

        &.full-width {
          grid-column: span 2;
        }
      }
    }

    .relation-list {
      .relation-item {
        padding: 10px;
        background: #f8f9fa;
        border-radius: 6px;
        margin-bottom: 8px;

        &:last-child {
          margin-bottom: 0;
        }

        .rel-name {
          font-weight: 500;
          color: #303133;
          margin-bottom: 4px;
        }
        .rel-desc {
          font-size: 12px;
          color: #909399;
        }
      }
    }

    /* Footer Action Bar */
    .footer-actions {
      position: fixed;
      bottom: 0;
      left: 0;
      width: 100%;
      height: 72px; /* Increased height */
      background: rgba(255, 255, 255, 0.9);
      backdrop-filter: blur(10px);
      box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.08);
      display: flex;
      align-items: center;
      padding: 0 20px;
      gap: 12px;
      z-index: 99;
      padding-bottom: env(safe-area-inset-bottom); /* Support for notched phones */

      .footer-btn {
        flex: 1;
        height: 46px;
        border-radius: 23px;
        font-weight: 600;
        font-size: 15px;
        border: none;
        transition: all 0.3s cubic-bezier(0.18, 0.89, 0.32, 1.28);

        &.action-warning {
          background: linear-gradient(135deg, #ff9800 0%, #f44336 100%);
          color: white;
          box-shadow: 0 4px 10px rgba(244, 67, 54, 0.3);
        }

        &.action-primary {
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          color: #fff;
          background: linear-gradient(135deg, #409eff 0%, #3a8ee6 100%);
          box-shadow: 0 4px 10px rgba(64, 158, 255, 0.3);
        }

        &:active {
          transform: scale(0.95);
          box-shadow: none;
        }

        .el-icon {
          margin-right: 4px;
        }
      }
    }

    .popup-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px;
      border-bottom: 1px solid #f5f6f7;

      .popup-title {
        font-size: 16px;
        font-weight: 600;
        color: #333;
      }
      .popup-close {
        font-size: 20px;
        color: #999;
        cursor: pointer;
      }
    }

    .popup-content {
      padding: 8px;
      padding-bottom: 32px;
    }

    .stocktake-list-vant {
      padding: 12px;

      .stocktake-card {
        background: #fff;
        border-radius: 16px;
        margin-bottom: 20px;
        overflow: hidden;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
        border: 1px solid rgba(0, 0, 0, 0.03);
        position: relative;
        transition: transform 0.2s ease;

        &:active {
          transform: scale(0.98);
        }

        .st-card-header {
          padding: 16px;
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          background: linear-gradient(to right, #ffffff, #fcfdff);
          border-bottom: 1px dashed #f0f3f7;

          .st-header-left {
            display: flex;
            align-items: flex-start;
            gap: 12px;

            .st-icon-container {
              width: 38px;
              height: 38px;
              background: linear-gradient(135deg, #409eff 0%, #348ef1 100%);
              color: #fff;
              display: flex;
              align-items: center;
              justify-content: center;
              border-radius: 10px;
              font-size: 20px;
              box-shadow: 0 4px 10px rgba(64, 158, 255, 0.3);
            }

            .st-title-group {
              display: flex;
              flex-direction: column;

              .st-name {
                font-size: 16px;
                font-weight: 700;
                color: #2c3e50;
                margin-bottom: 4px;
                line-height: 1.2;
              }

              .st-date {
                font-size: 12px;
                color: #94a3b8;
                display: flex;
                align-items: center;
                gap: 4px;

                .el-icon {
                  font-size: 13px;
                }
              }
            }
          }

          .st-status-tag {
            font-weight: 600;
            padding: 0 12px;
            height: 24px;
            line-height: 22px;
          }
        }

        .st-card-body {
          padding: 16px;
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 12px;

          .st-info-item {
            background: #f8fafc;
            padding: 10px 12px;
            border-radius: 8px;
            display: flex;
            flex-direction: column;
            gap: 4px;

            .st-label {
              font-size: 11px;
              color: #64748b;
              font-weight: 500;
              text-transform: uppercase;
              letter-spacing: 0.5px;
            }

            .st-value {
              font-size: 14px;
              color: #334155;
              font-weight: 600;
              word-break: break-all;
            }

            &.full {
              grid-column: span 2;
            }

            .highlight {
              color: #409eff;
            }
          }
        }

        .st-card-footer {
          padding: 12px 16px 16px;
          background: #fff;

          .process-btn {
            height: 44px;
            font-size: 15px;
            font-weight: 600;
            letter-spacing: 1px;
            box-shadow: 0 4px 12px rgba(64, 158, 255, 0.25);
            transition: all 0.2s;

            /* Ensure icon and text are perfectly aligned */
            display: flex;
            align-items: center;
            justify-content: center;

            :deep(.van-button__content) {
              display: flex;
              align-items: center;
              line-height: 1;
            }

            :deep(.van-button__icon) {
              font-size: 18px;
              margin-right: 6px;
              display: flex;
              align-items: center;
              height: 100%;
            }

            &:active {
              box-shadow: 0 2px 6px rgba(64, 158, 255, 0.2);
              transform: translateY(1px);
            }
          }
        }
      }
    }

    /* Mobile Popup Styles */
    :deep(.mobile-popup) {
      max-height: 85vh;
      display: flex;
      flex-direction: column;
      overflow: hidden; /* Manage scroll in content */
      background-color: #f7f8fa; /* Light gray background */

      .popup-header {
        background-color: #fff;
        flex-shrink: 0;
      }

      .popup-content {
        flex: 1;
        overflow-y: auto;
        padding: 0;
        /* Ensure content has some spacing if not using inset groups, or let inset groups handle it */
      }

      /* Adjust form layout */
      .van-form {
        display: flex;
        flex-direction: column;
        height: 100%;
      }

      .van-cell-group--inset {
        margin: 12px;
        overflow: hidden;
      }

      /* Popup Form Styles */
      .form-section {
        background: #fff;
        margin: 12px;
        padding: 16px;
        border-radius: 12px;

        .section-header {
          margin-bottom: 16px;
          text-align: center;

          .section-title {
            font-size: 16px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 4px;
          }

          .section-desc {
            font-size: 12px;
            color: #909399;
          }
        }
      }

      .status-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 16px;
        padding: 4px;

        .status-item {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          padding: 20px 12px;
          background: #fff;
          border: 2px solid #f1f5f9;
          border-radius: 16px;
          cursor: pointer;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          position: relative;
          overflow: hidden;

          .icon-wrapper {
            width: 48px;
            height: 48px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 12px;
            background: #f8fafc;
            margin-bottom: 12px;
            font-size: 24px;
            color: #94a3b8;
            transition: all 0.3s;
          }

          .status-label {
            font-size: 15px;
            color: #475569;
            font-weight: 600;
          }

          &.active {
            transform: translateY(-2px);
            box-shadow: 0 10px 20px -5px rgba(0, 0, 0, 0.1);

            &.is-normal {
              border-color: #67c23a;
              background: #f0f9eb;
              .icon-wrapper {
                color: #fff;
                background: #67c23a;
                box-shadow: 0 4px 12px rgba(103, 194, 58, 0.4);
              }
              .status-label {
                color: #67c23a;
              }
            }

            &.is-warning {
              border-color: #e6a23c;
              background: #fdf6ec;
              .icon-wrapper {
                color: #fff;
                background: #e6a23c;
                box-shadow: 0 4px 12px rgba(230, 162, 60, 0.4);
              }
              .status-label {
                color: #e6a23c;
              }
            }

            &.is-orange {
              border-color: #ff9800;
              background: #fff7ed;
              .icon-wrapper {
                color: #fff;
                background: #ff9800;
                box-shadow: 0 4px 12px rgba(255, 152, 0, 0.4);
              }
              .status-label {
                color: #ff9800;
              }
            }

            &.is-danger {
              border-color: #f56c6c;
              background: #fef0f0;
              .icon-wrapper {
                color: #fff;
                background: #f56c6c;
                box-shadow: 0 4px 12px rgba(245, 108, 108, 0.4);
              }
              .status-label {
                color: #f56c6c;
              }
            }
          }

          &:not(.active):active {
            background: #f1f5f9;
          }
        }
      }

      .custom-field {
        padding: 0;
        margin-top: 8px;
        :deep(.van-field__label) {
          font-weight: 700;
          color: #334155;
          margin-bottom: 10px;
          display: block;
        }
        :deep(.van-field__value) {
          background: #f8fafc;
          padding: 12px;
          border-radius: 12px;
          border: 1px solid #e2e8f0;
        }
      }

      .popup-btn-container {
        margin-top: auto; /* Push to bottom if content is short */
        padding: 16px;
        background-color: #fff;
        box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.03);
      }
    }
  }
</style>
