(ns rp.query.dsl.geo-test
  (:require [clojure.test :refer :all]
            [rp.query.dsl.geo :as g]))

(deftest test-distance->km
  (testing "Passes thru when unit is km"
    (is (= 5
           (g/distance->km [g/km 5]))))
  (testing "Converts when unit is miles"
    (is (= 8.046735439432222
           (g/distance->km [g/mi 5])))))
