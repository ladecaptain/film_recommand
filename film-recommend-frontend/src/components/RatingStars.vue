<template>
  <div class="rating-stars">
    <span
      v-for="star in 5"
      :key="star"
      class="star"
      :class="{ filled: star <= (hoverStar || modelValue), hover: star === hoverStar }"
      @mouseenter="hoverStar = star"
      @mouseleave="hoverStar = 0"
      @click="$emit('update:modelValue', star)"
    ></span>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

defineProps<{ modelValue: number }>()
defineEmits<{ 'update:modelValue': [value: number] }>()

const hoverStar = ref(0)
</script>

<style scoped lang="scss">
.rating-stars {
  display: inline-flex;
  gap: 4px;
}

.star {
  width: 28px;
  height: 28px;
  cursor: pointer;
  position: relative;

  &::before {
    content: '☆';
    font-size: 24px;
    color: var(--text-muted);
    transition: color 0.2s;
  }

  &.filled::before {
    content: '★';
    color: var(--accent-gold);
  }

  &.hover::before {
    transform: scale(1.2);
  }
}
</style>
