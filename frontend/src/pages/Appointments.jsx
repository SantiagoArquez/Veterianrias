import { useEffect, useState } from 'react'
import Crud from '../Crud.jsx'
import { appointmentApi, petApi } from '../api.js'

// Valores del enum (deben coincidir con Appointment.Estado del backend)
const ESTADOS = ['PROGRAMADA', 'CONFIRMADA', 'ATENDIDA', 'CANCELADA']

export default function Appointments() {
  const [pets, setPets] = useState([])

  useEffect(() => {
    petApi.get('/pets').then((r) => setPets(r.data)).catch(() => {})
  }, [])

  const petName = (id) => pets.find((p) => p.id === id)?.nombre ?? id

  const fields = [
    {
      name: 'petId',
      label: 'Mascota',
      number: true,
      required: true,
      options: pets.map((p) => ({ value: p.id, label: p.nombre })),
    },
    { name: 'fecha', label: 'Fecha', type: 'date', required: true },
    { name: 'hora', label: 'Hora', type: 'time', required: true },
    { name: 'motivoConsulta', label: 'Motivo de consulta', required: true },
    { name: 'observaciones', label: 'Observaciones' },
    {
      name: 'estado',
      label: 'Estado',
      onlyEdit: true,
      options: ESTADOS.map((v) => ({ value: v, label: v })),
    },
  ]

  const columns = [
    { key: 'id', label: 'ID' },
    { label: 'Mascota', render: (r) => petName(r.petId) },
    { key: 'fecha', label: 'Fecha' },
    { label: 'Hora', render: (r) => String(r.hora).slice(0, 5) },
    { key: 'motivoConsulta', label: 'Motivo' },
    { key: 'observaciones', label: 'Observaciones' },
    { key: 'estado', label: 'Estado' },
  ]

  return <Crud title="Citas" api={appointmentApi} path="/appointments" fields={fields} columns={columns} />
}
