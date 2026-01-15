import Taro from '@tarojs/taro'
import { request, setToken, getToken } from './request'

describe('request', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  test('should attach Authorization header when token exists', async () => {
    setToken('abc')

    ;(Taro.request as jest.Mock).mockResolvedValue({
      statusCode: 200,
      data: { ok: true },
    })

    await request('/api/ping')

    expect(Taro.request).toHaveBeenCalled()
    const args = (Taro.request as jest.Mock).mock.calls[0][0]
    expect(args.header.Authorization).toBe('Bearer abc')
  })

  test('should clear token and redirect when 401', async () => {
    setToken('abc')
    expect(getToken()).toBe('abc')

    ;(Taro.request as jest.Mock).mockResolvedValue({
      statusCode: 401,
      data: { message: 'unauthorized' },
    })

    await expect(request('/api/appointments/me')).rejects.toThrow('Unauthenticated')

    expect(Taro.removeStorageSync).toHaveBeenCalledWith('token')
    expect(Taro.reLaunch).toHaveBeenCalledWith({ url: '/pages/login/index' })
  })
})
