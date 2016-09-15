(ns rp.query.dsl.operator
  "Operators supported by the Query DSL"
  (:refer-clojure :exclude [and boolean not or]))

(defmacro defop
  "Helper macro for defining vars for operators."
  [name docstring kw]
  `(do
     (def ~name ~kw)
     (alter-meta! (var ~name)
                  (fn [m#] (assoc m#
                                 :const true
                                 ::operator true)))
     (var ~name)))

(defop roots
  "Search origin(s) operator used in a nested \":where\" vector."
  ::roots)

(defop all
  "Text search operator used in a nested \":where\" vector:
  [f/property-name [\"tree bent\" o/all]]."
  ::all)

(defop any
  "Text search operator. See 'all'."
  ::any)

(def text-op ::text-op)
(derive any text-op)
(derive all text-op)

(defop lt
  "< operator"
  ::lt)

(defop lteq
  "<= operator"
  ::lteq)

(defop gt
  "> operator"
  ::gt)

(defop gteq
  ">= operator"
  ::gteq)

(defop btwn
  "Operator for specifying a range of values, 'between'"
  ::btwn)

(defop gclt
  "< for geo needs"
  ::gclt)

(defop gcgt
  "> for geo needs"
  ::gcgt)

(defop gcbtwn
  "Operator for specifying a range of geo values, 'between'"
  ::gcbtwn)

(def num-op ::num-op)
(derive lt num-op)
(derive lteq num-op)
(derive gt num-op)
(derive gteq num-op)
(derive btwn num-op)

(def geo-op ::geo-op)
(derive gclt geo-op)
(derive gcgt geo-op)
(derive gcbtwn geo-op)

(def cmp-op ::cmp-op)
(derive num-op cmp-op)
(derive geo-op cmp-op)

(defop by
  "Grouping/roll-up operator used in a nested \":group\" vector:
  [o/by f/listing-id]"
  ::by)

(defop asc
  "Ascending direction, intended for sorting"
  ::asc)

(defop desc
  "Descending direction, intended for sorintg"
  ::desc)

(defop boolean
  "Boolean search operator used in a nested \":where\" vector."
  ::boolean)

(defop and
  "Logical AND. For Endeca specifically, this is limited to boolean sub-expressions and cannot be used arbitrarily throughout a query."
  ::and)

(defop or
  "Logical OR. For Endeca specifically, this is limited to boolean sub-expressions and cannot be used arbitrarily throughout a query."
  ::or)

(defop not
  "Logical negation. For Endeca specifically, this is limited to boolean sub-expressions and cannot be used arbitrarily throughout a query."
  ::not)

(def operator-vars
  (comp (filter (fn [var] (::operator (meta var))))
        (map deref)))

(def operators
  (into #{} operator-vars (vals (ns-publics *ns*))))


