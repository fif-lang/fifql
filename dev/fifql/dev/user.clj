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


#_(query {:url "http://localhost:8080/fifql"
          :sform (sform 2 2 +)})
