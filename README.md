# UniHub

UniHub is a **Modular Monolith** platform that connects **students, universities, and companies** in one system.

It is built with **Angular** for the frontend and **Java Spring Boot** for the backend, with a clean modular structure designed for maintainability, scalability, and production readiness.

---

## What UniHub does

UniHub is designed to help:

* **Students** build profiles, upload documents, apply for jobs, ask questions, and earn points/badges.
* **Universities** verify certificates, manage students, and create partnerships with companies.
* **Companies** publish jobs, review applicants, schedule interviews, and collaborate with universities.

---

## Core Features

### Student

* Student profile management
* Experience, projects, and certificates
* Daily check-in points and achievements
* CV generation with AI
* ATS score analysis
* Notes summary with AI
* Job application tracking

### University

* Student verification
* Certificate approval / rejection
* University profile management
* Partnerships with companies
* Student employment statistics
* Messaging with students and companies

### Company

* Company profile management
* Job creation as draft or published
* Applicant review and CV viewing
* Interview scheduling
* Partnership management with universities

### Jobs

* Create, update, publish, and archive jobs
* Search and filter jobs
* Manage applications
* Interview workflow

### Chat

* Student ↔ University chat
* Student ↔ Company chat
* Real-time communication

### AI

* CV generation
* ATS scoring
* Notes summarization
* Interview question generation

### Forum

* Ask and answer questions
* Voting system
* Accepted answers
* Community discussions

---

## Tech Stack

### Frontend

* Angular
* TypeScript
* SCSS

### Backend

* Java 17+
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Security
* Spring Modulith
* Validation
* Lombok

### Database & Infra

* PostgreSQL
* Redis
* RabbitMQ
* Docker

---

## Architecture

UniHub follows a **Modular Monolith** approach.

That means:

* One backend application
* One deployment unit
* Clear module boundaries
* Independent business modules
* Better maintainability than a classic monolith

### Backend Modules

* `identity`
* `student`
* `university`
* `company`
* `jobs`
* `applications`
* `chat`
* `forum`
* `documents`
* `ai`
* `notifications`
* `gamification`

Each module has its own:

* `api`
* `application`
* `domain`
* `infrastructure`
* `mapper`

---

## Repository Structure

```text
unihub/
├─ docs/
├─ infra/
├─ unihub-backend/
│  └─ src/
│     ├─ main/
│     │  ├─ java/com/unihub/
│     │  │  ├─ modules/
│     │  │  │  ├─ student/
│     │  │  │  ├─ university/
│     │  │  │  ├─ company/
│     │  │  │  ├─ jobs/
│     │  │  │  └─ ...
│     │  │  └─ shared/
│     │  └─ resources/
│     └─ test/
└─ unihub-frontend/
```

---

## Getting Started

### 1) Clone the repository

```bash
git clone https://github.com/abanoubwagim/unihub.git
cd unihub
```

### 2) Start the backend

Go to the backend folder:

```bash
cd unihub-backend
```

Run the backend:

```bash
mvn spring-boot:run
```

The backend will start on:

```text
http://localhost:8080
```

### 3) Start the frontend

Open a new terminal and go to the frontend folder:

```bash
cd unihub-frontend
```

Install dependencies:

```bash
npm install
```

Run the frontend:

```bash
ng serve
```

The frontend will start on:

```text
http://localhost:4200
```

---

## Configuration

### Backend configuration

Use `application.properties` or `application.yml` inside:

```text
unihub-backend/src/main/resources/
```

Example database config:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/unihub
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

> For production, use environment variables or secret management instead of hardcoding sensitive values.

---

## Modulith Testing

This project includes a module boundary test to make sure modules stay properly separated.

Example:

```java
ApplicationModules.of(UniHubApplication.class).verify();
```

This helps detect:

* Illegal module dependencies
* Circular references
* Boundary violations

---

## Development Rules

* Keep business logic out of controllers.
* Do not access another module's internal classes directly.
* Use events for cross-module communication when possible.
* Keep shared code small and generic.
* Store files in object storage, not in the database.
* Keep AI and heavy processing asynchronous.

---

## Roadmap

* [ ] Identity and authentication
* [ ] Student profiles
* [ ] University verification
* [ ] Company partnerships
* [ ] Job posting and applications
* [ ] Real-time chat
* [ ] Forum and Q&A
* [ ] AI-powered CV tools
* [ ] Notifications
* [ ] Gamification system
* [ ] Admin dashboard

---

## Contributing

Contributions are welcome. Please keep the architecture modular and follow the existing structure.

### Suggested workflow

1. Create a branch
2. Make your changes
3. Add tests
4. Run the application locally
5. Open a pull request

---

## License

This project is currently private / not licensed.

---

## Status

UniHub is under active development.
