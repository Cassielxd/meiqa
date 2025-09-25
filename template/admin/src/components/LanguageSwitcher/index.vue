<template>
  <div class="language-switcher">
    <div class="language-dropdown" v-if="showDropdown">
      <div class="current-language" @click="toggleDropdown">
        <span>{{ getLanguageDisplayName(currentLanguage) }}</span>
        <i class="iconfont" :class="{ 'rotate': dropdownOpen }">&#xe685;</i>
      </div>

      <div class="language-options" v-show="dropdownOpen">
        <div
          v-for="lang in supportedLanguages"
          :key="lang.code"
          class="language-option"
          :class="{ 'active': lang.code === currentLanguage }"
          @click="switchLang(lang.code)"
        >
          {{ lang.name }}
        </div>
      </div>
    </div>

    <!-- Simple button style for minimal interfaces -->
    <div class="language-buttons" v-else>
      <button
        v-for="lang in supportedLanguages"
        :key="lang.code"
        :class="{ 'active': lang.code === currentLanguage }"
        @click="switchLang(lang.code)"
      >
        {{ lang.short }}
      </button>
    </div>
  </div>
</template>

<script>
import { switchLanguage, getLanguageDisplayName } from '@/libs/i18nHelper';

export default {
  name: 'LanguageSwitcher',
  props: {
    // 是否显示下拉样式，false为按钮样式
    showDropdown: {
      type: Boolean,
      default: true
    },
    // 是否在切换后刷新页面
    autoReload: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      dropdownOpen: false,
      supportedLanguages: [
        { code: 'zh-CN', name: '简体中文', short: '中' },
        { code: 'en-US', name: 'English', short: 'EN' },
        { code: 'zh-TW', name: '繁體中文', short: '繁' }
      ]
    };
  },
  computed: {
    currentLanguage() {
      return this.$i18n.locale || 'zh-CN';
    }
  },
  mounted() {
    // 点击外部关闭下拉菜单
    document.addEventListener('click', this.handleClickOutside);
  },
  beforeDestroy() {
    document.removeEventListener('click', this.handleClickOutside);
  },
  methods: {
    toggleDropdown() {
      this.dropdownOpen = !this.dropdownOpen;
    },

    switchLang(lang) {
      if (lang === this.currentLanguage) return;

      this.dropdownOpen = false;

      if (this.autoReload) {
        switchLanguage(lang, this.$router, this.$route.query);
      } else {
        // 不刷新页面的切换
        this.$i18n.locale = lang;
        localStorage.setItem('local', lang);

        // 更新URL参数
        const newQuery = { ...this.$route.query, lang };
        this.$router.replace({
          path: this.$route.path,
          query: newQuery
        });
      }

      this.$emit('language-changed', lang);
    },

    getLanguageDisplayName,

    handleClickOutside(event) {
      if (!this.$el.contains(event.target)) {
        this.dropdownOpen = false;
      }
    }
  }
};
</script>

<style scoped>
.language-switcher {
  position: relative;
  font-size: 14px;
}

/* 下拉样式 */
.language-dropdown {
  position: relative;
}

.current-language {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 100px;
}

.current-language:hover {
  background: #e9ecef;
  border-color: #ced4da;
}

.current-language i {
  margin-left: auto;
  transition: transform 0.2s;
}

.current-language i.rotate {
  transform: rotate(180deg);
}

.language-options {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  margin-top: 2px;
}

.language-option {
  padding: 8px 12px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.language-option:hover {
  background: #f8f9fa;
}

.language-option.active {
  background: #007bff;
  color: white;
}

.language-option:first-child {
  border-radius: 4px 4px 0 0;
}

.language-option:last-child {
  border-radius: 0 0 4px 4px;
}

/* 按钮样式 */
.language-buttons {
  display: flex;
  gap: 4px;
}

.language-buttons button {
  padding: 4px 8px;
  border: 1px solid #dee2e6;
  background: #f8f9fa;
  color: #495057;
  border-radius: 3px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 12px;
  min-width: 32px;
}

.language-buttons button:hover {
  background: #e9ecef;
  border-color: #adb5bd;
}

.language-buttons button.active {
  background: #007bff;
  border-color: #007bff;
  color: white;
}

.language-buttons button:focus {
  outline: none;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
}
</style>