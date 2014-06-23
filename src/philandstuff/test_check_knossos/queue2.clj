(ns philandstuff.test-check-knossos.queue2
  (:import [clojure.lang PersistentQueue IDeref IObj]))

(defn create-queue [] (atom PersistentQueue/EMPTY))

(defn enqueue [q item]
  (swap! q conj item)
  nil)

;; can also use trade! from flatland/useful
;; (but don't look at the implementation!)
(defn dequeue [q]
  (loop []
    (let [oldval @q]
      (if (compare-and-set! q oldval (pop oldval))
        (first oldval)
        (recur)))))
