import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
})

export const setAuthToken = (token) => {
  if (token) {
    apiClient.defaults.headers.common.Authorization = `Bearer ${token}`
    localStorage.setItem('ecom_token', token)
    return
  }
  delete apiClient.defaults.headers.common.Authorization
  localStorage.removeItem('ecom_token')
}

export const bootstrapAuthToken = () => {
  const token = localStorage.getItem('ecom_token')
  if (token) {
    setAuthToken(token)
  }
  return token
}

export const getApiBaseUrl = () => API_BASE_URL
