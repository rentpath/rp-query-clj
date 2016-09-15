(ns rp.query.dsl.operator-test
  (:refer-clojure :exclude [and boolean not or])
  (:require [clojure.test :refer :all]
            [rp.query.dsl.operator :refer :all]))

(deftest test-operators-fn
  (is (> (count operators) 3))
  (is (every? keyword? operators)))
