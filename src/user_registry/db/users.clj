(ns user-registry.db.users
  (:require [clojure.string :as str]
            [hugsql.core :as hugsql])
  (:import (java.sql SQLException)))

(hugsql/def-db-fns "user_registry/db/sql/users.sql")

(defn insert-user-checked
  [db user]
  (try
    (insert-user db user)
    nil
    (catch SQLException e
      (let [msg (.getMessage e)]
        (if (str/includes? (str/lower-case msg) "email_unique")
          {:email "duplicate"}
          (throw e))))))
