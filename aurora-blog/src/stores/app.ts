import { defineStore } from 'pinia'
import { i18n } from '@/locales/index'
import cookies from 'js-cookie'
import nProgress from 'nprogress'
import 'nprogress/nprogress.css'

nProgress.configure({
  showSpinner: false,
  trickleSpeed: 100,
  parent: '#loading-bar-wrapper'
})

const setTheme = (theme: string) => {
  if (theme === 'theme-dark') {
    document.body.classList.remove('theme-light')
    document.body.classList.add('theme-dark')
  } else {
    document.body.classList.remove('theme-dark')
    document.body.classList.add('theme-light')
  }
}

export const useAppStore = defineStore('appStore', {
  state: () => {
    return {
      themeConfig: {
        theme: cookies.get('theme') ? String(cookies.get('theme')) : 'theme-light',
        profile_shape: 'circle-avatar',
        feature: true,
        gradient: {
          color_1: '#ee2de1',
          color_2: '#fd70a1',
          color_3: '#ff0099'
        },
        //头部的渐变颜色搭配
        header_gradient_css: 'linear-gradient(130deg, #e77ec4, #c46bdf 41.07%, #9d52eb 76.05%)',
        // header_gradient_css: '#C8152D',
        background_gradient_style: {
          background: 'linear-gradient(130deg, #f5eaf5, #fd70a1 41.07%, #ff0099 76.05%)',
          '-webkit-background-clip': 'text',
          '-webkit-text-fill-color': 'transparent',
          '-webkit-box-decoration-break': 'clone',
          'box-decoration-break': 'clone'
        }
      },
      appLoading: false,
      websiteConfig: '' as any,
      viewCount: 0,
      articleCount: 0,
      talkCount: 0,
      categoryCount: 0,
      tagCount: 0,
      NPTimeout: -1,
      loadingTimeout: -1,
      aurora_bot_enable: true
    }
  },
  actions: {
    changeLocale(locale: string) {
      cookies.set('locale', locale, { expires: 7 })
      i18n.global.locale = locale
    },
    initializeTheme(mode: string) {
      setTheme(mode)
    },
    toggleTheme(isDark?: boolean) {
      this.themeConfig.theme =
        isDark === true || this.themeConfig.theme === 'theme-light' ? 'theme-dark' : 'theme-light'
      cookies.set('theme', this.themeConfig.theme, { expires: 7 })
      setTheme(this.themeConfig.theme)
    },
    startLoading() {
      if (this.appLoading === true) return
      if (this.NPTimeout !== -1) clearTimeout(this.NPTimeout)
      if (this.loadingTimeout !== -1) clearTimeout(this.loadingTimeout)
      nProgress.start()
      this.appLoading = true
    },
    endLoading() {
      this.NPTimeout = <any>setTimeout(() => {
        nProgress.done()
      }, 100)

      this.loadingTimeout = <any>setTimeout(() => {
        this.appLoading = false
      }, 300)
    }
  }
})
