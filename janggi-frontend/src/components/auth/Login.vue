<template>
  <div class="login-container">
    <div class="logo-section">
      <img src="/favicon2.png" alt="logo" width="100px" height="100px" />
    </div>
    
    <div class="auth-container">
      <h2>Welcome back.</h2>
      <p class="subtitle">Sign in to continue</p>

      <div class="auth-buttons">
        <button class="auth-button google-button" @click="googleLogin">
          <img src="@/assets/google-icon.svg" alt="Google" class="provider-icon" />
          Continue with Google
        </button>

        <button class="auth-button kakao-button" disabled>
          <img src="@/assets/kakao-icon.svg" alt="Kakao" class="provider-icon" />
          Continue with Kakao
          <span class="coming-soon">Coming soon</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const googleLogin = () => {
  window.location.href = 'http://localhost:8080/oauth2/authorization/google';
};
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 100vh;
  padding: 2rem;
  background-color: #0a0a0a;
  color: #ffffff;
}

.logo-section {
  margin-bottom: 1rem;
  
  .logo {
    height: 48px;
  }
}

.auth-container {
  width: 100%;
  max-width: 400px;
  text-align: center;
  
  h2 {
    font-size: 2rem;
    font-weight: 600;
    margin-bottom: 0.5rem;
  }
  
  .subtitle {
    color: #888;
    margin-bottom: 2rem;
  }
}

.auth-buttons {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.auth-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  border: 1px solid #333;
  background: transparent;
  color: #fff;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  
  &:hover:not(:disabled) {
    background: rgba(255, 255, 255, 0.1);
  }
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
  
  .provider-icon {
    width: 20px;
    height: 20px;
    margin-right: 0.75rem;
  }

  .coming-soon {
    position: absolute;
    right: 1rem;
    font-size: 0.75rem;
    color: #666;
  }
}

.google-button {
  position: relative;
  height: 48px;
  padding: 0;
  border: 1px solid #333;
  border-radius: 8px;
  overflow: hidden;
  
  .provider-icon {
    position: absolute;
    left: 1.5rem;
    top: 50%;
    transform: translateY(-50%);
    width: 20px;
    height: 20px;
    z-index: 2;
    pointer-events: none;
  }
  
  .google-oauth-btn {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    
    > div {
      width: 100% !important;
      height: 100% !important;
      
      > div {
        width: 100% !important;
        height: 100% !important;
        background: transparent !important;
        color: white !important;
        
        > div {
          height: 100% !important;
          display: flex !important;
          align-items: center !important;
          justify-content: center !important;
        }
      }
    }
  }
  
  &:hover {
    background: rgba(255, 255, 255, 0.1);
  }
}

.provider-icon {
  position: absolute;
  left: 1.5rem;
  z-index: 1;
}
</style>