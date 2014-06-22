(ns philandstuff.test-check-knossos.model
  (:require [knossos.core :refer (inconsistent)])
  (:import  [knossos.core Model]))

(defrecord TicketMachineModel [next-ticket]
  Model
  (step [r op]
    (condp = (:f op)
      :reset (TicketMachineModel. 0)
      :take  (let [acquired-ticket (:value op)]
               (if-not (= acquired-ticket next-ticket)
                 (knossos.core/inconsistent
                  (str "Tried to take " acquired-ticket
                       " from machine offerring " next-ticket))
                 (TicketMachineModel. (inc next-ticket)))))))
