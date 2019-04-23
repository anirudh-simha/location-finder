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
      [:input {:type "text" :id "category" :placeholder "search by category"}]
      [:button {:id "submit" :on-click #(get-locations)} "Lets go!"]
      [:div#results]
      )
      (.getElementById js/document "app")
      )
      
 (defn get-locations
   []
   (enable-console-print!)
  (go (let[location (.-value (.getElementById js/document "location"))
           category (.-value (.getElementById js/document "category"))
       api-result (<! (http/get config/app-url-location {:query-params {"location" location}}))]
      (if(= (get api-result :body) "An error has occured.please try again")
        (r/render [:table [:tbody [:tr [:td "An error has occured"]]]] (.getElementById js/document "results"))
      (r/render
          (render-locations-table (get api-result :body) category)
        (.getElementById js/document "results")
        )
      )
    )
  )
  )

(defn render-locations-table
  [records category]
  [:table {:style {:border "1px solid black"}}
    [:tbody
      [:tr
        [:td "name"][:td "city"][:td "state"][:td "lat"][:td "lng"][:td "categories"][:td "image"]
      ]
      (for [record records]

      (if  (or (empty? category)   (and (not(empty? category)) (= category (get (get (get record :categories) 0) :name))))
      ^{:key (get record :id)}
      ;[:tr
      ;  [:td (get record :name)]
      ;  [:td (get (get record :location) :city)]
      ;  [:td (get (get record :location) :state)]
      ;  [:td (get (get record :location) :lat)]
      ;  [:td (get (get record :location) :lng)]
      ;  [:td (get (get (get record :categories) 0) :name)]
      ;    [:td [:img {:src (str (get (get (get (get record :categories) 0) :icon) :prefix) "bg_64" (get (get (get (get record :categories) 0) :icon) :suffix))}]]
      ;]
      [:tr
        [:td (get record :name)]
        [:td (get record :city)]
        [:td (get record :state)]
        [:td (get record :lat)]
        [:td (get record :lng)]
        [:td (get record :category)]
          [:td [:img {:src (get record :icon)}]]
      ]
      )
        
    )
    ]
  ]

)
  ;[:h1 "Hello from Reagent!"]
  ;(.getElementById js/document "results"))
      ;[:table
      ;  [:tr 
      ;    [:td
      ;      "Hello"
      ;     ]
      ;    [:td
      ;      "Hello"
      ;     ]
      ;   ]
      ; ]
