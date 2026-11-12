import Crud from '../Crud.jsx'
import { ownerApi } from '../api.js'

const fields = [
  { name: 'nombre', label: 'Nombre', required: true },
  { name: 'apellido', label: 'Apellido', required: true },
  { name: 'documento', label: 'Documento', required: true },
  { name: 'telefono', label: 'Teléfono', required: true },
  { name: 'correo', label: 'Correo', type: 'email', required: true },
  { name: 'direccion', label: 'Dirección' },
]

const columns = [
  { key: 'id', label: 'ID' },
  { label: 'Nombre', render: (r) => `${r.nombre} ${r.apellido}` },
  { key: 'documento', label: 'Documento' },
  { key: 'telefono', label: 'Teléfono' },
  { key: 'correo', label: 'Correo' },
  { key: 'direccion', label: 'Dirección' },
]

export default function Owners() {
  return <Crud title="Propietarios" api={ownerApi} path="/owners" fields={fields} columns={columns} />
}
