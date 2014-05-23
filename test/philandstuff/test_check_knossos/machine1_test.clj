(ns philandstuff.test-check-knossos.machine1-test
  (:require [philandstuff.test-check-knossos.machine1 :refer :all]
            [philandstuff.test-check-knossos.model :refer (->TicketMachine)]

            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer :all]
            [knossos.core :refer (analysis)]
            [knossos.op :as op]))

(defn sequential-history [functions test-case]
  (let [machine (create-machine)]
    (mapcat (fn [action]
              (let [value ((functions action) machine)]
                [(op/invoke :solo action value)
                 (op/ok     :solo action value)]))
            test-case)))

(defn annotate [id f history]
  (fn [me machine]
    (let [history-start (swap! history conj (op/invoke me id nil))
          invoke-index  (dec (count history-start))
          value         (f machine)]
      (swap! history conj (op/ok me :take value))
      (swap! history assoc-in [invoke-index :value] value))))

(defn gen-one-history [functions test-case]
  (let [history   (atom [])
        machine   (create-machine)
        processes (for [[process-id ops] test-case]
                    (fn []
                      (doseq [op (map #(annotate % (functions %) history) ops)]
                        (op process-id machine))))]
    (dorun (map deref (.invokeAll clojure.lang.Agent/soloExecutor processes)))
    @history))

(defspec machine1-should-fit-model-sequentially
  (prop/for-all [ops (gen/not-empty (gen/vector (gen/elements [:take :reset])))]
                (:valid? (analysis (->TicketMachine 0)
                                   (sequential-history {:take  take-ticket
                                                        :reset reset-machine}
                                                       ops)))))

(defspec machine1-should-have-linearizable-parallel-behaviour
  (prop/for-all [;; knossos.core/analysis doesn't like empty histories
                 t1 (gen/not-empty (gen/vector (gen/elements [:take :reset])))
                 t2 (gen/not-empty (gen/vector (gen/elements [:take :reset])))]
                (every? #(:valid? (analysis (->TicketMachine 0) %))
                          (take 40 (repeatedly #(gen-one-history
                                                 {:take take-ticket
                                                  :reset reset-machine}
                                                 {:t1 t1 :t2 t2}))))))
