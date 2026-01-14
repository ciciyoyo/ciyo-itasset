<template>
  <el-dialog
    v-model="dialogVisible"
    append-to-body
    :title="$t('system.userChoose.chooseUserDialogTitle')"
    v-if="dialogVisible"
    :fullscreen="screenWidth <= 768"
    :width="screenWidth <= 768 ? '95%' : '90%'"
  >
    <div class="user-choose-table-wrap">
      <el-row :gutter="10">
        <el-col :lg="15" :md="15" :sm="24" :xs="24">
          <!--用户数据-->
          <el-form
            v-show="showSearch"
            ref="queryFormRef"
            :inline="true"
            :model="queryParams"
            label-width="70px"
            size="default"
            label-position="left"
          >
            <el-row :gutter="5">
              <el-col :lg="7" :md="7" :sm="24" :xs="24">
                <el-form-item :label="$t('system.userChoose.userNameLabel')" prop="userName">
                  <el-input
                    class="input-width"
                    size="default"
                    v-model="queryParams.userName"
                    clearable
                    :placeholder="$t('system.userChoose.userNameLabel')"
                    @keyup.enter="handleQuery"
                  />
                </el-form-item>
              </el-col>
              <el-col :lg="7" :md="7" :sm="24" :xs="24">
                <el-form-item :label="$t('system.userChoose.nickNameLabel')" prop="nickName">
                  <el-input
                    class="input-width"
                    v-model="queryParams.nickName"
                    clearable
                    size="default"
                    :placeholder="$t('system.userChoose.nickNameLabel')"
                    @keyup.enter="handleQuery"
                  />
                </el-form-item>
              </el-col>
              <el-col :lg="7" :md="7" :sm="24" :xs="24">
                <el-form-item :label="$t('system.userChoose.phoneNumberLabel')" prop="phonenumber">
                  <el-input
                    class="input-width"
                    v-model="queryParams.phonenumber"
                    clearable
                    size="default"
                    :placeholder="$t('system.userChoose.phoneNumberLabel')"
                    @keyup.enter="handleQuery"
                  />
                </el-form-item>
              </el-col>
              <el-col :lg="2" :md="2" :sm="24" :xs="24">
                <el-form-item>
                  <el-link type="primary" underline="never" @click="moreFilter = !moreFilter">
                    <el-icon>
                      <ele-ArrowDown />
                    </el-icon>
                    <span v-if="!moreFilter">{{ $t('system.userChoose.more') }}</span>
                    <span v-else>{{ $t('system.userChoose.less') }}</span>
                  </el-link>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row v-if="moreFilter">
              <el-col :lg="8" :md="8" :sm="24" :xs="8" :xl="8">
                <el-form-item :label="$t('system.userManagement.belongingDept')" prop="deptId">
                  <el-tree-select
                    v-model="queryParams.deptId"
                    check-strictly
                    lazy
                    class="select-width"
                    :load="loadDeptTree"
                    :cache-data="deptOptions"
                    :props="{ value: 'id', label: 'label', isLeaf: 'isLeaf' }"
                    :placeholder="$t('system.userManagement.belongingDeptPlaceholder')"
                  />
                </el-form-item>
              </el-col>
              <el-col :lg="8" :md="24" :sm="24" :xs="24" :xl="8">
                <el-form-item :label="$t('system.userManagement.userRoles')" label-width="70px" prop="roleIds">
                  <el-select-v2
                    size="default"
                    v-model="queryParams.roleIds"
                    class="select-width"
                    multiple
                    clearable
                    collapse-tags
                    :props="{ value: 'id', label: 'roleName' }"
                    :options="roleList"
                  ></el-select-v2>
                </el-form-item>
              </el-col>
            </el-row>
            <div>
              <el-form-item>
                <el-button size="default" icon="ele-Search" type="primary" @click="handleQuery">
                  {{ $t('common.search') }}
                </el-button>
                <el-button size="default" icon="ele-Refresh" @click="resetQuery">
                  {{ $t('common.reset') }}
                </el-button>
              </el-form-item>
            </div>
          </el-form>
          <el-table
            ref="userTableRef"
            border
            v-loading="loading"
            :data="userList"
            row-key="id"
            @select="handleSelectionChange"
            @select-all="handleSelectionChange"
            :max-height="screenWidth <= 768 ? '400px' : 'auto'"
          >
            <el-table-column v-if="multiple" align="center" type="selection" width="50" :reserve-selection="true" />
            <el-table-column v-else label width="45">
              <template #default="scope">
                <el-radio v-model="radioId" :value="scope.row.id" @change="handleSingleUser(scope.row)"> &nbsp; </el-radio>
              </template>
            </el-table-column>
            <el-table-column key="userId" align="center" :label="$t('system.userChoose.userTableUserId')" prop="id" width="100" />
            <el-table-column
              key="userName"
              :show-overflow-tooltip="true"
              align="center"
              :label="$t('system.userChoose.userTableUserName')"
              prop="userName"
            />
            <el-table-column
              key="nickName"
              :show-overflow-tooltip="true"
              align="center"
              :label="$t('system.userChoose.userTableNickName')"
              prop="nickName"
            />
            <el-table-column
              key="deptName"
              :show-overflow-tooltip="true"
              align="center"
              :label="$t('system.userChoose.userTableDeptName')"
              prop="dept.deptName"
            />
            <el-table-column
              key="phonenumber"
              align="center"
              :label="$t('system.userChoose.userTablePhoneNumber')"
              prop="phonenumber"
              width="130"
            />
            <el-table-column align="center" :label="$t('system.userChoose.userTableCreateTime')" prop="createTime" width="180">
              <template #default="scope">
                <span>{{ parseTime(scope.row.createTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column v-if="screenWidth <= 768" type="expand" width="50">
              <template #default="props">
                <el-form class="table-expand" inline label-position="left">
                  <el-form-item :label="$t('system.userChoose.userTableDeptName')">
                    <span>{{ props.row.dept?.deptName }}</span>
                  </el-form-item>
                  <el-form-item :label="$t('system.userChoose.userTableCreateTime')">
                    <span>{{ parseTime(props.row.createTime) }}</span>
                  </el-form-item>
                </el-form>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            class="mt2 ml2"
            v-show="total > 0"
            v-model:page-size="queryParams.size"
            v-model:current-page="queryParams.current"
            :total="total"
            :page-sizes="[5, 10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="getList"
            @current-change="getList"
          />
        </el-col>
        <el-col :lg="9" :md="9" :sm="24" :xs="24">
          <p class="title-text pl5">{{ $t('system.userChoose.selectUserTitle') }}</p>
          <el-table border :data="selectedUserList" max-height="500">
            <template #empty>
              <el-empty :description="$t('system.userChoose.noData')" />
            </template>
            <el-table-column key="userId" align="center" :label="$t('system.userChoose.userTableUserId')" prop="id" />
            <el-table-column
              key="userName"
              :show-overflow-tooltip="true"
              align="center"
              :label="$t('system.userChoose.userTableUserName')"
              prop="userName"
            />
            <el-table-column
              key="nickName"
              :show-overflow-tooltip="true"
              align="center"
              :label="$t('system.userChoose.userTableNickName')"
              prop="nickName"
            />
            <el-table-column
              align="center"
              class-name="small-padding fixed-width"
              :label="$t('system.userChoose.selectUserTableOperation')"
              width="160"
            >
              <template #default="scope">
                <el-button icon="ele-Delete" link type="primary" @click="deleteSelectedUserHandle(scope.row)">
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
        <el-button type="primary" size="default" @click="submitSelectedUserHandle">
          {{ $t('common.confirm') }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
  import { nextTick, onMounted, reactive, ref } from 'vue'
  import { getDeptTree, getRoleList, pageUser } from '@/api/system/organization'
  import { difference, findIndex, uniqBy } from 'lodash-es'
  import { parseTime, resetFormRef } from '@/utils/business.ts'
  import { useWindowSize } from '@vueuse/core'

  const props = defineProps({
    // 多选
    multiple: {
      type: Boolean,
      default: true
    },
    // 不记录已选用户
    noRecord: {
      type: Boolean,
      default: false
    }
  })
  const emit = defineEmits(['submit'])

  const radioId = ref(null)
  const loading = ref(true)
  const dialogVisible = ref(false)
  const showSearch = ref(true)
  const total = ref(0)
  const userList = ref([])
  const selectedUserList = ref([])
  const defaultSelectRows = ref([])
  const removedSelectRows = ref([])

  const queryParams = reactive({
    current: 1,
    size: 5,
    userName: undefined,
    nickName: undefined,
    phonenumber: undefined,
    status: 0,
    deptId: undefined
  })

  const { width: screenWidth } = useWindowSize()

  onMounted(() => {
    getList()
  })

  const userTableRef = ref(null)

  const showDialog = (userList) => {
    dialogVisible.value = true
    if (props.noRecord) {
      nextTick(() => {
        userTableRef.value.clearSelection()
      })
      selectedUserList.value = []
    } else {
      removedSelectRows.value = difference(selectedUserList.value, userList).map((item) => item.id)
      selectedUserList.value = userList
      defaultSelectRows.value = userList.map((item) => item.id)
      nextTick(() => {
        handleDefaultRowSelection()
      })
    }
    getTreeselect()
    getRoleList({}, true).then((res) => {
      roleList.value = res
    })
  }

  defineExpose({
    showDialog
  })

  const getList = () => {
    loading.value = true
    pageUser(queryParams, true).then((response) => {
      userList.value = response.records
      total.value = response.total
      loading.value = false
      nextTick(() => {
        handleDefaultRowSelection()
      })
    })
  }

  const handleQuery = () => {
    queryParams.current = 1
    getList()
  }

  const queryFormRef = ref(null)

  const resetQuery = () => {
    resetFormRef(queryFormRef)
    handleQuery()
  }

  const handleSelectionChange = (selection) => {
    selectedUserList.value = uniqBy(selection, 'id')
  }

  const deleteSelectedUserHandle = (row) => {
    const index = findIndex(selectedUserList.value, ['id', row.id])
    selectedUserList.value.splice(index, 1)
    const user = userList.value.find((item) => item.id == row.id)
    userTableRef.value.toggleRowSelection(user, false)
    removedSelectRows.value.push(row.id)
  }

  const handleDefaultRowSelection = () => {
    if (defaultSelectRows.value.length === 0 && removedSelectRows.value.length === 0) {
      return
    }
    if (!userTableRef.value) return

    userList.value.forEach((user) => {
      const index = defaultSelectRows.value.findIndex((item) => user.id == item)
      if (index !== -1) {
        userTableRef.value.toggleRowSelection(user, true)
        defaultSelectRows.value.splice(index, 1)
      }

      const index1 = removedSelectRows.value.findIndex((item) => user.id == item)
      if (index1 !== -1) {
        nextTick(() => {
          if (userTableRef.value) {
            userTableRef.value.toggleRowSelection(user, false)
          }
        })
        removedSelectRows.value.splice(index1, 1)
      }
    })
  }

  const handleSingleUser = (user) => {
    selectedUserList.value = [user]
  }

  const submitSelectedUserHandle = () => {
    dialogVisible.value = false
    emit('submit', selectedUserList.value)
  }

  const deptOptions = ref(undefined)

  const roleList = ref([])

  const moreFilter = ref(false)

  const deptList = ref([])

  const defaultExpandedKeys = ref([])
  const getTreeselect = () => {
    getDeptTree().then((response) => {
      deptOptions.value = response
      const result = []
      deptList.value = flattenTree(response, result)
      const getFirstLevelKeys = (nodes) => {
        const keys = []
        nodes.forEach((node) => {
          keys.push(node.id)
        })
        return keys
      }
      defaultExpandedKeys.value = getFirstLevelKeys(response || [])
    })
  }
  const flattenTree = (nodes, result, parentId) => {
    if (!nodes || !Array.isArray(nodes)) {
      return result
    }

    nodes.forEach((node) => {
      const item = {
        id: node.id,
        label: node.label,
        parentId: parentId,
        level: node.level || 0,
        isLeaf: true // 默认是叶子节点
      }
      // 递归处理子节点
      if (node.children && node.children.length > 0) {
        flattenTree(node.children, result, node.id)
        item.isLeaf = false // 有子节点时，不是叶子节点，应该显示展开箭头
      }
      result.push(item)
    })
    return result
  }

  const loadDeptTree = (node, resolve) => {
    if (node.isLeaf) return resolve([])
    const datas = deptList.value.filter((item) => {
      if (!node?.data.id) {
        return !item.parentId
      }
      return item.parentId == node.data.id
    })
    resolve(datas)
  }
</script>
<style scoped lang="scss">
  :deep(.el-form--inline .el-form-item) {
    margin-right: 0 !important;
  }

  .input-width {
    width: 100%;
  }

  .table-expand {
    padding: 10px;

    .el-form-item {
      margin-right: 0;
      margin-bottom: 0;
      width: 100%;
    }
  }

  .title-text {
    margin-bottom: 10px;
  }

  @media screen and (max-width: 768px) {
    .app-container {
      padding-bottom: 60px;
    }
  }
</style>
