<template>
  <label class="relative inline-flex items-center cursor-pointer">
    <input type="checkbox" v-model="checked" class="sr-only peer">
    <div class="w-10 h-5 bg-gray-300 rounded-full peer-checked:bg-blue-600 transition"></div>
    <span class="ml-2">{{ checked ? 'ON' : 'OFF' }}</span>
  </label>
</template>

<script setup lang="ts">
import { defineProps, defineEmits, computed } from 'vue';

const props = defineProps<{ modelValue: boolean }>();
const emit = defineEmits(["update:modelValue"]);

const checked = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});
</script>
