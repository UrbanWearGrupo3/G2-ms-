# Tienda de Ropa - Backend (Microservicio de Productos y Usuarios)

Este es el microservicio encargado de la gestión de productos, categorías, stock, usuarios y autenticación en la tienda de ropa online. Está desarrollado con **Spring Boot**, **Java 21/25**, **Maven** y **H2 Database** (base de datos en memoria).

---

## 🚀 Cómo levantar el proyecto

Sigue estos pasos para compilar y ejecutar la aplicación localmente:

### Requisitos previos
* **Java 21** o superior (OpenJDK) instalado.
* El puerto **8080** libre en tu máquina.

### Pasos
1. **Navegar a la carpeta del proyecto** (donde se encuentra el archivo `pom.xml`):
   ```bash
   cd tienda-ropa
   ```
2. **Compilar y levantar el servidor:**
   ```bash
   ./mvnw spring-boot:run
   ```
   El servidor estará disponible en: `http://localhost:8080`

### 🛢️ Consola de Base de Datos H2
Para ver la base de datos en tiempo real y realizar consultas SQL, puedes acceder a la consola web de H2:
* **URL:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
* **JDBC URL:** `jdbc:h2:mem:tiendadb`
* **User Name:** `sa`
* **Password:** *(dejar vacío)*

---

## 📐 Modelo de Datos (Multi-Entidad)

El diseño del catálogo soporta prendas de vestir y control de acceso utilizando las siguientes entidades vinculadas:
* **`Producto`**: Contiene la información general de la prenda (nombre, descripción, precio base, marca, URL de imagen, y categoría).
* **`Variante`**: Representa la combinación física e inventario de un producto en un **talle** y **color** específico, con su propio **stock** y **código de barras** (SKU).
* **`Categoria`**: Clasificación jerárquica para organizar las prendas (ej. Remeras, Pantalones, Camperas, Accesorios).
* **`Usuario`**: Representa las cuentas registradas con información personal, credenciales encriptadas con BCrypt y un **Rol** (`CLIENTE` o `ADMIN`).

---

## 🔌 API Endpoints

La URL base para todas las peticiones es: `http://localhost:8080`

### 🔑 Módulo de Autenticación (`/api/auth`)

#### 1. Registrar un nuevo usuario (Público)
* **Método:** `POST`
* **Endpoint:** `/api/auth/register`
* **Cuerpo de la Petición (Request Body - JSON):**
  ```json
  {
    "nombre": "Juan",
    "apellido": "Perez",
    "email": "juan.perez@example.com",
    "password": "secure123"
  }
  ```
  *(Nota: Para registrar una cuenta con rol `ADMIN`, puedes enviar opcionalmente el campo `"adminPasscode": "URBANWEAR-SECRET-ADMIN-2026"`)*

#### 2. Iniciar Sesión (Público)
* **Método:** `POST`
* **Endpoint:** `/api/auth/login`
* **Cuerpo de la Petición:**
  ```json
  {
    "email": "juan.perez@example.com",
    "password": "secure123"
  }
  ```
* **Respuesta Exitosa (200 OK):** Retorna el token JWT que debe enviarse en la cabecera `Authorization: Bearer <token>` en las rutas protegidas.
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9.ey...",
    "email": "juan.perez@example.com",
    "rol": "CLIENTE",
    "nombre": "Juan",
    "apellido": "Perez"
  }
  ```

---

### 👤 Módulo de Usuarios (`/api/usuarios`)

#### 1. Obtener mi perfil (Autenticado)
* **Método:** `GET`
* **Endpoint:** `/api/usuarios/me`

#### 2. Actualizar mi perfil (Autenticado)
* **Método:** `PUT`
* **Endpoint:** `/api/usuarios/me`
* **Cuerpo de la Petición:**
  ```json
  {
    "nombre": "Juan Carlos",
    "apellido": "Perez",
    "password": "newpassword123" // Opcional
  }
  ```

#### 3. Listar todos los usuarios (Solo ADMIN)
* **Método:** `GET`
* **Endpoint:** `/api/usuarios`

#### 4. Cambiar rol de un usuario (Solo ADMIN)
* **Método:** `PATCH`
* **Endpoint:** `/api/usuarios/{id}/rol`
* **Parámetros de Query:**
  * `rol` (ej: `/api/usuarios/1/rol?rol=ADMIN`)

#### 5. Dar de baja a un usuario (Solo ADMIN)
* **Método:** `DELETE`
* **Endpoint:** `/api/usuarios/{id}`
* **Descripción:** Realiza la baja lógica (`activo = false`) de la cuenta.

---

### 👕 Módulo de Productos (`/api/productos`)

#### 1. Listar Productos (Público - Paginado y Filtrado)
* **Método:** `GET`
* **Endpoint:** `/api/productos`
* **Parámetros de Filtro (Query Params - Opcionales):**
  * `nombre`, `categoriaId`, `talle`, `color`, `precioMin`, `precioMax`, `activo`, `page`, `size`

#### 2. Obtener un Producto por ID (Público)
* **Método:** `GET`
* **Endpoint:** `/api/productos/{id}`

#### 3. Guardar un nuevo Producto con Variantes (Solo ADMIN)
* **Método:** `POST`
* **Endpoint:** `/api/productos`
* **Cuerpo de la Petición (Request Body - JSON):**
  ```json
  {
    "nombre": "Remera Slim Fit",
    "descripcion": "Remera de algodón de alta calidad",
    "precio": 15000.00,
    "marca": "UrbanWear",
    "imagenUrl": "http://images.com/remera.png",
    "categoriaId": 1,
    "variantes": [
      {
        "talle": "M",
        "color": "Negro",
        "stock": 20
      }
    ]
  }
  ```

#### 4. Actualizar un Producto (Solo ADMIN)
* **Método:** `PUT`
* **Endpoint:** `/api/productos/{id}`

#### 5. Agregar una Variante (Solo ADMIN)
* **Método:** `POST`
* **Endpoint:** `/api/productos/{id}/variantes`

#### 6. Actualizar el Stock de una Variante (Solo ADMIN)
* **Método:** `PATCH`
* **Endpoint:** `/api/productos/variantes/{varianteId}/stock?stock=50`

#### 7. Dar de baja un Producto (Solo ADMIN)
* **Método:** `DELETE` o `PATCH`
* **Endpoint:** `/api/productos/{id}` o `/api/productos/{id}/activo?activo=false`
