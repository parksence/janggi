import { ref, createApp, type App } from 'vue';
import CustomAlert from '@/components/CustomAlert.vue';

interface AlertOptions {
  title: string;
  message: string;
  type?: 'success' | 'error' | 'warning' | 'info';
  showCancel?: boolean;
  confirmText?: string;
  cancelText?: string;
  autoClose?: boolean;
  autoCloseDelay?: number;
}

interface AlertInstance {
  app: App;
  container: HTMLElement;
}

const alertInstances = ref<AlertInstance[]>([]);

export function useAlert() {
  function showAlert(options: AlertOptions): Promise<boolean> {
    return new Promise((resolve) => {
      // 컨테이너 생성
      const container = document.createElement('div');
      document.body.appendChild(container);
      
      // Vue 앱 생성
      const app = createApp(CustomAlert, {
        ...options,
        onConfirm: () => {
          resolve(true);
        },
        onCancel: () => {
          resolve(false);
        },
        onClose: () => {
          // 정리 작업
          setTimeout(() => {
            app.unmount();
            document.body.removeChild(container);
            
            // 인스턴스 목록에서 제거
            const index = alertInstances.value.findIndex(instance => instance.app === app);
            if (index > -1) {
              alertInstances.value.splice(index, 1);
            }
          }, 100);
        }
      });
      
      // 마운트
      app.mount(container);
      
      // 인스턴스 저장
      alertInstances.value.push({ app, container });
    });
  }
  
  // 편의 메서드들
  function success(title: string, message: string, options?: Partial<AlertOptions>) {
    return showAlert({
      title,
      message,
      type: 'success',
      autoClose: true,
      autoCloseDelay: 2000,
      ...options
    });
  }
  
  function error(title: string, message: string, options?: Partial<AlertOptions>) {
    return showAlert({
      title,
      message,
      type: 'error',
      ...options
    });
  }
  
  function warning(title: string, message: string, options?: Partial<AlertOptions>) {
    return showAlert({
      title,
      message,
      type: 'warning',
      ...options
    });
  }
  
  function info(title: string, message: string, options?: Partial<AlertOptions>) {
    return showAlert({
      title,
      message,
      type: 'info',
      autoClose: true,
      autoCloseDelay: 3000,
      ...options
    });
  }
  
  function confirm(title: string, message: string, options?: Partial<AlertOptions>) {
    return showAlert({
      title,
      message,
      type: 'confirm',
      showCancel: true,
      confirmText: '확인',
      cancelText: '취소',
      ...options
    });
  }
  
  return {
    showAlert,
    success,
    error,
    warning,
    info,
    confirm
  };
}
