import { useEffect, useState } from 'react'
import Crud from '../Crud.jsx'
import { ownerApi, petApi } from '../api.js'

// Valores de los enums (deben coincidir con Pet.Sexo y Pet.Estado del backend)
const SEXOS = ['MACHO', 'HEMBRA']
const ESTADOS = ['ACTIVO', 'EN_TRATAMIENTO', 'RECUPERADO', 'FALLECIDO']
const opts = (list) => list.map((v) => ({ value: v, label: v }))

export default function Pets() {
  const [owners, setOwners] = useState([])

  useEffect(() => {
    ownerApi.get('/owners').then((r) => setOwners(r.data)).catch(() => {})
  }, [])

  const ownerName = (id) => {
    const o = owners.find((x) => x.id === id)
    return o ? `${o.nombre} ${o.apellido}` : id
  }

  const fields = [
    { name: 'nombre', label: 'Nombre', required: true },
    { name: 'especie', label: 'Especie', required: true },
    { name: 'raza', label: 'Raza', required: true },
    { name: 'sexo', label: 'Sexo', required: true, options: opts(SEXOS) },
    { name: 'fechaNacimiento', label: 'Fecha de nacimiento', type: 'date', required: true },
    { name: 'peso', label: 'Peso (kg)', type: 'number', step: '0.01', number: true, required: true },
    { name: 'color', label: 'Color' },
    {
      name: 'ownerId',
      label: 'Propietario',
      number: true,
      required: true,
      options: owners.map((o) => ({ value: o.id, label: `${o.nombre} ${o.apellido}` })),
    },
    { name: 'estado', label: 'Estado', options: opts(ESTADOS) },
  ]

  const columns = [
    { key: 'id', label: 'ID' },
    { key: 'nombre', label: 'Nombre' },
    { key: 'especie', label: 'Especie' },
    { key: 'raza', label: 'Raza' },
    { key: 'sexo', label: 'Sexo' },
    { key: 'fechaNacimiento', label: 'Nacimiento' },
    { key: 'peso', label: 'Peso' },
    { label: 'Propietario', render: (r) => ownerName(r.ownerId) },
    { key: 'estado', label: 'Estado' },
  ]

  return <Crud title="Mascotas" api={petApi} path="/pets" fields={fields} columns={columns} />
}
