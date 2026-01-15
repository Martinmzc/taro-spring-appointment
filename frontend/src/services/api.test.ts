import { login, createAppointment, myAppointments } from './api'
import * as requestMod from './request'

describe('services/api', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  test('login should call request with correct args', async () => {
    const spy = jest.spyOn(requestMod, 'request').mockResolvedValue({
      token: 't',
      userId: 1,
    } as any)

    await login('13800000000', '123456')

    expect(spy).toHaveBeenCalledTimes(1)
    expect(spy).toHaveBeenCalledWith('/api/auth/login', {
      method: 'POST',
      auth: false,
      data: { phone: '13800000000', code: '123456' },
    })
  })

  test('createAppointment should call request with correct args', async () => {
    const spy = jest.spyOn(requestMod, 'request').mockResolvedValue({
      id: 1,
      serviceName: 'meeting',
      date: '2026-01-13',
      timeSlot: '10:00-11:00',
      createdAt: '2026-01-14T00:00:00Z',
    } as any)

    await createAppointment({
      serviceName: 'meeting',
      date: '2026-01-13',
      timeSlot: '10:00-11:00',
    })

    expect(spy).toHaveBeenCalledTimes(1)
    expect(spy).toHaveBeenCalledWith('/api/appointments', {
      method: 'POST',
      data: { serviceName: 'meeting', date: '2026-01-13', timeSlot: '10:00-11:00' },
    })
  })

  test('myAppointments should call request with correct args', async () => {
    const spy = jest.spyOn(requestMod, 'request').mockResolvedValue([] as any)

    await myAppointments()

    expect(spy).toHaveBeenCalledTimes(1)
    expect(spy).toHaveBeenCalledWith('/api/appointments/me', { method: 'GET' })
  })
})
