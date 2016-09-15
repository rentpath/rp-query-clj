# rp.query

This library provides a specification of a Clojure map-based DSL for writing relational (plus extensions) queries, inspired by [Honey SQL](https://github.com/jkk/honeysql). Supports a SQL-like syntax, with extensions for supporting operators from Endeca.

This library provides no concrete implementations of this DSL that target specific data stores.

## Usage

Currently this library provides:

 * A full clojure.spec specification of the query DSL in `rp.query.dsl-spec`
 * A namespace with all legal operators stored in vars (for easier autocomplete) in `rp.query.dsl.operator`
 
At some point, the `rp.query.dsl` namespace might include helpers akin to Honey SQL's helper functions for building a query map.

## License

Copyright © 2016 RentPath, LLC

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
