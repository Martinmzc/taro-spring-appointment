import Taro from '@tarojs/taro'

const TOKEN_KEY = 'token'

// change to real backend URL when needed
export const BASE_URL = 'http://127.0.0.1:8080'

export function getToken(): string {
  return Taro.getStorageSync(TOKEN_KEY) || ''
}

export function setToken(token: string) {
  Taro.setStorageSync(TOKEN_KEY, token)
}

export function clearToken() {
  Taro.removeStorageSync(TOKEN_KEY)
}

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE'

export async function request<T>(
  path: string,
  options?: {
    method?: HttpMethod
    data?: any
    auth?: boolean
  }
): Promise<T> {
  const method = options?.method ?? 'GET'
  const auth = options?.auth ?? true
  const data = options?.data

  const header: Record<string, string> = {
    'Content-Type': 'application/json',
  }

  if (auth) {
    const token = getToken()
    if (token) header['Authorization'] = `Bearer ${token}`
  }

  try {
    const res = await Taro.request({
      url: `${BASE_URL}${path}`,
      method,
      data,
      header,
    })

    if (res.statusCode === 401) {
      clearToken()
      await Taro.showToast({ title: '请先登录', icon: 'none' })
      Taro.reLaunch({ url: '/pages/login/index' })
      throw new Error('Unauthenticated')
    }

    if (res.statusCode >= 400) {
      const msg =
        (res.data && (res.data.message || res.data.error)) ||
        `请求失败(${res.statusCode})`
      await Taro.showToast({ title: msg, icon: 'none' })
      throw new Error(msg)
    }

    return res.data as T
  } catch (e: any) {
    // 网络错误等
    if (String(e?.message || '').includes('Unauthenticated')) throw e
    await Taro.showToast({ title: e?.message || '网络错误', icon: 'none' })
    throw e
  }
}
