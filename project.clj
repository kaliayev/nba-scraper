(defproject scrappy-ball "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [clj-http "2.3.0"]
                 [cheshire "5.7.0"]
                 [enlive "1.1.6"]
                 [incanter "1.5.7"]
                 [io.aviso/pretty "0.1.33"]]
  :plugins [[cider/cider-nrepl "0.15.0-SNAPSHOT"]
            [refactor-nrepl "2.3.0-SNAPSHOT"]
            [lein-ring "0.9.7"]
            [venantius/ultra "0.5.1"]]
  :ring {:handler scrappy-ball.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
