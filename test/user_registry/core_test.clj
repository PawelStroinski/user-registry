(ns user-registry.core-test
  (:require [midje.sweet :refer :all]
            [clojure.string :as str]
            [ring.mock.request :as mock]
            [cheshire.core :as json]
            [user-registry.core :as core]
            [user-registry.user-test :refer [input]]))

(fact "load config"
  (core/load-config) => map?)

(def config
  {:db-spec
   {:classname "org.h2.Driver"
    :subprotocol "h2:mem"
    :subname "user-registry;DB_CLOSE_DELAY=-1"}})

(def no-db (assoc-in config [:db-spec :subname] "x"))

(core/boot! config)

(defn handler [req] ((core/make-handler config) req))

(defonce email-counter (atom 0))

(defn next-user! [] (update input :email str (swap! email-counter inc)))

(defn post [m] (-> (mock/request :post "/user") (mock/json-body m) (handler)))

(facts "register"
  (fact "method not allowed"
    (:status (handler (mock/request :get "/user"))) => 405)

  (fact "malformed json"
    (:status (-> (mock/request :post "/user" "x")
               (mock/content-type "application/json")
               (handler))) => 400)

  (fact "unsupported content-type"
    (:status (-> (mock/request :post "/user")
               (mock/json-body (next-user!))
               (mock/content-type "application/unsupported")
               (handler))) => 415)

  (fact "invalid email"
    (let [res (post (assoc input :email "x"))]
      (:status res) => 400
      (:body res) => #(str/includes? % "email pattern")))

  (fact "success"
    (let [res (post (next-user!))]
      (:status res) => 201
      (get-in res [:headers "Location"])
      => #(str/starts-with? % "http://localhost/user/")))

  (fact "duplicate email"
    (let [user (next-user!)
          _ (post user)
          res (post user)]
      (:status res) => 400
      (:body res) => #(str/includes? % "duplicate")))

  (fact "db doesn't exist"
    (with-redefs [config no-db]
      (:status (post (next-user!))) => 500)))

(defn do-get [uri] (handler (mock/request :get uri)))

(facts "retrieve"
  (fact "registered"
    (let [user (next-user!)
          res (post user)
          location (get-in res [:headers "Location"])
          uri (str/replace location "http://localhost" "")
          res (do-get uri)
          m (json/parse-string (:body res) true)]
      (:status res) => 200
      (dissoc m :id) => (dissoc user :password)
      (count (:id m)) => 36))

  (fact "doesn't exist"
    (doseq [id ["xyz" "d7832415-8d74-4224-9a34-4d728141d987"]]
      (:status (do-get (str "/user/" id))) => 404))

  (fact "db doesn't exist"
    (with-redefs [config no-db]
      (:status (do-get "/user/d7832415-8d74-4224-9a34-4d728141d987")) => 500)))
