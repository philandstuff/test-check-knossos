(defproject test-check-knossos "0.1.0-SNAPSHOT"
  :description "Example project demonstrating how to use test.check and knossos together"
  :url "TODO"
  :license {:name "MIT Licence"
            :url  "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :profiles {:dev {:dependencies [[knossos "0.2"]
                                  [org.clojure/test.check "0.5.8"]
                                  [org.clojure/tools.namespace "0.2.4"]]}})
