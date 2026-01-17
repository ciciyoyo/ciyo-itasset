<template>
  <el-dialog
    v-model="visible"
    :title="$t('审批并分配资产')"
    width="800px"
    destroy-on-close
    :close-on-click-modal="false"
    append-to-body
  >
    <div class="space-y-4">
      <el-descriptions title="申请详情" :column="2" border>
        <el-descriptions-item label="申请人">{{ requestData?.userName }}</el-descriptions-item>
        <el-descriptions-item label="申请类型">{{ requestData?.itemTypeDesc }}</el-descriptions-item>
        <el-descriptions-item label="资产分类">{{ requestData?.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="申请数量">{{ requestData?.quantity }}</el-descriptions-item>
        <el-descriptions-item label="申请原因" :span="2">{{ requestData?.reason }}</el-descriptions-item>
      </el-descriptions>

      <div class="mt-4">
        <h4 class="mb-2 font-medium">选择要分配的资产</h4>

        <!-- Filter/Search for assets -->
        <div class="flex gap-2 mb-2">
          <el-input
            v-model="assetSearch.name"
            placeholder="搜索资产名称/编号"
            class="w-64"
            clearable
            @input="handleAssetSearch"
          />
          <el-alert v-if="isDeviceRequest" type="info" :closable="false" show-icon>
            仅显示状态为空闲(Ready)且分类匹配的设备
          </el-alert>
          <el-alert v-else-if="isAccessoryRequest" type="info" :closable="false" show-icon> 仅显示分类匹配的配件 </el-alert>
          <el-alert v-else-if="isConsumableRequest" type="info" :closable="false" show-icon> 仅显示分类匹配的耗材 </el-alert>
          <el-alert v-else-if="isLicenseRequest" type="info" :closable="false" show-icon> 仅显示分类匹配的软件授权 </el-alert>
          <el-alert v-else-if="isServiceRequest" type="info" :closable="false" show-icon> 仅显示服务信息 </el-alert>
        </div>

        <el-table
          v-loading="loadingAssets"
          :data="assetList"
          highlight-current-row
          @current-change="handleAssetSelect"
          border
          height="300px"
        >
          <el-table-column width="50">
            <template #default="{ row }">
              <el-radio :model-value="selectedAssetId" :label="row.id">{{ '' }}</el-radio>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="资产名称" />

          <!-- Device Columns -->
          <el-table-column v-if="isDeviceRequest" prop="deviceNo" label="资产编号" />
          <el-table-column v-if="isDeviceRequest" prop="modelName" label="型号" />
          <el-table-column v-if="isDeviceRequest" prop="statusEnv" label="状态">
            <template #default>
              <el-tag type="success">空闲</el-tag>
            </template>
          </el-table-column>

          <!-- Mobile/Accessory Columns -->
          <el-table-column v-if="isAccessoryRequest" prop="assetNumber" label="资产编号" />
          <el-table-column v-if="isAccessoryRequest" prop="specifications" label="规格" />
          <el-table-column v-if="isAccessoryRequest" prop="quantity" label="库存数量" />

          <!-- Consumable Columns -->
          <el-table-column v-if="isConsumableRequest" prop="itemNo" label="耗材编号" />
          <el-table-column v-if="isConsumableRequest" prop="quantity" label="库存数量" />
          <el-table-column v-if="isConsumableRequest" prop="minQuantity" label="最小库存" />

          <!-- License Columns -->
          <el-table-column v-if="isLicenseRequest" prop="licenseKey" label="授权Key" />
          <el-table-column v-if="isLicenseRequest" prop="totalSeats" label="总许可数" />
          <el-table-column v-if="isLicenseRequest" prop="expirationDate" label="过期时间" width="160">
            <template #default="{ row }">{{ row.expirationDate?.split(' ')[0] }}</template>
          </el-table-column>

          <!-- Service Columns -->
          <el-table-column v-if="isServiceRequest" prop="serviceNumber" label="服务编号" />
          <el-table-column v-if="isServiceRequest" prop="startDate" label="开始时间" width="160">
            <template #default="{ row }">{{ row.startDate?.split(' ')[0] }}</template>
          </el-table-column>
          <el-table-column v-if="isServiceRequest" prop="endDate" label="结束时间" width="160">
            <template #default="{ row }">{{ row.endDate?.split(' ')[0] }}</template>
          </el-table-column>
        </el-table>
        <div class="flex justify-end mt-2">
          <el-pagination
            v-model:current-page="pagination.current"
            :page-size="pagination.size"
            :total="pagination.total"
            layout="prev, pager, next"
            @current-change="fetchAssets"
          />
        </div>
      </div>

      <el-form label-width="80px">
        <el-form-item label="审批备注">
          <el-input v-model="note" type="textarea" placeholder="请输入审批备注 (可选)" />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <div class="flex justify-end gap-3">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="danger" @click="handleReject">驳回申请</el-button>
        <el-button type="primary" :disabled="!selectedAssetId" :loading="submitting" @click="handleSubmit"> 确认分配 </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
  import { ref, watch, reactive, computed } from 'vue'
  import { type AssetRequestVO, approveAndAssignRequest, rejectRequest } from '@/api/itam/requests'
  import { pageDevice } from '@/api/itam/device'
  import { pageAccessories } from '@/api/itam/accessories'
  import { pageConsumables } from '@/api/itam/consumables'
  import { pageLicenses } from '@/api/itam/licenses'
  import { pageOffering } from '@/api/itam/offering'
  import { AssetType } from '@/api/itam/enums'
  import { ElMessage, ElMessageBox } from 'element-plus'

  const visible = defineModel<boolean>('visible', { required: true })
  const requestData = defineModel<AssetRequestVO>('requestData')
  const emit = defineEmits(['success'])

  const note = ref('')
  const submitting = ref(false)
  const selectedAssetId = ref<number>()

  const loadingAssets = ref(false)
  const assetList = ref<any[]>([])
  const pagination = reactive({
    current: 1,
    size: 10,
    total: 0
  })

  const assetSearch = reactive({
    name: ''
  })

  const isDeviceRequest = computed(() => requestData.value?.itemType === AssetType.DEVICE)
  const isAccessoryRequest = computed(() => requestData.value?.itemType === AssetType.ACCESSORY)
  const isConsumableRequest = computed(() => requestData.value?.itemType === AssetType.CONSUMABLE)
  const isLicenseRequest = computed(() => requestData.value?.itemType === AssetType.LICENSE)
  const isServiceRequest = computed(() => requestData.value?.itemType === AssetType.SERVICE)

  const fetchAssets = async () => {
    if (!requestData.value) return

    loadingAssets.value = true
    try {
      let res
      const params = {
        current: pagination.current,
        size: pagination.size,
        name: assetSearch.name
      } as any

      if (isDeviceRequest.value) {
        res = await pageDevice({
          ...params,
          categoryId: requestData.value.categoryId,
          assetsStatus: 1
        })
      } else if (isAccessoryRequest.value) {
        // accessories filter by category
        res = await pageAccessories({
          ...params,
          categoryId: String(requestData.value.categoryId)
        })
      } else if (isConsumableRequest.value) {
        res = await pageConsumables({
          ...params,
          categoryId: String(requestData.value.categoryId)
        })
      } else if (isLicenseRequest.value) {
        res = await pageLicenses({
          ...params,
          categoryId: String(requestData.value.categoryId)
        })
      } else if (isServiceRequest.value) {
        // Services might not have categoryId in the same way or strict filtering, check API usage if needed.
        // Assuming simple search for now
        res = await pageOffering({
          ...params
        })
      }

      if (res) {
        assetList.value = res.records
        pagination.total = res.total
      } else {
        assetList.value = []
        pagination.total = 0
      }
    } catch (error) {
      console.error(error)
      assetList.value = []
    } finally {
      loadingAssets.value = false
    }
  }

  const handleAssetSearch = () => {
    pagination.current = 1
    fetchAssets()
  }

  const handleAssetSelect = (row: any | undefined) => {
    if (row) {
      selectedAssetId.value = row.id
    }
  }

  watch(
    () => visible.value,
    (val) => {
      if (val && requestData.value) {
        note.value = ''
        selectedAssetId.value = undefined
        pagination.current = 1
        assetSearch.name = ''
        fetchAssets()
      }
    }
  )

  const handleSubmit = async () => {
    if (!requestData.value || !selectedAssetId.value) return

    try {
      submitting.value = true
      // Pass selectedAssetId as deviceId (generic allocated item id)
      await approveAndAssignRequest({
        id: requestData.value.id,
        deviceId: selectedAssetId.value,
        note: note.value
      })
      ElMessage.success('审批成功，资产已分配')
      visible.value = false
      emit('success')
    } catch (error) {
      console.error(error)
    } finally {
      submitting.value = false
    }
  }

  const handleReject = async () => {
    if (!requestData.value) return

    try {
      let reason = note.value?.trim()

      if (reason) {
        await ElMessageBox.confirm('确定要驳回该申请吗？', '确认驳回', {
          type: 'warning',
          confirmButtonText: '驳回',
          cancelButtonText: '取消'
        })
      } else {
        const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回申请', {
          inputPattern: /\S/,
          inputErrorMessage: '原因不能为空'
        })
        reason = value
      }

      submitting.value = true
      await rejectRequest({
        id: requestData.value.id,
        reason
      })
      ElMessage.success('申请已驳回')
      visible.value = false
      emit('success')
    } catch {
      // User cancelled or error
    } finally {
      submitting.value = false
    }
  }
</script>

<style scoped>
  .el-table {
    --el-table-header-bg-color: var(--el-fill-color-light);
  }
</style>
