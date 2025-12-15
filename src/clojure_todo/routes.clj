(ns clojure-todo.routes
  (:require [compojure.core :refer [defroutes GET POST PUT DELETE OPTIONS]]
            [compojure.route :as route]
            [clojure-todo.handlers :as handlers]
            [clojure-todo.middleware :as middleware]))

(defroutes api-routes
  ;; Auth routes (no auth required)
  (POST "/api/auth/register" [] handlers/register-handler)
  (POST "/api/auth/login" [] handlers/login-handler)
  
  ;; User routes (auth required)
  (GET "/api/me" [] (middleware/wrap-auth handlers/get-me-handler))
  
  ;; Todo routes (auth required)
  (GET "/api/todos" [] (middleware/wrap-auth handlers/get-todos-handler))
  (POST "/api/todos" [] (middleware/wrap-auth handlers/create-todo-handler))
  (GET "/api/todos/:id" [] (middleware/wrap-auth handlers/get-todo-handler))
  (PUT "/api/todos/:id" [] (middleware/wrap-auth handlers/update-todo-handler))
  (DELETE "/api/todos/:id" [] (middleware/wrap-auth handlers/delete-todo-handler))
  
  ;; CORS preflight
  (OPTIONS "*" [] (fn [_] {:status 200
                            :headers {"Access-Control-Allow-Origin" "*"
                                     "Access-Control-Allow-Methods" "GET, POST, PUT, DELETE, OPTIONS"
                                     "Access-Control-Allow-Headers" "Content-Type, Authorization"}}))
  
  ;; Catch all
  (route/not-found {:status 404
                   :body {:error "Not found"}}))

