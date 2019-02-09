(ns fifql.dev.user
  (:require
   [mount.core :as mount :refer [defstate]]
   [fifql.dev.server :refer [http-server]]))


(defn start []
  (mount/start))


(defn stop []
  (mount/stop))


(defn restart []
  (stop)(start))
