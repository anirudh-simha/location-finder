(ns places-api.server
  (:gen-class))

(def places-api-url "https://api.foursquare.com/v2/venues/search")
(def places-api-venue-url "https://api.foursquare.com/v2/venues")
(def client-id "5CVO2JLIW52KQBZOQPXEOPMZBGGFL214MQOSIVYJPC5FA0AK")
(def client-secret "E42WUCYQ02J32VTHU4GT2ZNFJRIFUHUZWKZY2RTOB2KXZHVK")
(def port 5001)
(def default-http-opts
    {:socket-timeout 10000
     :conn-timeout 10000
     :insecure? true
     :throw-entire-message? false})
(def app-url-location (str "localhost:" port "/location-data"))
(def app-url-saved-location (str "localhost:" port "/saved-locations"))
