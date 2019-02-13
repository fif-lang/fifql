(ns fifql.server
 (:require
  [fif.core :as fif]
  [fifql.core :as fifql]))


(def body-parser (js/require "body-parser"))


(defn get-request-input-string
  [request]
  (let [url-query (aget request "query" "query")
        body-query (aget request "body")
        body-query (if (string? body-query) body-query nil)]
    (or body-query url-query)))


(defn add-request-middleware!
  [app]
  (doto app
    (.use (.text body-parser #js {:type "text/plain"}))
    (.use (.text body-parser #js {:type "application/edn"}))
    (.use (.text body-parser #js {:type "application/fif"}))))


(defn- get-content-type
  [request]
  (let [content-type (.get request "Content-Type")]
    (if (or (= content-type "application/edn")
            (= content-type "application/fif"))
      "application/edn"
      "text/plain")))


(defn- create-output-fn
  ""
  [*stdout *stderr]
  (fn [{:keys [tag value]}]
    (case tag
     :out (swap! *stdout conj value)
     :error (swap! *stderr conj value))))


(defn- get-stack-machine
  "If `stack-machine` is a function, passes in the `request` to the function to get the desired stack-machine.
   Otherwise, returns the stack-machine."
  [request stack-machine]
  (if (fn? stack-machine)
    (stack-machine request)
    stack-machine))


(defn create-express-request-handler
  ""
  [& {:keys [prepare-stack-machine
             post-response]
      :or {post-response (fn [sm request response])}}]
  (fn [request response]
    (let [*stdout (atom [])
          *stderr (atom [])
          output-fn (create-output-fn *stdout *stderr)
          input-string (get-request-input-string request)
          stack-machine (get-stack-machine request prepare-stack-machine)
          evaled-sm (fif/prepl-eval stack-machine input-string output-fn)]
      (post-response evaled-sm request response)
      (doto response
        (.append "Content-Type" (get-content-type request))
        (.send
         (pr-str {:input-string input-string
                  :stack (fif/get-stack evaled-sm)
                  :stdout @*stdout
                  :stderr @*stderr}))))))
