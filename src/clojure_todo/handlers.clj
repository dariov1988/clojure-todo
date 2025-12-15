(ns clojure-todo.handlers
  (:require [clojure-todo.auth :as auth]
            [clojure-todo.db :as db]
            [ring.util.response :as response]
            [clojure.data.json :as json]))

(defn parse-json-body
  "Parse JSON body from request"
  [request]
  (try
    (when-let [body (:body request)]
      (cond
        (string? body)
        (json/read-str body :key-fn keyword)
        
        (map? body)
        ;; Convert string keys to keywords if needed
        (if (some string? (keys body))
          (into {} (map (fn [[k v]] [(if (string? k) (keyword k) k) v])) body)
          body)
        
        :else
        body))
    (catch Exception _
      nil)))

(defn parse-path-id
  "Safely parse ID from path parameters"
  [request]
  (try
    (when-let [id-str (or (get-in request [:path-params :id])
                          (get-in request [:params :id]))]
      (Integer/parseInt id-str))
    (catch Exception _
      nil)))

(defn register-handler
  "Handle user registration"
  [request]
  (if-let [body (parse-json-body request)]
    (let [{:keys [username email password]} body]
      (if (and username email password)
        (let [result (auth/register-user username email password)]
          (if (:error result)
            (-> {:error (:error result)}
                response/response
                (response/status 400))
            (-> {:success true
                 :user (:user result)}
                response/response
                (response/status 201))))
        (-> {:error "Missing required fields: username, email, password"}
            response/response
            (response/status 400))))
    (-> {:error "Invalid JSON body"}
        response/response
        (response/status 400))))

(defn login-handler
  "Handle user login"
  [request]
  (if-let [body (parse-json-body request)]
    (let [{:keys [username password]} body]
      (if (and username password)
        (let [result (auth/authenticate-user username password)]
          (if (:error result)
            (-> {:error (:error result)}
                response/response
                (response/status 401))
            (-> {:success true
                 :token (:token result)
                 :user (:user result)}
                response/response
                (response/status 200))))
        (-> {:error "Missing required fields: username, password"}
            response/response
            (response/status 400))))
    (-> {:error "Invalid JSON body"}
        response/response
        (response/status 400))))

(defn get-me-handler
  "Get current user info"
  [request]
  (if-let [user (:user request)]
    (-> {:success true
         :user {:id (:id user)
                :username (:username user)
                :email (:email user)
                :created_at (:created_at user)}}
        response/response
        (response/status 200))
    (-> {:error "User not found"}
        response/response
        (response/status 404))))

(defn get-todos-handler
  "Get all todos for current user"
  [request]
  (if-let [user-id (:user-id request)]
    (let [todos (db/get-todos-by-user user-id)]
      (-> {:success true
           :todos todos}
          response/response
          (response/status 200)))
    (-> {:error "Unauthorized"}
        response/response
        (response/status 401))))

(defn create-todo-handler
  "Create a new todo"
  [request]
  (if-let [user-id (:user-id request)]
    (if-let [body (parse-json-body request)]
      (let [{:keys [title description]} body]
        (if title
          (let [todo (db/create-todo! user-id title description)]
            (-> {:success true
                 :todo todo}
                response/response
                (response/status 201)))
          (-> {:error "Missing required field: title"}
              response/response
              (response/status 400))))
      (-> {:error "Invalid JSON body"}
          response/response
          (response/status 400)))
    (-> {:error "Unauthorized"}
        response/response
        (response/status 401))))

(defn get-todo-handler
  "Get a specific todo"
  [request]
  (if-let [user-id (:user-id request)]
    (if-let [todo-id (parse-path-id request)]
      (if-let [todo (db/get-todo-by-id todo-id user-id)]
        (-> {:success true
             :todo todo}
            response/response
            (response/status 200))
        (-> {:error "Todo not found"}
            response/response
            (response/status 404)))
      (-> {:error "Invalid todo ID"}
          response/response
          (response/status 400)))
    (-> {:error "Unauthorized"}
        response/response
        (response/status 401))))

(defn update-todo-handler
  "Update a todo"
  [request]
  (if-let [user-id (:user-id request)]
    (if-let [todo-id (parse-path-id request)]
      (if-let [body (parse-json-body request)]
        (do
          (db/update-todo! todo-id user-id body)
          (if-let [todo (db/get-todo-by-id todo-id user-id)]
            (-> {:success true
                 :todo todo}
                response/response
                (response/status 200))
            (-> {:error "Todo not found"}
                response/response
                (response/status 404))))
        (-> {:error "Invalid JSON body"}
            response/response
            (response/status 400)))
      (-> {:error "Invalid todo ID"}
          response/response
          (response/status 400)))
    (-> {:error "Unauthorized"}
        response/response
        (response/status 401))))

(defn delete-todo-handler
  "Delete a todo"
  [request]
  (if-let [user-id (:user-id request)]
    (if-let [todo-id (parse-path-id request)]
      (do
        (db/delete-todo! todo-id user-id)
        (-> {:success true
             :message "Todo deleted"}
            response/response
            (response/status 200)))
      (-> {:error "Invalid todo ID"}
          response/response
          (response/status 400)))
    (-> {:error "Unauthorized"}
        response/response
        (response/status 401))))

