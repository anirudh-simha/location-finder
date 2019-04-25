(ns places-api.server-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [cheshire.core :as chs-json]
            [places-api.server :refer :all]))


(defn json-map [req-type uri params]
  {:remote-addr "localhost"
   :headers {"host" "localhost"
             "content-type" "application/json"
             "accept" "application/json"}
   :server-port 80
   :content-type "application/json"
   :uri uri
   :server-name "localhost"
   :query-string nil
   :body params
   :params params
   :scheme :http
   :request-method req-type})

(deftest test-app
  (testing "main route"
    (let [response (app (json-map  :get "/" nil))]
      (is (= (:status response) 200))
      (is (= (:body response) "<html><head><title>Places API</title></head><body><div id=\"app\"></div><script src=\"js/main.js\" type=\"text/javascript\"></script></body></html>")))))

  (testing "get location"
    (let [response (app (json-map :get "/location-data" {:location "airoli" :filterstr ""}))]
      (is (= (:status response) 200))
      ;cant test data as it keeps varying
      ))
  
  (testing "get location filter no data"
    (let [response (app (json-map :get "/location-data" {:location "airoli" :filterstr "dfgdfbdfg"}))]
      (is (= (:status response) 204))
      ;cant test data as it keeps varying
      ))
  
  (testing "get location not found"
    (let [response (app (json-map :get "/location-data" {:location "asdd" :filterstr ""}))]
      (is (= (:status response) 400))
      (is (= (:body response) "{\"error\":\"Couldn't geocode param near: asdd\"}"))
      ))
  
  (testing "get location no data passed"
    (let [response (app (mock/request :get "/location-data"))]
      (is (= (:status response) 400))
      (is (= (:body response) "please enter a location!"))
    )

  )

  (testing "save location"
    (let [response (app (json-map :post "/saved-locations" {"data" ["abc" "def"]}))]
      (is (= (:status response) 200))
      (is (= (:body response) "[\"abc\",\"def\"]"))

    )

  )
  
  (testing "save location no data"
    (let [response (app (json-map :post "/saved-locations" {"data" []}))]
      (is (= (:status response) 400))
      (is (= (:body response) "{\"error\":\"Please provide some data to save!\"}"))

    )

  )
