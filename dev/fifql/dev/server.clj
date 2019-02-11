(ns fifql.dev.server
  (:require

   ;; For managing Clojure App State
   [mount.core :as mount :refer [defstate]]

   ;; High-performance Web Server
   [org.httpkit.server :as httpkit]

   ;; Routing Library
   [compojure.core :refer [GET POST defroutes]]
   [compojure.route :as route]
   
   ;; Ring Middleware
   [ring.middleware.file :refer [wrap-file]]

   ;; Fifql Library
   [fifql.server :refer [create-ring-request-handler]]
   [fifql.core :as fifql]))


(def server-name "A fifql Example Server")
(def server-port 8080)


;; Create our stack machine, and define some word functions
(def stack-machine
  (-> (fifql/create-stack-machine)

      (fifql/set-word 'add2 (fifql/wrap-function 1 (fn [x] (+ 2 x)))
       :doc "(n -- n) Add 2 to the value"
       :group :fifql/example)

      (fifql/set-var 'server-details {:server-port server-port :server-name server-name}
       :doc "The server details"
       :group :fifql/example)))


;; Create our ring request handler to use with Httpkit
(def fifql-handler
  (create-ring-request-handler
   :prepare-stack-machine stack-machine))


;; Create our routes. The fifql ring handler supports both GET and POST requests
(defroutes app
  (GET "/" [] (slurp "resources/public/index.html"))
  (GET "/fifql" req fifql-handler)
  (POST "/fifql" req fifql-handler)
  (route/not-found "<h1>Page not Found</h1>"))

(def wapp
  (-> app
      (wrap-file "resources/public")))

;; Start the web server. Note that any server that supports ring
;; request handlers are supported.
(defn start
  []
  (httpkit/run-server #'wapp {:port server-port}))


;; Manage our http server using Mount (optional)
(defstate http-server
  :start (start)
  :stop (http-server))
