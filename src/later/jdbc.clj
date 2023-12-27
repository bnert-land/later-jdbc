(ns later.jdbc
  (:require
    [next.jdbc :as prelude]
    [next.jdbc.transaction :as tx-prelude]
    [honey.sql :as sql]
    [manifold.deferred :as d]
    [virtual.core :refer [virtual]]))

(set! *warn-on-reflection* true)

(defn inspect [query-spec]
  (first (sql/format query-spec {:pretty true, :inline true})))

(defn q 
  ([db query-spec]
   (q db query-spec {}))
  ([db query-spec opts]
   (prelude/execute! db (sql/format query-spec) opts)))

(defn q&
  ([db query-spec]
   (q& db query-spec {}))
  ([db query-spec opts]
   (let [result (d/deferred)]
     ; Not going to worry about the 
     (virtual
       (try
         (let [result* (q db (sql/format query-spec) opts)]
           (d/success! result result*))
         (catch Exception e
           (d/error! result e))))
     result)))

(defmacro tx [[sym transactable opts] & body]
  `(binding [tx-prelude/*nested-tx* :prohibit]
     (prelude/with-transaction [~sym ~transactable ~opts]
       ~@body)))

(defmacro tx& [[sym transactable opts] & body]
  ; nested transactions using `(locking ...)` which may trigger
  ; the virtual thread not parking on I/O, given that thread coordination
  ; is implmented (as far as I know) via Object monitors.
  ;
  ; Therefore, we need to disallow nested transactions by default
  `(binding [tx-prelude/*nested-tx* :prohibit]
     (let [result# (d/deferred)]
        (virtual
          (try
            (d/success!
              result#
              (prelude/with-transaction [~sym ~transactable ~opts]
                ~@body))
            (catch Exception e#
              (d/error! result# e#))))
        result#)))

