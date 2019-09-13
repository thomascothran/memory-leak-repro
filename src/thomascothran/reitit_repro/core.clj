(ns thomascothran.reitit-repro.core
  (:require [aleph.http :as http]
            [clojure.data.json :as json])
  (:import java.io.ByteArrayInputStream))

(defonce server (atom nil))

(defn enlarge-map
  [m]
  (reduce #(assoc %1 %2 m)
          {}
          (range 100)))

(def sample-map
  (->> "sample.json"
       clojure.java.io/resource
       slurp
       json/read-str
       enlarge-map))

(defn handler
  [req]
  {:status 200
   :body (ByteArrayInputStream. (.getBytes ^String (json/write-str sample-map)))})

(defn start-server
  []
  (->> (http/start-server handler {:port 9000})
       (reset! server)))

(defn stop-server
  []
  (let [s @server]
    (.close s)
    (reset! server nil)))
