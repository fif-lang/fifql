(ns fifql.dev.server
  (:require
   [fif.core :as fif]
   [fifql.core :as fifql]
   [fifql.server :refer [create-express-request-handler
                         add-request-middleware!]]))


(def express (js/require "express"))
(def app (express))

(def server-name "A fifql Example Server")
(def server-port 8081)
(def server-details
  {:server-port server-port :server-name server-name})


;; Create our stack machine, and define some word functions
(def stack-machine
  (-> (fifql/create-stack-machine)

      (fifql/set-word 'add2 (fifql/wrap-function 1 (fn [x] (+ 2 x)))
       :doc "(n -- n) Add 2 to the value"
       :group :fifql/example)

      (fifql/set-var 'server-details server-details
       :doc "The server details"
       :group :fifql/example)))


(def fifql-handler
  (create-express-request-handler
   :prepare-stack-machine stack-machine))


(doto app
  (add-request-middleware!)
  (.get "/fifql" fifql-handler)
  (.post "/fifql" fifql-handler))


(defn -main [& args]
  (.listen app server-port
           #(.log js/console (str "Server Started on port " server-port))))


(set! *main-cli-fn* -main)

