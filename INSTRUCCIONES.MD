# Test técnico SR Software Engineer - Data Platform
El objetivo del examen es evaluar tus habilidades técnicas en el desarrollo de microservicios utilizando Java 11 con Spring Webflux. Además, queremos validar tu comprensión y capacidad para garantizar el buen funcionamiento del sistema en un ambiente distribuido.
Deberás desarrollar un microservicio reactivo con SpringBoot y Webflux que maneje eventos relacionados a objetos **almacenados** en Amazon S3.

-----------

### Parte 1: Desarrollo de endpoints con Spring Webflux
Implementa los siguientes endpoints reactivos:
- **POST /api/v1/s3-events**
  - Registra eventos relacionados con creación o modificación de objetos en AWS S3.
    - Request body ejemplo:
      ```json 
      {
          "bucketName": "zonda-data-bucket",
          "objectKey": "reports/daily/report_2024-01-25.csv",
          "eventType": "OBJECT_CREATED",
          "eventTime": "2025-07-30T14:00:00Z",
          "objectSize": 2048
      }
      ```
  - La respuesta debe retornar un identificador único del evento generado y almacenar esta información en una base de datos MongoDB.


- **GET /api/v1/s3-events/{bucketName}**
  - Devuelve un listado reactivo de forma paginada con los eventos relacionados a un bucket específico.
  - La respuesta debe ser ordenada por fecha (eventTime) de manera descendente.

#### Consideraciones técnicas:
- Implementar arquitectura clara y modular.
- La persistencia debe realizarse utilizando MongoDB.

-----------

### Parte 2: Integración con AWS SQS
Al registrar exitosamente un evento (_POST /api/v1/s3-events_), se debe enviar un mensaje a una cola AWS SQS con información relevante, por ejemplo:
````json
{
    "eventId": "65b33d9d2e8f4f1e1a8b4567",
    "bucketName": "zonda-data-bucket",
    "objectKey": "reports/daily/report_2024-01-25.csv",
    "eventTime": "2024-01-25T14:00:00Z"
}
````
Documenta brevemente cómo configuraste la integración con AWS SQS en el archivo README o símil.

-----------
### Parte 3: Escalabilidad e idempotencia (respuesta teórica)
Imagina que tu aplicación está desplegada en Kubernetes y ejecutándose en múltiples pods simultáneamente:
- Describe brevemente cómo garantizarías que, al consumir mensajes desde AWS SQS, los eventos se procesen de manera **idempotente**, evitando procesamiento duplicado.
- Puedes incluir conceptos tales como:
  - Estrategias para identificar mensajes duplicados.
  - Patrones para asegurar el procesamiento exactamente una vez.
  - Uso de identificadores únicos, restricciones de base de datos, o bloqueos distribuidos.
    
Esta explicación debe estar incluida en el archivo README o símil.

-----------

### Formato de entrega
Repositorio Git público (GitHub) con:
- Código fuente.
- Archivo README.md con:
  - Instrucciones para ejecutar y configurar el proyecto.
  - Explicación breve de decisiones técnicas.
  - Conceptos teóricos que considere relevantes.
  - Información técnica acerca de la integración con SQS.
  - Estrategia de idempotencia claramente explicada.

Enviar a la casilla [frebora@santandertecnologia.com.ar](mailto:frebora@santandertecnologia.com.ar) con el asunto “EXAMEN TECNICO SR – {nombre y apellido candidato}”.