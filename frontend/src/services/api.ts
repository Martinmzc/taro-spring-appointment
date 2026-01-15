import { request } from './request'

export type LoginResponse = {
  token: string
  userId: number
}

export async function login(phone: string, code: string) {
  return request<LoginResponse>('/api/auth/login', {
    method: 'POST',
    auth: false,
    data: { phone, code },
  })
}

export type Appointment = {
  id: number
  serviceName: string
  date: string // 后端 LocalDate -> JSON 字符串
  timeSlot: string
  createdAt: string
}

export async function createAppointment(payload: {
  serviceName: string
  date: string
  timeSlot: string
}) {
  return request<Appointment>('/api/appointments', {
    method: 'POST',
    data: payload,
  })
}

export async function myAppointments() {
  return request<Appointment[]>('/api/appointments/me', {
    method: 'GET',
  })
}
