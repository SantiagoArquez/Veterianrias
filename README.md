
# 🐾 Clínica Veterinaria: Plataforma de microservicios

Plataforma distribuida para clínicas veterinarias que gestiona propietarios, mascotas y citas. Está construida con Java y Spring Boot (tres microservicios con su propia base MySQL), desplegada con Docker Compose y con una interfaz gráfica en React.

## Arquitectura

| Componente | Puerto | Descripción |
|---|---|---|
| `owner-service` | 8081 | API REST de propietarios (`/owners`) |
| `pet-service` | 8082 | API REST de mascotas (`/pets`) |
| `appointment-service` | 8083 | API REST de citas (`/appointments`) |
| `mysql-owners` | 3307 | Base de datos de propietarios |
| `mysql-pets` | 3308 | Base de datos de mascotas |
| `mysql-appointments` | 3309 | Base de datos de citas |
| `frontend` (React + Vite) | 5173 | Interfaz gráfica |

El frontend no llama directamente a los puertos de los servicios: usa rutas `/api/...` y el servidor de Vite las redirige (proxy) a cada microservicio.

## Requisitos

- Docker y Docker Compose
- Node.js 18 o superior y npm
- Git

> En GitHub Codespaces ya viene todo instalado.

## 1. Obtener el proyecto

```bash
git clone https://github.com/SantiagoArquez/Veterianrias.git
cd Veterianrias
git checkout frontend
```

## 2. Levantar los microservicios y las bases de datos

Desde la raíz del repositorio:

```bash
docker compose up -d --build
```

La primera vez puede tardar varios minutos. Espera uno o dos minutos y verifica que todo esté en estado `Up`:

```bash
docker compose ps -a
```

Comprueba que las APIs respondan (deben devolver `200`):

```bash
curl -i http://localhost:8081/owners
curl -i http://localhost:8082/pets
curl -i http://localhost:8083/appointments
```

Si algún servicio quedó sin conexión a su base, reinícialo:

```bash
docker compose restart owner-service pet-service appointment-service
```

## 3. Ejecutar el frontend

En otra terminal:

```bash
cd frontend
npm install
npm run dev
```

Abre <http://localhost:5173>.

**En GitHub Codespaces:** abre la pestaña **Puertos**, busca el puerto **5173** y haz clic en el icono del globo.

## 4. Cómo usar la interfaz

Sigue este orden, porque cada entidad depende de la anterior:

1. **Propietarios**: crea al menos uno.
2. **Mascotas**: crea una y selecciona su propietario.
3. **Citas**: programa una para esa mascota.

La interfaz permite crear, editar y eliminar registros. Los errores de las APIs (por ejemplo, eliminar un propietario con mascotas o programar una cita en un horario ocupado) se muestran en rojo con el mensaje del servicio.

## 5. Apagar todo

1. Frontend: presiona `Ctrl + C` en la terminal donde corre `npm run dev`.
2. Servicios y bases de datos:

```bash
docker compose stop
```

Esto conserva los datos. **No uses** `docker compose down -v`, porque borra los volúmenes y con ellos la información de las bases de datos.

En Codespaces, además, detén el entorno desde **github.com/codespaces** (menú `...` → **Stop codespace**).

## 6. Volver a usarlo

```bash
docker compose up -d
cd frontend
npm run dev
```

Espera uno o dos minutos después de `docker compose up -d` antes de abrir la interfaz, para que las bases terminen de iniciar.

## Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| Error `502` en la interfaz | Una base de datos o un servicio está caído o aún arrancando | `docker compose ps -a`, luego `docker compose up -d` y esperar uno o dos minutos |
| Las listas aparecen vacías o con error de conexión | Servicio sin conexión a su base | `docker compose restart <servicio>` |
| El puerto 5173 no abre | Vite no está corriendo | Ejecutar `npm run dev` en `frontend` |
| Un servicio no arranca | Error en el arranque | `docker compose logs --tail=50 <servicio>` |

## Endpoints principales

| Servicio | Método y ruta | Descripción |
|---|---|---|
| Owners | `GET/POST /owners`, `GET/PUT/DELETE /owners/{id}` | CRUD de propietarios |
| Pets | `GET/POST /pets`, `GET/PUT/DELETE /pets/{id}` | CRUD de mascotas |
| Pets | `GET /pets/owner/{ownerId}` | Mascotas de un propietario |
| Pets | `GET /pets/{id}/detalle` | Mascota con su propietario |
| Appointments | `GET/POST /appointments`, `GET/PUT/DELETE /appointments/{id}` | CRUD de citas |
| Appointments | `GET /appointments/pet/{petId}` | Citas de una mascota |
| Appointments | `GET /appointments/{id}/completa` | Cita con mascota y propietario |

## Estados disponibles

- **Mascota:** `ACTIVO`, `EN_TRATAMIENTO`, `RECUPERADO`, `FALLECIDO`
- **Cita:** `PROGRAMADA`, `CONFIRMADA`, `ATENDIDA`, `CANCELADA`
- **Sexo:** `MACHO`, `HEMBRA`
