# Docker Setup for Ticket Booking System

This guide explains how to run the Ticket Booking System using Docker.

## Quick Start

### Option 1: Using Docker Compose (Recommended)

1. **Build and run everything:**
   ```bash
   docker-compose up --build
   ```

2. **Run in background:**
   ```bash
   docker-compose up -d --build
   ```

3. **Stop services:**
   ```bash
   docker-compose down
   ```

### Option 2: Manual Docker Build

1. **Build the Docker image:**
   ```bash
   docker build -t ticket-booking-app .
   ```

2. **Run PostgreSQL database:**
   ```bash
   docker run -d \
     --name postgres-db \
     -e POSTGRES_DB=demo \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=mypassword \
     -p 5432:5432 \
     postgres:16
   ```

3. **Run the application:**
   ```bash
   docker run -d \
     --name ticket-booking-app \
     -p 8888:8888 \
     -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/demo \
     -e SPRING_DATASOURCE_USERNAME=postgres \
     -e SPRING_DATASOURCE_PASSWORD=mypassword \
     ticket-booking-app
   ```

## Configuration

### Environment Variables

The application supports the following environment variables:

#### Database

- `SPRING_DATASOURCE_URL` - PostgreSQL connection URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password

#### PayPal (Required for payment features)

- `PAYPAL_CLIENT_ID` - Your PayPal client ID
- `PAYPAL_CLIENT_SECRET` - Your PayPal client secret
- `PAYPAL_MODE` - `sandbox` or `live`

#### Application

- `LOGGING_LEVEL_ROOT` - Root logging level (default: `INFO`)
- `LOGGING_LEVEL_COM_EXAMPLE` - Application-specific logging level

#### Database/JPA (Production-Safe Defaults)

- `SPRING_JPA_HIBERNATE_DDL_AUTO` - Database schema management (default: `validate`)
    - `validate` - Production-safe: only validates schema
    - `create` - Development: recreates schema
    - `update` - Development: updates schema
    - `none` - Production: no schema management
- `SPRING_JPA_SHOW_SQL` - Show SQL queries in logs (default: `false`)
- `SPRING_JPA_FORMAT_SQL` - Format SQL queries (default: `false`)
- `SPRING_JPA_GENERATE_STATISTICS` - Generate Hibernate statistics (default: `false`)

### Example with PayPal Configuration

Create a `.env` file in the project root:

```bash
# PayPal Configuration
PAYPAL_CLIENT_ID=your_actual_paypal_client_id_here
PAYPAL_CLIENT_SECRET=your_actual_paypal_client_secret_here
PAYPAL_MODE=sandbox

# Optional: Logging
LOGGING_LEVEL_COM_EXAMPLE=DEBUG
```

Then run with:

```bash
docker-compose --env-file .env up --build
```

## Access Points

Once running, access the application at:

- **Main Application:** http://localhost:8888/
- **User Management:** http://localhost:8888/users.html
- **Purchase Tickets:** http://localhost:8888/purchase.html
- **Payment Management:** http://localhost:8888/payments.html
- **Booking Management:** http://localhost:8888/bookings.html
- **API Documentation:** http://localhost:8888/swagger-ui.html
- **Health Check:** http://localhost:8888/actuator/health

## Logs

### View application logs:

```bash
# With docker-compose
docker-compose logs -f app

# With docker
docker logs -f ticket-booking-app
```

### Log files are also persisted to `./logs/` directory.

## Database Access

### Connect to PostgreSQL:

```bash
# With docker-compose
docker-compose exec db psql -U postgres -d demo

# With docker
docker exec -it postgres-db psql -U postgres -d demo
```

## Troubleshooting

### Common Issues:

1. **Port already in use:**
   ```bash
   # Check what's using port 8888
   netstat -tulpn | grep 8888
   # Kill the process or change the port in docker-compose.yml
   ```

2. **Database connection issues:**
   ```bash
   # Check if database is ready
   docker-compose logs db

   # Restart services
   docker-compose restart
   ```

3. **Build issues:**
   ```bash
   # Clean rebuild
   docker-compose down
   docker system prune -f
   docker-compose up --build --force-recreate
   ```

4. **PayPal integration not working:**
    - Ensure `PAYPAL_CLIENT_ID` and `PAYPAL_CLIENT_SECRET` are set
    - Check that you're using the correct PayPal mode (`sandbox` vs `live`)
    - Verify PayPal credentials are valid

## Development

### For development with live reload:

```bash
# Run only database
docker-compose up -d db

# Run Spring Boot locally (uses localhost database by default)
./gradlew bootRun
```

### Useful commands:

```bash
# View container status
docker-compose ps

# Restart specific service
docker-compose restart app

# View real-time logs
docker-compose logs -f

# Execute commands in running container
docker-compose exec app sh

# Clean up everything
docker-compose down -v --rmi all
```

## Production Considerations

### 1. **Database Schema Management:**

```bash
# For production, use database migrations instead of Hibernate DDL
SPRING_JPA_HIBERNATE_DDL_AUTO=validate  # or none
```

### 2. **Security:**

- Change default database passwords
- Use proper PayPal production credentials
- Configure proper CORS origins
- Use HTTPS in production
- Set secure JWT secrets

### 3. **Performance:**

```bash
# Production-optimized settings
SPRING_JPA_SHOW_SQL=false
SPRING_JPA_FORMAT_SQL=false
SPRING_JPA_GENERATE_STATISTICS=false
LOGGING_LEVEL_ROOT=WARN
LOGGING_LEVEL_COM_EXAMPLE=INFO
```

### 4. **Production Configuration:**

For production, copy the template and configure it:

```bash
# Copy production template
cp src/main/resources/application-prod.yml.template src/main/resources/application-prod.yml

# Set production profile
SPRING_PROFILES_ACTIVE=prod
```

**Production Environment Variables:**

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db-host:5432/prod_db
SPRING_DATASOURCE_USERNAME=app_user
SPRING_DATASOURCE_PASSWORD=secure_password

# JPA - Production Safe
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_JPA_SHOW_SQL=false

# PayPal - Production
PAYPAL_MODE=live
PAYPAL_CLIENT_ID=production_client_id
PAYPAL_CLIENT_SECRET=production_client_secret

# Logging
LOGGING_LEVEL_ROOT=WARN
```

### 5. **Database Migrations:**

Consider using Flyway or Liquibase for production database schema management instead of Hibernate DDL auto.

### 6. **Monitoring:**

- Use health check endpoints
- Monitor application logs
- Set up proper alerting

## Architecture

The Docker setup includes:

- **Multi-stage build** for optimized image size
- **Health checks** for both app and database
- **Non-root user** for security
- **Volume persistence** for database data and logs
- **Network isolation** with custom bridge network
- **Graceful shutdown** handling