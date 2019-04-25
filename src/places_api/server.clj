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
            [ring.middleware.json :as mdlware]
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


;;store locations

(def saved-locations (atom #{}))


;;util

(defn get-filtered-response [data filter-query]
  (let [venues-response (transient [])]
  (doseq [venue data]
    (let[
                   locname (get venue :name "")
                   city (get-in venue [:location :city] "")
                   state (get (get venue :location) :state "")
                   country (get (get venue :location) :country "")
                   lat (get (get venue :location) :lat)
                   lng (get (get venue :location) :lng)
                   category (get (get (get venue :categories []) 0 ) :name "")
                   icon (str (get (get (get (get venue :categories) 0) :icon) :prefix) "bg_64" (get (get (get (get venue :categories) 0) :icon) :suffix))
                   queryable-fields (map string/lower-case [city state country locname category])
                   ]
              (do
                (if (or (empty? filter-query) (some #(string/includes? % (string/lower-case filter-query)) queryable-fields) )
                (conj! venues-response {:id (get venue :id)
                  , :city city
                  , :name locname
                  , :state state
                  , :country country
                  , :lat lat
                  , :lng lng
                  , :category category
                  , :icon icon})
                )
                )
            )
          )
  (persistent! venues-response)
  )

)

;;;; API Handlers

(defn get-location-details
  [location-name filter-query]
  (if (empty? location-name)
    {:status 400 :body "please enter a location!"}
    (try
      (let [api-result (http/get places-api-url {:accept :json :as :json :query-params {"client_id" client-id, "client_secret" client-secret, "near" location-name, "intent" "browse", "v" "20190223"}})
          ;venues (get (get (get api-result :body) "response") "venues")
          body (get api-result :body)
          response (get body :response)
          venues (get response :venues)
          venues-response (get-filtered-response venues filter-query)

          ]
           
      {:status (if (empty? venues-response) 204 200) :headers {"Content-Type" "application/json"} :body (chs-json/generate-string venues-response)}
    )
    (catch Exception e
      (println e)
      (if (contains? (ex-data e) :http-client)
        {:status (get (ex-data e) :status) :headers {"Content-Type" "application/json"} :body (chs-json/generate-string {:error (get (get (chs-json/parse-string(:body (ex-data e))) "meta") "errorDetail")}) };can be better :)
        {:status 500 :headers {"Content-Type" "application/json"} :body (chs-json/generate-string {:error "An error has occured.Please try again"})}
      ;"An error has occured.please try again"
      )
    )
    )
  )
)

(defn save-locations
  [request]
  (let [data (get-in request [:body "data"])]
    (println data)
  (if (empty? data)
    {:headers {"Content-Type" "application/json"} :body (chs-json/generate-string {:error "Please provide some data to save!"}) :status 400}
    (do
      ;(println locations)
      (swap! saved-locations into data)
      {:headers {"Content-Type" "application/json"} :body (chs-json/generate-string @saved-locations) :status 200}
    )
  )
  )
)

(defn get-saved-locations
  []
  {:status 200, :headers {"Content-Type" "application/json"}, :body (chs-json/generate-string @saved-locations)}
  ;(let [api-result (http/get places-api-url {:query-params {"client_id" client-id, "client_secret" client-secret, "near" location-name, "intent" "browse"}})]
   ; (println api-result)
  ;)
)

;;;; Handlers and middleware

(defroutes app
  (GET "/" [] (hiccup/html main-page))
  (GET "/location-data" [location filterstr] (get-location-details location filterstr) )
  ;(GET "/location-data" [location] {:body (get-location-details location)})
  (GET "/saved-locations" [] (get-saved-locations)) ;{:headers {"Content-Type" "application/json"}, :body (get-user-saved-locations username)})
  (POST "/saved-locations" request save-locations) ;{:headers {"Content-Type" "application/json"}, :body (get-user-saved-locations username)})
  (route/resources "/")
  (route/not-found (hiccup/html not-found-page)))

(def site (handler/site (mdlware/wrap-json-body app)))

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
