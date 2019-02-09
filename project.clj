(defproject fifql "0.1.0-SNAPSHOT"
  :description "Stack-based Query Language for the web"
  :url "http://github.com/fif-lang/fifql"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [ring/ring-codec "1.1.1"]
                 [funcool/cuerdas "2.0.5"]
                 [fif "1.2.0"]]
  :repl-options {:init-ns fifql.core}

  :profiles
  {:dev
   {:main fifql.commandline
    :source-paths ["src" "dev" "test"]
    :dependencies [[mount "0.1.16"]
                   [ring/ring-mock "0.3.2"]
                   [ring/ring-defaults "0.3.2"]
                   [compojure "1.6.1"]
                   [http-kit "2.3.0"]]
    :repl-options {:init-ns fifql.dev.user
                   :port 9005}}})
