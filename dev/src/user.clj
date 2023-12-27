(ns user
  (:require
    [clojure.tools.namespace.repl :as repl]
    [next.jdbc :as nxt]))

(repl/disable-unload!)
(repl/disable-reload!)

(require '[manifold.deferred :as d])

(comment
  (repl/refresh)
  (require 'later.jdbc)

  (def db
    (nxt/get-datasource
      {:dbtype   "postgres"
       :dbname   "postgres"
       :host     "localhost"
       :user     "postgres"
       :password "postgres"}))

  (def bad-db
    (nxt/get-datasource
      {:dbtype   "postgres"
       :dbname   "postgres"
       :host     "localhost"
       :user     "postgres"
       :password (reduce str (reverse "postgres"))}))

  (def create-tweets-table
    '{create-table (tweets if-not-exists)
      with-columns
      ((tweeter_id bigint not-null)
       (tweet text not-null)
       ((constraint tweet_max_length) (check (< (length tweet) 140)))
       ((constraint tweet_min_length) (check (<= 4 (length tweet)))))})

  (-> (later.jdbc/inspect create-tweets-table)
      (println))

  (later.jdbc/q db create-tweets-table)
  (later.jdbc/q db '{insert-into (tweets :tweeter_id :tweet)
                     returning   *
                     values
                     ((0, "from jack")
                      (0, "hi @elon")
                      (2, "go away @jack")
                      (2, "@jack, i'd buy X from u tho...")
                      (0, "@elon X what...?"))})

  (later.jdbc/q db '{select * from tweets})

  (deref
    (d/catch
      (d/chain
        (later.jdbc/q& bad-db '{select * from tweets})
        #(mapv
           (fn [m]
             (reduce-kv
               (fn [m' k v]
                 (assoc m' (keyword (name k)) v))
               {}
               m))
           %))
      (fn [e]
        (println "error>" e))))

  (repl/refresh)
  (deref
    (later.jdbc/tx& [t db]
      (later.jdbc/q t '{insert-into (tweets id msg)
                        values
                        ((10, "random tweet"))})
      ; will throw
      #_@(later.jdbc/tx& [tt db]
        (println "NESTED>" txn/*nested-tx*)
        (later.jdbc/q tt '{insert-into (tweets id msg)
                           values
                           ((100, "im dangerous"))}))

      ; this'll throw also
      #_(later.jdbc/tx [tt db]
        (println "NESTED>" txn/*nested-tx*)
        (later.jdbc/q tt '{insert-into (tweets id msg)
                           values
                           ((101, "im dangerous"))}))

      #_(throw (ex-info "error" {}))
      (later.jdbc/q t '{insert-into (tweets id msg)
                        values
                        ((11, "other tweet"))})

      (later.jdbc/q t '{select * from tweets}
                    {:limit 3})))
)
