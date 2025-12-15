(ns clojure-todo.middleware
  (:require [clojure-todo.auth :as auth]
            [ring.util.response :as response]
            [clojure.data.json :as json]))

(defn wrap-cors
  "CORS middleware"
  [handler]
  (fn [request]
    (let [response (handler request)]
      (-> response
          (response/header "Access-Control-Allow-Origin" "*")
          (response/header "Access-Control-Allow-Methods" "GET, POST, PUT, DELETE, OPTIONS")
          (response/header "Access-Control-Allow-Headers" "Content-Type, Authorization")))))

(defn wrap-json-response
  "JSON response middleware"
  [handler]
  (fn [request]
    (let [response (handler request)]
      (if (and (map? response) (contains? response :status))
        (-> response
            (update :body #(if (string? %) % (json/write-str %)))
            (response/content-type "application/json"))
        response))))

(defn extract-token
  "Extract JWT token from Authorization header"
  [request]
  (when-let [auth-header (get-in request [:headers "authorization"])]
    (when (.startsWith auth-header "Bearer ")
      (subs auth-header 7))))

(defn wrap-auth
  "Authentication middleware"
  [handler]
  (fn [request]
    (if-let [token (extract-token request)]
      (if-let [user (auth/get-current-user token)]
        (handler (assoc request :user user :user-id (:id user)))
        (-> {:error "Invalid or expired token"}
            response/response
            (response/status 401)))
      (-> {:error "Missing authorization token"}
          response/response
          (response/status 401)))))

(defn optional-auth
  "Optional authentication middleware"
  [handler]
  (fn [request]
    (if-let [token (extract-token request)]
      (if-let [user (auth/get-current-user token)]
        (handler (assoc request :user user :user-id (:id user)))
        (handler request))
      (handler request))))

