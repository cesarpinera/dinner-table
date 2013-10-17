(ns dinner-table.core
  (:require [dinner-table.data :as data]
            [dinner-table.solver :as solver]
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
          velocity 0.001
          ]
      (println "Computing a solution for" i "in" seconds "seconds")
      (let [configuration (data/parse-data-file i)
            solution (solver/simulated-annealing configuration velocity seconds)]
        (output/output-solution solution o)))))

(comment
  (-main "resources/hw1-inst1.txt" "hw1-soln1.txt" 5)
  )
