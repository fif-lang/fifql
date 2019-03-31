(defproject fifql "1.3.0"
  :description "Stack-based Query Language for the web"
  :url "http://github.com/fif-lang/fifql"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.520"]
                 [ring/ring-codec "1.1.1"]
                 [funcool/cuerdas "2.2.0"]
                 [org.clojure/core.async "0.4.490"]
                 [org.clojure/tools.reader "1.3.2"]
                 [http-kit "2.3.0"]
                 [cljs-http "0.1.46"]
                 [fif "1.3.0"]]
  :repl-options {:init-ns fifql.core}

  :npm {:dependencies [[body-parser "1.18.3"]]
        :devDependencies [[express "4.16.4"]]}

  :repositories [["clojars" {:sign-releases false}]]
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :profiles
  {:dev
   {:main fifql.commandline
    :plugins [[lein-cljsbuild "1.1.7"]
              [lein-ancient "0.6.15"]
              [lein-npm "0.6.2"]]
    :source-paths ["src" "dev" "test"]
    :dependencies [[mount "0.1.16"]
                   [ring/ring-mock "0.3.2"]
                   [ring/ring-defaults "0.3.2"]
                   [compojure "1.6.1"]]
    :repl-options {:init-ns fifql.dev.user
                   :port 9005}}}

  :cljsbuild {:builds {:dev-client
                       {:source-paths ["src" "dev" "test"]
                        :compiler {:output-dir "resources/public/js/compiled/out"
                                   :output-to "resources/public/js/compiled/fifql-client.js"
                                   :optimizations :whitespace
                                   :pretty-print true
                                   :main fifql.dev.client
                                   :source-map "resources/public/js/compiled/fif-client.js.map"}}

                       :dev-server
                       {:source-paths ["src" "dev" "test"]
                        :compiler {:output-dir "target/js/compiled/out"
                                   :output-to "target/js/compiled/fifql-server.js"
                                   :optimizations :none
                                   :main fifql.dev.server
                                   :target :nodejs}}}})
