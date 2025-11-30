# Bottle-Crate-Store

Lightweight Spring Boot web application for managing bottles, crates and orders (MVC + JPA).

## 📘 Table of contents
- Description
- Features
- Architecture  
- Tech stack
- Prerequisites
- Quick start (Windows)
- Configuration
- Running with PostgreSQL
- Running with in-memory H2 (default for development)
- Tests
- Troubleshooting
- Contributing
- License


## 📝 Description
Bottle-Crate-Store is a Spring Boot application that demonstrates a simple catalogue and ordering flow for beverages (bottles and crates). The project uses Spring MVC, Spring Data JPA, Thymeleaf and Spring Security.

## ⭐ Features
- Manage bottles and crates
- Shopping cart and order checkout
- User registration and roles
- JPA entities with validation
- Demo seed data on startup (when not running with the `test` profile)

## 🏛️ Architecture
- **Controller Layer** (Spring MVC)
- **Service Layer**
- **Repository Layer** (Spring Data JPA)
- **Entity Model** (Hibernate 6)
- **Thymeleaf UI**
- **Security Layer** (Spring Security)

Module:  
`beverage_store/`  
Contains controllers, services, entities, config, templates, and security.

---

## 🛠 Tech stack
- Java 21
- Spring Boot 3.x
- Spring Data JPA (Hibernate 6.x)
- Thymeleaf
- Spring Security
- Configuration  
- Database Options  
  - H2 In-Memory  
  - PostgreSQL  
  - Firebase (Optional)  
- Tests  
- Troubleshooting  
- Contributing  

## ✔ Prerequisites
- Java 21 JDK installed and `JAVA_HOME` set
- Git
- (Optional) PostgreSQL server if you want to run against Postgres
- (Optional) Firebase project + service account if you want to enable Firebase features (may require billing for some features)
- Windows PowerShell or CMD for commands shown below

## ⚡ Quick start (Windows)
### 1️⃣ Navigate to project
```sh
cd D:\Git project\Bottle-Crate-Store
```

### 2️⃣ Build the project
```sh
.\gradlew.bat clean build
```

### 3️⃣ Run app using H2 (default)
```sh
.\gradlew.bat :beverage_store:bootRun
```

### 4️⃣ Open in browser
```
http://localhost:8080
```

---

## ⚙ Configuration
Project configuration files live under:
 `beverage_store/src/main/resources/`

- `application.properties` — default configuration (development)
- `application-postgres.properties` — profile to use PostgreSQL
- `application-postgres.properties` and Firebase settings are not enabled by default; update credentials and profiles to activate them

Typical properties to edit for Postgres:
- `spring.datasource.url=jdbc:postgresql://localhost:5432/your_db`
- `spring.datasource.username=your_user`
- `spring.datasource.password=your_password`

The default development configuration uses H2 in-memory:
- H2 console (if enabled) available at `/h2-console` (see `application.properties`)

---

## 🗄 Database Options

---
# 1️⃣ H2 In‑Memory (Default)
Best for development — zero setup.

**Runs automatically** if you do nothing.

**Start app**
```sh
.\gradlew.bat :beverage_store:bootRun
```

**Access H2 console**
```
http://localhost:8080/h2-console
```

Default JDBC URL:
```
jdbc:h2:mem:beverage
```

---

# 2️⃣ PostgreSQL (Optional)

### Enable Postgres profile:
PowerShell:
```sh
$env:SPRING_PROFILES_ACTIVE='postgres'
.\gradlew.bat :beverage_store:bootRun
```

Or JVM flag:
```sh
.\gradlew.bat :beverage_store:bootRun -Dspring.profiles.active=postgres
```
### Configure Postgres
In `application-postgres.properties`:

```
spring.datasource.url=jdbc:postgresql://localhost:5432/your_db
spring.datasource.username=your_user
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

---

# 3️⃣ Firebase Integration (Optional)

The project includes Firebase Admin SDK integration.

Firebase is **optional** and disabled by default.

### To enable Firebase:
1. Create Firebase project  
2. Download **Service Account JSON**  
3. Place file under:
```
beverage_store/src/main/resources/firebase/service-account.json
```
4. Add config to `application.properties`:
```
firebase.enabled=true
firebase.service-account=classpath:firebase/service-account.json
```

> ⚠ Firebase features may require Google Cloud billing.

The app works fully **without Firebase** using only local DB.
- Firebase: The repository includes integration code for Firebase (Firebase Admin SDK), but Firebase database/hosting features may require setting up a Firebase project and enabling billing for some services. The Firebase connection is not enabled by default — supply a service account JSON and the appropriate configuration to activate it. The REST/API endpoints in this app work without an active Firebase connection (they use the local DB by default).
- Remote DB / hosted Hibernate: Connections to hosted databases or managed DB services (remote PostgreSQL, cloud databases) may require paid accounts and proper credentials. The demo and development setup use in-memory H2 so you can run the application and API locally without paid services.
---

## 🧪 Tests
Run unit and integration tests:
- `.\gradlew.bat test`

## 🐞 Troubleshooting / Notes
- jakarta.validation types
  - If you see compile errors like `package jakarta.validation.constraints does not exist`, add the validation starter to the module dependencies:
    - `implementation 'org.springframework.boot:spring-boot-starter-validation'`

- MultipleBagFetchException
  - Hibernate will throw `MultipleBagFetchException` if it tries to fetch multiple un-ordered List collections (bags) in the same query. Fixes:
    - Use `Set` instead of `List` for collections that are fetched together, or
    - Add `@OrderColumn` to Lists, or
    - Use `@Fetch(FetchMode.SUBSELECT)` when appropriate.

- Lombok @Builder warning
  - If Lombok `@Builder` is used on an entity with an initializing expression (e.g. `private Set<Order> orders = new HashSet<>();`) annotate the field with `@Builder.Default` to keep the default value when using the builder.

- Database migrations
  - The project currently uses Hibernate `ddl-auto=update` for development. For production, prefer a migration tool such as Flyway or Liquibase.

- Demo users
  - The development demo runner seeds sample data (users like `Admin` and `Max` with simple passwords in the demo code). Check `BeverageStoreApplication` for details. Remove or change demo credentials before deploying.

## 🤝 Contributing
1. Fork the repository
2. Create a branch: `git checkout -b feature/your-feature`
3. Commit changes and push
4. Open a pull request

Keep changes small and focused. Include tests where appropriate.

## ✔ Author
**Nihar Sawant**  
DevOps & Software Engineer passionate about automation, backend systems, and cloud technologies.

