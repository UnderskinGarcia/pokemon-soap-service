# Pokemon SOAP Service

SOAP Web Service para consumir la API de Pokemon (PokeAPI) utilizando arquitectura hexagonal. Este servicio actúa como un gateway entre clientes SOAP y la PokeAPI REST, transformando peticiones SOAP en consultas HTTP y devolviendo respuestas XML.

## Descripción

El servicio permite consultar información de Pokemon mediante 6 operaciones SOAP:
- **GetPokemonAbilities** - Obtiene las habilidades de un Pokemon
- **GetPokemonBaseExperience** - Consulta la experiencia base
- **GetPokemonHeldItems** - Lista los objetos que puede portar
- **GetPokemonId** - Obtiene el ID del Pokemon
- **GetPokemonName** - Obtiene el nombre del Pokemon
- **GetPokemonLocationAreaEncounters** - Consulta ubicaciones donde puede ser encontrado

**Arquitectura actual**: El servicio funciona como un **proxy/gateway** que:
1. Recibe peticiones SOAP XML
2. Consulta la PokeAPI REST (https://pokeapi.co/api/v2)
3. Transforma la respuesta JSON a objetos de dominio
4. Devuelve respuestas SOAP XML al cliente

## Stack Tecnológico

### Core
- **Java 21** - Lenguaje de programación
- **Spring Boot 3.5.6** - Framework principal
- **Gradle 8.x** - Herramienta de construcción (con Gradle Wrapper)
- **Spring Dependency Management 1.1.7** - Gestión de versiones

### Web Services (SOAP)
- **Spring Web Services (Spring-WS)** - Framework SOAP
- **JAXB 4.0.4** - Marshalling/Unmarshalling XML
- **WSDL4J 1.6.3** - Generación automática de WSDL
- **XJC Plugin 1.8.2** - Generación de clases Java desde XSD

### Cliente HTTP
- **Spring WebFlux** - Cliente HTTP reactivo (WebClient)
- **Project Reactor** - Programación reactiva

### Mapeo y Transformación
- **MapStruct 1.5.5.Final** - Mapeo entre DTOs y modelos de dominio
- **Lombok** - Reducción de código boilerplate

### Logging
- **Log4j2** - Sistema de logging (excluye Logback por defecto de Spring Boot)
- Configuración con múltiples appenders (Application, Error, SOAP, Performance)

### Monitoreo
- **Spring Actuator** - Endpoint de health check

### Base de Datos (Configurada pero NO implementada aún)
- **Spring Data JPA** - Dependencia presente pero sin entidades ni repositorios
- **PostgreSQL Driver** - Driver JDBC incluido pero sin uso actual
- **HikariCP** - Pool de conexiones (incluido con Spring Boot)

### Testing
- **JUnit 5** - Framework de testing
- **Spring Boot Test** - Utilidades de testing
- **Reactor Test** - Testing reactivo

### DevOps
- **Docker** - Contenedorización
- **Docker Compose** - Orquestación multi-contenedor
- **Spring DevTools** - Recarga automática en desarrollo

## Arquitectura

El proyecto sigue una **Arquitectura Hexagonal** (Ports & Adapters) que separa la lógica de negocio de los detalles de infraestructura:

```
src/main/java/com/bankaya/pokemon/
├── application/          # Capa de Aplicación
│   └── service/         # Casos de uso y orquestación
├── domain/              # Núcleo del Negocio
│   ├── model/          # Entidades de dominio
│   └── port/           # Interfaces (Puertos)
│       ├── in/        # Puertos de entrada (use cases)
│       └── out/       # Puertos de salida (repositorios, APIs)
└── infrastructure/      # Capa de Infraestructura
    ├── adapter/        # Implementaciones de adaptadores
    │   ├── persistence/  # Adaptador de BD (JPA/PostgreSQL)
    │   ├── rest/        # Adaptador REST (Cliente PokeAPI)
    │   └── soap/        # Adaptador SOAP (Endpoints)
    └── config/          # Configuraciones de Spring
```

### Flujo de Datos (Implementación Actual)

```
Cliente SOAP (XML Request)
    ↓
PokemonEndpoint.java (infrastructure/adapter/soap)
    - Recibe petición SOAP
    - Deserializa XML mediante JAXB
    - Extrae el nombre del Pokemon
    ↓
PokemonService.java (application/service)
    - Implementa GetPokemonUseCase (domain/port/in)
    - Valida parámetros
    - Delega a PokemonApiPort
    ↓
PokeApiClient.java (infrastructure/adapter/rest)
    - Implementa PokemonApiPort (domain/port/out)
    - Construye URL: https://pokeapi.co/api/v2/pokemon/{name}
    - Ejecuta petición HTTP GET con WebClient
    - Maneja errores (404 → PokemonNotFoundException)
    ↓
PokeAPI Externa (JSON Response)
    ↓
PokemonMapper.java (MapStruct)
    - Transforma PokemonApiResponse → Pokemon (domain model)
    - Mapea habilidades, items, ubicaciones
    ↓
Pokemon (domain/model)
    - Modelo de dominio inmutable (Java Record)
    - Contiene: id, name, baseExperience, abilities, heldItems, locationAreaEncounters
    ↓
PokemonService.java
    - Convierte Pokemon → SOAP Response DTO específico
    - Retorna objeto de respuesta SOAP
    ↓
PokemonEndpoint.java
    - Serializa respuesta a XML mediante JAXB
    ↓
Cliente SOAP (XML Response)
```

**Nota importante**:
- ✅ Cada petición consulta directamente la PokeAPI
- ✅ El patrón hexagonal permite agregar persistencia fácilmente en el futuro

### Principios Aplicados

- **Separación de Responsabilidades**: Cada capa tiene un propósito específico
- **Inversión de Dependencias**: El dominio no depende de infraestructura
- **Ports & Adapters**: Interfaces desacoplan lógica de implementación
- **Single Responsibility**: Cada clase tiene una única razón para cambiar

## Requisitos Previos

- **Java 21** o superior ([Descargar OpenJDK](https://adoptium.net/))
- **Docker** y **Docker Compose** ([Instalar Docker](https://docs.docker.com/get-docker/))
- **Gradle** (incluido wrapper - no requiere instalación)

## Configuración Inicial

### 1. Clonar el Repositorio

```bash
git clone https://github.com/UnderskinGarcia/pokemon-soap-service.git
cd pokemon-soap-service
```

### 2. Configurar Variables de Entorno

Copia el archivo de ejemplo y ajusta según tu entorno:

```bash
cp .env.example .env
```

Variables disponibles en `.env`:

```properties
# PostgreSQL Configuration
POSTGRES_DB=pokemondb
POSTGRES_HOST=postgres                    # 'postgres' para Docker, 'localhost' para local
POSTGRES_PORT=5432:5432                   # Puerto de PostgreSQL
POSTGRES_USER=pokemon
POSTGRES_PASSWORD=change_this_password    # Cambiar en producción

# Application
APP_PORT=8080:8080                        # Puerto de la aplicación
SPRING_PROFILES_ACTIVE=default            # Perfil de Spring (default, dev, prod)
```

## Instrucciones de Ejecución

### Opción 1: Con Docker Compose (Recomendado)

Esta es la forma más sencilla de ejecutar el proyecto completo (aplicación + base de datos):

```bash
# Construir e iniciar todos los servicios
docker-compose up -d

# Ver logs de la aplicación
docker-compose logs -f pokemon-service

# Ver logs de PostgreSQL
docker-compose logs -f postgres

# Verificar el estado de los servicios
docker-compose ps

# Detener los servicios
docker-compose down

# Detener y eliminar volúmenes (limpieza completa)
docker-compose down -v
```

La aplicación estará disponible en:
- **SOAP Endpoint**: http://localhost:8080/pokemon/ws
- **WSDL**: http://localhost:8080/pokemon/ws/pokemon.wsdl
- **Health Check**: http://localhost:8080/actuator/health

### Opción 2: Ejecución Local (Sin Docker)

Si prefieres ejecutar la aplicación localmente sin contenedores:

#### 2.1. Configurar Variables de Entorno

> **Nota**: Aunque PostgreSQL está configurado, **no es necesario tenerlo corriendo** ya que la aplicación actualmente no lo usa. Solo requiere las variables de entorno definidas.

#### 2.2. Configurar Variables de Entorno para la Aplicación

En Windows (PowerShell):
```powershell
$env:POSTGRES_HOST="localhost"
$env:POSTGRES_PORT="5432"
$env:POSTGRES_DB="pokemondb"
$env:POSTGRES_USER="pokemon"
$env:POSTGRES_PASSWORD="change_this_password"
$env:SPRING_PROFILES_ACTIVE="dev"
```

En Linux/Mac:
```bash
export POSTGRES_HOST=localhost
export POSTGRES_PORT=5432
export POSTGRES_DB=pokemondb
export POSTGRES_USER=pokemon
export POSTGRES_PASSWORD=change_this_password
export SPRING_PROFILES_ACTIVE=dev
```

#### 2.3. Ejecutar la Aplicación

```bash
# Otorgar permisos de ejecución al wrapper (Linux/Mac)
chmod +x gradlew

# Ejecutar la aplicación
./gradlew bootRun

# En Windows
gradlew.bat bootRun
```

### Opción 3: Ejecutar JAR Compilado

```bash
# Compilar el proyecto
./gradlew build

# Ejecutar el JAR generado
java -jar build/libs/pokemon-soap-service-0.0.1-SNAPSHOT.jar
```

## Uso del Servicio SOAP

### Endpoints Disponibles

El servicio expone 6 operaciones SOAP (namespace: `http://bankaya.com/pokemon/soap`):

| Operación | Request | Response | Descripción |
|-----------|---------|----------|-------------|
| **GetPokemonAbilities** | `PokemonNameRequest` | `GetPokemonAbilitiesResponse` | Lista de habilidades del Pokemon |
| **GetPokemonBaseExperience** | `PokemonNameRequest` | `GetPokemonBaseExperienceResponse` | Experiencia base (int) |
| **GetPokemonHeldItems** | `PokemonNameRequest` | `GetPokemonHeldItemsResponse` | Objetos que puede portar |
| **GetPokemonId** | `PokemonNameRequest` | `GetPokemonIdResponse` | ID numérico del Pokemon |
| **GetPokemonName** | `PokemonNameRequest` | `GetPokemonNameResponse` | Nombre del Pokemon |
| **GetPokemonLocationAreaEncounters** | `PokemonNameRequest` | `GetPokemonLocationAreaEncountersResponse` | Ubicaciones de encuentro |

### Ejemplo de Petición SOAP

Endpoint: `http://localhost:8080/pokemon/ws`

Usando **curl**:

```bash
curl -X POST http://localhost:8080/pokemon/ws \
  -H "Content-Type: text/xml" \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:soap="http://bankaya.com/pokemon/soap">
   <soapenv:Header/>
   <soapenv:Body>
      <soap:GetPokemonAbilitiesRequest>
         <soap:name>pikachu</soap:name>
      </soap:GetPokemonAbilitiesRequest>
   </soapenv:Body>
</soapenv:Envelope>'
```

Usando **Postman** o **SoapUI**:
```xml
POST http://localhost:8080/pokemon/ws
Content-Type: text/xml

<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:soap="http://bankaya.com/pokemon/soap">
   <soapenv:Header/>
   <soapenv:Body>
      <soap:GetPokemonAbilitiesRequest>
         <soap:name>pikachu</soap:name>
      </soap:GetPokemonAbilitiesRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

**Ejemplo de Respuesta**:
```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Body>
      <ns2:GetPokemonAbilitiesResponse xmlns:ns2="http://bankaya.com/pokemon/soap">
         <ns2:abilities>
            <ns2:name>static</ns2:name>
            <ns2:url>https://pokeapi.co/api/v2/ability/9/</ns2:url>
            <ns2:hidden>false</ns2:hidden>
            <ns2:slot>1</ns2:slot>
         </ns2:abilities>
         <ns2:abilities>
            <ns2:name>lightning-rod</ns2:name>
            <ns2:url>https://pokeapi.co/api/v2/ability/31/</ns2:url>
            <ns2:hidden>true</ns2:hidden>
            <ns2:slot>3</ns2:slot>
         </ns2:abilities>
      </ns2:GetPokemonAbilitiesResponse>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

### Acceder al WSDL

El contrato WSDL se genera automáticamente desde `pokemon.xsd`:

```
http://localhost:8080/pokemon/ws/pokemon.wsdl
```

Puedes importar este WSDL en SoapUI, Postman o cualquier cliente SOAP para generar automáticamente las peticiones.

### Endpoint REST (Testing)

Adicionalmente, existe un endpoint REST para pruebas:

```bash
GET http://localhost:8080/pokemon/{name}

# Ejemplo
curl http://localhost:8080/pokemon/pikachu
```

Este endpoint devuelve el objeto completo de dominio en formato JSON.

## Desarrollo y Testing

### Ejecutar Tests

```bash
# Ejecutar todos los tests
./gradlew test

# Ejecutar tests con reporte detallado
./gradlew test --info

# Ver reporte de tests en HTML
# Abre: build/reports/tests/test/index.html
```

### Build del Proyecto

```bash
# Compilar sin ejecutar tests
./gradlew build -x test

# Compilar con tests
./gradlew build

# Limpiar y compilar
./gradlew clean build
```

### Verificar Código

```bash
# Verificar compilación
./gradlew check

# Ver dependencias del proyecto
./gradlew dependencies
```

## Estructura del Proyecto

```
pokemon-soap-service/
├── docs/                           # Documentación del challenge
├── src/
│   ├── main/
│   │   ├── java/com/bankaya/pokemon/
│   │   │   ├── application/        # Capa de Aplicación
│   │   │   │   └── service/       # Servicios y casos de uso
│   │   │   ├── domain/            # Capa de Dominio
│   │   │   │   ├── model/        # Entidades de dominio
│   │   │   │   └── port/         # Interfaces (puertos)
│   │   │   │       ├── in/       # Puertos de entrada
│   │   │   │       └── out/      # Puertos de salida
│   │   │   └── infrastructure/    # Capa de Infraestructura
│   │   │       ├── adapter/
│   │   │       │   ├── persistence/  # JPA + PostgreSQL
│   │   │       │   ├── rest/        # Cliente PokeAPI
│   │   │       │   └── soap/        # Endpoints SOAP
│   │   │       └── config/          # Configuraciones Spring
│   │   └── resources/
│   │       ├── xsd/                # Esquemas XSD para SOAP
│   │       │   └── pokemon.xsd    # Definición del contrato
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── log4j2.properties  # Configuración de logging
│   └── test/                       # Tests unitarios e integración
├── build/                          # Archivos generados por Gradle
│   └── generated-sources/jaxb/    # Clases Java generadas desde XSD
├── .env                            # Variables de entorno (no en git)
├── .env.example                    # Plantilla de variables
├── docker-compose.yml              # Orquestación Docker
├── Dockerfile                      # Imagen Docker de la app
├── build.gradle                    # Configuración Gradle
├── gradlew / gradlew.bat          # Gradle Wrapper
└── README.md                       # Documentación
```

## Configuración de Base de Datos

> ⚠️ **IMPORTANTE**: La persistencia en PostgreSQL está **configurada pero NO implementada actualmente**.
> La aplicación funciona como un proxy directo a la PokeAPI sin almacenar datos.

### Estado Actual

**Configurado**:
- ✅ Dependencias JPA/Hibernate en `build.gradle`
- ✅ Driver PostgreSQL incluido
- ✅ Variables de entorno para conexión
- ✅ PostgreSQL en `docker-compose.yml`

**NO Implementado**:
- ❌ Entidades JPA (`@Entity`)
- ❌ Repositorios Spring Data
- ❌ Lógica de persistencia
- ❌ Transacciones

### Parámetros de Conexión

Si decides implementar persistencia, los parámetros configurados son:

- **Host**: `localhost:5432` (local) / `postgres:5432` (Docker)
- **Database**: `pokemondb`
- **User**: `pokemon`
- **Password**: `change_this_password` (configurable en `.env`)
- **Driver**: `org.postgresql.Driver`
- **URL**: `jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/${POSTGRES_DB}`

### Acceder a PostgreSQL (si está corriendo)

```bash
# Con Docker Compose
docker-compose exec postgres psql -U pokemon -d pokemondb

# Con Docker standalone
docker exec -it pokemon-postgres psql -U pokemon -d pokemondb

# Comandos útiles en psql:
\l               # Listar bases de datos
\dt              # Listar tablas (actualmente vacío)
\q               # Salir
```

## Monitoreo y Logs

### Health Check

Verifica el estado de la aplicación:

```bash
curl http://localhost:8080/actuator/health
```

Respuesta esperada:
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

> **Nota**: El componente `db` aparecerá solo si PostgreSQL está corriendo y conectado. Actualmente la app no lo requiere.

### Logs

El sistema de logging está configurado con **Log4j2** (no Logback). Los logs se separan en múltiples archivos:

**Archivos de Log** (en `logs/`):
1. **application.log** - Logs generales de la aplicación (retención: 30 días)
2. **error.log** - Solo errores (retención: 30 días)
3. **soap.log** - Peticiones y respuestas SOAP (retención: 15 días)
4. **database.log** - Logs de Hibernate/JPA si se usa (retención: 7 días)
5. **performance.log** - Logs de WebClient/Reactor (retención: 15 días)

**Ver logs en Docker**:
```bash
# Ver todos los logs
docker-compose logs -f pokemon-service

# Filtrar por nivel
docker-compose logs pokemon-service | grep ERROR
docker-compose logs pokemon-service | grep WARN
docker-compose logs pokemon-service | grep "SOAP Request"
```

**Ver logs en local**:
```bash
# Logs en tiempo real
tail -f logs/application.log

# Solo errores
tail -f logs/error.log

# Peticiones SOAP
tail -f logs/soap.log
```

**Niveles de log** (configurables en `log4j2.properties`):
- **TRACE**: Información muy detallada
- **DEBUG**: Información de debugging (usado para Spring WS)
- **INFO**: Eventos informativos (nivel por defecto)
- **WARN**: Advertencias (usado para Hibernate)
- **ERROR**: Errores
- **FATAL**: Errores críticos

**Loggers configurados**:
- `com.bankaya.pokemon`: INFO (código de la aplicación)
- `org.springframework.ws`: DEBUG (Spring Web Services)
- `org.springframework.web.reactive`: INFO (WebClient)
- `org.hibernate`: WARN (JPA/Hibernate)
- `com.zaxxer.hikari`: INFO (Pool de conexiones)

## Troubleshooting

### La aplicación no inicia

```bash
# Verificar que PostgreSQL esté corriendo
docker-compose ps

# Ver logs de errores
docker-compose logs pokemon-service

# Verificar variables de entorno
docker-compose config
```

### Puerto 8080 en uso

Edita `.env` y cambia el puerto:
```properties
APP_PORT=9090:8080
```

Luego reinicia:
```bash
docker-compose down
docker-compose up -d
```

### Problemas con Gradle

```bash
# Limpiar cache de Gradle
./gradlew clean

# Refrescar dependencias
./gradlew build --refresh-dependencies

# Verificar versión de Java
java -version  # Debe ser Java 21+
```

### Error al consumir PokeAPI

```bash
# Verificar conectividad
curl https://pokeapi.co/api/v2/pokemon/pikachu

# Ver logs de la aplicación para detalles
docker-compose logs -f pokemon-service | grep ERROR

# Verificar que el nombre del Pokemon es válido (debe estar en minúsculas)
# Correcto: pikachu, charizard, mewtwo
# Incorrecto: Pikachu, CHARIZARD
```

## Estado del Proyecto

### ✅ Implementado

- **Arquitectura Hexagonal completa** con separación clara de capas
- **6 operaciones SOAP** funcionales con generación WSDL automática
- **Cliente REST** a PokeAPI con manejo de errores
- **Mapeo automático** con MapStruct entre DTOs y modelos de dominio
- **Sistema de logging** multi-archivo con Log4j2
- **Manejo de excepciones** a nivel de dominio
- **Endpoint REST** para testing/validación
- **Health check** con Actuator
- **Contenerización** con Docker y Docker Compose
- **Modelo de dominio** inmutable usando Java Records

## Licencia

Este proyecto es parte del challenge técnico de Bankaya.

## Contacto

- **GitHub**: [UnderskinGarcia](https://github.com/UnderskinGarcia)
- **Repositorio**: [pokemon-soap-service](https://github.com/UnderskinGarcia/pokemon-soap-service)

---

**Versión**: 0.0.1-SNAPSHOT
**Última actualización**: 2025
**Java**: 21
**Spring Boot**: 3.5.6
