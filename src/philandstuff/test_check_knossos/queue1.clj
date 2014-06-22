(ns philandstuff.test-check-knossos.queue1
  (:import [clojure.lang PersistentQueue]))

(defn create-queue [] (atom PersistentQueue/EMPTY))

(defn enqueue [q item]
  (swap! q conj item)
  nil)

(defn dequeue [q]
  (let [item (first @q)]
    (swap! q pop)
    item))
