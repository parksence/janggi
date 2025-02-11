import { createApp } from 'vue';
import { createPinia } from "pinia";
import App from './App.vue';
import router from './router';
import './assets/styles/global.css';

const app = createApp(App);
const pinia = createPinia();  // Pinia 인스턴스 생성

app.use(pinia); // Pinia를 등록해야 Pinia 스토어 사용 가능
app.use(router); // 라우터 등록
app.mount('#app'); // 모든 설정 후 mount 실행
