(ns user-registry.user
  (:require [clojure.string :as str]
            [crypto.password.pbkdf2 :as password]
            [schema.core :as s]
            [user-registry.schema-utils :refer [preds explain-errors]])
  (:import (java.util UUID)))

(defn register
  [m]
  (-> m
    (assoc :id (UUID/randomUUID)
           :passhash (password/encrypt (:password m)))
    (dissoc :password)))

(def non-whitespace-string
  (preds "non-whitespace string" string? (comp seq str/trim)))

(def UserInput
  {:email (preds "email pattern" string? (partial re-matches #"^\S+@\S+\.\S+$"))
   :password (preds "min 7 characters" string? #(>= (count %) 7))
   :name non-whitespace-string
   :surname non-whitespace-string})

(def validate (comp explain-errors (s/checker UserInput)))

(defn select [m] (select-keys m [:email :name :surname :id]))
