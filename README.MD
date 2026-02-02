# Santander Challenge - WebFlux Reactive Service

## Premisa

La premisa del challenge es hace un servicio que sea escalable, robusto, con enfoque en idempotencia; sin perder la
capacidad reactiva de WebFlux.

## Stack tecnológico

Para completar este challenge, se hizo uso del Stack solicitado y algunas herramientas adicionales para simplificar el
flujo de trabajo:

- [Java 11](https://adoptium.net/temurin/releases/?version=11&os=any&arch=any)
- [Spring Boot + WebFlux](https://spring.io/projects/spring-boot)
- [MongoDB](https://www.mongodb.com/try/download/community)
- [Docker](https://www.docker.com/#) (La última versión no es compatible con Windows 10
  21H2. [Descargar ésta](https://docs.docker.com/desktop/release-notes/#4490) en su lugar)
- [Maven](https://maven.apache.org/download.cgi)
- [AWS SDK V2](https://docs.aws.amazon.com/es_es/sdk-for-java/latest/developer-guide/get-started.html)
- [GIT](https://git-scm.com/install/windows)

Cada una de estas dependencias, solo como referencia, tienen asociados links para que puedan descargar/revisar de manera
independiente

------

## Instrucciones

Estos son los pasos a seguir/pre-requisitos para probar el sistema

### Variables de entorno

Antes de construir la aplicación, debe proporcionar
algunas [variables de entorno](https://es.wikipedia.org/wiki/Variable_de_entorno).

También las puede disponer de forma simplificada mediante un archivo `.env` donde almacenar todas las variables que ya
utiliza por defecto el `application.yaml` de Spring.
En caso de que le interese generar su propio `.env`, estas serían todas las variables necesarias

```
NOSQL_USER=admin
NOSQL_PASSWORD=admin
NOSQL_ADDRESS=localhost:27017
MONGO_UI_USER=admin
MONGO_UI_PASSWORD=admin123
AWS_REGION_ID=us-east-1
AWS_ACCESS_KEY_ID={AWS_ACCESS}
AWS_SECRET_KEY_ID={AWS_SECRET}
S3_BUCKET_ID={BUCKET_ID}
SQS_QUEUE_URL={QUEUE_URL}
```

El archivo debe estar en la ruta base del proyecto y solo debe llamarse `.env` sin más.

### Levantar la aplicación

Hay tres formas distintas de hacerlo, dependiendo de las limitaciones del sistema o qué tanto se quiera jugar con
levantar la aplicación de forma local.
Yendo en orden de más simple a más compleja:

#### 1) Docker Full (Recomendado)

Si su sistema cuenta con Docker Desktop instalado o tiene la posibilidad de instalarlo, se sugiere ir por esta opción.

1. Descargue la última versión de Docker Desktop para su sistema operativo.[^](#stack-tecnológico)
2. Abra una nueva terminal y diríjase a la ruta base de este proyecto (ej: `C:\dev\santa-challenge`)
3. Escriba en la consola `docker compose --profile full up -d` y espere a que el proceso termine automáticamente
    - Sí quiere probar hacerlo funcionar como multiples instancias, agregue el parametro `--scale` para app. ej:
      `docker compose --profile full up --build --scale app=3 -d`
4. Acceda normalmente al sistema a través de `http://localhost:8080`

#### 2) Docker sin App

Si tiene ganas de debuggear la aplicación, o por algún motivo quiere hacer otro tipo de pruebas, es totalmente posible y
el `docker-compose.yml` ya está habilitado para eso.

1. Siga los pasos de [arriba](#docker-full-recomendado) hasta llegar al paso 2.
2. Escriba en la consola `docker compose up -d` y aguarde a que termine la inicialización.
2. Si aún no lo tiene, instale la versión de Java 11 para su sistema operativo. Instale ambos el JDK y JRE.
3. Con su versión de Maven instalada o con el wrapper proporcionado, inicie el sistema:
    1. `mvn install; mvn spring-boot:run`
    2. `.\mvnw.cmd install; .\mvnw.cmd spring-boot:run`
4. Acceda normalmente al sistema a través de `http://localhost:8080`

#### 3) Totalmente desde cero (Evitar de ser posible)

Si no tiene más remedio, en este caso cada sistema es particular y podría ser propenso a errores, pero le deseo buena
suerte:
Recuerde: [arriba](#stack-tecnológico) están todos los links para ver todas las descargas de dependencias.

1. Descargue la versión correspondiente de Java 11 para su sistema. Instale ambos el JDK y JRE.
2. Descargue e instale la última versión de MongoDB Community.
    - Asegúrese de que esté corriendo al levantar el sistema y que se encuentra en el puerto `27017`
3. (Opcional) Descargue e instale la última versión de Maven
4. Configure su instalación de MongoDB a su gusto, la inicialización la realiza Spring Boot asi que no necesita tener en
   cuenta ningún paso previo
5. Utilice su propia versión de Maven o el wrapper proporcionado para construir y correr la aplicación
    1. `mvn install; mvn spring-boot:run`
    2. `.\mvnw.cmd install; .\mvnw.cmd spring-boot:run`
6. Acceda normalmente al sistema a través de `http://localhost:8080`

-----

## Extras

### Mongo Express (Docker)

Si optó por la ruta Docker completa o parcial, en ambos casos tiene a su disposición un simple DB Manager en el
navegador para ver las interacciones en la base de datos y la formación de los objetos. El user y password está definido
en las variables de entorno (o .env)

- ruta: [localhost:8081](http://localhost:8081)
- user: admin
- pass: admin123

-----

## ¿Cómo usar la aplicación?

Su uso requiere de alguna API
Client ([Postman](https://www.postman.com/), [Bruno](https://www.usebruno.com/), [Yaak](https://yaak.app/)).
Alternativamente, se puede usar cURL que es nativo de cualquier sistema operativo, pero es mucho menos visual.

En los templates a continuación puede copiar los `curl` e importarlos en cualquiera de los API clients mencionados
arriba.

### Subir archivos

    POST localhost:8080/api/v1/s3_events
    BODY
        Multipart Form/formdata
        name=file type=file
        name=metadata type=default content-type=application/json

    Response:
    {
        eventId: "aws-sqs-event-unique-id"
    }

    Example cURL:
    curl --request POST \
        --url http://localhost:8080/api/v1/s3_events \
        --header 'content-type: multipart/form-data' \
        --form 'file=@C:\images\spain.png' \
        --form 'metadata={"key": "images/flags/spain.png", "bucketName": "santa-s3bucket-challenge"};type=application/json'

### Leer los eventos generados

    GET localhost:8080/api/v1/s3_events/{bucketName}
    BODY
        no-body

    Response:
    {
        "content": [
            {
                "id": "44e0d331-b9ab-43f8-85fd-a8d9cd4d4ff2",
                "bucketName": "santa-s3bucket-challenge",
                "objectKey": "images/flags/spain.png",
                "eventType": "OBJECT_UPDATED",
                "eventTime": "2026-02-01T23:30:57.188Z",
                "objectSize": 155,
                "etag": "26ceeb44309b123676860c7d283a5cbc"
            },
            {
                "id": "18c2ed94-7e1b-4636-bd13-eec9fee52b0f",
                "bucketName": "santa-s3bucket-challenge",
                "objectKey": "images/flags/spain.png",
                "eventType": "OBJECT_UPDATED",
                "eventTime": "2026-02-01T22:55:45.779Z",
                "objectSize": 155,
                "etag": "26ceeb44309b123676860c7d283a5cbc"
            },
            {
                "id": "14b0b2a5-af29-485d-b7ad-fb24ad53cb1d",
                "bucketName": "santa-s3bucket-challenge",
                "objectKey": "images/memories/xmas.jpg",
                "eventType": "OBJECT_UPDATED",
                "eventTime": "2026-02-01T18:53:49.771Z",
                "objectSize": 1476817,
                "etag": "45228617994e43cc76b33afb6dec8549"
            },
            {
                "id": "f42e46d4-7e10-43e0-9aa1-94a82c1e1717",
                "bucketName": "santa-s3bucket-challenge",
                "objectKey": "images/memories/xmas.jpg",
                "eventType": "OBJECT_UPDATED",
                "eventTime": "2026-02-01T18:20:44.494Z",
                "objectSize": 1476817,
                "etag": "45228617994e43cc76b33afb6dec8549"
            },
            {
                "id": "9183d2d6-bf96-47f5-8908-aec9e2669629",
                "bucketName": "santa-s3bucket-challenge",
                "objectKey": "images/memories/xmas.jpg",
                "eventType": "OBJECT_UPDATED",
                "eventTime": "2026-02-01T18:18:00.602Z",
                "objectSize": 1476817,
                "etag": "45228617994e43cc76b33afb6dec8549"
            },
            {
                "id": "fec80933-80bb-463e-869f-e3ca41bfa284",
                "bucketName": "santa-s3bucket-challenge",
                "objectKey": "images/memories/christmas.jpg",
                "eventType": "OBJECT_CREATED",
                "eventTime": "2026-02-01T17:31:50.211Z",
                "objectSize": 1476817,
                "etag": "45228617994e43cc76b33afb6dec8549"
            },
            {
                "id": "cadda422-de30-4283-9fd5-614fe2572972",
                "bucketName": "santa-s3bucket-challenge",
                "objectKey": "images/memories/xmas.jpg",
                "eventType": "OBJECT_UPDATED",
                "eventTime": "2026-02-01T17:23:58.012Z",
                "objectSize": 1476817,
                "etag": "45228617994e43cc76b33afb6dec8549"
            },
            {
                "id": "2f2694cf-9736-4481-a9c7-6ca86d02c818",
                "bucketName": "santa-s3bucket-challenge",
                "objectKey": "images/flags/spain.png",
                "eventType": "OBJECT_UPDATED",
                "eventTime": "2026-02-01T17:11:03.052Z",
                "objectSize": 155,
                "etag": "26ceeb44309b123676860c7d283a5cbc"
            }
        ],
        "pageable": {
            "sort": {
                "empty": false,
                "sorted": true,
                "unsorted": false
            },
            "offset": 0,
            "pageNumber": 0,
            "pageSize": 10,
            "unpaged": false,
            "paged": true
        },
        "last": true,
        "totalElements": 8,
        "totalPages": 1,
        "number": 0,
        "size": 10,
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "first": true,
        "numberOfElements": 8,
        "empty": false
    }

    Example cURL:
    curl --request GET \
        --url http://localhost:8080/api/v1/s3_events/santa-s3bucket-challenge

## Detener la aplicación

- **Para Docker full:** Escriba en la consola `docker compose --profile full down`
- **Para Docker parcial:** Escriba en la consola `docker compose down`
- **Ruta manual:** Diríjase a la consola de comandos donde está corriendo el sistema y simplemente corte el proceso
  `Ctrl + C`. Cierre además la tarea de MongoDB.

-----

## Decisiones técnicas

En este apartado, hablamos más de las decisiones técnicas y explicamos mejor el funcionamiento de ciertas rutinas o
procesos del sistema. También se va a hablar de cómo el sistema resuelve las incógnitas presentadas en las
instrucciones.

### 1) Arquitectura

Se optó por una arquitectura MVC, hay arquitecturas más robustas (Clean Code, Hexagonal) y mejor pensadas para un
ecosistema de microservicios, pero MVC sigue siendo posible que funcione en arquitectura de microservicios y es más
simple de mantener para sistemas de baja complejidad como este.

Clean Code y Hexagonal agregan mucho boiler-plate por muy poco beneficio para la base de código reducida que estamos
manejando.

### 2) Docker desde el inicio

Decidí manejar todo en contenedores que es mucho más fácil de levantar en cualquier ambiente con configuración
mínima, reduciendo la fricción de levantar el sistema en otros equipos.

Como plus, suma al requisito de _"Imagina que tu aplicación está desplegada en Kubernetes y ejecutándose en
múltiples pods simultáneamente"_, actuando como herramienta de testeo para el caso de sistemas distribuidos y multiples
instancias del servicio, balanceador de carga y fuentes compartidas de datos.

### 3) Archivo de entorno sobre Variables de entorno

Los archivos `.env` son mucho más fáciles de modificar, ajustar o incluso expandir, lo cual lo hace una herramienta
invaluable sobre todo en ambientes de desarrollo locales que son mucho más dinámicos. Por supuesto no son tan
recomendables para sistemas distribuidos/on-premise, pero una cosa no quita la otra, siempre se puede usar las variables
de entorno si se desea, incluso ambas soluciones a la vez.

### 4) Escalabilidad

Se pueden tener múltiples instancias de esta aplicación, y siempre y cuando haya un balanceador de cargas en medio,
funcionaría de forma desacoplada y directa.

1. La configuración puede ser compartida, AWS no limita el uso de las ACCESS/SECRET keys a una sola instancia
2. Cada instancia accede a la misma fuente de datos No-relacional. Esto asegura que no exista situaciones de carrera
   ni interbloqueo
3. Ninguna función bloquea el funcionamiento general de subida de archivos y generación de eventos. El bloqueo no es de
   base de datos completa, sino por entrada/elemento.

> **Nota:**
>
> En el caso de que la aplicación escalase a una cantidad muy masiva de pods, es posible que tener un único MongoDB sea
> un cuello de botella a largo plazo. En ese caso, se podría pensar en un Redis con Persistencia de datos, o agregar
> Sharding a MongoDB para reducir el impacto de multiples usuarios, operando sobre la base de datos.

## 5. Idempotencia

Hay varios mecanismos contra la duplicación de eventos y estados inesperados en el sistema.

### A. Versionado pesimista (Pessimistic Locking)

Para impedir que varios usuarios quieran crear/modificar el mismo archivo (mediante bucket/key), el primer usuario en
iniciar la operación, bloquea el acceso a tocar este recurso (pero no a otros recursos) hasta que este finalice su
operación. Es la mejor forma para este caso, ya que la subida de archivos podría tomar segundos, como podría tomar
minutos u horas. De esta forma el usuario se asegura de que tiene reservada la operación de principio a fin.

El resto de los usuarios van a recibir un LockedFileException y se les hará saber que su operación debe esperar.

### B. MessageDeduplicationId

En la configuración de SQS, el cálculo anti-duplicación de eventos es proporcionado por el sistema mismo.
La clave está compuesta por un **Hash SHA-256** de:

- **eTag** (código calculado del archivo proporcionado por S3)
- **Key** (La ruta/clave proporcionada por el usuario durante la subida)
- **Bucket name** (El balde donde fue a parar este archivo)

Con esto obtenemos una clave única, más no aleatoria (el SHA-256 **va a ser idéntico** si el `eTag+key+bucketName`
son iguales). En cualquier eventualidad que un usuario dispare el mismo evento 2 veces, S3 recibirá el archivo ambas
veces, pero solo la primera disparará un evento (el eventId será idéntico para ambos putObject).

### C. Heartbeat

En caso de que el sistema entre en un estado inestable, se detenga inesperadamente, falle o se pierda conexión con el
usuario, cada subida de archivo tiene un "HeartBeat" de corta duración. Cada 15 segundos mientras la operación esté en
curso, el sistema envía un KeepAlive de la conexión, renovando el lock del recurso solicitado. De esta forma incluso si
ocurre un error irrecuperable, al cabo de 30 segundos el lock expira y otro usuario en cualquier instancia,
puede readquirir el lock.

-----

# Últimas palabras

Espero que la documentación sea lo suficientemente expeditiva. Intenté tanto lo más que pude de explicar a fondo muchas
decisiones, organizar las ideas y establecer un pseudo-producto firme y robusto. Puede que olvidara mencionar cosas, o
haya omitido ciertas cosas que no consideré importantes, pero también hay comentarios en el código para ayudar a
reforzar la explicación del flujo.

Ante cualquier duda, no duden en contactarme, dejo a disposición mis datos:

- Teléfono: [11 6453-6395](https://wa.me/+5491164536395)
- Email: [eleazarmm8@gmail.com](mailto:eleazarmm8@gmail.com)
- LinkedIn: [emolinar](https://www.linkedin.com/in/emolinar/)

Saludos y buena jornada