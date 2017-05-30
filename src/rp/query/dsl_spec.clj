(ns rp.query.dsl-spec
  (:require [clojure.string :as str]
            [clojure.spec.alpha :as spec]
            [rp.query.dsl :as dsl]
            [rp.query.dsl.geo :as g]
            [rp.query.dsl.operator :as op]))

;;;;;;;;;;;;;;;;;;;;;
;; Shareable specs ;;
;;;;;;;;;;;;;;;;;;;;;


(def latitude (spec/and number? #(<= -90 % 90)))
(def longitude (spec/and number? #(<= -180 % 180)))

;;;;;;;;;;;;;
;; Helpers ;;
;;;;;;;;;;;;;

(defn between?
  ([low-keys high-keys]
   (between? low-keys high-keys identity))
  ([low-keys high-keys to-num-f]
   (fn [coll]
     (let [low (get-in coll low-keys)
           high (get-in coll high-keys)]
       (if (and low high)
         (< (to-num-f low) (to-num-f high)))))))

;;;;;;;;;;;;;;;;;;
;; Spec for DSL ;;
;;;;;;;;;;;;;;;;;;

;; Fundamentals
(spec/def ::dsl/field-name (spec/or :kw keyword?
                                    :str (spec/and string? (complement str/blank?))))
(spec/def ::dsl/dim-id nat-int?)

(spec/def ::g/lat latitude)
(spec/def ::g/lng longitude)
(spec/def ::g/geocode (spec/keys :req [::g/lat ::g/lng]))

(spec/def ::g/distance (spec/cat :unit #{g/km g/mi}
                                 :value (spec/and number? pos?)))

;; Select clause
(spec/def ::dsl/select (spec/coll-of (spec/or :field-name ::dsl/field-name
                                              :dim-id ::dsl/dim-id)))

;; Where clause

(spec/def ::dsl/roots-where
  (spec/cat :operator #{op/roots}
            :dim-ids (spec/+ ::dsl/dim-id)))

(spec/def ::dsl/text-where
  (spec/cat :operator #{op/all op/any}
            :field-name ::dsl/field-name
            :value (spec/+ (spec/or :str string?
                                    :num number?))))

(spec/def ::dsl/unary-range
  (spec/cat :operator #{op/lt op/lteq op/gt op/gteq}
            :field-name ::dsl/field-name
            :value number?))

(spec/def ::dsl/btwn-range
  (spec/and (spec/cat :operator #{op/btwn}
                      :field-name ::dsl/field-name
                      :low number?
                      :high number?)
            (between? [:low] [:high])))

(spec/def ::dsl/geo-unary-range
  (spec/cat :operator #{op/gclt op/gcgt}
            :field-name ::dsl/field-name
            :geocode ::g/geocode
            :radius (spec/spec ::g/distance)))

(spec/def ::dsl/geo-btwn-range
  (spec/and (spec/cat :operator #{op/gcbtwn}
                      :field-name ::dsl/field-name
                      :geocode ::g/geocode
                      :low-radius (spec/spec ::g/distance)
                      :high-radius (spec/spec ::g/distance))
            (between? [:low-radius] [:high-radius] #(g/distance->km ((juxt :unit :value) %)))))

(spec/def ::dsl/range-where
  (spec/or :unary ::dsl/unary-range
           :btwn ::dsl/btwn-range
           :geo-unary ::dsl/geo-unary-range
           :geo-btwn ::dsl/geo-btwn-range))

(spec/def ::dsl/boolean-leaf
  (spec/or :dim-id ::dsl/dim-id
           :property-kv (spec/cat :field-name ::dsl/field-name
                                  :value (spec/or :num number?
                                                  :str string?))))

(spec/def ::dsl/boolean-expression
  (spec/or :leaf   ::dsl/boolean-leaf
           :not    (spec/cat :operator #{op/not}
                             :operand ::dsl/boolean-expression)
           :and-or (spec/cat :operator #{op/and op/or}
                             :operands (spec/+ ::dsl/boolean-expression))))

(spec/def ::dsl/boolean-where
  (spec/cat :operator #{op/boolean}
            :expression ::dsl/boolean-expression))

(defn count-where-type
  [coll type]
  (count (filter #(= type (first %)) coll)))

(defn valid-where-op-counts?
  [coll]
  ;; This will be handled by Endeca impl:
  ;;   (and (= 1 (count-where-type coll :roots)))
  (> 2 (count-where-type coll :boolean)))

;; Check on presence of roots, etc., belongs to Endeca-specific implementation
(spec/def ::dsl/where (spec/nilable
                       (spec/and (spec/coll-of (spec/or :roots ::dsl/roots-where
                                                        :text ::dsl/text-where
                                                        :range ::dsl/range-where
                                                        :boolean ::dsl/boolean-where))
                                 valid-where-op-counts?)))

;; Group clause
(spec/def ::dsl/group-by
  (spec/cat :operator #{op/by}
            :field-name ::dsl/field-name))

(spec/def ::dsl/group
  ;; Note that group could be a collection in the future, but currently it only accepts one
  ;; child (a `op/by` clause) so opted to use a `cat` instead.
  (spec/cat :by (spec/spec ::dsl/group-by)))

;; Sort clause
(spec/def ::dsl/sort
  (spec/coll-of (spec/cat :operator #{op/asc op/desc}
                          :field-name ::dsl/field-name
                          :geocode (spec/? ::g/geocode))))

;; Limit and offset clauses
(spec/def ::dsl/limit nat-int?)
(spec/def ::dsl/offset nat-int?)

;; All together now!
(spec/def ::dsl/query
  (spec/keys :req [::dsl/where]
             :opt [::dsl/select
                   ::dsl/group
                   ::dsl/sort
                   ::dsl/limit
                   ::dsl/offset]))

;; (spec/fdef rp.query-dsl/query-spec
;;         :args (spec/cat :user-query ::dsl/query)
;;         :ret ::q/query-spec)
