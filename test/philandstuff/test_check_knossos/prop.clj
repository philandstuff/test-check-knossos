(ns philandstuff.test-check-knossos.prop
  (:require [clojure.test.check.generators :as gen]))

;; from ztellman/collection-check
(defn tuple* [& args]
  (->> args
    (map
      #(if (and (map? %) (contains? % :gen))
         %
         (gen/return %)))
    (apply gen/tuple)))

(defn always [n prop]
  (gen/fmap #(or (first (remove :result (drop-last %))) (last %))
            (apply gen/tuple (repeat n prop))))


