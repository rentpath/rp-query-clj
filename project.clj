(defproject com.rentpath/rp-query-clj "0.1.4"
  :description "RentPath query abstractions in Clojure"
  :url "https://github.com/rentpath/rp-query-clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :scm {:url "git@github.com:rentpath/rp-query-clj.git"}
  :deploy-repositories [["releases" {:url "https://clojars.org/repo/"
                                     :username [:gpg :env/CLOJARS_USERNAME]
                                     :password [:gpg :env/CLOJARS_PASSWORD]
                                     :sign-releases false}]]
  :global-vars {*warn-on-reflection* true}
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.9.0"]]}})
