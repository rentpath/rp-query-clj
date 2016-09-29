(ns rp.query)

(defprotocol IQuery
  (execute! [this query-dsl] "Executes a query which possibly has side-effects.")
  (query [this query-dsl] "Issues a (read-only) query to query engine."))
