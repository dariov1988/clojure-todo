# Clojure Todo App

A modern todo application built with Clojure, demonstrating best practices including:
- User authentication with encrypted password storage
- SQLite database with encrypted data
- RESTful API
- Svelte frontend with shadcn components

## Features

- **Authentication**: User registration and login with JWT tokens
- **Password Security**: BCrypt password hashing
- **Database**: SQLite with encrypted storage
- **API**: RESTful endpoints for todos
- **Modern Frontend**: Svelte with shadcn UI components

## Prerequisites

- Clojure CLI (clj)
- Node.js and npm (for frontend)
- entr (optional, for file watching in development)

## Development

### Quick Start

The easiest way to run the project in development mode:

```bash
# Run both backend and frontend
./dev.sh both

# Or run them separately:
./dev.sh backend   # Backend only (port 3000)
./dev.sh frontend  # Frontend only (port 5173)
./dev.sh watch     # Backend with file watching (requires entr)
```

### Manual Setup

**Backend:**

1. Start the REPL:
```bash
clj -M:dev
```

2. In the REPL:
```clojure
(require '[clojure-todo.core :as core])
(core/-main)
```

Or run directly:
```bash
clj -M -m clojure-todo.core
```

The server will start on port 3000 (or PORT environment variable).

**Frontend:**

```bash
cd frontend
npm install
npm run dev
```

## API Endpoints

### Authentication

- `POST /api/auth/register` - Register a new user
  ```json
  {
    "username": "user123",
    "email": "user@example.com",
    "password": "password123"
  }
  ```

- `POST /api/auth/login` - Login
  ```json
  {
    "username": "user123",
    "password": "password123"
  }
  ```

### Todos (Requires Authorization Header: `Authorization: Bearer <token>`)

- `GET /api/me` - Get current user info
- `GET /api/todos` - Get all todos
- `POST /api/todos` - Create a todo
  ```json
  {
    "title": "My Todo",
    "description": "Optional description"
  }
  ```
- `GET /api/todos/:id` - Get a specific todo
- `PUT /api/todos/:id` - Update a todo
  ```json
  {
    "title": "Updated title",
    "description": "Updated description",
    "completed": true
  }
  ```
- `DELETE /api/todos/:id` - Delete a todo

## Frontend Setup

See `frontend/README.md` for frontend setup instructions.

## Database

The SQLite database (`todo_app.db`) is created automatically on first run. 

### Security

- **Password Encryption**: Passwords are hashed using BCrypt (cost factor 12) before storage. Passwords are never stored in plain text.
- **Database Encryption**: For production use, consider using SQLCipher for full database encryption. The current implementation focuses on password security through hashing.
- **JWT Tokens**: Authentication uses JWT tokens with 24-hour expiration.

## Configuration

- `PORT`: Server port (default: 3000)
- JWT secret key: Change `secret-key` in `src/clojure_todo/auth.clj` for production

## Project Structure

```
clojure-todo/
├── src/
│   └── clojure_todo/
│       ├── core.clj       # Main entry point
│       ├── db.clj         # Database operations
│       ├── auth.clj       # Authentication logic
│       ├── handlers.clj   # Request handlers
│       ├── middleware.clj # Middleware functions
│       └── routes.clj     # API routes
├── frontend/              # Svelte frontend
├── deps.edn              # Dependencies
└── dev.sh                # Development script
```

## Best Practices Demonstrated

1. **Separation of Concerns**: Clear namespace organization
2. **Immutable Data**: Pure functions where possible
3. **Error Handling**: Proper error responses
4. **Security**: Password hashing, JWT tokens
5. **Database**: Prepared statements, connection management

