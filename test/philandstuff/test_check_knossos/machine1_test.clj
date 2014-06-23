(ns philandstuff.test-check-knossos.machine1-test
  (:require [philandstuff.test-check-knossos.machine1 :as m1]
            [philandstuff.test-check-knossos.model :refer (->TicketMachineModel)]
            [philandstuff.test-check-knossos.history :refer (sequential-history
                                                             recorded-parallel-history)]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer :all]
            [knossos.core :refer (analysis)]
            [knossos.op :as op]))

(def actions
  {:take  m1/take-ticket
   :reset m1/reset-machine})

(def gen-queue-action
  (gen/elements [[:take] [:reset]]))

(defspec machine1-should-fit-model-sequentially
  (prop/for-all [ops (gen/not-empty (gen/vector gen-queue-action))]
                (:valid? (analysis (->TicketMachineModel 0)
                                   (sequential-history
                                    (m1/create-machine)
                                    actions
                                    ops)))))

(defn always [n prop]
  (gen/fmap #(or (first (remove :result (drop-last %))) (last %))
            (apply gen/tuple (repeat n prop))))

(defspec machine1-should-have-linearizable-parallel-behaviour
  (always
   40
   (prop/for-all [ ;; knossos.core/analysis doesn't like empty histories
                  t1 (gen/not-empty (gen/vector gen-queue-action))
                  t2 (gen/not-empty (gen/vector gen-queue-action))]
                 (:valid? (analysis (->TicketMachineModel 0)
                                    (recorded-parallel-history
                                     (m1/create-machine)
                                     actions
                                     {:t1 t1 :t2 t2}))))))
