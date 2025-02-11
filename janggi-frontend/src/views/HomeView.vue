<template>
  <div class="home">
    <header class="header">
      <div class="logo">
        <img :src="user.profileImage" alt="User Profile" class="profile-img" />
        {{ user.nickname }}
      </div>
      <nav class="nav">
        <button @click="logout" class="nav-button">Sign out</button>
      </nav>
    </header>
    <main class="main-content">
      <h1>Welcome, {{ user.nickname }}!</h1>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { parseJwt } from "@/utils/auth";

const user = ref({ nickname: "", profileImage: "" });

onMounted(() => {
  const token = localStorage.getItem("token");
  if (token) {
    const decoded = parseJwt(token);
    if (decoded) {
      user.value = {
        nickname: decoded.nickname,
        profileImage: decoded.profileImage || "/default-avatar.png",
      };
    }
  }
});

const logout = () => {
  localStorage.removeItem("token");
  window.location.href = "/login";
};
</script>

<style scoped>
.profile-img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 10px;
}
</style>
