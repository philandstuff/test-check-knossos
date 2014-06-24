(ns philandstuff.test-check-knossos.queue2
  (:import [clojure.lang PersistentQueue IDeref IObj])
  (:refer-clojure :exclude [remove]))

(defn create-queue [] (atom PersistentQueue/EMPTY))

(defn add [q item]
  (swap! q conj item)
  nil)

;; can also use trade! from flatland/useful
;; (but don't look at the implementation!)
(defn remove [q]
  (loop []
    (let [oldval @q]
      (if (compare-and-set! q oldval (pop oldval))
        (first oldval)
        (recur)))))
