<template>
  <div class="user-info-head" @click="editCropper()">
    <img :src="avatarUrl" :title="$t('system.userAvatar.title')" class="img-circle img-lg" />
    <el-dialog :title="title" v-model="open" width="800px" append-to-body @opened="modalOpened" @close="closeDialog">
      <el-row>
        <el-col :xs="24" :md="12" :style="{ height: '350px' }">
          <vue-cropper
            ref="cropper"
            :img="options.img"
            :info="true"
            :auto-crop="options.autoCrop"
            :auto-crop-width="options.autoCropWidth"
            :auto-crop-height="options.autoCropHeight"
            :fixed-box="options.fixedBox"
            :output-type="options.outputType"
            @realTime="realTime"
            v-if="visible"
          />
        </el-col>
        <el-col :xs="24" :md="12" :style="{ height: '350px' }">
          <div class="preview-container">
            <div class="avatar-upload-preview">
              <img v-if="previewImg" :src="previewImg" />
            </div>
          </div>
        </el-col>
      </el-row>
      <br />
      <el-row>
        <el-col :lg="2" :md="2">
          <el-upload action="#" :http-request="requestUpload" :show-file-list="false" :before-upload="beforeUpload">
            <el-button>
              {{ $t('system.userAvatar.choose') }}
              <el-icon class="el-icon--right">
                <ele-Upload />
              </el-icon>
            </el-button>
          </el-upload>
        </el-col>
        <el-col :lg="{ span: 1, offset: 2 }" :md="2">
          <el-button icon="ele-Plus" @click="changeScale(1)"></el-button>
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :md="2">
          <el-button icon="ele-Minus" @click="changeScale(-1)"></el-button>
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :md="2">
          <el-button icon="ele-RefreshLeft" @click="rotateLeft()"></el-button>
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :md="2">
          <el-button icon="ele-RefreshRight" @click="rotateRight()"></el-button>
        </el-col>
        <el-col :lg="{ span: 2, offset: 6 }" :md="2">
          <el-button type="primary" @click="uploadImg()">
            {{ $t('system.userAvatar.submit') }}
          </el-button>
        </el-col>
      </el-row>
    </el-dialog>
  </div>
</template>

<script setup name="UserAvatar">
  import 'vue-cropper/dist/index.css'
  import { VueCropper } from 'vue-cropper'
  import { uploadAvatar } from '@/api/system/user'
  import { useUserStore } from '@/store/modules/user'
  import { getCurrentInstance, reactive, ref, computed } from 'vue'
  import { i18n } from '@/i18n'
  import { MessageUtil } from '@/utils/messageUtil'

  const userStore = useUserStore()
  const { proxy } = getCurrentInstance()

  const open = ref(false)
  const visible = ref(false)
  const title = ref(i18n.global.t('system.userAvatar.modify'))
  const previewImg = ref('')

  // 获取头像 URL
  const avatarUrl = computed(() => {
    const avatar = userStore.getUserInfo.avatar
    return avatar
  })

  //图片裁剪数据
  const options = reactive({
    img: avatarUrl.value, // 裁剪图片的地址
    autoCrop: true, // 是否默认生成截图框
    autoCropWidth: 200, // 默认生成截图框宽度
    autoCropHeight: 200, // 默认生成截图框高度
    fixedBox: true, // 固定截图框大小 不允许改变
    outputType: 'png' // 默认生成截图为PNG格式
  })

  /** 编辑头像 */
  function editCropper() {
    // 每次打开对话框时,使用最新的头像
    options.img = avatarUrl.value
    open.value = true
  }

  /** 打开弹出层结束时的回调 */
  function modalOpened() {
    visible.value = true
  }

  /** 覆盖默认上传行为 */
  function requestUpload() {}

  /** 向左旋转 */
  function rotateLeft() {
    proxy.$refs.cropper.rotateLeft()
  }

  /** 向右旋转 */
  function rotateRight() {
    proxy.$refs.cropper.rotateRight()
  }

  /** 图片缩放 */
  function changeScale(num) {
    num = num || 1
    proxy.$refs.cropper.changeScale(num)
  }

  /** 上传预处理 */
  function beforeUpload(file) {
    if (file.type.indexOf('image/') == -1) {
      MessageUtil.error(i18n.global.t('system.userAvatar.error'))
    } else {
      const reader = new FileReader()
      reader.readAsDataURL(file)
      reader.onload = () => {
        options.img = reader.result
      }
    }
  }

  const emits = defineEmits(['uploadSuccess'])

  /** 上传图片 */
  function uploadImg() {
    proxy.$refs.cropper.getCropBlob((data) => {
      let formData = new FormData()
      formData.append('avatarfile', data)
      uploadAvatar(formData)
        .then((response) => {
          console.log('Upload response:', response)

          // 获取图片 URL,支持多种响应格式
          let imgUrl = response?.data?.imgUrl || response?.imgUrl || response?.data

          if (!imgUrl) {
            MessageUtil.error('上传失败:未获取到图片地址')
            console.error('Invalid response structure:', response)
            return
          }

          open.value = false
          options.img = imgUrl
          // 更新用户信息中的头像
          const currentInfo = userStore.getUserInfo
          if (currentInfo) {
            userStore.setUserInfo({
              ...currentInfo,
              avatar: imgUrl
            })
          }
          MessageUtil.success(i18n.global.t('common.saveSuccess'))
          visible.value = false
          emits('uploadSuccess', imgUrl)
        })
        .catch((error) => {
          console.error('Upload error:', error)
          MessageUtil.error('上传失败:' + (error.message || '未知错误'))
        })
    })
  }

  /** 实时预览 */
  function realTime(data) {
    // 使用 getCropData 获取裁剪后的图片作为预览
    proxy.$refs.cropper.getCropData((data) => {
      previewImg.value = data
    })
  }

  /** 关闭窗口 */
  function closeDialog() {
    options.img = avatarUrl.value
    visible.value = false
  }
</script>

<style lang="scss" scoped>
  .user-info-head {
    position: relative;
    display: inline-block;
    width: 100px;
    height: 100px;
  }

  .user-info-head img {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    object-fit: cover;
    display: block;
  }

  .user-info-head:hover:after {
    content: '+';
    position: absolute;
    left: 0;
    right: 0;
    top: 0;
    bottom: 0;
    color: #eee;
    background: rgba(0, 0, 0, 0.5);
    font-size: 24px;
    font-style: normal;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    cursor: pointer;
    line-height: 100px;
    border-radius: 50%;
  }

  .preview-container {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
  }

  .avatar-upload-preview {
    width: 200px;
    height: 200px;
    border-radius: 50%;
    border: 1px solid #d9d9d9;
    box-shadow: 0 0 4px #ccc;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f5f5;
  }

  .avatar-upload-preview img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
</style>
