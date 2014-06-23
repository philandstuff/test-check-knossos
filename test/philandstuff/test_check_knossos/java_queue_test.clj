(ns philandstuff.test-check-knossos.java-queue-test
  (:require [philandstuff.test-check-knossos.java-queue :as jq]
            [philandstuff.test-check-knossos.model :refer (empty-queue-model)]
            [philandstuff.test-check-knossos.history :refer (sequential-history
                                                             recorded-parallel-history)]
            [philandstuff.test-check-knossos.prop :refer (always tuple*)]

            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer :all]
            [knossos.core :refer (analysis)]))

(def actions
  {:enqueue (fn [q val] (jq/enqueue q val) val)
   :dequeue jq/dequeue})

(def gen-queue-action
  (gen/one-of [(tuple* :dequeue)
               (tuple* :enqueue gen/int)]))

(defspec java-queue-should-fit-model-sequentially
  (prop/for-all [ops (gen/not-empty (gen/vector gen-queue-action))]
                (:valid? (analysis empty-queue-model
                                   (sequential-history
                                    (jq/create-queue)
                                    actions
                                    ops)))))

;; reasonably slow, so commented out for the moment
#_(defspec java-queue-should-have-linearizable-parallel-behaviour
  (always
   40
   (prop/for-all [ ;; knossos.core/analysis doesn't like empty histories
                  t1 (gen/not-empty (gen/vector gen-queue-action))
                  t2 (gen/not-empty (gen/vector gen-queue-action))]
                 (:valid? (analysis empty-queue-model
                                    (recorded-parallel-history
                                     (jq/create-queue)
                                     actions
                                     {:t1 t1 :t2 t2}))))))
