import { View, Input, Button } from '@tarojs/components'
import Taro, { useDidShow } from '@tarojs/taro'
import { useState } from 'react'
import { createAppointment } from '../../services/api'
import { getToken } from '../../services/request'
import './index.scss'

export default function CreatePage() {
  const [serviceName, setServiceName] = useState('restaurant')
  const [date, setDate] = useState('2026-01-13')
  const [timeSlot, setTimeSlot] = useState('10:00-11:00')
  const [loading, setLoading] = useState(false)

  useDidShow(() => {
    // 如果没 token 直接回登录
    if (!getToken()) {
      Taro.reLaunch({ url: '/pages/login/index' })
    }
  })

  const onSubmit = async () => {
    if (!serviceName || !date || !timeSlot) {
      Taro.showToast({ title: '请填写完整', icon: 'none' })
      return
    }
    setLoading(true)
    try {
      await createAppointment({ serviceName, date, timeSlot })
      await Taro.showToast({ title: '已提交', icon: 'success' })
      Taro.navigateTo({ url: '/pages/my/index' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <View className='page'>
      <View className='title'>创建预约</View>

      <View className='field'>
        <View className='label'>服务名称</View>
        <Input value={serviceName} onInput={(e) => setServiceName(e.detail.value)} />
      </View>

      <View className='field'>
        <View className='label'>日期（YYYY-MM-DD）</View>
        <Input value={date} onInput={(e) => setDate(e.detail.value)} />
      </View>

      <View className='field'>
        <View className='label'>时间段</View>
        <Input value={timeSlot} onInput={(e) => setTimeSlot(e.detail.value)} />
      </View>

      <Button type='primary' loading={loading} onClick={onSubmit}>
        提交预约
      </Button>

      <View style={{ height: '12px' }} />

      <Button onClick={() => Taro.navigateTo({ url: '/pages/my/index' })}>
        查看我的预约
      </Button>
    </View>
  )
}
