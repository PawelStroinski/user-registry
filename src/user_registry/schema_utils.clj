(ns user-registry.schema-utils
  (:require [clojure.walk :as walk]
            [schema.core :as s]
            [schema.utils :as su])
  (:import (schema.utils ValidationError)))

(defn preds [name & fns] (s/pred (apply every-pred fns) name))

(defn explain-errors
  [error]
  (walk/prewalk
    (fn [x]
      (cond-> x
        (instance? ValidationError x) su/validation-error-explain))
    error))
