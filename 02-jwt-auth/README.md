# Spring Security + JWT (Node.js → Spring Mental Model)

## Overview

This guide explains how JWT authentication in **Node.js (Express)** translates into **Spring Boot + Spring Security**.

Instead of writing a few simple functions like in Node.js, Spring breaks the same logic into multiple classes because it follows a **strict, modular security architecture**.

---

# The Node.js Version (Simple & Direct)

In Express, authentication is typically just a few blocks:

```javascript
const jwt = require('jsonwebtoken');

// Middleware: authentication check
function authMiddleware(req, res, next) {
    const token = req.headers.authorization?.split(' ')[1];
    if (!token) return res.status(401).json({ message: 'Unauthorized' });

    const decoded = jwt.verify(token, 'secret');
    req.user = decoded;

    next();
}

// Login route: generate token
app.post('/login', async (req, res) => {
    const user = await db.findUser(req.body.username);

    if (!user || !bcrypt.compare(req.body.password, user.password)) {
        return res.status(401).json({ message: 'Invalid credentials' });
    }

    const token = jwt.sign({ username: user.username }, 'secret');
    res.json({ token });
});

// Protected route
app.get('/employees', authMiddleware, (req, res) => {
    res.json([]);
});
```

### Key idea:

Everything happens in **functions + middleware**.

---

# The Spring Boot Version (Same Logic, Split into Layers)

Spring implements the same flow, but splits responsibilities:

| Responsibility            | Node.js             | Spring Boot                   |
| ------------------------- | ------------------- | ----------------------------- |
| JWT creation & validation | `jsonwebtoken`      | `JwtService`                  |
| Request interception      | `authMiddleware`    | `JwtFilter`                   |
| Login logic               | route handler       | `AuthService`                 |
| HTTP layer                | Express route       | `AuthController`              |
| User lookup               | inline DB call      | `CustomUserDetailsService`    |
| Unauthorized response     | `res.status(401)`   | `JwtAuthenticationEntryPoint` |
| Security rules            | middleware mounting | `SecurityConfig`              |

---

# Request Flow in Spring Security

## Incoming Request Pipeline

```
Request
  │
  ▼
SecurityConfig
  │
  ▼
JwtFilter  ──► JwtService ──► UserDetailsService
  │
  ├── valid → Controller
  └── invalid → AuthenticationEntryPoint (401)
```

---

## What Each Class Does (Mental Model)

### 🔐 JwtService — Token Utility

Handles:

* generateToken()
* extractUsername()
* validateToken()

**Node equivalent:**

```js
jwt.sign()
jwt.verify()
```

---

### 🛡 JwtFilter — The Bouncer

Runs on **every request before controllers**.

Responsibilities:

* Extract Bearer token
* Validate token
* Load user details
* Set authentication in Spring context

**Node equivalent:**

```js
function authMiddleware(req, res, next){}
```

---

### 👤 CustomUserDetailsService — User Loader

Loads user from database in a Spring-compatible format.

**Why it exists:**
Spring Security needs a **standard user model (`UserDetails`)**.

**Node equivalent:**

```js
const user = await User.findOne(...)
```

---

### 🚫 JwtAuthenticationEntryPoint — 401 Handler

Handles unauthorized access.

**Node equivalent:**

```js
res.status(401).json({ message: "Unauthorized" })
```

---

### ⚙️ SecurityConfig — Security Blueprint

Defines:

* Which routes are public/private
* Which filters run
* Session policy
* Authentication rules

**Node equivalent:**

```js
app.use(authMiddleware)
```

---

### 🔑 AuthService + AuthController — Login System

* Controller → handles HTTP requests
* Service → contains business logic

**Node equivalent:**

```js
app.post('/login', ...)
```

---

# End-to-End Request Tracing

## Login Flow

```
POST /auth/login
   │
   ▼
AuthController
   │
   ▼
AuthService
   │
   ├── UserRepository (DB lookup)
   ├── PasswordEncoder (verify password)
   └── JwtService (generate token)
   │
   ▼
Response → JWT Token
```

---

## Protected Request Flow

```
GET /employees
Authorization: Bearer <token>

   │
   ▼
SecurityConfig
   │
   ▼
JwtFilter
   │
   ├── JwtService (validate token)
   ├── UserDetailsService (load user)
   │
   ▼
SecurityContext (user stored)
   │
   ▼
Controller executes

OR

Invalid token → JwtAuthenticationEntryPoint (401)
```

---

# Core Mapping Cheat Sheet

| Question                            | Spring Component           |
| ----------------------------------- | -------------------------- |
| How is a JWT created/verified?      | `JwtService`               |
| Where is every request intercepted? | `JwtFilter`                |
| How is a user loaded from DB?       | `UserDetailsService`       |
| What handles login logic?           | `AuthService`              |
| What defines security rules?        | `SecurityConfig`           |
| What returns 401 responses?         | `AuthenticationEntryPoint` |

---

# Why Spring Does This

## Node.js mindset

You control everything:

* request flow
* middleware order
* logic location

## Spring mindset

Spring controls the lifecycle:

* You plug into hooks
* Framework manages execution order
* You define behavior per layer

---

# Key Insight (Most Important Part)

In Node.js:

```js
req.user = user
```

In Spring:

```java
SecurityContextHolder.getContext().setAuthentication(authToken);
```

👉 This is the same concept:

> “Attach authenticated user to current request”

But Spring stores it in a **global security context instead of req object**.

---

# Final Mental Model

Think of Spring Security as:

```
A controlled security pipeline where:
- Filters = middleware chain
- SecurityContext = req.user storage
- Services = reusable logic blocks
- Config = firewall rules
```

---