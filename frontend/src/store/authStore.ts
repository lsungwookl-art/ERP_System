import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import client from '../api/client'

interface AuthState {
  accessToken: string | null
  refreshToken: string | null
  user: { userId: number; email: string; name: string; companyId: number; companyName: string } | null
  login: (email: string, password: string) => Promise<void>
  logout: () => void
  isAuthenticated: () => boolean
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      refreshToken: null,
      user: null,

      login: async (email, password) => {
        const res = await client.post('/auth/login', { email, password })
        const data = res.data.data
        localStorage.setItem('accessToken', data.accessToken)
        localStorage.setItem('refreshToken', data.refreshToken)
        set({
          accessToken: data.accessToken,
          refreshToken: data.refreshToken,
          user: {
            userId: data.userId,
            email: data.email,
            name: data.name,
            companyId: data.companyId,
            companyName: data.companyName,
          },
        })
      },

      logout: () => {
        localStorage.clear()
        set({ accessToken: null, refreshToken: null, user: null })
      },

      isAuthenticated: () => !!get().accessToken,
    }),
    { name: 'erp-auth' }
  )
)
