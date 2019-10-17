(ns user-registry.schema-utils
  (:require [schema.core :as s]
            [schema.utils :as su]
            [cheshire.core :as json]
            [clojure.walk :as walk])
  (:import (schema.utils ValidationError)))

(defn preds [name & fns] (s/pred (apply every-pred fns) name))

(defn explain-errors
  [error]
  (walk/prewalk
    (fn [x]
      (cond-> x
        (instance? ValidationError x) su/validation-error-explain))
    error))
