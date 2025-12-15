(ns clojure-todo.auth
  (:require [buddy.sign.jwt :as jwt]
            [clojure-todo.db :as db])
  (:import [org.mindrot.jbcrypt BCrypt]
           [java.time Instant]))

(def secret-key "your-secret-key-change-in-production")

(defn hash-password
  "Hash password using BCrypt"
  [password]
  (BCrypt/hashpw password (BCrypt/gensalt 12)))

(defn check-password
  "Verify password against hash"
  [password password-hash]
  (try
    (when (and password password-hash (not (empty? password-hash)))
      (BCrypt/checkpw password password-hash))
    (catch Exception _
      false)))

(defn generate-token
  "Generate JWT token for user"
  [user-id username]
  (let [exp-time (.plusSeconds (Instant/now) (* 24 60 60))
        claims {:user-id user-id
                :username username
                :exp (.getEpochSecond exp-time)}]
    (jwt/sign claims secret-key {:alg :hs256})))

(defn verify-token
  "Verify and decode JWT token"
  [token]
  (try
    (jwt/unsign token secret-key {:alg :hs256})
    (catch Exception _
      nil)))

(defn register-user
  "Register a new user"
  [username email password]
  (cond
    (db/find-user-by-username username)
    {:error "Username already exists"}
    
    (db/find-user-by-email email)
    {:error "Email already exists"}
    
    (< (count password) 8)
    {:error "Password must be at least 8 characters"}
    
    :else
    (try
      (let [password-hash (hash-password password)
            user (db/create-user! username email password-hash)]
        (if user
          {:success true
           :user {:id (:users/id user)
                  :username (:users/username user)
                  :email (:users/email user)}}
          {:error "Failed to create user"}))
      (catch Exception e
        (println "Registration error:" (.getMessage e))
        (.printStackTrace e)
        {:error "Failed to create user"}))))

(defn authenticate-user
  "Authenticate user and return token"
  [username password]
  (if-let [user (db/find-user-by-username username)]
    (if-let [password-hash (:password_hash user)]
      (if (and (not (empty? password-hash)) (check-password password password-hash))
        (let [token (generate-token (:id user) username)]
          {:success true
           :token token
           :user {:id (:id user)
                  :username username
                  :email (:email user)}})
        {:error "Invalid credentials"})
      {:error "Invalid credentials"})
    {:error "Invalid credentials"}))

(defn get-current-user
  "Get current user from token"
  [token]
  (when-let [claims (verify-token token)]
    (db/find-user-by-id (:user-id claims))))

