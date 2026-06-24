import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

api.interceptors.response.use(
  resp => resp.data,
  error => {
    const msg = error.response?.data?.message || error.message
    return Promise.reject(new Error(msg))
  }
)

export default api
