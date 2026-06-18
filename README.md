# Tienda de Ropa - Backend (Microservicio de Productos)

Este es el microservicio encargado de la gestión de productos, categorías y stock en la tienda de ropa online. Está desarrollado con **Spring Boot**, **Java 21/25**, **Maven** y **H2 Database** (base de datos en memoria).

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

El diseño del catálogo soporta prendas de vestir utilizando las siguientes entidades vinculadas:
* **`Producto`**: Contiene la información general de la prenda (nombre, descripción, precio base, marca, URL de imagen, y categoría).
* **`Variante`**: Representa la combinación física e inventario de un producto en un **talle** y **color** específico, con su propio **stock** y **código de barras** (SKU).
* **`Categoria`**: Clasificación jerárquica para organizar las prendas (ej. Remeras, Pantalones, Camperas, Accesorios).

---

## 🔌 API Endpoints

La URL base para todas las peticiones es: `http://localhost:8080`

### 1. Listar Productos (Catálogo Paginado y Filtrado)
* **Método:** `GET`
* **Endpoint:** `/api/productos`
* **Parámetros de Filtro (Query Params - Opcionales):**
  * `nombre` (búsqueda parcial insensible a mayúsculas/minúsculas)
  * `categoriaId` (filtro por ID de la categoría)
  * `talle` (filtro exacto de talle, ej: `M`)
  * `color` (filtro exacto de color, ej: `Negro`)
  * `precioMin` / `precioMax` (rango de precio base)
  * `activo` (por defecto `true` para ver prendas publicadas)
  * `page` (número de página, base 0)
  * `size` (elementos por página, por defecto 12)
* **Respuesta Exitosa (200 OK):**
  ```json
  {
    "content": [
      {
        "id": 1,
        "nombre": "Remera Slim Fit",
        "descripcion": "Remera de algodón de alta calidad",
        "precio": 15000.00,
        "marca": "UrbanWear",
        "imagenUrl": "http://images.com/remera.png",
        "activo": true,
        "categoria": {
          "id": 1,
          "nombre": "Remeras",
          "descripcion": "Remeras de todo tipo",
          "activo": true
        },
        "variantes": [
          {
            "id": 1,
            "talle": "M",
            "color": "Negro",
            "stock": 20,
            "codigoBarras": "BAR-REMERAM-NEG"
          }
        ],
        "fechaCreacion": "2026-06-18T10:00:00",
        "fechaActualizacion": "2026-06-18T10:00:00"
      }
    ],
    "page": {
      "size": 12,
      "number": 0,
      "totalElements": 1,
      "totalPages": 1
    }
  }
  ```

### 2. Obtener un Producto por ID
* **Método:** `GET`
* **Endpoint:** `/api/productos/{id}`
* **Respuesta Exitosa (200 OK):** Retorna el detalle del producto y sus variantes asociadas.

### 3. Guardar un nuevo Producto con Variantes
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
        "stock": 20,
        "codigoBarras": "BAR-REMERAM-NEG"
      }
    ]
  }
  ```
  *(Nota: Si no envías `codigoBarras` en la variante, el sistema autogenerará uno único)*

### 4. Actualizar datos generales de un Producto
* **Método:** `PUT`
* **Endpoint:** `/api/productos/{id}`
* **Cuerpo de la Petición:** JSON similar al del `POST` (modifica campos generales).

### 5. Agregar una Variante a un Producto existente
* **Método:** `POST`
* **Endpoint:** `/api/productos/{id}/variantes`
* **Cuerpo de la Petición:**
  ```json
  {
    "talle": "L",
    "color": "Negro",
    "stock": 15,
    "codigoBarras": "BAR-REMERAL-NEG"
  }
  ```

### 6. Actualizar el Stock de una Variante
* **Método:** `PATCH`
* **Endpoint:** `/api/productos/variantes/{varianteId}/stock`
* **Parámetros de Query (Query Params - Obligatorios):**
  * `stock` (ej: `/api/productos/variantes/1/stock?stock=50`)

### 7. Activar/Desactivar un Producto (Baja Lógica)
* **Método:** `PATCH`
* **Endpoint:** `/api/productos/{id}/activo`
* **Parámetros de Query:**
  * `activo` (ej: `/api/productos/1/activo?activo=false`)

### 8. Eliminar Producto (Desactivación)
* **Método:** `DELETE`
* **Endpoint:** `/api/productos/{id}`
* **Descripción:** Realiza la desactivación por defecto del producto (baja lógica).
* **Respuesta:** `204 No Content`.
