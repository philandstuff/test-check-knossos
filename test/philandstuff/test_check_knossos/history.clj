(ns philandstuff.test-check-knossos.history
  (:require [knossos.op :as op]))

(defn sequential-history
  "Executes a series of actions in sequence against a unit under test,
  returning the recorded history

  (sequential-history stack {:push push-fn, :pop pop-fn}
                            [[:push 4] [:push 5] [:pop]])
"
  [target actions test-case]
  (mapcat (fn [[action & args]]
            (let [value (apply (actions action) target args)]
              [(op/invoke :solo action value)
               (op/ok     :solo action value)]))
          test-case))

(defn wrap-fn-record-history
  "Wraps a fn to record invocations and successful returns in a
  knossos history atom.  The atom should contain a vector, which will
  have new history items conjed onto it.  id is a keyword that
  represents this fn in the knossos history."
  [actions [op & args] history]
  (fn [me target]
    (swap! history conj (op/invoke me op nil))
    (let [value (apply (actions op) target args)]
      (swap! history conj (op/ok me op value)))))

(defn recorded-parallel-history
  "Executes in parallel a number of series of actions against a unit
  under test, returning the recorded history.

  This function is nondeterministic; different executions can result
  in different interleavings of the parallel threads' execution.

  (recorded-parallel-history obj {:take take-fn, :put put-fn}
                                 {:t1 [:take :take] :t2 [:take :put]})"
  [target actions test-case]
  (let [history   (atom [])
        processes (for [[process-id ops] test-case]
                    (fn []
                      (doseq [op (map #(wrap-fn-record-history actions % history) ops)]
                        (op process-id target))))]
    (dorun (map deref (.invokeAll clojure.lang.Agent/soloExecutor processes)))
    @history))
