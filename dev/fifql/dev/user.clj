(ns fifql.dev.user
  (:require
   [mount.core :as mount :refer [defstate]]
   [fifql.dev.server :refer [http-server]]
   [fifql.client :refer [query sform]]))


(defn start []
  (mount/start))


(defn stop []
  (mount/stop))


(defn restart []
  (stop)(start))


#_(query "http://localhost:8081/fifql"
         (sform 2 2 +))
