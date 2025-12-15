(ns clojure-todo.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.json :as json]
            [ring.middleware.params :as params]
            [clojure-todo.routes :as routes]
            [clojure-todo.middleware :as middleware]
            [clojure-todo.db :as db])
  (:gen-class))

(defn app []
  (-> routes/api-routes
      middleware/wrap-cors
      middleware/wrap-json-response
      (json/wrap-json-body {:keywords? true})
      params/wrap-params))

(defn -main [& _]
  (println "Initializing database...")
  (db/init-db!)
  (println "Database initialized!")
  
  (let [port (or (some-> (System/getenv "PORT") Integer/parseInt) 3000)]
    (println (str "Starting server on port " port "..."))
    (jetty/run-jetty (app) {:port port :join? true})))
