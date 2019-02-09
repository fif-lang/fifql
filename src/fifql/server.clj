(ns fifql.server
  (:require
   [ring.util.codec :as codec]
   [cuerdas.core :as str]
   [fif.core :as fif]))


(defn create-output-fn
  ""
  [*stdout *stderr]
  (fn [{:keys [tag value]}]
    (case tag
     :out (swap! *stdout conj value)
     :error (swap! *stderr conj value))))


(defn parse-query-string
  [s]
  (-> s
      (str/split #"&")
      (as-> $
          (map #(str/split % #"=") $)
          (map (fn [[k v]] {(str/keyword (codec/url-decode k)) (codec/url-decode v)}) $)
          (reduce merge $))
      :query))


(defn parse-content-body
  [request]
  (slurp (:body request)))


(defn get-request-input-string
  [request]
  (case (:request-method request)
    :get (parse-query-string (:query-string request))
    :post (parse-content-body request)
    ""))


(defn- get-stack-machine
  "If `stack-machine` is a function, passes in the `request` to the function to get the desired stack-machine.
   Otherwise, returns the stack-machine."
  [request stack-machine]
  (if (fn? stack-machine)
    (stack-machine request)
    stack-machine))


(defn determine-content-type
  [request]
  (let [accept (-> request :headers (get "accept" ""))]
    (if (str/includes? accept "application/edn")
       "application/edn"
       "text/plain")))


(defn create-ring-request-handler
  [stack-machine]
  (fn
    ([request]
     (let [*stdout (atom [])
           *stderr (atom [])
           output-fn (create-output-fn *stdout *stderr)
           input-string (get-request-input-string request)
           stack-machine (get-stack-machine request stack-machine)
           evaled-sm (fif/prepl-eval stack-machine input-string output-fn)]
       {:status 200
        :headers {"Content-Type" (determine-content-type request)}
        :body (pr-str {:input-string input-string
                       :stack (fif/get-stack evaled-sm)
                       :stdout @*stdout
                       :stderr @*stderr})}))

    ([request respond raise]
     (let [*stdout (atom [])
           *stderr (atom [])
           output-fn (create-output-fn *stdout *stderr)
           input-string (get-request-input-string request)
           stack-machine (get-stack-machine request stack-machine)
           evaled-sm (fif/prepl-eval stack-machine input-string output-fn)]
       (respond {:status 200
                 :headers {"Content-Type" (determine-content-type request)}
                 :body (pr-str {:input-string input-string
                                :stack (fif/get-stack evaled-sm)
                                :stdout @*stdout
                                :stderr @*stderr})})))))
