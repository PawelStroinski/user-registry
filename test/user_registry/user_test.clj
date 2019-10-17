(ns user-registry.user-test
  (:require [midje.sweet :refer :all]
            [clojure.string :as str]
            [crypto.password.pbkdf2 :as password]
            [user-registry.user :as user]))

(def input {:email "e@e.com", :password "1234567", :name "n", :surname "s"})

(facts "register"
  (with-redefs [password/encrypt {"1234567" "h"}]
    (let [act (user/register input)]
      (dissoc act :id :passhash) => {:email "e@e.com", :name "n", :surname "s"}
      (:id act) => uuid?
      (:passhash act) => "h")))

(tabular "validate"
  (fact (user/validate ?m) => ?error)
  ?m ?error
  input nil
  nil some?
  {} some?
  (dissoc input :name) {:name 'missing-required-key}
  (assoc input :name " ") #(str/includes? (:name %) "whitespace string")
  (assoc input :surname "") #(str/includes? (:surname %) "whitespace string")
  (assoc input :email "x@") #(str/includes? (:email %) "email pattern")
  (assoc input :password "123456") #(str/includes? (:password %) "min 7")
  (assoc input :password "12345678") nil)

(fact "select"
  (user/select (assoc input :foo "bar" :id "baz"))
  => {:email "e@e.com", :name "n", :surname "s", :id "baz"})
