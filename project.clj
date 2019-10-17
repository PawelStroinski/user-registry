(defproject user-registry "0.1.0-SNAPSHOT"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler user-registry.core/handler}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [com.layerware/hugsql "0.5.1"]
                 [com.h2database/h2 "1.4.200"]
                 [liberator "0.15.3"]
                 [compojure "1.6.1"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [crypto-password "0.2.1" :exclusions [org.clojure/clojure]]
                 [prismatic/schema "1.1.12"]
                 [cheshire "5.8.0"]]
  :repl-options {:init-ns user-registry.core}
  :profiles {:dev {:dependencies [[midje "1.9.1"]
                                  [ring/ring-mock "0.4.0"]]}})
