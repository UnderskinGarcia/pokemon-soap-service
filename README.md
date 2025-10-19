# Pokemon SOAP Service

SOAP Web Service para consumir la API de Pokemon (PokeAPI) utilizando arquitectura hexagonal.

## Tecnologías

- **Java 21**
- **Spring Boot 3.5.6**
- **PostgresSQL** (Base de datos)
- **Gradle** (Build tool)
- **Docker & Docker Compose**

## Arquitectura

El proyecto sigue una **Arquitectura Hexagonal** (Ports & Adapters):

```
src/main/java/com/bankaya/pokemon/
├── application/          # Casos de uso y servicios
├── domain/              # Entidades y puertos
└── infrastructure/      # Adaptadores (REST, SOAP, DB)
    ├── adapter/
    │   ├── persistence/ # JPA/PostgreSQL
    │   ├── rest/       # Cliente PokeAPI
    │   └── soap/       # Endpoints SOAP
    └── config/         # Configuraciones
```

## Requisitos Previos

- **Java 21** o superior
- **Docker** y **Docker Compose**
- **Gradle** (incluido wrapper)`

### Archivo `.env`

Copia el archivo de ejemplo y ajusta según tu entorno:

```bash
  cp .env.example .env
```

Variables disponibles en `.env`:

```properties
# Spring Profile
SPRING_PROFILES_ACTIVE=dev

# PostgreSQL Database
POSTGRES_HOST=localhost
POSTGRES_DB=pokemon_db
POSTGRES_USER=pokemon_user
POSTGRES_PASSWORD=pokemon_password
```

## Ejecución

### Con Docker Compose (Recomendado)

Inicia la base de datos y la aplicación:

```bash
docker-compose up -d
```

### Sin Docker (Local)

1. **Inicia PostgreSQL** (debe estar corriendo en localhost:5432)

2. **Configura las variables de entorno**:
   ```bash
   export SPRING_PROFILES_ACTIVE=dev
   ```

3. **Ejecuta la aplicación**:
   ```bash
   ./gradlew bootRun
   ```

## Estructura del Proyecto

```
pokemon-soap-service/
├── docs/                           # Documentación del challenge
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/bankaya/pokemon/
│   │   │       ├── application/    # Lógica de negocio
│   │   │       ├── domain/         # Modelos y puertos
│   │   │       └── infrastructure/ # Adaptadores
│   │   └── resources/
│   │       ├── xsd/               # Esquemas XSD para SOAP
│   │       ├── application.properties
│   │       └── application-dev.properties
│   └── test/
├── .env
├── docker-compose.yml
├── Dockerfile
├── build.gradle
└── README.md
```

### Ejecutar Tests

```bash
./gradlew test
```

### Build del Proyecto

```bash
./gradlew build
```

## Base de Datos

### Conexión

- **Host**: localhost:5432 (configurable vía `POSTGRES_HOST`)
- **Database**: pokemon_db
- **User**: pokemon_user
- **Password**: pokemon_password

## Docker

### Construir Imagen

```bash
docker build -t pokemon-soap-service .
```

### Ejecutar con Docker Compose

```bash
# Iniciar servicios
docker-compose up -d

# Ver logs
docker-compose logs -f app

# Detener servicios
docker-compose down

# Eliminar volumen
docker-compose down -v
```

## Licencia
Este proyecto es parte del challenge técnico de Bankaya.

## Contacto

- **GitHub**: [UnderskinGarcia](https://github.com/UnderskinGarcia)
- **Repositorio**: [pokemon-soap-service](https://github.com/UnderskinGarcia/pokemon-soap-service)
