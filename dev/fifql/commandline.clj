(ns fifql.commandline
  (:require
   [fifql.dev.server :refer [http-server]]
   [mount.core :as mount]))


(defn -main
  [& args]
  (println "Starting Http Server on port 8080")
  (mount/start))
