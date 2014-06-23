(ns philandstuff.test-check-knossos.queue0
  (:import [clojure.lang PersistentQueue]))

(defn create-queue [] (atom PersistentQueue/EMPTY))

(defn enqueue [q item]
  (swap! q conj item)
  nil)

(defn dequeue [q]
  (let [[item rest] ((juxt first pop) @q)]
    (reset! q rest)
    item))
