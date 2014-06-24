(ns philandstuff.test-check-knossos.queue0
  (:import [clojure.lang PersistentQueue])
  (:refer-clojure :exclude [remove]))

(defn create-queue [] (atom PersistentQueue/EMPTY))

(defn add [q item]
  (swap! q conj item)
  nil)

(defn remove [q]
  (let [[item rest] ((juxt first pop) @q)]
    (reset! q rest)
    item))
