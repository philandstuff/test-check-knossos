(ns philandstuff.test-check-knossos.machine1)

(defn create-machine []
  (atom 0))

(defn take-ticket [machine]
  (let [current-ticket @machine]
    (reset! machine (inc current-ticket))
    current-ticket))

(defn reset-machine [machine]
  (reset! machine 0))
