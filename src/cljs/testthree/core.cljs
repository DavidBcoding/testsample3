( ns testthree.core
  (:require
    [day8.re-frame.http-fx]
    [reagent.dom :as rdom]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [testthree.ajax :as ajax]
    [testthree.events]
    [reitit.core :as reitit]
    [reitit.frontend.easy :as rfe]
    [clojure.string :as string]
    [ajax.core :refer [GET POST]])
  (:import goog.History))

;;Adding button function and placement
(def app-data (r/atom {:x 0 :y 0 :total 0}))

;; Updates the value of total in app-data
(defn swap [val]
      (swap! app-data assoc
             :total val))


;; Calls the math API for a specific operation and x and y values
(defn math [operation x y]
      (ajax.core/POST (str "/api/math/" operation)
                      {:headers       {"accept" "application/transit-json"}
                       :params        {:x x :y y}
                       :error-handler #(do
                                         (println %)
                                         (rf/dispatch [:total 0]))
                       :handler       #(do
                                         (println %)
                                         (rf/dispatch [:total %]))}))

;; Function for hard coded math API for Year + 1
(defn getAdd []
      (ajax.core/GET "/api/math/plus?x=1&y=2022"
                     {:headers {"accept" "application/json"}
                      :handler #(swap (:total %))}))

(defn int-value [v]
      (-> v .-target .-value int))

(defn change-color []
      (cond
        (<= 0 (:total @app-data) 19) {:style {:color "lightgreen" :font-weight :bold}}
        (<= 20 (:total @app-data) 49) {:style {:color "lightblue" :font-weight :bold}}
        :default {:style {:color "lightsalmon" :font-weight :bold}}))

(defn nav-link [uri title page]
      [:a.navbar-item
       {:href  uri
        :class (when (= page @(rf/subscribe [:common/page-id])) :is-active)}
       title])

(rf/reg-sub
  :x
  (fn [db _]
      (:x db)))

(rf/reg-event-db
  :x
  (fn [db [_ new-value]]
      (assoc db :x new-value)))


(comment
  (rf/dispatch [:x 8])
  (rf/dispatch [:x 100])
  (rf/dispatch [:x 8])


  @re-frame.db/app-db

  @(rf/subscribe [:x])

  ())

(rf/reg-sub
  :y
  (fn [db _]
      (:y db)))

(rf/reg-event-db
  :y
  (fn [db [_ new-value]]
      (assoc db :y new-value)))


(comment
  (rf/dispatch [:y 81])
  (rf/dispatch [:y 1001])
  (rf/dispatch [:y 81])


  @re-frame.db/app-db

  @(rf/subscribe [:y])
  ())

(rf/reg-sub
  :total
  (fn [db _]
      (:total db)))

(rf/reg-event-db
  :total
  (fn [db [_ new-value]]
      (assoc db :total new-value)))

(defn navbar []
      (r/with-let [expanded? (r/atom false)]
                  [:nav.navbar.is-info>div.container
                   [:div.navbar-brand
                    [:a.navbar-item {:href "/" :style {:font-weight :bold}} "sample3"]
                    [:span.navbar-burger.burger
                     {:data-target :nav-menu
                      :on-click    #(swap! expanded? not)
                      :class       (when @expanded? :is-active)}
                     [:span] [:span] [:span]]]
                   [:div#nav-menu.navbar-menu
                    {:class (when @expanded? :is-active)}
                    [:div.navbar-start
                     [nav-link "#/" "Home" :home]
                     [nav-link "#/about" "About" :about]]]]))


(defn home-page []
      (r/with-let [x (rf/subscribe [:x])
                   y (rf/subscribe [:y])
                   total (rf/subscribe [:total])]
                  [:div.content.box
                   [:button.button.is-primary {:on-click #(getAdd)} "2022 + 1"]
                   [:p "That answer is: "
                    [:span @total]]
                   [:section.section>div.container>div.content
                    [:p "Enter numbers in the text boxes below for your own equation then click an operation for your answer."]
                    [:form
                     [:div.form-group
                      [:input {:type :text :value (str @x) :placeholder "First number here" :on-change #(rf/dispatch [:x (int-value %)])}]]
                     [:p]
                     [:div.form-group
                      [:input {:type :text :value (str @y) :placeholder "Second number here" :on-change #(rf/dispatch [:y (int-value %)])}]]
                     [:p]
                     [:button.button.is-primary {:on-click #(math "plus" @x @y)} "+"]
                     [:button.button.is-black {:on-click #(math "minus" @x @y)} "-"]
                     [:button.button.is-primary {:on-click #(math "multiply" @x @y)} "x"]
                     [:button.button.is-black {:on-click #(math "divide" @x @y)} "/"]
                     [:p]
                     [:div.form-group
                      [:label "Your answer is: " + [:span (change-color) @total]]]]]]))

(defn about-page []
      [:section.section>div.container>div.content
       [:img {:src "/img/warning_clojure.png"}]])

(defn page []
      (if-let [page @(rf/subscribe [:common/page])]
              [:div
               [navbar]
               [page]]))

(defn navigate! [match _]
      (rf/dispatch [:common/navigate match]))

(def router
  (reitit/router
    [["/" {:name        :home
           :view        #'home-page
           :controllers [{:start (fn [_] (rf/dispatch [:page/init-home]))}]}]
     ["/about" {:name :about
                :view #'about-page}]]))

(defn start-router! []
      (rfe/start!
        router
        navigate!
        {}))

;; -------------------------
;; Initialize app
(defn ^:dev/after-load mount-components []
      (rf/clear-subscription-cache!)
      (rdom/render [#'page] (.getElementById js/document "app")))

(defn init! []
      (start-router!)
      (ajax/load-interceptors!)
      (mount-components))