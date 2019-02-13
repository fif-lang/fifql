(ns fifql.client
  (:require-macros
   [cljs.core.async.macros :refer [go]])
  (:require
   [cljs-http.client :as http]
   [clojure.tools.reader.edn :as edn]
   [cljs.core.async :refer [<!]]))


(defn ^:export query
  "Performs a POST request on the given `url` with the given fif
  `sform`. Upon success will call the `callback` function with the
  result of the fif stack-machine.

  # Examples
  (def x 10)
  (query \"http://localhost:8080/fifql\"
         (sform %= x 2 +)
         (fn [data] (println data)))
  ;; {:input-string \"10 2 +\", :stack (12), :stdout [], :stderr []}
  "
  [url sform callback]
  (go (let [options {:body sform
                     :headers {"Content-Type" "application/fif"
                               "Accept" "application/edn"}}
            response (<! (http/post url options))]
        ;; cljs-http automatically converts into EDN
        (when-let [response-body (:body response)]
          (callback response-body)))))
