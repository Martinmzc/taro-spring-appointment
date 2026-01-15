import { View, Input, Button } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { useState } from 'react'
import { login } from '../../services/api'
import { setToken } from '../../services/request'
import './index.scss'

export default function LoginPage() {
  const [phone, setPhone] = useState('15900000000')
  const [code, setCode] = useState('123456')
  const [loading, setLoading] = useState(false)

  const onSubmit = async () => {
    if (!phone) {
      Taro.showToast({ title: '请输入手机号', icon: 'none' })
      return
    }
    if (!code) {
      Taro.showToast({ title: '请输入验证码', icon: 'none' })
      return
    }

    setLoading(true)
    try {
      const res = await login(phone, code)
      setToken(res.token)
      await Taro.showToast({ title: '登录成功', icon: 'success' })
      Taro.reLaunch({ url: '/pages/create/index' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <View className='page'>
      <View className='title'>登录</View>

      <View className='field'>
        <View className='label'>手机号</View>
        <Input
          value={phone}
          type='number'
          placeholder='请输入手机号'
          onInput={(e) => setPhone(e.detail.value)}
        />
      </View>

      <View className='field'>
        <View className='label'>验证码（固定 123456）</View>
        <Input
          value={code}
          type='number'
          placeholder='请输入验证码'
          onInput={(e) => setCode(e.detail.value)}
        />
      </View>

      <Button type='primary' loading={loading} onClick={onSubmit}>
        登录
      </Button>
    </View>
  )
}
