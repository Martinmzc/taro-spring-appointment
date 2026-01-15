const storage: Record<string, any> = {}

const Taro = {
  request: jest.fn(),

  getStorageSync: jest.fn((k: string) => storage[k]),
  setStorageSync: jest.fn((k: string, v: any) => {
    storage[k] = v
  }),
  removeStorageSync: jest.fn((k: string) => {
    delete storage[k]
  }),

  showToast: jest.fn(() => Promise.resolve()),
  reLaunch: jest.fn(),
  navigateTo: jest.fn(),

  useDidShow: (cb: Function) => cb(),
}

export default Taro
export const useDidShow = Taro.useDidShow
