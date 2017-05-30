(ns rp.query.dsl-spec-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as spec]
            [clojure.spec.test.alpha :as spec-test]
            [rp.query.dsl :as dsl]
            [rp.query.dsl-spec]
            [rp.query.dsl.geo :as g]
            [rp.query.dsl.operator :as op]))

(deftest test-fundamental-specs
  (are [spec expected-output input]
      (let [actual (spec/valid? spec input)]
        (is (= expected-output actual)
            (spec/explain-str spec input)))

    ::dsl/field-name true :field
    ::dsl/field-name true "field"
    ::dsl/field-name false nil
    ::dsl/field-name false ""
    ::dsl/field-name false 1

    ::dsl/dim-id true 0
    ::dsl/dim-id true 1
    ::dsl/dim-id false nil
    ::dsl/dim-id false -1
    ::dsl/dim-id false 1.0
    ::dsl/dim-id false "1"

    ::g/lat true 0
    ::g/lat true 0.0
    ::g/lat true 90
    ::g/lat true -90
    ::g/lat false nil
    ::g/lat false "0"
    ::g/lat false 90.1
    ::g/lat false -90.1

    ::g/lng true 0
    ::g/lng true 0.0
    ::g/lng true 180
    ::g/lng true -180
    ::g/lng false nil
    ::g/lng false "0"
    ::g/lng false 180.1
    ::g/lng false -180.1

    ::g/geocode true {::g/lat 35.3242 ::g/lng -78.87987}
    ::g/geocode false {:lat 35.3242 :lng -78.87987}
    ::g/geocode false {}
    ::g/geocode false nil

    ::g/distance true [::g/km 1]
    ::g/distance true [::g/mi 1]
    ::g/distance true [::g/km 1.1]
    ::g/distance true [::g/mi 1.1]
    ::g/distance false [::g/km 0]
    ::g/distance false [::g/mi ]
    ::g/distance false [:km 1]
    ::g/distance false [:mi 1]
    ::g/distance false nil
    ::g/distance false 1
    ::g/distance false []))

(deftest test-select
  (are [expected-output input]
      (let [actual (spec/valid? ::dsl/select input)]
        (is (= expected-output actual)
            (spec/explain-str ::dsl/select input)))

    true []
    true ["1" "2" "field3"]
    true [:one :two :field-3]
    true [1 2 3]
    true [:one "two" 3]

    true #{}
    true #{"1" "2" "field3"}
    true #{:one :two :field-3}
    true #{1 2 3}
    true #{:one "two" 3}

    false nil
    false 42
    false "42"
    false :forty-two
    false [["one"]]
    false #{["one"]}))

(deftest test-roots-where
  (are [expected-output input]
      (let [actual (spec/valid? ::dsl/roots-where input)]
        (is (= expected-output actual)
            (spec/explain-str ::dsl/roots-where input)))

    true [op/roots 0]
    true [op/roots 1]
    true [op/roots 0 1 2]

    false nil
    false []
    false [0]
    false [:roots]
    false [op/roots -1]
    false [op/roots "1"]))

(deftest test-text-where
  (are [expected-output input]
      (let [actual (spec/valid? ::dsl/text-where input)]
        (is (= expected-output actual)
            (spec/explain-str ::dsl/text-where input)))

    true [op/any :field "value"]
    true [op/all :field "value"]
    true [op/any "field" "value"]
    true [op/all "field" "value"]
    true [op/any :field 123]
    true [op/all :field 123]
    true [op/all "field" "value"]
    true [op/any :field "value" "another" "yet another" 123 456]
    true [op/all :field "value" "another" "yet another" 123 456]

    false nil
    false []
    false [:field "value"]
    false ["field" "value"]
    false [:any :field "value"]
    false [:all :field "value"]
    false [:any "field" "value"]
    false [:all "field" "value"]

    false [op/any]
    false [op/all]
    false [op/any nil]
    false [op/all nil]
    false [op/any :field]
    false [op/all :field]
    false [op/any "field"]
    false [op/all "field"]
    false [op/any :field nil]
    false [op/all :field nil]))

(deftest test-range-where
  (are [expected-output input]
      (let [actual (spec/valid? ::dsl/range-where input)]
        (is (= expected-output actual)
            (spec/explain-str ::dsl/range-where input)))

    true [op/lt :field 5]
    true [op/lt "field" 5]
    true [op/lteq :field 5]
    true [op/lteq "field" 5]
    true [op/gt :field 5]
    true [op/gt "field" 5]
    true [op/gteq :field 5]
    true [op/gteq "field" 5]
    true [op/btwn :field 5 10]
    true [op/btwn "field" 5 10]

    true [op/gclt :field {::g/lat 35.3242 ::g/lng -78.87987} [::g/km 1]]
    true [op/gclt "field" {::g/lat 35.3242 ::g/lng -78.87987} [::g/km 1]]
    true [op/gclt :field {::g/lat 35.3242 ::g/lng -78.87987} [::g/mi 1]]
    true [op/gclt "field" {::g/lat 35.3242 ::g/lng -78.87987} [::g/mi 1]]
    true [op/gcgt :field {::g/lat 35.3242 ::g/lng -78.87987} [::g/km 1]]
    true [op/gcgt "field" {::g/lat 35.3242 ::g/lng -78.87987} [::g/km 1]]
    true [op/gcgt :field {::g/lat 35.3242 ::g/lng -78.87987} [::g/mi 1]]
    true [op/gcgt "field" {::g/lat 35.3242 ::g/lng -78.87987} [::g/mi 1]]
    true [op/gcbtwn :field {::g/lat 35.3242 ::g/lng -78.87987} [::g/km 1] [::g/km 2]]
    true [op/gcbtwn "field" {::g/lat 35.3242 ::g/lng -78.87987} [::g/km 1] [::g/km 2]]
    true [op/gcbtwn :field {::g/lat 35.3242 ::g/lng -78.87987} [::g/mi 1] [::g/mi 2]]
    true [op/gcbtwn "field" {::g/lat 35.3242 ::g/lng -78.87987} [::g/mi 1] [::g/mi 2]]
    ;; Units for low and high radii need not match:
    true [op/gcbtwn "field" {::g/lat 35.3242 ::g/lng -78.87987} [::g/km 1.5] [::g/mi 1]]

    false nil
    false []

    false [op/lt]
    false [op/lt :field]
    false [op/lt :field 5 10]

    false [op/lteq]
    false [op/lteq :field]
    false [op/lteq :field 5 10]

    false [op/gt]
    false [op/gt :field]
    false [op/gt :field 5 10]

    false [op/gteq]
    false [op/gteq :field]
    false [op/gteq :field 5 10]

    false [op/btwn]
    false [op/btwn :field]
    false [op/btwn :field 5]
    false [op/btwn :field 5 10 15]

    false [op/gclt]
    false [op/gclt :field]
    false [op/gcgt]
    false [op/gcgt :field]
    false [op/gcbtwn]
    false [op/gcbtwn :field]

    false [op/gclt :field {::g/lat 35.3242 ::g/lng -78.87987}]
    false [op/gclt :field {::g/lat 35.3242 ::g/lng -78.87987} 1]
    false [op/gclt :field {::g/lat 35.3242 ::g/lng -78.87987} [::g/km 1] [::g/km 2]]
    false [op/gcgt :field {::g/lat 35.3242 ::g/lng -78.87987}]
    false [op/gcgt :field {::g/lat 35.3242 ::g/lng -78.87987} 1]
    false [op/gcgt :field {::g/lat 35.3242 ::g/lng -78.87987} [::g/km 1] [::g/km 2]]
    false [op/gcbtwn :field {::g/lat 35.3242 ::g/lng -78.87987}]
    false [op/gcbtwn :field {::g/lat 35.3242 ::g/lng -78.87987} [::g/km 1]]
    false [op/gcbtwn :field {::g/lat 35.3242 ::g/lng -78.87987} 1]
    false [op/gcbtwn :field {::g/lat 35.3242 ::g/lng -78.87987} [::g/km 1] [::g/km 2] [::g/km 3]]
    ;; Low-rad >= high-rad:
    false [op/gcbtwn :field {::g/lat 35.3242 ::g/lng -78.87987} [::g/km 1] [::g/km 1]]
    false [op/gcbtwn :field {::g/lat 35.3242 ::g/lng -78.87987} [::g/km 2] [::g/km 1]]
    false [op/gcbtwn :field {::g/lat 35.3242 ::g/lng -78.87987} [::g/mi 1] [::g/km 1.5]]))

(deftest test-boolean-where
  (are [expected-output input]
      (let [actual (spec/valid? ::dsl/boolean-where input)]
        (is (= expected-output actual)
            (spec/explain-str ::dsl/boolean-where input)))

    true [op/boolean 1]
    true [op/boolean [:foo "bar"]]
    true [op/boolean ["foo" "bar"]]
    true [op/boolean [op/not 1]]
    true [op/boolean [op/not [:foo "bar"]]]

    false nil
    false {}
    false []
    false [op/boolean]
    false [op/boolean [op/not]]
    false [op/boolean [op/not 1 2]]))

(def valid-where? (partial spec/valid? ::dsl/where))

(deftest test-where
  (is (valid-where? [[op/roots 1]]))
  (is (valid-where? [[op/roots 1 2 3]]))

  ;; Example with a mix of where clause types:
  (is (valid-where? [[op/roots 1]
                     [op/any :field 123 456 789]
                     [op/all :field "foo" "bar" "baz"]
                     [op/gt :field 1000]
                     [op/gclt :field {::g/lat 35.3242 ::g/lng -78.87987} [::g/mi 1]]
                     [op/boolean [op/and
                                  100
                                  [:field "val"]
                                  [op/or 200 300]
                                  [op/not 400]]]]))

  (testing "Generic DSL doesn't enforce a where, though impl's may"
    (is (valid-where? nil))
    (is (valid-where? {}))
    (is (valid-where? [])))

  (is (not (valid-where? [[op/roots]])))

  (is (not (valid-where? [[op/roots 1]
                          [op/boolean 1]
                          [op/boolean 2]]))
      "Only one boolean sub-expression allowed"))

(deftest test-group
  (are [expected-output input]
      (let [actual (spec/valid? ::dsl/group input)]
        (is (= expected-output actual)
            (spec/explain-str ::dsl/group input)))

    true [[op/by "field"]]
    true [[op/by :field]]

    false nil
    false {}
    false []
    false [[op/by 1]]
    false [[op/by]]
    false [[op/by :field :extra]]))

(deftest test-sort
  (are [expected-output input]
      (let [actual (spec/valid? ::dsl/sort input)]
        (is (= expected-output actual)
            (spec/explain-str ::dsl/sort input)))

    true [[op/asc "field"]]
    true [[op/asc :field]]
    true [[op/desc "field"]]
    true [[op/desc :field]]
    true [[op/asc :field {::g/lat 35.3242 ::g/lng -78.87987}]]
    true [[op/asc "field" {::g/lat 35.3242 ::g/lng -78.87987}]]
    true [[op/desc :field {::g/lat 35.3242 ::g/lng -78.87987}]]
    true [[op/desc "field" {::g/lat 35.3242 ::g/lng -78.87987}]]
    true [[op/asc :field1]
          [op/desc :field2]
          [op/desc :field3 {::g/lat 35.3242 ::g/lng -78.87987}]]

    false nil
    false [[op/asc]]
    false [[op/desc]]
    false [[op/asc :field :extra]]
    false [[:asc :field]]
    false [[op/desc :field {::g/lat 35.3242}]]))

(deftest test-limit-offset
  (are [spec expected-output input]
      (let [actual (spec/valid? spec input)]
        (is (= expected-output actual)
            (spec/explain-str spec input)))

    ::dsl/limit true 0
    ::dsl/limit true 1
    ::dsl/limit true 100
    ::dsl/offset true 0
    ::dsl/offset true 1
    ::dsl/offset true 100

    ::dsl/limit false nil
    ::dsl/limit false -1
    ::dsl/limit false "1"
    ::dsl/offset false nil
    ::dsl/offset false -1
    ::dsl/offset false "1"))

(deftest test-query
  (are [expected-output input]
      (let [actual (spec/valid? ::dsl/query input)]
        (is (= expected-output actual)
            (spec/explain-str ::dsl/query input)))

    ;; Minimal valid query
    true {::dsl/where [[op/roots 0]]}

    ;; Fleshed out query
    true {::dsl/select [:field1 :field2 :field3 ; selected field names
                        1 2 3]        ; exposed refinements
          ::dsl/where [[op/roots 1]
                       [op/any :field "val"]
                       [op/all :field "val"]
                       [op/gt :field 1000]
                       [op/gclt :field {::g/lat 35.3242 ::g/lng -78.87987} [::g/mi 1]]
                       [op/boolean [op/and
                                    100
                                    [:field "val"]
                                    [op/or 200 300]
                                    [op/not 400]]]]
          ::dsl/group [[op/by :listingid]]
          ::dsl/sort [[op/desc :price]
                      [op/asc :propertyname]]
          ::dsl/offset 40
          ::dsl/limit 20}

    false nil
    false {}
    false []))
