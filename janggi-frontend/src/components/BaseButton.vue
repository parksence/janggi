<template>
  <button class="py-2 px-4 rounded-lg font-bold shadow-md transition" :class="classes">
    <slot />
  </button>
</template>

<script setup lang="ts">
import { defineProps, computed } from 'vue';

const props = defineProps<{ class?: string }>();

const classes = computed(() => props.class || 'bg-gray-500 text-white');
</script>
