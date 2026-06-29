<template>
  <div class="pupil-dot-wrapper" :style="{ width: size + 'px', height: size + 'px' }">
    <div class="dot" :style="dotStyle"></div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  size: { type: Number, default: 12 },
  maxDistance: { type: Number, default: 5 },
  forceLookX: { type: Number, default: undefined },
  forceLookY: { type: Number, default: undefined }
})

const dotStyle = computed(() => {
  let x = props.forceLookX !== undefined ? props.forceLookX : 0
  let y = props.forceLookY !== undefined ? props.forceLookY : 0
  const max = props.maxDistance
  x = Math.max(-max, Math.min(max, x))
  y = Math.max(-max, Math.min(max, y))
  return {
    transform: `translate(${x}px, ${y}px)`
  }
})
</script>

<style scoped>
.pupil-dot-wrapper {
  background: rgba(255,255,255,0.15);
  border-radius: 50%;
  position: relative;
  flex-shrink: 0;
}
.dot {
  width: 100%;
  height: 100%;
  background: #2D2D2D;
  border-radius: 50%;
  transition: transform 0.25s ease-out;
}
</style>