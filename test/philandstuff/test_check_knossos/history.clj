(ns philandstuff.test-check-knossos.history
  (:require [knossos.op :as op]))

(defn sequential-history
  "Executes a series of actions in sequence against a unit under test,
  returning the recorded history

  (sequential-history stack {:push push-fn, :pop pop-fn}
                            [:push :push :pop])
"
  [machine actions test-case]
  (mapcat (fn [[action & args]]
            (let [value (apply (actions action) machine args)]
              [(op/invoke :solo action value)
               (op/ok     :solo action value)]))
          test-case))

(defn wrap-fn-record-history
  "TODO: Update this

Wraps a fn to record invocations and successful returns in a
  knossos history atom.  The atom should contain a vector, which will
  have new history items conjed onto it.  id is a keyword that
  represents this fn in the knossos history.

  The :invoke record is conjed before the function is called, but at
  that time the return value is not known and recorded as nil; once
  the fn returns, the :ok record is conjed, and the :invoke record is
  updated to indicate the actual value returned."
  [actions [op & args] history]
  (fn [me machine]
    (let [history-start (swap! history conj (op/invoke me op nil))
          invoke-index  (dec (count history-start))
          value         (apply (actions op) machine args)]
      (swap! history conj (op/ok me op value))
      (swap! history assoc-in [invoke-index :value] value))))

(defn recorded-parallel-history
  "Executes in parallel a number of series of actions against a unit
  under test, returning the recorded history.

  This function is nondeterministic; different executions can result
  in different interleavings of the parallel threads' execution.

  (recorded-parallel-history obj {:take take-fn, :put put-fn}
                                 {:t1 [:take :take] :t2 [:take :put]})"
  [machine actions test-case]
  (let [history   (atom [])
        processes (for [[process-id ops] test-case]
                    (fn []
                      (doseq [op (map #(wrap-fn-record-history actions % history) ops)]
                        (op process-id machine))))]
    (dorun (map deref (.invokeAll clojure.lang.Agent/soloExecutor processes)))
    @history))
