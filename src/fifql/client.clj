(ns fifql.client
  (:require
   [org.httpkit.client :as http]
   [clojure.tools.reader.edn :as edn]
   [fif.client]))


(def ^:dynamic *fif-clojure-value-escape* '%=)


(defmacro form
  "quoted form with escaped evaluation. Values preceding
  *fif-clojure-value-escape* are evaluated in clojure(script).
  Examples:
  ;; Assuming we want to pull a clojure value into a quoted form
  (def x 10)
  (form value %= x) ;; => '[form value 10]
  Notes:
  - *fif-clojure-value-escape can be replaced with a different escape
  symbol as desired."
  [& body]
  (:result
    (reduce
      (fn [{:keys [result eval-next?]} atom]
        (cond
          (= atom *fif-clojure-value-escape*)
          {:result result
           :eval-next? true}
          eval-next?
          {:result (conj result atom)}
          :else
          {:result (conj result `(quote ~atom))}))
      {:result []}
      body)))


(defmacro sform
  "Equivlant to `form`, but presents the result as a string that can
  be consumed by a fifql stack-machine.

  # Notes
   
  - This should be used with the `query` function to construct the stack form.
  "
  [& body]
  `(let [sform# (pr-str (form ~@body))]
     ;; Remove surrounding vector
     (subs sform# 1 (dec (count sform#)))))


(defn query
  "Performs a POST request on the given `url` with the given fif
  `form`. Returns a map consisting of the `:stack`, `:input-string`, a
  vector of strings sent to `stderr` and a vector of strings sent to
  `stdout`.

  # Examples
  (def x 10)
  (query {:url \"http://localhost:8080/fifql\"
          :sform (sform %= x 2 +)})
  ;; {:input-string \"10 2 +\", :stack (12), :stdout [], :stderr []}
  "
  [url sform]
  (let [options
        {:body sform
         :headers {"Accept" "application/edn"}}
        result @(http/post url options)]
    (when-let [sresult (-> result :body slurp)]
      (edn/read-string sresult))))
    
  
  
