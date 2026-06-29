<template>
  <div class="eye-ball" :style="{ width: size + 'px', height: size + 'px' }">
    <div class="pupil" :style="pupilStyle" :class="{ blink: isBlinking }">
      <div class="pupil-dot"></div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  size: { type: Number, default: 18 },
  pupilSize: { type: Number, default: 7 },
  maxDistance: { type: Number, default: 5 },
  isBlinking: { type: Boolean, default: false },
  forceLookX: { type: Number, default: undefined },
  forceLookY: { type: Number, default: undefined }
})

const pupilStyle = computed(() => {
  let x = props.forceLookX !== undefined ? props.forceLookX : 0
  let y = props.forceLookY !== undefined ? props.forceLookY : 0
  const max = props.maxDistance
  x = Math.max(-max, Math.min(max, x))
  y = Math.max(-max, Math.min(max, y))
  return {
    width: props.pupilSize + 'px',
    height: props.pupilSize + 'px',
    transform: `translate(${x}px, ${y}px)`,
    transition: props.forceLookX !== undefined ? 'transform 0.15s ease-out' : 'transform 0.3s ease-out'
  }
})
</script>

<style scoped>
.eye-ball {
  background: white;
  border-radius: 50%;
  position: relative;
  flex-shrink: 0;
  box-shadow: inset 0 2px 6px rgba(0,0,0,0.08);
}
.pupil {
  background: #2D2D2D;
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease-out;
}
.pupil.blink {
  transform: translate(-50%, -50%) scaleY(0.1) !important;
  transition: transform 0.08s ease-in-out;
}
.pupil-dot {
  width: 2px;
  height: 2px;
  background: rgba(255,255,255,0.3);
  border-radius: 50%;
  position: absolute;
  top: 20%;
  left: 25%;
}
</style>