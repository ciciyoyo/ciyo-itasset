<template>
  <el-dialog :title="$t('form.setting.selectRole')" v-model="dialogVisible" append-to-body width="80%">
    <div class="app-container">
      <el-row :gutter="10">
        <el-col :span="15">
          <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" @submit.prevent>
            <el-form-item :label="$t('form.setting.roleName')" prop="roleName">
              <el-input
                v-model="queryParams.roleName"
                clearable
                :placeholder="$t('form.setting.roleName')"
                style="width: 240px"
                @keyup.enter="handleQuery"
              />
            </el-form-item>
            <el-form-item>
              <el-button icon="ele-Search" type="primary" @click="handleQuery">
                {{ $t('common.search') }}
              </el-button>
              <el-button icon="ele-Refresh" @click="resetQuery">
                {{ $t('common.reset') }}
              </el-button>
            </el-form-item>
          </el-form>

          <el-table
            ref="roleTable"
            v-loading="loading"
            :data="roleList"
            row-key="id"
            @select="handleSelectionChange"
            @select-all="handleSelectionChange"
          >
            <el-table-column align="center" type="selection" :reserve-selection="true" />
            <el-table-column :label="$t('form.setting.roleID')" prop="id" />
            <el-table-column :show-overflow-tooltip="true" :label="$t('form.setting.roleName')" prop="roleName" />
            <el-table-column :show-overflow-tooltip="true" :label="$t('form.setting.permissionKey')" prop="roleKey" />
          </el-table>

          <pagination
            v-show="total > 0"
            v-model:limit="queryParams.size"
            v-model:page="queryParams.current"
            :total="total"
            @pagination="getList"
          />
        </el-col>
        <el-col :span="9">
          <p class="title-text pl5">{{ $t('form.setting.selectedRoleList') }}</p>
          <el-table class="mt10" border :data="selectedRoleList" max-height="500">
            <template #empty>
              <el-empty :description="$t('form.setting.noSelectedRoles')" />
            </template>
            <el-table-column :label="$t('form.setting.roleID')" prop="id" />
            <el-table-column :show-overflow-tooltip="true" :label="$t('form.setting.roleNameLabel')" prop="roleName" />
            <el-table-column align="center" class-name="small-padding fixed-width" :label="$t('form.setting.operation')">
              <template #default="scope">
                <el-button icon="ele-Delete" link type="primary" @click="deleteSelectedRoleHandle(scope.row)">
                  {{ $t('common.delete') }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button size="default" @click="dialogVisible = false">
          {{ $t('common.cancel') }}
        </el-button>
        <el-button size="default" type="primary" @click="submitSelectedRoleHandle">
          {{ $t('common.confirm') }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script>
  import { pageRole } from '@/api/system/organization'
  import { difference, findIndex, uniqBy } from 'lodash-es'

  export default {
    name: 'RoleChooseTable',
    emits: ['submit'],
    data() {
      return {
        // 遮罩层
        loading: true,
        dialogVisible: false,
        // 显示搜索条件
        showSearch: true,
        // 总条数
        total: 0,
        // 角色表格数据
        roleList: [],
        // 用户表格数据
        selectedRoleList: null,
        // 记录默认选中的行Id
        defaultSelectRows: [],
        // 记录被删除的行id 避免分页导致选中状态未被取消
        removedSelectRows: [],
        // 查询参数
        queryParams: {
          current: 1,
          size: 10,
          roleName: undefined,
          roleKey: undefined,
          status: '0'
        }
      }
    },
    created() {
      this.getList()
    },
    methods: {
      // 显示角色选择弹窗
      showDialog(roleList) {
        this.dialogVisible = true
        // 比较删除掉的
        this.removedSelectRows = difference(this.selectedRoleList, roleList).map((item) => item.id)
        this.selectedRoleList = roleList
        // 默认选中
        this.defaultSelectRows = roleList.map((item) => item.id)
        this.$nextTick(() => {
          this.handleDefaultRowSelection()
        })
      },
      /** 查询角色列表 */
      getList() {
        this.loading = true
        pageRole(this.queryParams, true).then((response) => {
          this.roleList = response.data.records
          this.total = response.data.total
          this.loading = false
          this.handleDefaultRowSelection()
        })
      },
      handleSelectionChange(selection) {
        console.log(selection)
        this.selectedRoleList = uniqBy(selection, 'id')
      },
      /** 搜索按钮操作 */
      handleQuery() {
        this.queryParams.current = 1
        this.getList()
      },
      /** 重置按钮操作 */
      resetQuery() {
        this.resetForm('queryForm')
        this.handleQuery()
      },
      deleteSelectedRoleHandle(row) {
        let index = findIndex(this.selectedRoleList, ['id', row.id])
        this.selectedRoleList.splice(index, 1)
        let role = this.roleList.find((item) => item.id == row.id)
        this.$refs.roleTable.toggleRowSelection(role, false)
        // 记录 避免不在同一页导致删除失败
        this.removedSelectRows.push(row.id)
      },
      handleDefaultRowSelection() {
        // 比较对象 toggleRowSelection 不是同一个对象不生效。。。
        if (this.defaultSelectRows.length == 0 && this.removedSelectRows.length == 0) {
          return
        }
        this.roleList.forEach((role) => {
          // 设置选中
          let index = this.defaultSelectRows.findIndex((item) => role.id == item)
          if (index !== -1) {
            this.$refs.roleTable.toggleRowSelection(role, true)
            this.defaultSelectRows.splice(index, 1)
          }
          // 取消选中
          let index1 = this.removedSelectRows.findIndex((item) => role.id == item)
          if (index1 !== -1) {
            this.$nextTick(() => {
              this.$refs.roleTable.toggleRowSelection(role, false)
            })
            this.removedSelectRows.splice(index, 1)
          }
        })
      },
      submitSelectedRoleHandle() {
        this.dialogVisible = false
        this.$emit('submit', this.selectedRoleList)
      }
    }
  }
</script>
