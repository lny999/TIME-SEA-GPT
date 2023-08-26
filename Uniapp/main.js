import App from './App'

// #ifndef VUE3
// #ifdef VUE3
import Vue, {createSSRApp} from 'vue'
import './uni.promisify.adaptor'
import share from '@/wxcomponents/share/share.js'

Vue.config.productionTip = false
Vue.mixin(share)
App.mpType = 'app'
const app = new Vue({
  ...App
})
app.$mount()
// #endif
export function createApp() {
  const app = createSSRApp(App)
  return {
    app
  }
}
// #endif
