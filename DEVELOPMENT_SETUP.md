# Development Setup Guide

This guide will help you set up the Dauth (Authentication & Authorization) project on your local machine.

## Prerequisites

Before you begin, ensure you have the following software installed on your system:

### Required Software

1. **Java Development Kit (JDK) 21**
   - Download from: https://adoptium.net/ or https://www.oracle.com/java/technologies/downloads/
   - Verify installation: `java -version`
   - Should show version 21.x.x

2. **Apache Maven 3.9+**
   - Download from: https://maven.apache.org/download.cgi
   - Installation guide: https://maven.apache.org/install.html
   - Verify installation: `mvn -version`
   - Should show Maven 3.9.x or higher

3. **Docker Desktop**
   - Download from: https://www.docker.com/products/docker-desktop
   - Required for running PostgreSQL, Redis, and pgAdmin containers
   - Verify installation: `docker --version` and `docker-compose --version`

4. **Git**
   - Download from: https://git-scm.com/downloads
   - Verify installation: `git --version`

### Recommended Software

1. **IntelliJ IDEA** (Ultimate or Community Edition)
   - Download from: https://www.jetbrains.com/idea/download/
   - Recommended for Java development with excellent Spring Boot support

2. **Visual Studio Code** (Alternative)
   - Download from: https://code.visualstudio.com/
   - Install extensions: Java Extension Pack, Spring Boot Extension Pack

3. **Postman** or **Insomnia**
   - For API testing
   - Postman: https://www.postman.com/downloads/
   - Insomnia: https://insomnia.rest/download

## Installation Steps

### 1. Install Java 21

#### Windows
```bash
# Download and run the installer from Adoptium
# Add JAVA_HOME to environment variables
# Add %JAVA_HOME%\bin to PATH
```

#### macOS
```bash
# Using Homebrew
brew install openjdk@21

# Add to PATH in ~/.zshrc or ~/.bash_profile
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH=$JAVA_HOME/bin:$PATH
```

#### Linux
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-21-jdk

# Verify
java -version
```

### 2. Install Maven

#### Windows
```bash
# Download Maven from https://maven.apache.org/download.cgi
# Extract to C:\Program Files\Apache\maven
# Add M2_HOME environment variable
# Add %M2_HOME%\bin to PATH
```

#### macOS
```bash
# Using Homebrew
brew install maven

# Verify
mvn -version
```

#### Linux
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install maven

# Verify
mvn -version
```

### 3. Install Docker Desktop

#### Windows & macOS
- Download and install Docker Desktop from https://www.docker.com/products/docker-desktop
- Start Docker Desktop application
- Verify: `docker --version`

#### Linux
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install docker.io docker-compose

# Start Docker service
sudo systemctl start docker
sudo systemctl enable docker

# Add user to docker group (to run without sudo)
sudo usermod -aG docker $USER
# Log out and log back in for changes to take effect
```

## Project Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd Dauth
```

### 2. Start Required Services

Start PostgreSQL, Redis, and pgAdmin using Docker Compose:

```bash
docker-compose up -d
```

This will start:
- **PostgreSQL** on port 5432 (Required)
- **Redis** on port 6379 (Required)
- **pgAdmin** on port 5050 (Optional - http://localhost:5050)
- **OpenLDAP** on ports 389 and 636 (Optional - not currently used)
- **phpLDAPadmin** on port 8081 (Optional - http://localhost:8081)

**Note**: Only PostgreSQL and Redis are required. LDAP services are optional and not currently integrated.

To start only required services:
```bash
docker-compose up -d postgres redis
```

Verify containers are running:
```bash
docker ps
```

### 3. Configure Environment Variables (Optional)

The application uses sensible defaults for local development. If you need to customize, create a `.env` file or set environment variables:

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=dauth
DB_USERNAME=dauth_user
DB_PASSWORD=dauth_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT Secret (use a strong secret in production)
JWT_SECRET=your-secret-key-here

# Email Configuration (optional for local dev)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200

# LDAP Configuration (optional for local dev)
LDAP_URLS=ldap://localhost:389
LDAP_BASE=dc=datavion,dc=com
LDAP_USERNAME=cn=admin,dc=datavion,dc=com
LDAP_PASSWORD=admin
LDAP_USER_SEARCH_BASE=ou=users
LDAP_USER_SEARCH_FILTER=(uid={0})
LDAP_GROUP_SEARCH_BASE=ou=groups
LDAP_GROUP_SEARCH_FILTER=(member={0})
```

### 4. Build the Project

```bash
# Clean and install dependencies
mvn clean install

# Or skip tests for faster build
mvn clean install -DskipTests
```

### 5. Run Database Migrations

Flyway migrations will run automatically when you start the application. The migrations are located in `src/main/resources/db/migration/`.

### 6. Run the Application

#### Using Maven
```bash
mvn spring-boot:run
```

#### Using IDE (IntelliJ IDEA)
1. Open the project in IntelliJ IDEA
2. Wait for Maven to download dependencies
3. Navigate to `AuthenticationApplication.java`
4. Right-click and select "Run 'AuthenticationApplication'"

#### Using JAR
```bash
# Build JAR
mvn clean package -DskipTests

# Run JAR
java -jar target/Dauth-0.0.1-SNAPSHOT.jar
```

The application will start on **http://localhost:8080**

### 7. Verify Installation

Check if the application is running:

```bash
# Health check
curl http://localhost:8080/api/v1/health

# Or visit in browser
http://localhost:8080/api/v1/health
```

## Access Points

Once the application is running, you can access:

- **API Base URL**: http://localhost:8080/api/v1
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **Health Check**: http://localhost:8080/api/v1/health
- **pgAdmin**: http://localhost:5050
  - Email: admin@dauth.com
  - Password: admin
- **phpLDAPadmin**: http://localhost:8081
  - Login DN: cn=admin,dc=datavion,dc=com
  - Password: admin

## IDE Setup

### IntelliJ IDEA

1. **Install Lombok Plugin**
   - Go to File → Settings → Plugins
   - Search for "Lombok"
   - Install and restart IDE

2. **Enable Annotation Processing**
   - Go to File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Check "Enable annotation processing"

3. **Configure Code Style**
   - Go to File → Settings → Editor → Code Style → Java
   - Set tab size to 4 spaces
   - Enable "Use tab character" if preferred

### VS Code

1. **Install Extensions**
   - Java Extension Pack
   - Spring Boot Extension Pack
   - Lombok Annotations Support

2. **Configure Settings**
   - Open settings.json
   - Add Java 21 path if needed

## Database Access

### Using pgAdmin

1. Open http://localhost:5050
2. Login with credentials (admin@dauth.com / admin)
3. Add new server:
   - Name: Dauth Local
   - Host: postgres (or localhost if connecting from host machine)
   - Port: 5432
   - Database: dauth
   - Username: dauth_user
   - Password: dauth_password

### Using Command Line

```bash
# Connect to PostgreSQL container
docker exec -it dauth-postgres psql -U dauth_user -d dauth

# List tables
\dt

# Exit
\q
```

## LDAP Setup & Configuration (Optional)

**Note**: LDAP is currently **not integrated** into the authentication flow. The application uses database-based authentication with JWT tokens. This section is for future LDAP integration.

The project includes LDAP dependencies and configuration. For local development, OpenLDAP is provided via Docker if you want to test LDAP integration.

### Access phpLDAPadmin

1. Open http://localhost:8081
2. Click "login"
3. Login DN: `cn=admin,dc=datavion,dc=com`
4. Password: `admin`

### Create Test LDAP Users

You can create test users and groups using phpLDAPadmin or via command line:

```bash
# Create LDIF file for test users
cat > test-users.ldif << 'EOF'
# Create organizational units
dn: ou=users,dc=datavion,dc=com
objectClass: organizationalUnit
ou: users

dn: ou=groups,dc=datavion,dc=com
objectClass: organizationalUnit
ou: groups

# Create test user
dn: uid=testuser,ou=users,dc=datavion,dc=com
objectClass: inetOrgPerson
objectClass: posixAccount
objectClass: shadowAccount
uid: testuser
cn: Test User
sn: User
givenName: Test
mail: testuser@datavion.com
userPassword: password123
uidNumber: 10000
gidNumber: 10000
homeDirectory: /home/testuser
loginShell: /bin/bash

# Create admin user
dn: uid=admin,ou=users,dc=datavion,dc=com
objectClass: inetOrgPerson
objectClass: posixAccount
objectClass: shadowAccount
uid: admin
cn: Admin User
sn: Admin
givenName: Admin
mail: admin@datavion.com
userPassword: admin123
uidNumber: 10001
gidNumber: 10001
homeDirectory: /home/admin
loginShell: /bin/bash

# Create groups
dn: cn=users,ou=groups,dc=datavion,dc=com
objectClass: groupOfNames
cn: users
member: uid=testuser,ou=users,dc=datavion,dc=com

dn: cn=admins,ou=groups,dc=datavion,dc=com
objectClass: groupOfNames
cn: admins
member: uid=admin,ou=users,dc=datavion,dc=com
EOF

# Import into LDAP
docker exec -i dauth-openldap ldapadd -x -D "cn=admin,dc=datavion,dc=com" -w admin -f /dev/stdin < test-users.ldif
```

### Test LDAP Connection

```bash
# Search for all users
docker exec dauth-openldap ldapsearch -x -b "dc=datavion,dc=com" -D "cn=admin,dc=datavion,dc=com" -w admin

# Search for specific user
docker exec dauth-openldap ldapsearch -x -b "ou=users,dc=datavion,dc=com" -D "cn=admin,dc=datavion,dc=com" -w admin "(uid=testuser)"

# Verify user authentication
docker exec dauth-openldap ldapwhoami -x -D "uid=testuser,ou=users,dc=datavion,dc=com" -w password123
```

### LDAP Configuration in Application

The application is configured to use LDAP in `application.yml`. For local development, the default settings work with the Docker OpenLDAP container:

- **LDAP URL**: ldap://localhost:389
- **Base DN**: dc=datavion,dc=com
- **Admin DN**: cn=admin,dc=datavion,dc=com
- **Admin Password**: admin
- **User Search Base**: ou=users
- **Group Search Base**: ou=groups

### LDAP is Optional

**Important**: LDAP is currently **NOT required** to run the application. The project uses database-based authentication (DaoAuthenticationProvider) with JWT tokens. LDAP configuration is present for future integration but is not actively used.

You can safely:
1. Skip starting the LDAP containers by commenting them out in `docker-compose.yml`
2. Run without LDAP environment variables
3. The application will work perfectly with just PostgreSQL and Redis

To run without LDAP services:
```bash
# Start only required services
docker-compose up -d postgres redis pgadmin
```

If you want to integrate LDAP authentication in the future, the infrastructure is ready to use.

## Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test
```bash
mvn test -Dtest=AuthenticationApplicationTests
```

### Run with Coverage
```bash
mvn clean test jacoco:report
```

## Common Issues & Troubleshooting

### Port Already in Use

If port 8080, 5432, or 6379 is already in use:

```bash
# Windows - Find process using port
netstat -ano | findstr :8080

# macOS/Linux - Find process using port
lsof -i :8080

# Kill the process or change port in application.yml
```

### Docker Containers Not Starting

```bash
# Stop all containers
docker-compose down

# Remove volumes and restart
docker-compose down -v
docker-compose up -d
```

### Maven Build Fails

```bash
# Clear Maven cache
mvn dependency:purge-local-repository

# Rebuild
mvn clean install -U
```

### Lombok Not Working

- Ensure Lombok plugin is installed in your IDE
- Enable annotation processing in IDE settings
- Restart IDE after installation

## Development Workflow

1. **Create a new branch** for your feature
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make changes** and test locally

3. **Run tests** before committing
   ```bash
   mvn test
   ```

4. **Commit changes**
   ```bash
   git add .
   git commit -m "Description of changes"
   ```

5. **Push to remote**
   ```bash
   git push origin feature/your-feature-name
   ```

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Docker Documentation](https://docs.docker.com/)
- [Maven Documentation](https://maven.apache.org/guides/)

## Support

If you encounter any issues during setup, please:
1. Check the troubleshooting section above
2. Review application logs in the console
3. Contact the development team

---

Happy coding! 🚀
