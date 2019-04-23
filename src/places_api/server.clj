(ns places-api.server
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [hiccup.core :as hiccup]
            [hiccup.page :refer [include-js]]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as resp]
            [clj-http.client :as http]
            [clojure.walk :as walk]
            [clojure.string :as string]
            [cheshire.core :as chs-json]
            [places-api.config])
  (:gen-class))

;;;; Base HTML pages

(def main-page
  [:html
   [:head
    [:title "Places API"]]
   [:body
    [:div#app
     ]
    (include-js "js/main.js")]])

(def not-found-page
  [:html
   [:head
    [:title "404 Not Found"]]
   [:body
    [:h1 "Page not found"]]])


;;;; API Handlers

(defn get-location-details
  [location-name filter-query]
  (try
    (let [api-result (http/get places-api-url {:accept :json :as :json :query-params {"client_id" client-id, "client_secret" client-secret, "near" location-name, "intent" "browse", "v" "20190223"}})
          ;venues (get (get (get api-result :body) "response") "venues")
          body (get api-result :body)
          response (get body :response)
          venues (get response :venues)
          venues-response (transient [])

          ]
           (doseq [venue (get response :venues)]
             (let [locname (get venue :name "")
                   state (get (get venue :location) :state "")
                   lat (get (get venue :location) :lat)
                   lng (get (get venue :location) :lng)
                   category (get (get (get venue :categories []) 0 ) :name "")
                   icon (str (get (get (get (get venue :categories) 0) :icon) :prefix) "bg_64" (get (get (get (get venue :categories) 0) :icon) :suffix))
                   queryable-fields (map string/lower-case [state locname category])
                   ]
              (do
                (if (or (empty? filter-query) (some #(string/includes? % (string/lower-case filter-query)) queryable-fields) )
                (conj! venues-response {:id (get venue :id), :city (get (get venue :location) :city)
                  , :name (get venue :name)
                  , :state (get (get venue :location) :state)
                  , :lat (get (get venue :location) :lat)
                  , :lng (get (get venue :location) :lng)
                  , :category (get (get (get venue :categories) 0) :name)
                  , :icon (str (get (get (get (get venue :categories) 0) :icon) :prefix) "bg_64" (get (get (get (get venue :categories) 0) :icon) :suffix))})
                )
                )
            )
          )
      (persistent! venues-response)
    )
    (catch Exception e
      (println e)
      "An error has occured.please try again"
    )
  )
)

(defn save-locations
  [username locations]
  ;(let [api-result (http/get places-api-url {:query-params {"client_id" client-id, "client_secret" client-secret, "near" location-name, "intent" "browse"}})]
   ; (println api-result)
  ;)
)

(defn get-user-saved-locations
  [username]
  ;(let [api-result (http/get places-api-url {:query-params {"client_id" client-id, "client_secret" client-secret, "near" location-name, "intent" "browse"}})]
   ; (println api-result)
  ;)
)

;;;; Handlers and middleware

(defroutes app
  (GET "/" [] (hiccup/html main-page))
  (GET "/location-data" [location filterstr] {:headers {"Content-Type" "application/json"}, :body (chs-json/generate-string (get-location-details location filterstr))})
  ;(GET "/location-data" [location] {:body (get-location-details location)})
  (GET "/saved-locations" [username] {:headers {"Content-Type" "application/json"}, :body (get-user-saved-locations username)})
  (route/resources "/")
  (route/not-found (hiccup/html not-found-page)))

(def site (handler/site app))

;;;; Managing the server in the REPL or from 'lein run'

(defonce ^:dynamic server nil)

(defn stop
  []
  (when server
    (.stop server)))

(defn start
  [& [args]]
  (stop)
  (alter-var-root
    #'server
    (constantly
      (jetty/run-jetty #'site
                       {:port (Long. (or port 5000))
                        :join? false}))))

(defn -main
  [& [port]]
  (start port))
