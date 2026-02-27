import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Needs from '../views/Needs.vue'
import Achievements from '../views/Achievements.vue'
import SmartMatch from '../views/SmartMatch.vue'
import Evidence from '../views/Evidence.vue'

import AchievementDetail from '../views/AchievementDetail.vue'
import ContactExpert from '../views/ContactExpert.vue'
import PublishAchievement from '../views/PublishAchievement.vue'
import TeamDetail from '../views/TeamDetail.vue'
import DemandDetail from '../views/DemandDetail.vue'
import PublishNeed from '../views/PublishNeed.vue'

import Register from '../views/Register.vue'
import UserProfile from '../views/UserProfile.vue'
import MyMessages from '../views/MyMessages.vue'
import AdminDashboard from '../views/AdminDashboard.vue'

import MatchingDashboard from '../views/MatchingDashboard.vue'
import LibraryView from '../views/LibraryView.vue'
import ResourceDetail from '../views/ResourceDetail.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior(to, from, savedPosition) {
    // Force scroll to top for all navigations to ensure "jump to top" behavior
    window.scrollTo(0, 0)
    
    // Also try to scroll the main container if it exists (for layouts where body doesn't scroll)
    const main = document.querySelector('.app-main')
    if (main) {
        main.scrollTo(0, 0)
    }

    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0, left: 0 }
    }
  },
  routes: [
    {
      path: '/libraries/:category',
      name: 'library',
      component: LibraryView,
      props: true
    },
    {
      path: '/libraries/:category/:id',
      name: 'resource-detail',
      component: ResourceDetail,
      props: true
    },
    {
      path: '/dashboard/:id',
      name: 'matching-dashboard',
      component: MatchingDashboard
    },
    {
      path: '/admin',
      name: 'admin-dashboard',
      component: AdminDashboard,
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/',
      name: 'home',
      component: Home
    },
    {
      path: '/login',
      name: 'login',
      component: Login
    },
    {
      path: '/register',
      name: 'register',
      component: Register
    },
    {
      path: '/profile',
      name: 'user-profile',
      component: UserProfile,
      meta: { requiresAuth: true }
    },
    {
      path: '/messages',
      name: 'my-messages',
      component: MyMessages,
      meta: { requiresAuth: true }
    },
    {
      path: '/needs',
      name: 'needs',
      component: Needs
    },
    {
      path: '/needs/:id',
      name: 'demand-detail',
      component: DemandDetail
    },
    {
      path: '/needs/publish',
      name: 'publish-need',
      component: PublishNeed,
      meta: { requiresAuth: true }
    },
    {
      path: '/achievements',
      name: 'achievements',
      component: Achievements
    },
    {
      path: '/achievements/publish',
      name: 'publish-achievement',
      component: PublishAchievement,
      meta: { requiresAuth: true }
    },
    {
      path: '/achievements/:id/contact',
      name: 'contact-expert',
      component: ContactExpert
    },
    {
      path: '/needs/:id/contact',
      name: 'contact-demand-owner',
      component: ContactExpert
    },
    {
      path: '/achievements/:id',
      name: 'achievement-detail',
      component: AchievementDetail
    },
    {
      path: '/team/:id',
      name: 'team-detail',
      component: TeamDetail
    },
    {
      path: '/match',
      name: 'match',
      component: SmartMatch
    },
    {
      path: '/evidence',
      name: 'evidence',
      component: Evidence
    }
  ]
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
  } else if (to.meta.role && userStore.userRole !== to.meta.role) {
    // Check for role requirement
    next('/') // Redirect to home if role doesn't match
  } else {
    next()
  }
})

export default router
