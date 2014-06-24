(ns philandstuff.test-check-knossos.queue1
  (:import [clojure.lang PersistentQueue])
  (:refer-clojure :exclude [remove]))

(defn create-queue [] (atom PersistentQueue/EMPTY))

(defn add [q item]
  (swap! q conj item)
  nil)

(defn remove [q]
  (let [item (first @q)]
    (swap! q pop)
    item))
