<template>
  <div class="page">
    <NavBar />
    <div class="container">
      <div class="main-layout">
        <div class="content">
          <div class="feed-header">
            <h1 class="feed-title">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                <path d="M2 17l10 5 10-5"/>
                <path d="M2 12l10 5 10-5"/>
              </svg>
              全站新帖
            </h1>
            <div class="feed-tabs">
              <button
                v-for="tab in tabs"
                :key="tab.key"
                class="tab-btn"
                :class="{ active: activeTab === tab.key }"
                @click="activeTab = tab.key"
              >
                {{ tab.label }}
              </button>
            </div>
          </div>

          <div class="posts-list">
            <PostCard v-for="p in posts" :key="p.id" :post="p" />
          </div>

          <div v-if="posts.length === 0" class="empty-state">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="3" y="3" width="18" height="18" rx="2"/>
              <line x1="9" y1="9" x2="15" y2="9"/>
              <line x1="9" y1="13" x2="15" y2="13"/>
              <line x1="9" y1="17" x2="11" y2="17"/>
            </svg>
            <p>暂无帖子，来发布第一条吧！</p>
            <el-button type="primary" @click="$router.push('/submit')">发布帖子</el-button>
          </div>

          <el-pagination
            v-if="total > 0"
            v-model:current-page="page"
            :page-size="20"
            :total="total"
            layout="prev, pager, next"
            @current-change="load"
            class="pagination"
          />
        </div>

        <aside class="sidebar">
          <div class="sidebar-card">
            <h3 class="sidebar-title">热门板块</h3>
            <div class="board-list">
              <div
                v-for="board in hotBoards"
                :key="board.id"
                class="board-item"
                @click="$router.push('/b/' + board.slug)"
              >
                <div class="board-icon" :style="{ background: board.color }">
                  {{ board.name[0] }}
                </div>
                <div class="board-info">
                  <span class="board-name">{{ board.name }}</span>
                  <span class="board-desc">{{ board.description }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="sidebar-card">
            <h3 class="sidebar-title">社区规则</h3>
            <ul class="rule-list">
              <li>友善交流，尊重他人</li>
              <li>禁止发布违规内容</li>
              <li>鼓励分享有价值的内容</li>
            </ul>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { listPosts } from '@/api/post'
import { listBoards } from '@/api/board'
import NavBar from '@/components/NavBar.vue'
import PostCard from '@/components/PostCard.vue'

const route = useRoute()
const posts = ref<any[]>([])
const page = ref(1)
const total = ref(0)
const hotBoards = ref<any[]>([])
const activeTab = ref('hot')

const tabs = [
  { key: 'hot', label: '热门' },
  { key: 'new', label: '最新' },
]

async function load() {
  const res: any = await listPosts({ page: page.value, size: 20 })
  posts.value = res.data?.records || []
  total.value = res.data?.total || 0
}

async function loadBoards() {
  const res: any = await listBoards()
  hotBoards.value = (res.data || []).map((b: any, i: number) => ({
    ...b,
    color: ['#667eea', '#f093fb', '#4facfe', '#43e97b'][i % 4]
  }))
}

onMounted(() => {
  load()
  loadBoards()
})

watch(() => route.path, (newPath) => {
  if (newPath === '/') {
    page.value = 1
    load()
  }
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f8f9fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.main-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
}

.content {
  min-width: 0;
}

.feed-header {
  margin-bottom: 20px;
}

.feed-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.feed-title svg {
  width: 28px;
  height: 28px;
  color: #4f46e5;
}

.feed-tabs {
  display: flex;
  gap: 8px;
}

.tab-btn {
  padding: 8px 20px;
  border: none;
  background: transparent;
  color: #667085;
  font-size: 14px;
  font-weight: 500;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn:hover {
  background: #f2f4f7;
  color: #1a1a1a;
}

.tab-btn.active {
  background: #eff4ff;
  color: #4f46e5;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #667085;
}

.empty-state svg {
  width: 64px;
  height: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 20px;
}

.pagination {
  margin-top: 24px;
  justify-content: center;
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
}

.sidebar-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 16px 0;
}

.board-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.board-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
}

.board-item:hover {
  background: #f9fafb;
}

.board-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  font-size: 16px;
}

.board-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.board-name {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
}

.board-desc {
  font-size: 12px;
  color: #667085;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

.rule-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.rule-list li {
  padding: 8px 0;
  font-size: 14px;
  color: #667085;
  border-bottom: 1px solid #f2f4f7;
}

.rule-list li:last-child {
  border-bottom: none;
}

.rule-list li::before {
  content: '•';
  color: #4f46e5;
  margin-right: 8px;
}

@media (max-width: 900px) {
  .main-layout {
    grid-template-columns: 1fr;
  }
  .sidebar {
    display: none;
  }
}
</style>
