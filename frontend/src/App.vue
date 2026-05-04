<template>
  <router-view />
</template>

<script setup lang="ts">
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: #f8f9fa;
  color: #1a1a1a;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* Modern scrollbar */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* Element Plus overrides for modern look */
.el-card {
  border-radius: 12px !important;
  border: none !important;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02) !important;
  transition: box-shadow 0.3s ease, transform 0.2s ease;
}

.el-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08), 0 2px 4px rgba(0, 0, 0, 0.04) !important;
}

.el-button {
  border-radius: 8px !important;
  font-weight: 500 !important;
}

.el-input__wrapper {
  border-radius: 10px !important;
  box-shadow: 0 0 0 1px #e4e7ec inset !important;
}

.el-input__wrapper:hover {
  box-shadow: 0 0 0 1px #d0d5dd inset !important;
}

.el-input__wrapper.is-focus {
  box-shadow: 0 0 0 1px #409eff inset !important;
}

.el-menu {
  border-bottom: none !important;
}

.el-tag {
  border-radius: 6px !important;
  font-weight: 500 !important;
}

.el-pagination .el-pager li {
  border-radius: 8px !important;
}

.el-avatar {
  border: 2px solid #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.el-dialog {
  border-radius: 16px !important;
  overflow: hidden;
}

.el-textarea__inner {
  border-radius: 10px !important;
}

.el-select .el-input__wrapper {
  border-radius: 10px !important;
}
</style>
