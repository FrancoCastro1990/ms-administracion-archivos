# ms-administracion-archivos

Microservicio Spring Boot para la administración de facturas y archivos en S3, con integración a RabbitMQ.

---

## Características

- **Listar archivos** de un bucket S3.
- **Descargar archivos** individuales.
- **Subir archivos** (soporte para multipart).
- **Mover archivos** dentro de un bucket.
- **Eliminar archivos**.
- **Arquitectura limpia** (DTO para salida).

---

## Tecnologías

- Java 21
- Spring Boot 3.3.12
- Spring Web
- Spring Cloud AWS (S3) 3.3.1
- Lombok

---

## Instalación y configuración

### 1. Clonar el repositorio

```sh
git clone https://github.com/<tu-usuario>/ms-administracion-archivos.git
cd ms-administracion-archivos
```

### 2. Configurar acceso AWS

Agrega tus credenciales y región en `application.yml` o como variables de entorno:

```yaml
spring:
  cloud:
    aws:
      region:
        static: us-east-1
      credentials:
        access-key: TU_ACCESS_KEY
        secret-key: TU_SECRET_KEY
        session-token: TU_SESSION_TOKEN
```

### 3. Compilar y ejecutar

```sh
./mvnw spring-boot:run
```

---

## Endpoints principales

### Listar objetos en un bucket

```
GET /s3/{bucket}/objects
```
**Respuesta:** Lista de archivos (`S3ObjectDto`)

---

### Descargar archivo como stream

```
GET /s3/{bucket}/object/stream/{key}
```
**Respuesta:** Archivo (binario, header para descarga directa)

---

### Descargar archivo como byte[]

```
GET /s3/{bucket}/object/{key}
```
**Respuesta:** Archivo (binario, header para descarga directa)

---

### Subir archivo (Multipart)

```
POST /s3/{bucket}/object/{key}
Content-Type: multipart/form-data
Parámetro: file (archivo)
```
**Ejemplo con Postman:**
- Tipo: `POST`
- URL: `http://localhost:8080/s3/mi-bucket/object/archivo.txt`
- Form-data: clave = `file`, valor = (selecciona archivo)

---

### Mover archivo dentro del bucket

```
POST /s3/{bucket}/move?sourceKey=origen.txt&destKey=destino.txt
```
**Body:** vacío

---

### Eliminar archivo

```
DELETE /s3/{bucket}/object/{key}
```

---

### 1. Crear una factura
Crea una factura y la envía a la cola RabbitMQ para su procesamiento y generación de PDF.

**POST /facturas**
```json
{
  "clienteId": "123",
  "fechaEmision": "2025-07-20",
  "descripcion": "Compra de insumos",
  "monto": 1000.0
}
```
**Respuesta:**
```json
{
  "id": 1,
  "clienteId": "123",
  "fechaEmision": "2025-07-20",
  "descripcion": "Compra de insumos",
  "monto": 1000.0,
  "nombreArchivo": null
}
```

### 2. Obtener una factura por ID

**GET /facturas/{id}**

**Respuesta:**
```json
{
  "id": 1,
  "clienteId": "123",
  "fechaEmision": "2025-07-20",
  "descripcion": "Compra de insumos",
  "monto": 1000.0,
  "nombreArchivo": "factura-123-<timestamp>.pdf"
}
```

### 3. Obtener historial de facturas por cliente

**GET /facturas/historial/{clienteId}**

**Respuesta:**
Lista de facturas del cliente.

### 4. Actualizar una factura

**PUT /facturas/{id}**
```json
{
  "clienteId": "123",
  "fechaEmision": "2025-07-21",
  "descripcion": "Compra actualizada",
  "monto": 1200.0,
  "nombreArchivo": "factura-123-<timestamp>.pdf"
}
```

### 5. Eliminar una factura

**DELETE /facturas/{id}**

**Respuesta:**
- 204 No Content

### 6. Subir el PDF de la factura a S3

**POST /facturas/{id}/upload**

No requiere archivo en el request. El microservicio busca el PDF generado y lo sube a S3.

**Respuesta:**
```json
"Archivo subido correctamente a S3"
```

---

## Flujo general
1. Crea la factura con el endpoint POST `/facturas`.
2. El PDF se genera automáticamente en el backend (por el consumer de RabbitMQ).
3. Para subir el PDF a S3, llama a POST `/facturas/{id}/upload`.

---

## Recomendaciones para pruebas
- Usa Postman, Insomnia o `curl` para probar los endpoints.
- Revisa la consola de RabbitMQ en [http://localhost:15672](http://localhost:15672) (usuario/clave: guest/guest).
- Verifica los archivos en S3 según la configuración.

---

## Ejemplo de uso con curl

**Crear factura:**
```sh
curl -X POST http://localhost:8080/facturas -H "Content-Type: application/json" -d '{"clienteId":"123","fechaEmision":"2025-07-20","descripcion":"Compra de insumos","monto":1000.0}'
```

**Subir PDF a S3:**
```sh
curl -X POST http://localhost:8080/facturas/1/upload
```

**Obtener factura:**
```sh
curl http://localhost:8080/facturas/1
```

**Obtener historial:**
```sh
curl http://localhost:8080/facturas/historial/123
```

**Actualizar factura:**
```sh
curl -X PUT http://localhost:8080/facturas/1 -H "Content-Type: application/json" -d '{"clienteId":"123","fechaEmision":"2025-07-21","descripcion":"Compra actualizada","monto":1200.0,"nombreArchivo":"factura-123-<timestamp>.pdf"}'
```

**Eliminar factura:**
```sh
curl -X DELETE http://localhost:8080/facturas/1
```

---

## Notas
- El PDF se genera automáticamente en el backend, no es necesario subirlo manualmente.
- El endpoint `/facturas/{id}/upload` solo sube el PDF generado a S3.
- Revisa la configuración de S3 y RabbitMQ para pruebas locales y despliegue.

---

## Estructura de proyecto

- `controller/` - Controladores REST
- `service/` - Lógica de negocio y acceso a S3
- `dto/` - Clases DTO para respuesta

---

## Dependencias principales (`pom.xml`)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>io.awspring.cloud</groupId>
    <artifactId>spring-cloud-aws-starter-s3</artifactId>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```
*(Incluye `spring-cloud-aws-dependencies:3.3.1` en `<dependencyManagement>`)*
