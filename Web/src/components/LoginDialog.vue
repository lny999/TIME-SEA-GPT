<template>
  <el-dialog v-model="dialogVisible" width="330px" class="login-dialog" :show-close="false" align-center @close="close"
             style="border-radius: 10px; overflow-x: hidden;">
    <div class="login-box">
      <div class="head">
        <div class="head_img"></div>
      </div>
      <div class="login-title">扫码登录</div>
      <div class="form">
        <div class="animation" v-if="!qrCodeLoaded">
          <view class="loading-model">
            <view class="loader"></view>
          </view>
        </div>
        <div class="content" v-if="qrCode">
          <div style="position: relative;background-color: white;  border-radius: 5px;">
            <img :src="qrCode" class="qc_code" alt="二维码">
            <div class="cover-div" v-if="isFailure">
              二维码已失效
            </div>
          </div>

        </div>
      </div>
      <div class="btn-generate" v-if="isFailure">
        <el-button type="primary" color="#626aef" @click="getLoginQRCode()">重新生成</el-button>
      </div>
      <div class="h5 prompt-style" v-if="!loginAnimation">
        正在加载中...
      </div>
      <div class="h5 prompt-style" v-if="!promptAnimation">
        使用微信扫一扫快速登录后使用
      </div>
    </div>
  </el-dialog>
</template>

<script>
import {defineComponent, ref, watch} from "vue";

import {GetUserInfo, GetWechatCode, isQrCodeLoginSucceed} from "../../api/BSideApi";
import {ElNotification} from "element-plus";
import store from "@/store";


export default defineComponent({
  name: "LoginDialog",
  props: {
    show: {
      type: Boolean,
      default: false
    }
  },
  setup(props, {
    emit
  }) {
    let qrCode = ref('')
    let qrCodeLoaded = ref(false)
    let promptAnimation = ref(true)
    let loginAnimation = ref(false)
    let loginLoading = ref(false)
    let dialogVisible = ref(false)
    let isLogin = ref(true)
    // eslint-disable-next-line no-unused-vars
    let verifyCode = ref('')
    let isFailure = ref(false)
    let timerId;
    let lock = ref(false)
    watch(() => props.show, (newValue) => {
      if (newValue) {
        getLoginQRCode()
        dialogVisible.value = true

      }
    }, {
      immediate: true
    })


    /**
     * 获取登录二维码
     */
    async function getLoginQRCode() {
      try {
        let newVar = await GetWechatCode();
        if (newVar) {
          isFailure.value = false
          verifyCode.value = newVar.verifyCode
          qrCode.value = newVar.qrCode
          qrCodeLoaded.value = true
          loginAnimation.value = true
          promptAnimation.value = false
          timerId = setInterval(() => {
            scanCodeLoginResults();
          }, 5000);
        }
      } catch (e) {
        console.log(e)
      }
    }

    /**
     * 扫码登录结果
     * @returns {Promise<void>}
     */
    async function scanCodeLoginResults() {
      try {
        let promise = await isQrCodeLoginSucceed(verifyCode.value);
        if (promise) {
          if (!lock.value) {
            lock.value = true;
            clearInterval(timerId);
            localStorage.setItem('token', promise);
            dialogVisible.value = false
            loginLoading.value = false
            try {
              let res = await GetUserInfo()
              store.commit("setUserinfo", res);
              // eslint-disable-next-line no-empty
            } catch (e) {
              console.log(e)
            }
            ElNotification({
              title: '登录成功',
              message: '欢迎使用TIME SEA PLUS',
              type: 'success',
            })
            location.reload();
            lock.value = false
          }

        }
      } catch (e) {
        clearInterval(timerId);
        isFailure.value = true
      }

    }


    function close() {
      emit('close')
    }

    return {
      qrCode,
      qrCodeLoaded,
      loginAnimation,
      promptAnimation,
      loginLoading,
      dialogVisible,
      close,
      isLogin,
      getLoginQRCode,
      isFailure
    }
  }
});
</script>

<style scoped>


.animation {
  width: 160px;
  height: 160px;
  overflow: hidden;
  margin: 20px auto 0;
  background-color: rgb(53, 57, 60);
}

.animation img {
  width: 100%;
  height: 100%;
  object-fit: none;
}

.h5 {
  font-size: 10px;
}

@media (max-width: 767px) {
  .h5 {
    font-size: 12px;
  }
}


.login-box {

  overflow: hidden;
  width: 100%;
  padding: 0;
  background-color: rgb(27,30,32)
}

.cover-div {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(27,30,32, 0.8);
  padding: 10px;
  border-radius: 5px;
  width: 150px;
  height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: white;
}

.login-box > .head > img {
  display: block;
  width: 100%;
  margin: 0 auto;
  user-select: none;
  margin-bottom: -20px;
}

.head_img {
  background-image: linear-gradient(to top, rgb(27,30,32) 30%, transparent 100%), url("../assets/login-header.png");
  background-size: cover;
  background-position: center;
  height: 100px;
  box-shadow: 0px -10px 20px rgba(27,30,32, 0.2);
}

.login-dialog {

}


.form {
  position: relative;
  padding: 10px;
}

.submit-button {
  width: 100%;
  letter-spacing: 2px;
  font-weight: 300;
  margin-top: 15px;
  --el-button-bg-color: #686efe;
  border: none;
}

.submit-button:hover,
.submit-button:focus,
.submit-button:active {
  background-color: #7d80ff;
  outline: 0;
}


.form > .content {
  display: flex;
  justify-content: center;
  align-items: center;


}

.loader {
  width: 20px;
  height: 20px;
  position: relative;
}


.loader:before,
.loader:after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.loader:before {
  width: 15px;
  height: 15px;
  border-radius: 50%;
  background: #c2a5ff;
  animation: preloader_4_before 1.5s infinite ease-in-out;
}

.loader:after {
  width: 15px;
  height: 15px;
  border-radius: 50%;
  background: #ffa4c8;
  left: 5px;
  animation: preloader_4_after 1.5s infinite ease-in-out;
}

@keyframes preloader_4_before {
  0% {
    transform: translateX(0px) rotate(0deg);
  }
  50% {
    transform: translateX(15px) scale(1.2) rotate(260deg);
    background: #cca7f1;
    border-radius: 0px;
  }
  100% {
    transform: translateX(0px) rotate(0deg);
  }
}

@keyframes preloader_4_after {
  0% {
    transform: translateX(0px);
  }
  50% {
    transform: translateX(-15px) scale(1.2) rotate(-260deg);
    background: #9dbefa;
    border-radius: 0;
  }
  100% {
    transform: translateX(0px);
  }
}

.loading-model {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 160px;
  height: 160px;

}

.login-title {
  text-align: center;
  font-weight: 550;
  padding-bottom: 10px;
  color: #bababa;
}

.qc_code {
  width: 140px;
  height: 140px;
  padding: 10px;

}

.btn-generate {
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 20px
}

.prompt-style {
  text-align: center;
  padding-top: 30px;
  padding-bottom: 50px
}

</style>
