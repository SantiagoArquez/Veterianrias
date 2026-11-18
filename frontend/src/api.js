   import axios from 'axios'

   export const ownerApi = axios.create({ baseURL: '/api' })
   export const petApi = axios.create({ baseURL: '/api' })
   export const appointmentApi = axios.create({ baseURL: '/api' })