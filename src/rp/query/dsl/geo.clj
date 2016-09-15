(ns rp.query.dsl.geo)

;; Keywords for geocode data
(def ^:const lat ::lat)
(def ^:const lng ::lng)

;; Keywords for distance units
(def ^:const mi ::mi)
(def ^:const km ::km)

(def ^:const KM-PER-MI 0.62137)

(defn miles->km
  [miles]
  (/ miles KM-PER-MI))

(defmulti distance->km
  "Given a distance (pair of unit (either `mi` or `km`) and value, return the distance in kilometers."
  first)

(defmethod distance->km km
  [[unit value]]
  value)

(defmethod distance->km mi
  [[unit value]]
  (miles->km value))

(defmethod distance->km :default
  [d]
  (throw (ex-info (format "Expected a pair of [unit distance] where unit is either %s or %s" mi km)
                  {:argument d})))
