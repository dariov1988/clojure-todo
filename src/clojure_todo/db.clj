(ns clojure-todo.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]))

(def db-spec {:dbtype "sqlite" :dbname "todo_app.db"})

(defn get-connection []
  (jdbc/get-connection db-spec))

(defn init-db!
  "Initialize database with tables"
  []
  (with-open [conn (get-connection)]
    (jdbc/execute! conn ["
      CREATE TABLE IF NOT EXISTS users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        username TEXT UNIQUE NOT NULL,
        email TEXT UNIQUE NOT NULL,
        password_hash TEXT NOT NULL,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
      )"])
    (jdbc/execute! conn ["
      CREATE TABLE IF NOT EXISTS todos (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        title TEXT NOT NULL,
        description TEXT,
        completed BOOLEAN DEFAULT 0,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
      )"])
    (jdbc/execute! conn ["
      CREATE INDEX IF NOT EXISTS idx_todos_user_id ON todos(user_id)"])
    (jdbc/execute! conn ["
      CREATE INDEX IF NOT EXISTS idx_todos_completed ON todos(completed)"])))

(defn create-user!
  "Create a new user and return the created user"
  [username email password-hash]
  (with-open [conn (get-connection)]
    (sql/insert! conn :users {:username username
                              :email email
                              :password_hash password-hash})
    (let [user-id-result (jdbc/execute-one! conn ["SELECT last_insert_rowid() AS id"])
          user-id (:id user-id-result)]
      (when user-id
        (first (sql/query conn ["SELECT * FROM users WHERE id = ?" user-id]))))))

(defn find-user-by-username
  "Find user by username"
  [username]
  (with-open [conn (get-connection)]
    (let [user (first (sql/query conn ["SELECT * FROM users WHERE username = ?" username]))]
      (when user
        {:id (:users/id user)
         :username (:users/username user)
         :email (:users/email user)
         :password_hash (:users/password_hash user)
         :created_at (:users/created_at user)}))))

(defn find-user-by-email
  "Find user by email"
  [email]
  (with-open [conn (get-connection)]
    (let [user (first (sql/query conn ["SELECT * FROM users WHERE email = ?" email]))]
      (when user
        {:id (:users/id user)
         :username (:users/username user)
         :email (:users/email user)
         :password_hash (:users/password_hash user)
         :created_at (:users/created_at user)}))))

(defn find-user-by-id
  "Find user by id"
  [id]
  (with-open [conn (get-connection)]
    (let [user (first (sql/query conn ["SELECT id, username, email, created_at FROM users WHERE id = ?" id]))]
      (when user
        {:id (:users/id user)
         :username (:users/username user)
         :email (:users/email user)
         :created_at (:users/created_at user)}))))

(defn normalize-todo
  "Normalize todo map from database result"
  [todo]
  (when todo
    {:id (or (:todos/id todo) (:id todo))
     :title (or (:todos/title todo) (:title todo))
     :description (or (:todos/description todo) (:description todo))
     :completed (= 1 (or (:todos/completed todo) (:completed todo) 0))
     :created_at (or (:todos/created_at todo) (:created_at todo))
     :updated_at (or (:todos/updated_at todo) (:updated_at todo))}))

(defn create-todo!
  "Create a new todo and return it"
  [user-id title description]
  (with-open [conn (get-connection)]
      (let [result (sql/insert! conn :todos {:user_id user-id
                                            :title title
                                            :description description
                                            :completed false})]
      (normalize-todo (first (sql/query conn ["SELECT * FROM todos WHERE id = ?" (:todos/id result)]))))))

(defn get-todos-by-user
  "Get all todos for a user"
  [user-id]
  (with-open [conn (get-connection)]
    (map normalize-todo (sql/query conn ["SELECT * FROM todos WHERE user_id = ? ORDER BY created_at DESC" user-id]))))

(defn get-todo-by-id
  "Get a specific todo by id (ensuring it belongs to the user)"
  [todo-id user-id]
  (with-open [conn (get-connection)]
    (normalize-todo (first (sql/query conn ["SELECT * FROM todos WHERE id = ? AND user_id = ?" todo-id user-id])))))

(defn update-todo!
  "Update a todo"
  [todo-id user-id {:keys [title description completed]}]
  (with-open [conn (get-connection)]
    (let [updates (remove nil? 
                   [(when title {:title title})
                    (when (some? description) {:description description})
                    (when (some? completed) {:completed (if completed 1 0)})])
          update-map (apply merge updates)]
      (when (seq update-map)
        (sql/update! conn :todos 
                     (assoc update-map :updated_at (java.time.Instant/now))
                     ["id = ? AND user_id = ?" todo-id user-id])))))

(defn delete-todo!
  "Delete a todo"
  [todo-id user-id]
  (with-open [conn (get-connection)]
    (sql/delete! conn :todos ["id = ? AND user_id = ?" todo-id user-id])))

