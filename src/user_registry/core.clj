(ns user-registry.core
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [compojure.core :refer [defroutes ANY]]
            [compojure.route :as route]
            [ring.middleware.defaults :as ring-defaults]
            [liberator.core :refer [resource]]
            [user-registry.resource-defaults
             :refer [post-defaults get-defaults]]
            [user-registry.db.users :as db-users]
            [user-registry.user :as user])
  (:import (java.util UUID)))

(defn db-spec [ctx] (get-in ctx [:request :user-registry/config :db-spec]))

(defn try-parse-uuid [s] (try (UUID/fromString s) (catch Exception _)))

(defn throw-error [m] (throw (ex-info (str m) {:user-registry/error m})))

(defroutes app
  (ANY "/user" []
    (resource
      post-defaults
      :post! (fn [{:keys [:user-registry/data] :as ctx}]
               (when-let [error (user/validate data)]
                 (throw-error error))
               (let [m (user/register data)]
                 (if-let [error (db-users/insert-user-checked (db-spec ctx) m)]
                   (throw-error error)
                   {:user-registry/id (:id m)})))))

  (ANY "/user/:id" [id]
    (resource
      get-defaults
      :exists? (fn [ctx]
                 (when-let [m (db-users/user-by-id (db-spec ctx)
                                {:id (try-parse-uuid id)})]
                   {:user-registry/data (user/select m)}))))

  (route/not-found "Resource not found."))

(defn boot!
  [{:keys [db-spec]}]
  (db-users/create-users-table db-spec))

(defn wrap-config
  [handler config]
  (fn [req]
    (-> req
      (assoc :user-registry/config config)
      (handler))))

(defn make-handler
  [config]
  (boot! config)
  (-> app
    (wrap-config config)
    (ring-defaults/wrap-defaults ring-defaults/api-defaults)))

(def handler-atom (atom nil))

(defn load-config [] (-> "config.edn" (io/resource) (slurp) (edn/read-string)))

(defn handler
  [req]
  (if-let [h @handler-atom]
    (h req)
    (let [new-h (make-handler (load-config))]
      (reset! handler-atom new-h)
      (new-h req))))
