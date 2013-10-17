(ns dinner-table.output
  (:require [dinner-table.score :as score]
            [clojure.java.io :refer [writer]]))

(defn output-solution
  "Saves the solution to a file according to the output specified in the homework"
  [configuration filename]
  (with-open [w (writer filename)]
    (.write w (str (score/score configuration) "\n"))
    (doseq [i (range (count configuration))]
      (.write w (str (:person (nth configuration i)) " " i "\n") ))))


(comment
  (require '[dinner-table.data :as data])
  (let [d (data/parse-data-file "resources/hw1-inst1.txt")]
    (output-solution d "test-out.txt"))
)