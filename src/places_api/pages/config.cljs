(ns places-api.pages.config)

(def port 5001)
(def default-http-opts
    {:socket-timeout 10000
     :conn-timeout 10000
     :insecure? true
     :throw-entire-message? false})
(def app-url-location (str "http://localhost:" port "/location-data"))
(def app-url-saved-location (str "http://localhost:" port "/saved-locations"))
