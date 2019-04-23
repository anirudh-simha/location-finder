(defproject minimal-webapp "0.1.0-SNAPSHOT"
  :description "Minimal webapp using ClojureScript, Compojure, and Reagent"
  :url "https://github.com/raxod502/minimal-webapp"
  :license {:name "MIT License"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :main places-api.server
  :dependencies [;; Language
                 [org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.520"]

                 ;; Server
                 [compojure "1.6.1"]
                 [hiccup "1.0.5"]
                 [ring/ring-jetty-adapter "1.7.1"]
                 [clj-http "3.9.1"]
                 [cljs-http "0.1.46"]
                 [cheshire "5.8.1"]

                 ;; Client
                 [reagent "0.8.1"]

                 ;; Emacs integration
                 [com.cemerick/piggieback "0.2.1"]
                 [figwheel-sidecar "0.5.18"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-figwheel "0.5.18"]]

  :cljsbuild {:builds [{:id "main"
                        :source-paths ["src"]
                        :figwheel true
                        :compiler {:main "places-api.pages.splash"
                                   :output-to "resources/public/js/main.js"
                                   :output-dir "resources/public/js/out"
                                   :asset-path "js/out"}}]}
  :figwheel {:ring-handler places-api.server/site}

  :clean-targets ^{:protect false} ["resources/public" "target"]

  :uberjar-name "places-api-standalone.jar"
  :profiles {:uberjar {:aot :all
                       :main places-api.server
                       :hooks [leiningen.cljsbuild]}})
