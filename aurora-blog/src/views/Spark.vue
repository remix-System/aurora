<template>
    <div>
      <Breadcrumb :current="t('menu.sparkgpt')" />
      <div class="flex flex-col">
        <div class="post-header">
          <h1 class="post-title text-white uppercase">{{ t('titles.sparkgpt') }}</h1>
        </div>
        <div class="main-grid">
          <div class="relative space-y-5">
            <div class="post-html">
             这里暂时还没有东西哦，敬请期待吧~~~
            </div>
                                            
           
            <!-- <Comment /> -->
          </div>
          <div class="col-span-1">
            <Sidebar>
              <Profile />
            </Sidebar>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  
  <script lang="ts">
  import { defineComponent, reactive, provide, computed, toRefs, onMounted } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { Sidebar, Profile } from '../components/Sidebar'
  import Breadcrumb from '@/components/Breadcrumb.vue'
  import { Comment } from '../components/Comment'
  import { useCommentStore } from '@/stores/comment'
  import emitter from '@/utils/mitt'
  import api from '@/api/api'
  
  
  export default defineComponent({
    name: 'sparkgpt',
    components: { Sidebar, Profile, Breadcrumb, Comment },
    
    
    setup() {
      const { t } = useI18n()
      const commentStore = useCommentStore()
      const reactiveData = reactive({
        links: '' as any,
        comments: [] as any,
        haveMore: false as any,
        isReload: false as any
      })
      const pageInfo = reactive({
        current: 1,
        size: 7
      })
      commentStore.type = 4
      onMounted(() => {
        fetchLinks()
        fetchComments()
      })
      provide(
        'comments',
        computed(() => reactiveData.comments)
      )
      provide(
        'haveMore',
        computed(() => reactiveData.haveMore)
      )
      emitter.on('friendLinkFetchComment', () => {
        pageInfo.current = 1
        reactiveData.isReload = true
        fetchComments()
      })
      emitter.on('friendLinkFetchReplies', (index) => {
        fetchReplies(index)
      })
      emitter.on('friendLinkLoadMore', () => {
        fetchComments()
      })
      const fetchLinks = () => {
        api.getFriendLink().then(({ data }) => {
          reactiveData.links = data.data
        })
      }
      const fetchComments = () => {
        const params = {
          type: 4,
          topicId: null,
          current: pageInfo.current,
          size: pageInfo.size
        }
        api.getComments(params).then(({ data }) => {
          if (reactiveData.isReload) {
            reactiveData.comments = data.data.records
            reactiveData.isReload = false
          } else {
            reactiveData.comments.push(...data.data.records)
          }
          if (data.data.count <= reactiveData.comments.length) {
            reactiveData.haveMore = false
          } else {
            reactiveData.haveMore = true
          }
          pageInfo.current++
        })
      }
      const fetchReplies = (index: any) => {
        api.getRepliesByCommentId(reactiveData.comments[index].id).then(({ data }) => {
          reactiveData.comments[index].replyDTOs = data.data
        })
      }
      return {
        ...toRefs(reactiveData),
        t
      }
    }
  
  })
  </script>
  <!-- <link rel="stylesheet" href="https://chenxi-1302638718.cos.ap-nanjing.myqcloud.com/css/APlayer.min.css"> -->
  <style lang="scss" scoped>
  .block {
    display: inline-block;
    width: 24%;
  }
  .info {
    display: inline-block;
    width: 76%;
    height: 100%;
  }
  .link-name {
    margin-left: 20px;
    margin-bottom: 5px;
    margin-top: 2px;
    color: var(--text-normal);
    font-size: larger;
  }
  .link-intro {
    margin-left: 20px;
    margin-bottom: 1px;
    color: var(--text-normal);
  }
  .el-card {
    background: var(--background-primary);
    border-radius: 10px;
    border: 0;
  }
  
  </style>
  