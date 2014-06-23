(ns philandstuff.test-check-knossos.java-queue
  (:import [java.util.concurrent ConcurrentLinkedQueue]
           [java.util Queue]))

(set! *warn-on-reflection* true)

(defn create-queue [] (ConcurrentLinkedQueue.))

(defn enqueue [^Queue q item]
  (.add q item))

(defn dequeue [^Queue q]
  (.poll q))
