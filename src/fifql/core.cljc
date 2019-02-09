(ns fifql.core
  (:require
   [fif.core :as fif :include-macros true]
   [fif.def]
   [fif.stack-machine.words]
   [fif.stack-machine.variable]
   [fif.stack-machine :as stack]))


(def default-max-step
  "The default maximum execution step before the stack machine will stop
  executing the query."
  20000)


(defn wrap-function
  "Wrap function, `f` with given `arity`. The function's result is
  placed on the stack."
  [arity f]
  (fif.def/wrap-function-with-arity arity f))


(defn wrap-procedure
  "Wrap procedure, `f`, with given `arity`. The function's result is
  *not* placed on the stack."
  [arity f]
  (fif.def/wrap-procedure-with-arity arity f))


(defn set-word
  "Set the global word defintion with the name `sym`, with the stack
  function `f`."
  [sm sym f & {:keys [doc group]}]
  (fif.stack-machine.words/set-global-word-defn sm sym f :doc doc :group group))


(defn set-var
  "Set the global word variable with the name `sym`, with the given
  `value`."
  [sm sym value & {:keys [doc group]}]
  (fif.stack-machine.words/set-global-word-defn
   sm sym
   (fif.stack-machine.variable/wrap-global-variable value)
   :doc doc
   :group group))


(defn create-stack-machine
  "Create a stack machine suitable for fifql."
  [& {:keys [step-max]
      :or {step-max default-max-step}}]
  (-> fif/*default-stack*
      (stack/set-step-max step-max)))
