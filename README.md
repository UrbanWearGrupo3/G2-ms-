# Tienda de Ropa - Backend (Microservicio de Productos)

Este es el microservicio encargado de la gestión de productos en la tienda de ropa, desarrollado con **Spring Boot**, **Java 25**, **Maven** y **H2 Database** (base de datos en memoria).

---

## 🚀 Cómo levantar el proyecto

Sigue estos pasos para compilar y ejecutar la aplicación localmente:

### Requisitos previos
* **Java 25** (OpenJDK) instalado.
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

## 🔌 API Endpoints (para el Frontend)

La URL base para todas las peticiones es: `http://localhost:8080`

### 1. Listar todos los productos
* **Método:** `GET`
* **Endpoint:** `/productos`
* **Descripción:** Devuelve una lista con todos los productos registrados.
* **Respuesta Exitosa (200 OK):**
  ```json
  [
    {
      "id": 1,
      "nombre": "Remera de Algodón Classic",
      "descripcion": "Remera clásica de algodón 100% peinado.",
      "precio": 15990.00,
      "stock": 35,
      "codigoBarras": "7791234567890",
      "activo": true,
      "fechaCreacion": "2026-06-17T17:00:00",
      "fechaActualizacion": "2026-06-17T17:00:00"
    }
  ]
  ```

### 2. Guardar un nuevo producto
* **Método:** `POST`
* **Endpoint:** `/productos`
* **Descripción:** Crea un nuevo producto en la base de datos.
* **Cuerpo de la Petición (Request Body - JSON):**
  ```json
  {
    "nombre": "Remera de Algodón Classic",
    "descripcion": "Remera clásica de algodón 100% peinado.",
    "precio": 15990.00,
    "stock": 35,
    "codigoBarras": "7791234567890",
    "activo": true
  }
  ```
* **Respuesta Exitosa (200 OK / 201 Created):** Retorna el producto creado con su `id` y fechas de auditoría autogeneradas.
  ```json
  {
    "id": 1,
    "nombre": "Remera de Algodón Classic",
    "descripcion": "Remera clásica de algodón 100% peinado.",
    "precio": 15990.00,
    "stock": 35,
    "codigoBarras": "7791234567890",
    "activo": true,
    "fechaCreacion": "2026-06-17T17:00:00",
    "fechaActualizacion": "2026-06-17T17:00:00"
  }
  ```
