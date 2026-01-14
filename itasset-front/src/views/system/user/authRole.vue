<template>
  <div class="app-container">
    <h4 class="form-header h4">{{ $t('system.userManagement.info') }}</h4>
    <el-form ref="form" :model="form" label-width="80px">
      <el-row>
        <el-col :offset="2" :span="8">
          <el-form-item :label="$t('system.userManagement.userName')" prop="nickName">
            <el-input v-model="form.nickName" disabled />
          </el-form-item>
        </el-col>
        <el-col :offset="2" :span="8">
          <el-form-item :label="$t('system.userManagement.loginAccount')" prop="phonenumber">
            <el-input v-model="form.userName" disabled />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <h4 class="form-header h4">{{ $t('system.userManagement.roleInfo') }}</h4>
    <el-table
      ref="table"
      v-loading="loading"
      :data="roles.slice((current - 1) * size, current * size)"
      :row-key="getRowKey"
      @row-click="clickRow"
      @selection-change="handleSelectionChange"
    >
      <el-table-column align="center" label="ID" type="index">
        <template #default="scope">
          <span>{{ (current - 1) * size + scope.$index + 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column :reserve-selection="true" type="selection" width="55" />
      <el-table-column align="center" :label="$t('system.userManagement.roleCode')" prop="id" />
      <el-table-column align="center" :label="$t('system.userManagement.roleName')" prop="roleName" />
      <el-table-column align="center" :label="$t('system.userManagement.roleKey')" prop="roleKey" />
      <el-table-column align="center" :label="$t('system.userManagement.createTime')" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" v-model:limit="size" v-model:page="current" :total="total" />

    <el-form label-width="700px">
      <el-form-item style="text-align: center; margin-top: 30px">
        <el-button type="primary" @click="submitForm()">
          {{ $t('common.confirm') }}
        </el-button>
        <el-button @click="close()">{{ $t('common.cancel') }}</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
  import { getAuthRole, updateAuthRole } from '@/api/system/user'
  import { i18n } from '@/i18n'
  import { useWorktabStore } from '@/store/modules/worktab'

  export default {
    name: 'AuthRole',
    setup() {
      const worktabStore = useWorktabStore()
      return {
        worktabStore
      }
    },
    data() {
      return {
        // 遮罩层
        loading: true,
        // 分页信息
        total: 0,
        current: 1,
        size: 10,
        // 选中角色编号
        roleIds: [],
        // 角色信息
        roles: [],
        // 用户信息
        form: {}
      }
    },
    created() {
      const userId = this.$route.params && this.$route.params.userId
      if (userId) {
        this.loading = true
        getAuthRole(userId).then((response) => {
          this.form = response.data.user
          this.roles = response.data.roles
          this.total = this.roles.length
          this.$nextTick(() => {
            this.roles.forEach((row) => {
              if (row.flag) {
                this.$refs.table.toggleRowSelection(row)
              }
            })
          })
          this.loading = false
        })
      }
    },
    methods: {
      /** 单击选中行数据 */
      clickRow(row) {
        this.$refs.table.toggleRowSelection(row)
      },
      // 多选框选中数据
      handleSelectionChange(selection) {
        this.roleIds = selection.map((item) => item.id)
      },
      // 保存选中的数据编号
      getRowKey(row) {
        return row.id
      },
      /** 提交按钮 */
      submitForm() {
        const userId = this.form.id
        const roleIds = this.roleIds.join(',')
        updateAuthRole({ userId: userId, roleIds: roleIds }).then((response) => {
          this.msgSuccess(i18n.global.t('common.saveSuccess'))
          this.close()
        })
      },
      /** 关闭按钮 */
      close() {
        this.worktabStore.removeTab(this.$route.path)
        this.$router.push({ path: '/sys/user' })
      }
    }
  }
</script>
