# UniHub

UniHub is a **Modular Monolith** platform that connects **students, universities, and companies** in one system.

It is built with **Angular** for the frontend and **Java Spring Boot** for the backend, with a clean modular structure designed for maintainability, scalability, and production readiness.

---

## What UniHub does
UniHub is designed to help:

* **Students** build profiles, upload documents, apply for jobs.
* **Universities** manage students, and create partnerships with companies.
* **Companies** publish jobs, review applicants, and collaborate with universities.

---

## Core Features

### Student

* Student profile management
* Experience, projects, and certificates
* Job application tracking

### University

* Student verification
* University profile management
* Partnerships with companies
* Student employment statistics
* Messaging with students and companies

### Company

* Company profile management
* Job creation as draft or published
* Applicant review and CV viewing
* Partnership management with universities

### Jobs

* Create, update, publish, and archive jobs
* Search and filter jobs
* Manage applications

### Chat

* Student ↔ University chat
* Student ↔ Company chat
* Real-time communication

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
* `documents`
* `notifications`

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
## Roadmap

* [ ] Identity and authentication
* [ ] Student profiles
* [ ] University verification
* [ ] Company partnerships
* [ ] Job posting and applications
* [ ] Real-time chat
* [ ] Notifications
* [ ] Admin dashboard

---
