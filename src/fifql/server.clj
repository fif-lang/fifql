(ns fifql.server
  (:require
   [ring.util.codec :as codec]
   [cuerdas.core :as str]
   [fif.core :as fif]))


(defn- create-output-fn
  ""
  [*stdout *stderr]
  (fn [{:keys [tag value]}]
    (case tag
     :out (swap! *stdout conj value)
     :error (swap! *stderr conj value))))


(defn- parse-query-string
  [s]
  (-> s
      (str/split #"&")
      (as-> $
          (map #(str/split % #"=") $)
          (map (fn [[k v]] {(str/keyword (codec/url-decode k)) (codec/url-decode v)}) $)
          (reduce merge $))
      :query))


(defn- parse-content-body
  [request]
  (slurp (:body request)))


(defn- get-request-input-string
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


(defn- determine-content-type
  [request]
  (let [accept (-> request :headers (get "accept" ""))]
    (if (str/includes? accept "application/edn")
      "application/edn"
      "text/plain")))


(def ^:dynamic
  *context* 
  "Serves as a context bound for each request --> response handler.

  This can be used to expose additional data about the client for each
  wrapped word function within the fif stack-machine."
  nil)


(defn create-ring-request-handler
  "Create a ring request handler for use in an HTTP server.

  # Key Arguments (opts)
  
  prepare-stack-machine - Can be either the fif stack-machine to use
  for queries, or a function of the form (fn [request]) which receives
  the request map, and expects a stack-machine to be returned.

  prepare-context - Accepts a function of the form (fn [request]),
  where the returned value is dynamically bound to `*context*`.

  post-response (optional) - Called after the stack-machine is
  evaluated. This is an optional function that can perform further
  modifications on the generated response. Function is of the
  form (fn [sm request response]), and expects a response map.

  # Notes

  - prepare-stack-machine allows you to return difference stack
  machine's with different capabilities based on the current
  session. This can be useful for providing say a 'logged in' user
  with more functionality.

  - prepare-context allows you to dynamically bind user-specific data
  within `*context*`, which can be accessed within wrapped word
  functions used within the fif stack machine. This is particularly
  useful for passing session data, or authenication data into wrapped
  functions.

  - post-response can allow you to check the stack-machine for login
  sequences, and change the session data within the response
  appropriately."
  [& {:keys [prepare-stack-machine prepare-context post-response]
      :or {prepare-context (constantly nil)
           post-response (fn [sm request response] response)}
      :as opts}]
  (fn
    ([request]
     (binding [*context* (prepare-context request)]
       (let [*stdout (atom [])
             *stderr (atom [])
             output-fn (create-output-fn *stdout *stderr)
             input-string (get-request-input-string request)
             stack-machine (get-stack-machine request prepare-stack-machine)
             evaled-sm (fif/prepl-eval stack-machine input-string output-fn)
             response {:status 200
                       :headers {"Content-Type" (determine-content-type request)}
                       :body (pr-str {:input-string input-string
                                      :stack (fif/get-stack evaled-sm)
                                      :stdout @*stdout
                                      :stderr @*stderr})}]
         (post-response evaled-sm request response))))

    ([request respond raise]
     (binding [*context* (prepare-context request)]
       (let [*stdout (atom [])
             *stderr (atom [])
             output-fn (create-output-fn *stdout *stderr)
             input-string (get-request-input-string request)
             stack-machine (get-stack-machine request prepare-stack-machine)
             evaled-sm (fif/prepl-eval stack-machine input-string output-fn)
             response {:status 200
                       :headers {"Content-Type" (determine-content-type request)
                                 "Access-Control-Allow-Origin" "*"}
                       :body (pr-str {:input-string input-string
                                      :stack (fif/get-stack evaled-sm)
                                      :stdout @*stdout
                                      :stderr @*stderr})}]
         (respond (post-response evaled-sm request response)))))))
