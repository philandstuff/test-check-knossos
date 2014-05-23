(ns philandstuff.test-check-knossos.model
  (:require [knossos.core :refer (inconsistent)])
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
