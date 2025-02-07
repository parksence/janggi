<template>
  <div class="home">
    <header class="header">
      <div class="logo">Janggi</div>
      <nav class="nav">
        <button @click="logout" class="nav-button">Sign out</button>
      </nav>
    </header>
    <main class="main-content">
      <h1>Welcome to Janggi</h1>
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();

const logout = () => {
  localStorage.removeItem('token');
  router.push('/login');
};
</script>

<style scoped>
.home {
  min-height: 100vh;
  background-color: #0a0a0a;
  color: #ffffff;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(10px);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
}

.nav-button {
  padding: 0.5rem 1rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  background-color: transparent;
  color: #fff;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s;
}

.nav-button:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.main-content {
  padding-top: 5rem;
  text-align: center;
}

h1 {
  font-size: 2.5rem;
  font-weight: 600;
}
</style> 