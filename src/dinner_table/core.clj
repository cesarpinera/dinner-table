(ns dinner-table.core
  (:require [dinner-table.data :as data]
            [dinner-table.solver :as solver]
            [dinner-table.score :as score]
            [dinner-table.output :as output])
  (:gen-class))

(defn -main
  "Read the filename in the first argument and produce the output of the best solution. found in the number of seconds specified in the second parameter.
   Currently using simmulated-anneling"
  [& args]
  (if (<(count args) 3)
    (println "Usage: <problem-file> <output-file> <seconds>")
    (let [i (first args)
          o (second args)
          seconds (read-string (nth args 2))
          velocity 0.0001
          ]
      (println "Computing a solution for" i "in" seconds "seconds")
      (let [configuration (data/parse-data-file i)
            sa (future (solver/simulated-annealing configuration velocity seconds))
            r (future (solver/best-random configuration seconds))
            best (if (> (score/score @sa)
                        (:score @r))
                   @sa
                   (:configuration @r))]
        (solver/print-solution best)
        (output/output-solution best o))
      (System/exit 0))))

(comment
  (-main "resources/hw1-inst1.txt" "hw1-soln1.txt" "5")
  )
