(ns places-api.pages.splash
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [cljs-http.client :as http]
            [clojure.string :as s]
            [cljs.core.async :refer [<!]]
            [places-api.pages.config :as config]))

(r/render
  (list
  [:label {:for "location"} "Search for any location"]
      [:input {:type "text" :id "location" :placeholder "search for location"}]
      [:input {:type "text" :id "category" :placeholder "search by parameters"}]
      [:button {:id "submit" :on-click #(get-locations)} "Lets go!"]
      [:button {:id "save" :on-click #(save-locations)} "save locations"]
      [:button {:id "get-saved-locations" :on-click #(get-saved-locations)} "get saved locations"]
      [:div#results]
      )
      (.getElementById js/document "app")
      )
      
 (defn get-locations
  []
  (enable-console-print!)
  (if (empty? (.-value (.getElementById js/document "location")))
    (js/alert "Please enter location!")
    (go 
      (let[location (.-value (.getElementById js/document "location"))
           category (.-value (.getElementById js/document "category"))
           api-result (<! (http/get config/app-url-location {:query-params {"location" location, "filterstr" category}}))]
        (if(not= (get api-result :status) 200)
          (if (= (get api-result :status 204))
            (js/alert "no data found!")
            (r/render [:table [:tbody [:tr [:td (get (get api-result :body) :error)]]]] (.getElementById js/document "results"))
          )
          (r/render
            (render-locations-table (get api-result :body) category)
            (.getElementById js/document "results")
          )
        )
      )
    )
  )
)

(defn save-locations
  []
  (enable-console-print!)
  (let [checked-elements (map #(.parse js/JSON (.-value %)) (filter #(= true (.-checked %)) (array-seq (.getElementsByClassName js/document "venue-ids-select"))))]
    (if (empty? checked-elements)
      (js/alert "Please select a location to save")
  (go
  (let [
        response (<!
        (http/post config/app-url-saved-location {:json-params {:data checked-elements} }))
        ]
    (if(= (get response :status) 200)
      (js/alert "data saved successfully")
      (js/alert (get (get response :body) :error))
    )
  )
  )
  )
    )
)

(defn get-saved-locations
  []
  (go
    (let[api-result (<! (http/get config/app-url-saved-location ))]
      (r/render (render-locations-table (get api-result :body) nil) (.getElementById js/document "results"))
    )
  )
)



(defn render-locations-table
  [records category]
  [:table {:style {:border "1px solid black"}}
    [:tbody
      [:tr
        [:td "select"][:td "name"][:td "city"][:td "state"][:td "country"][:td "lat"][:td "lng"][:td "categories"][:td "image"]
      ]
      (for [record records]

      ^{:key (get record :id)}
      [:tr
        [:td [:input {:type "checkbox" :class "venue-ids-select" :value (.stringify js/JSON (clj->js record))}]]
        [:td (get record :name)]
        [:td (get record :city)]
        [:td (get record :state)]
        [:td (get record :country)]
        [:td (get record :lat)]
        [:td (get record :lng)]
        [:td (get record :category)]
          [:td [:img {:src (get record :icon)}]]
      ]
        
    )
    ]
  ]

)
