(ns fifql.dev.server
  (:require
   [mount.core :as mount :refer [defstate]]
   [org.httpkit.server :as httpkit]
   [compojure.core :refer [GET POST defroutes]]
   [compojure.route :as route]
   [fifql.server :refer [create-ring-request-handler]]
   [fifql.core :as fifql]
   [fif.core :as fif]))


(def stack-machine
  (-> (fifql/create-stack-machine)

      (fifql/set-word 'add2 (fifql/wrap-function 1 (fn [x] (+ 2 x)))
       :doc "(n -- n) Add 2 to the value"
       :group :test)

      (fifql/set-word 'fail (fifql/wrap-procedure 0 (fn [] (throw (ex-info "Failed" {}))))
       :doc "Throw an error, stop the stack-machine."
       :group :test)

      (fifql/set-var 'server-details {:port 8080 :server-name "test server"}
       :doc "The server details"
       :group :test)))


(def fifql-handler
  (create-ring-request-handler
   :prepare-stack-machine
   (fn [request] stack-machine)
   :post-response
   (fn [sm request response] response)))


(defroutes app
  (GET "/fifql" req fifql-handler)
  (POST "/fifql" req fifql-handler)
  (route/not-found "<h1>Page not Found</h1>"))


(defn start
  []
  (httpkit/run-server #'app {:port 8080}))


(defstate http-server
  :start (start)
  :stop (http-server))
