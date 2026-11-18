import { useEffect, useState } from 'react'

export const errMsg = (e) =>
  e.response?.data?.mensaje ||
  (e.response ? `Error ${e.response.status}` : 'No se pudo conectar con el servicio')

export default function Crud({ title, api, path, fields, columns }) {
  const empty = Object.fromEntries(fields.map((f) => [f.name, '']))
  const [rows, setRows] = useState([])
  const [form, setForm] = useState(empty)
  const [editId, setEditId] = useState(null)
  const [msg, setMsg] = useState(null)

  const fail = (e) => setMsg({ ok: false, text: errMsg(e) })
  const load = () => api.get(path).then((r) => setRows(r.data)).catch(fail)
  useEffect(() => { load() }, [])

  const set = (name, value) => setForm({ ...form, [name]: value })
  const reset = () => { setForm(empty); setEditId(null) }

  const submit = async (e) => {
    e.preventDefault()
    const body = {}
    fields.forEach((f) => {
      const v = form[f.name]
      body[f.name] = v === '' || v === undefined ? null : f.number ? Number(v) : v
    })
    try {
      if (editId) await api.put(`${path}/${editId}`, body)
      else await api.post(path, body)
      setMsg({ ok: true, text: editId ? 'Registro actualizado' : 'Registro creado' })
      reset()
      load()
    } catch (err) {
      fail(err)
    }
  }

  const edit = (row) => {
    setEditId(row.id)
    setForm(Object.fromEntries(fields.map((f) => [f.name, row[f.name] ?? ''])))
    setMsg(null)
    window.scrollTo(0, 0)
  }

  const remove = async (id) => {
    if (!window.confirm('¿Eliminar este registro?')) return
    try {
      await api.delete(`${path}/${id}`)
      setMsg({ ok: true, text: 'Registro eliminado' })
      load()
    } catch (err) {
      fail(err)
    }
  }

  return (
    <section>
      <h2>{title}</h2>
      {msg && <p className={msg.ok ? 'ok' : 'error'}>{msg.text}</p>}

      <form onSubmit={submit} className="form">
        {fields.filter((f) => !f.onlyEdit || editId).map((f) => (
          <label key={f.name}>
            {f.label}
            {f.options ? (
              <select value={form[f.name]} required={f.required} onChange={(e) => set(f.name, e.target.value)}>
                <option value="">-- Seleccione --</option>
                {f.options.map((o) => (
                  <option key={o.value} value={o.value}>{o.label}</option>
                ))}
              </select>
            ) : (
              <input
                type={f.type || 'text'}
                step={f.step}
                value={form[f.name]}
                required={f.required}
                onChange={(e) => set(f.name, e.target.value)}
              />
            )}
          </label>
        ))}
        <div className="actions">
          <button type="submit">{editId ? 'Guardar cambios' : 'Crear'}</button>
          {editId && <button type="button" className="secondary" onClick={reset}>Cancelar</button>}
        </div>
      </form>

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              {columns.map((c) => <th key={c.label}>{c.label}</th>)}
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {rows.length === 0 && (
              <tr><td colSpan={columns.length + 1}>No hay registros</td></tr>
            )}
            {rows.map((row) => (
              <tr key={row.id}>
                {columns.map((c) => <td key={c.label}>{c.render ? c.render(row) : row[c.key]}</td>)}
                <td className="row-actions">
                  <button onClick={() => edit(row)}>Editar</button>
                  <button className="danger" onClick={() => remove(row.id)}>Eliminar</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  )
}
