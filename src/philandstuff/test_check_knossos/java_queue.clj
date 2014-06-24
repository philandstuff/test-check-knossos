(ns philandstuff.test-check-knossos.java-queue
  (:import [java.util.concurrent ConcurrentLinkedQueue]
           [java.util Queue])
  (:refer-clojure :exclude [remove]))

(set! *warn-on-reflection* true)

(defn create-queue [] (ConcurrentLinkedQueue.))

(defn add [^Queue q item]
  (.add q item))

(defn remove [^Queue q]
  (.poll q))
