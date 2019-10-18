(ns user-registry.resource-defaults
  (:require [clojure.java.io :as io]
            [liberator.representation :refer [ring-response]]
            [cheshire.core :as json]
            [ring.util.request :as ring-request]))

(defn known-content-type?
  [ctx]
  (= (get-in ctx [:request :headers "content-type"]) "application/json"))

(defn parse-json-or-error
  [ctx]
  (try
    (with-open [rdr (io/reader (get-in ctx [:request :body]))]
      [false {:user-registry/data (json/parse-string (slurp rdr) true)}])
    (catch Exception _
      true)))

(defn handle-created
  [{:keys [request :user-registry/id]}]
  (ring-response
    {:headers {"Location" (str (ring-request/request-url request) "/" id)}}))

(defn generate-json [x] (str (json/generate-string x) "\n"))

(defn handle-exception
  [{:keys [exception]}]
  (when-let [error (:user-registry/error (ex-data exception))]
    (ring-response {:status 400, :body (generate-json error)})))

(defn handle-ok
  [{:keys [:user-registry/data]}]
  (generate-json data))

(def post-defaults
  {:allowed-methods [:post]
   :available-media-types ["application/json"]
   :known-content-type? known-content-type?
   :malformed? parse-json-or-error
   :handle-created handle-created
   :handle-exception handle-exception})

(def get-defaults
  {:allowed-methods [:get]
   :available-media-types ["application/json"]
   :handle-ok handle-ok
   :handle-exception handle-exception})
