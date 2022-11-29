(ns testthree.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[testthree started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[testthree has shut down successfully]=-"))
   :middleware identity})
