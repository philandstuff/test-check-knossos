(ns philandstuff.test-check-knossos.machine1-test
  (:require [philandstuff.test-check-knossos.machine1 :refer :all]

            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer :all]
            [knossos.core :refer (linearizations)]
            [knossos.op :as op])
  (:import  [knossos.core Model]))

(defrecord TicketMachine [next-ticket]
  Model
  (step [r op]
    (condp = (:f op)
      :reset (->TicketMachine 0)
      :take  (let [acquired-ticket (:value op)]
               (if-not (= acquired-ticket next-ticket)
                 (knossos.core/inconsistent
                  (str "Tried to take " acquired-ticket
                       " from machine offerring " next-ticket))
                 (->TicketMachine (inc next-ticket)))))))

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

(defspec machine1-should-have-linearizable-parallel-behaviour
  (prop/for-all [t1 (gen/vector (gen/elements [:take :reset]))
                 t2 (gen/vector (gen/elements [:take :reset]))]
                (every? #(not-empty (linearizations (->TicketMachine 0) %))
                        (take 40 (repeatedly #(gen-one-history
                                               {:take take-ticket
                                                :reset reset-machine}
                                               {:t1 t1 :t2 t2}))))))
