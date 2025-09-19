<template>
  <div v-if="isVisible" class="alert-overlay" @click="closeAlert">
    <div class="alert-container" @click.stop>
      <div class="alert-header">
        <div class="alert-icon">
          <span v-if="type === 'success'">✅</span>
          <span v-else-if="type === 'error'">❌</span>
          <span v-else-if="type === 'warning'">⚠️</span>
          <span v-else-if="type === 'confirm'">❓</span>
          <span v-else>ℹ️</span>
        </div>
        <h3 class="alert-title">{{ title }}</h3>
      </div>
      
      <div class="alert-content">
        <p>{{ message }}</p>
      </div>
      
      <div class="alert-actions">
        <button 
          v-if="showCancel" 
          @click="handleCancel" 
          class="alert-btn cancel-btn"
        >
          {{ cancelText }}
        </button>
        <button 
          @click="handleConfirm" 
          class="alert-btn confirm-btn"
          :class="type"
        >
          {{ confirmText }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

interface Props {
  title: string;
  message: string;
  type?: 'success' | 'error' | 'warning' | 'info' | 'confirm';
  showCancel?: boolean;
  confirmText?: string;
  cancelText?: string;
  autoClose?: boolean;
  autoCloseDelay?: number;
}

const props = withDefaults(defineProps<Props>(), {
  type: 'info',
  showCancel: false,
  confirmText: '확인',
  cancelText: '취소',
  autoClose: false,
  autoCloseDelay: 3000
});

const emit = defineEmits<{
  confirm: [];
  cancel: [];
  close: [];
}>();

const isVisible = ref(false);

onMounted(() => {
  isVisible.value = true;
  
  if (props.autoClose) {
    setTimeout(() => {
      closeAlert();
    }, props.autoCloseDelay);
  }
});

function handleConfirm() {
  emit('confirm');
  closeAlert();
}

function handleCancel() {
  emit('cancel');
  closeAlert();
}

function closeAlert() {
  isVisible.value = false;
  setTimeout(() => {
    emit('close');
  }, 300); // 애니메이션 시간과 맞춤
}
</script>

<style scoped>
.alert-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.3s ease;
}

.alert-container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  max-width: 400px;
  width: 90%;
  animation: slideIn 0.3s ease;
  overflow: hidden;
}

.alert-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 24px 16px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.alert-icon {
  font-size: 24px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #f8f9fa;
}

.alert-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.alert-content {
  padding: 16px 24px;
}

.alert-content p {
  margin: 0;
  font-size: 16px;
  color: #666;
  line-height: 1.6;
}

.alert-actions {
  display: flex;
  gap: 12px;
  padding: 16px 24px 24px 24px;
  justify-content: flex-end;
}

.alert-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 80px;
}

.cancel-btn {
  background: #f8f9fa;
  color: #666;
  border: 1px solid #dee2e6;
}

.cancel-btn:hover {
  background: #e9ecef;
}

.confirm-btn {
  background: #007bff;
  color: white;
}

.confirm-btn:hover {
  background: #0056b3;
  transform: translateY(-1px);
}

.confirm-btn.success {
  background: #28a745;
}

.confirm-btn.success:hover {
  background: #218838;
}

.confirm-btn.error {
  background: #dc3545;
}

.confirm-btn.error:hover {
  background: #c82333;
}

.confirm-btn.warning {
  background: #ffc107;
  color: #212529;
}

.confirm-btn.warning:hover {
  background: #e0a800;
}

.confirm-btn.confirm {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.confirm-btn.confirm:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
</style>
