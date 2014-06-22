(ns philandstuff.test-check-knossos.queue1-test
  (:require [philandstuff.test-check-knossos.queue1 :as q1]
            [philandstuff.test-check-knossos.model :refer (empty-queue-model)]
            [philandstuff.test-check-knossos.history :refer (sequential-history
                                                             recorded-parallel-history)]
            [philandstuff.test-check-knossos.prop :refer (always)]

            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer :all]
            [knossos.core :refer (analysis)]))

;; from ztellman/collection-check
(defn- tuple* [& args]
  (->> args
    (map
      #(if (and (map? %) (contains? % :gen))
         %
         (gen/return %)))
    (apply gen/tuple)))

(def actions
  {:enqueue (fn [q val] (q1/enqueue q val) val)
   :dequeue q1/dequeue})

(def gen-queue-action
  (gen/one-of [(tuple* :dequeue)
               (tuple* :enqueue gen/int)]))

(defspec queue1-should-fit-model-sequentially
  (prop/for-all [ops (gen/not-empty (gen/vector gen-queue-action))]
                (:valid? (analysis empty-queue-model
                                   (sequential-history
                                    (q1/create-queue)
                                    actions
                                    ops)))))

(defspec queue1-should-have-linearizable-parallel-behaviour
  (always
   40
   (prop/for-all [ ;; knossos.core/analysis doesn't like empty histories
                  t1 (gen/not-empty (gen/vector gen-queue-action))
                  t2 (gen/not-empty (gen/vector gen-queue-action))]
                 (:valid? (analysis empty-queue-model
                                    (recorded-parallel-history
                                     (q1/create-queue)
                                     actions
                                     {:t1 t1 :t2 t2}))))))
