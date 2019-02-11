(ns fifql.dev.client
  (:require [fifql.client :as fifql :include-macros true]))

(enable-console-print!)


(def x 10)


(fifql/query
 "http://localhost:8080/fifql"
 (fifql/sform %= x 2 +)
 (fn [result]
   (println (pr-str result))
   (println (:stack result))))
