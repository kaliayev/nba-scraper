(ns scrappy-ball.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [scrappy-ball.utils :as utils]
            [cheshire.core :as json]
            [scrappy-ball.sample :as sample]
            [net.cgrand.enlive-html :as html])
  (:import [java.net URL]))





(defroutes app-routes
  #_(GET "/seasons/:season" [season]
         (handle-get-season))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
