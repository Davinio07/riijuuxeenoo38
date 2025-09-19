import { createApp } from 'vue'
import App from './App.vue'
import router from './router' // Importeer de router

const app = createApp(App)

app.use(router) // Vertel de Vue app om de router te gebruiken

app.mount('#app')
