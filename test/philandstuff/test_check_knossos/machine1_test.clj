(ns philandstuff.test-check-knossos.machine1-test
  (:require [philandstuff.test-check-knossos.machine1 :as m1]
            [philandstuff.test-check-knossos.model :refer (->TicketMachine)]

            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer :all]
            [knossos.core :refer (analysis)]
            [knossos.op :as op]))

(defn sequential-history
  "Executes a series of actions in sequence against a unit under test,
  returning the recorded history"
  [machine actions test-case]
  (mapcat (fn [action]
            (let [value ((actions action) machine)]
              [(op/invoke :solo action value)
               (op/ok     :solo action value)]))
          test-case))

(defn wrap-fn-record-history
  "Wraps a fn to record invocations and successful returns in a
  knossos history atom.  The atom should contain a vector, which will
  have new history items conjed onto it.  id is a keyword that
  represents this fn in the knossos history.

  The :invoke record is conjed before the function is called, but at
  that time the return value is not known and recorded as nil; once
  the fn returns, the :ok record is conjed, and the :invoke record is
  updated to indicate the actual value returned."
  [id f history]
  (fn [me machine]
    (let [history-start (swap! history conj (op/invoke me id nil))
          invoke-index  (dec (count history-start))
          value         (f machine)]
      (swap! history conj (op/ok me :take value))
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
                      (doseq [op (map #(wrap-fn-record-history % (actions %) history) ops)]
                        (op process-id machine))))]
    (dorun (map deref (.invokeAll clojure.lang.Agent/soloExecutor processes)))
    @history))

(def actions
  {:take  m1/take-ticket
   :reset m1/reset-machine})

(defspec machine1-should-fit-model-sequentially
  (prop/for-all [ops (gen/not-empty (gen/vector (gen/elements [:take :reset])))]
                (:valid? (analysis (->TicketMachine 0)
                                   (sequential-history
                                    (m1/create-machine)
                                    actions
                                    ops)))))

(defspec machine1-should-have-linearizable-parallel-behaviour
  (prop/for-all [;; knossos.core/analysis doesn't like empty histories
                 t1 (gen/not-empty (gen/vector (gen/elements [:take :reset])))
                 t2 (gen/not-empty (gen/vector (gen/elements [:take :reset])))]
                (every? #(:valid? (analysis (->TicketMachine 0) %))
                          (take 40 (repeatedly #(recorded-parallel-history
                                                 (m1/create-machine)
                                                 actions
                                                 {:t1 t1 :t2 t2}))))))
