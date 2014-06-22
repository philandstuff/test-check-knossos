(ns philandstuff.test-check-knossos.model
  (:require [knossos.core :refer (inconsistent)])
  (:import  [knossos.core Model]
            [clojure.lang PersistentQueue]))

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

(def initial-ticket-machine-model (TicketMachineModel. 0))

(defrecord QueueModel [items]
  Model
  (step [r op]
    (condp = (:f op)
      :enqueue (QueueModel. (conj items (:value op)))
      :dequeue (let [acquired-item (:value op)]
                 (if-not (= acquired-item (first items))
                   (inconsistent
                    (str "Tried to take " acquired-item
                         " from queue offering " (first items)))
                   (QueueModel. (pop items)))))))

(def empty-queue-model (QueueModel. PersistentQueue/EMPTY))
