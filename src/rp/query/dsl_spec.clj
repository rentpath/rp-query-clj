(ns rp.query.dsl-spec
  (:require [clojure.string :as str]
            [clojure.spec :as s]
            [rp.query.dsl :as dsl]
            [rp.query.dsl.geo :as g]
            [rp.query.dsl.operator :as op]))

;;;;;;;;;;;;;;;;;;;;;
;; Shareable specs ;;
;;;;;;;;;;;;;;;;;;;;;


(def latitude (s/and number? #(<= -90 % 90)))
(def longitude (s/and number? #(<= -180 % 180)))

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
(s/def ::dsl/field-name (s/or :kw keyword?
                              :str (s/and string? (complement str/blank?))))
(s/def ::dsl/dim-id nat-int?)

(s/def ::g/lat latitude)
(s/def ::g/lng longitude)
(s/def ::g/geocode (s/keys :req [::g/lat ::g/lng]))

(s/def ::g/distance (s/cat :unit #{g/km g/mi}
                           :value (s/and number? pos?)))

;; Select clause
(s/def ::dsl/select (s/coll-of (s/or :field-name ::dsl/field-name
                                     :dim-id ::dsl/dim-id)))

;; Where clause

(s/def ::dsl/roots-where
  (s/cat :operator #{op/roots}
         :dim-ids (s/+ ::dsl/dim-id)))

(s/def ::dsl/text-where
  (s/cat :operator #{op/all op/any}
         :field-name ::dsl/field-name
         :value (s/+ (s/or :str string?
                           :num number?))))

(s/def ::dsl/unary-range
  (s/cat :operator #{op/lt op/lteq op/gt op/gteq}
         :field-name ::dsl/field-name
         :value number?))

(s/def ::dsl/btwn-range
  (s/and (s/cat :operator #{op/btwn}
                :field-name ::dsl/field-name
                :low number?
                :high number?)
         (between? [:low] [:high])))

(s/def ::dsl/geo-unary-range
  (s/cat :operator #{op/gclt op/gcgt}
         :field-name ::dsl/field-name
         :geocode ::g/geocode
         :radius (s/spec ::g/distance)))

(s/def ::dsl/geo-btwn-range
  (s/and (s/cat :operator #{op/gcbtwn}
                :field-name ::dsl/field-name
                :geocode ::g/geocode
                :low-radius (s/spec ::g/distance)
                :high-radius (s/spec ::g/distance))
         (between? [:low-radius] [:high-radius] #(g/distance->km ((juxt :unit :value) %)))))

(s/def ::dsl/range-where
  (s/or :unary ::dsl/unary-range
        :btwn ::dsl/btwn-range
        :geo-unary ::dsl/geo-unary-range
        :geo-btwn ::dsl/geo-btwn-range))

(s/def ::dsl/boolean-leaf
  (s/or :dim-id ::dsl/dim-id
        :property-kv (s/cat :field-name ::dsl/field-name
                            :value (s/or :num number?
                                         :str string?))))

(s/def ::dsl/boolean-expression
  (s/or :leaf   ::dsl/boolean-leaf
        :not    (s/cat :operator #{op/not}
                       :operand ::dsl/boolean-expression)
        :and-or (s/cat :operator #{op/and op/or}
                       :operands (s/+ ::dsl/boolean-expression))))

(s/def ::dsl/boolean-where
  (s/cat :operator #{op/boolean}
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
(s/def ::dsl/where (s/nilable
                    (s/and (s/coll-of (s/or :roots ::dsl/roots-where
                                            :text ::dsl/text-where
                                            :range ::dsl/range-where
                                            :boolean ::dsl/boolean-where))
                           valid-where-op-counts?)))

;; Group clause
(s/def ::dsl/group-by
  (s/cat :operator #{op/by}
         :field-name ::dsl/field-name))

(s/def ::dsl/group
  ;; Note that group could be a collection in the future, but currently it only accepts one
  ;; child (a `op/by` clause) so opted to use a `cat` instead.
  (s/cat :by (s/spec ::dsl/group-by)))

;; Sort clause
(s/def ::dsl/sort
  (s/coll-of (s/cat :operator #{op/asc op/desc}
                    :field-name ::dsl/field-name
                    :geocode (s/? ::g/geocode))))

;; Limit and offset clauses
(s/def ::dsl/limit nat-int?)
(s/def ::dsl/offset nat-int?)

;; All together now!
(s/def ::dsl/query
  (s/keys :req [::dsl/where]
          :opt [::dsl/select
                ::dsl/group
                ::dsl/sort
                ::dsl/limit
                ::dsl/offset]))

;; (s/fdef rp.query-dsl/query-spec
;;         :args (s/cat :user-query ::dsl/query)
;;         :ret ::q/query-spec)

