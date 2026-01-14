<template>
    <el-dialog title="分配设备" v-model="visible" width="500px" append-to-body @close="reset">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
            <el-form-item label="分配给谁" prop="ownerId">
                <el-input v-model="form.ownerName" placeholder="请选择人员" readonly @click="handleOpenUserSelect">
                    <template #append>
                        <el-button @click="handleOpenUserSelect">选择</el-button>
                    </template>
                </el-input>
            </el-form-item>
            <el-form-item label="备注" prop="note">
                <el-input v-model="form.note" type="textarea" :rows="3" placeholder="请输入备注信息" />
            </el-form-item>
        </el-form>
        <template #footer>
            <div class="dialog-footer" style="display: flex; justify-content: flex-end">
                <el-button :loading="loading" @click="visible = false"> 取消 </el-button>
                <el-button :loading="loading" type="primary" @click="submit"> 提交 </el-button>
            </div>
        </template>

        <!-- 用户选择对话框 -->
        <user-choose-table ref="userChooseTableRef" :multiple="false" @submit="handleUserSelectSubmit" />
    </el-dialog>
</template>

<script setup lang="ts">
import { ref, useTemplateRef } from 'vue'
import { allocateDevice, DeviceEntity } from '@/api/itam/device'
import { AssetType } from '@/api/itam/enums'
import { MessageUtil } from '@/utils/messageUtil'
import { resetFormRef } from '@/utils/business'
import UserChooseTable from '@/views/system/user/chooseTable.vue'

const visible = ref(false)
const loading = ref(false)
const formRef = useTemplateRef('formRef')
const userChooseTableRef = useTemplateRef('userChooseTableRef')
const currentDevice = ref<DeviceEntity | null>(null)

const form = ref({
    ownerId: undefined as number | undefined,
    ownerName: '',
    note: ''
})

const rules = {
    ownerId: [{ required: true, message: '请选择分配人员', trigger: 'change' }]
}

const emit = defineEmits(['success'])

const open = (row: DeviceEntity) => {
    reset()
    currentDevice.value = row
    visible.value = true
}

const reset = () => {
    form.value = {
        ownerId: undefined,
        ownerName: '',
        note: ''
    }
    currentDevice.value = null
    resetFormRef(formRef)
}

const handleOpenUserSelect = () => {
    userChooseTableRef.value?.showDialog([])
}

const handleUserSelectSubmit = (val: any[]) => {
    if (val && val.length > 0) {
        const user = val[0]
        form.value.ownerId = user.id
        form.value.ownerName = user.nickName
    }
}

const submit = () => {
    formRef.value!.validate((valid: boolean) => {
        if (valid && currentDevice.value) {
            loading.value = true
            allocateDevice({
                itemType: AssetType.DEVICE,
                itemId: currentDevice.value.id!,
                ownerType: 'user',
                ownerId: form.value.ownerId!,
                note: form.value.note
            })
                .then(() => {
                    MessageUtil.success('分配成功')
                    visible.value = false
                    emit('success')
                })
                .finally(() => {
                    loading.value = false
                })
        }
    })
}

defineExpose({
    open
})
</script>

<style scoped lang="scss"></style>
