(ns thomascothran.reitit-repro.core
  (:require [aleph.http :as http]
            [reitit.ring :as ring]
            [reitit.coercion.spec]
            [reitit.ring.coercion :as rrc]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.parameters :as parameters]
            [jsonista.core :as j]
            [clojure.data.json :as json]
            [muuntaja.core]))

(def server (atom nil))

(def m
  (muuntaja.core/create
   (assoc-in muuntaja.core/default-options
             [:formats "application/json" :decoder-opts]
             {:decode-key-fn identity})))

(defn enlarge-map
  [m]
  (reduce #(assoc %1 %2 m)
          {}
          (range 100)))

(def sample-map
  (->> "sample.json"
       clojure.java.io/resource
       slurp
       j/read-value
       enlarge-map))

(defn test-handler [req]
  {:status 200
   :body {:sample sample-map}})

(defn test-handler2 [req]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/write-str sample-map)})

(defn cors-mw
  [handler]
  (fn [request]
    (let [response (handler request)]
      (-> response
          (assoc-in [:headers "Access-Control-Allow-Origin"]
                    "*")
          (assoc-in [:headers "Access-Control-Allow-Methods"]
                    "GET,PUT,POST,DELETE,OPTIONS")
          (assoc-in [:headers "Access-Control-Allow-Headers"]
                    "X-Requested-With,Content-Type,Cache-Control")))))

(defn get-router
  []
  (ring/router
   [["/test" {:handler test-handler}]
    ["/test2" {:handler test-handler2}]]
    {:data {:muuntaja m
            :middleware [parameters/parameters-middleware
                        ;; content-negotiation
                        muuntaja/format-negotiate-middleware
                        ;; encoding response body
                        muuntaja/format-response-middleware
                        ;; decoding request body
                        muuntaja/format-request-middleware
                        ;; multipart
                        multipart/multipart-middleware
                        ;; CORS
                        cors-mw]}}))
(defn make-app
  []
  (ring/ring-handler
   (get-router)))

(defn start-server
  []
  (->> (http/start-server (make-app) {:port 9000})
       (reset! server)))

(defn stop-server
  []
  (let [s @server]
    (.close s)
    (reset! server nil)))
