import { View } from '@tarojs/components'
import Taro, { useDidShow } from '@tarojs/taro'
import { useState } from 'react'
import { myAppointments, Appointment } from '../../services/api'
import { clearToken, getToken } from '../../services/request'
import './index.scss'

export default function MyPage() {
  const [list, setList] = useState<Appointment[]>([])
  const [loading, setLoading] = useState(false)

  const load = async () => {
    setLoading(true)
    try {
      const res = await myAppointments()
      setList(res)
    } finally {
      setLoading(false)
    }
  }

  useDidShow(() => {
    if (!getToken()) {
      Taro.reLaunch({ url: '/pages/login/index' })
      return
    }
    load()
  })

  const onLogout = async () => {
    clearToken()
    await Taro.showToast({ title: '已退出', icon: 'none' })
    Taro.reLaunch({ url: '/pages/login/index' })
  }

  return (
    <View className='page'>
      <View className='title'>我的预约</View>

      <View className='actions'>
        <View className='link' onClick={() => Taro.navigateTo({ url: '/pages/create/index' })}>
          去创建
        </View>
        <View className='link' onClick={load}>
          刷新
        </View>
        <View className='link danger' onClick={onLogout}>
          退出登录
        </View>
      </View>

      {loading ? <View>加载中...</View> : null}

      {list.length === 0 && !loading ? <View className='empty'>暂无预约</View> : null}

      {list.map((a) => (
        <View className='card' key={a.id}>
          <View>服务：{a.serviceName}</View>
          <View>日期：{a.date}</View>
          <View>时间：{a.timeSlot}</View>
          <View className='meta'>ID：{a.id}</View>
        </View>
      ))}
    </View>
  )
}
