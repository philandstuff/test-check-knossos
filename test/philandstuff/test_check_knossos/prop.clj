(ns philandstuff.test-check-knossos.prop
  (:require [clojure.test.check.generators :as gen]))

(defn always [n prop]
  (gen/fmap #(or (first (remove :result (drop-last %))) (last %))
            (apply gen/tuple (repeat n prop))))


