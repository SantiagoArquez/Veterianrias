import { NavLink, Navigate, Route, Routes } from 'react-router-dom'
import Owners from './pages/Owners.jsx'
import Pets from './pages/Pets.jsx'
import Appointments from './pages/Appointments.jsx'

export default function App() {
  return (
    <>
      <header>
        <h1>🐾 Clínica Veterinaria</h1>
        <nav>
          <NavLink to="/owners">Propietarios</NavLink>
          <NavLink to="/pets">Mascotas</NavLink>
          <NavLink to="/appointments">Citas</NavLink>
        </nav>
      </header>
      <main>
        <Routes>
          <Route path="/owners" element={<Owners />} />
          <Route path="/pets" element={<Pets />} />
          <Route path="/appointments" element={<Appointments />} />
          <Route path="*" element={<Navigate to="/owners" />} />
        </Routes>
      </main>
    </>
  )
}
