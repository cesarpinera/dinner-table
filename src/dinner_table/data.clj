(ns dinner-table.data
  (:require [clojure.string :as string :refer [split-lines split]]))

(defn- parse-person-string
  "Parses the sitting preferences of a single person contained in a string
   Returns map of the preferences where the person where (k,v) are (person, preference)
  "
  [s]
  (let [values (map read-string (split s #" "))]
    (zipmap (range (count values)) values)))

(comment
  (parse-person-string "0 -4 6 5 8 1 1 -6 2 -9")
  ;; {0 0, 1 -4, 2 6, 3 5, 4 8, 5 1, 6 1, 7 -6, 8 2, 9 -9}
  )

(defn- parse-data-string
  "Parses the starting configuration of the dinner table contained in a string.
   Returns a sequence containing the configuration map for each person
   with keys for :person :gender and :preferences"
  [s]
  (let [lines (split-lines s)
        total (read-string (first lines))]
    (map (fn [n]
           {:person n
            :gender (if (< n (/ total 2)) :female :male)
            :preferences (parse-person-string (nth lines (inc n)))})
         (range total))))

(comment
  (parse-data-string "2\n0 -4\n1 0")
  ;; ({:person 0, :gender :female, :preferences {1 -4, 0 0}}
  ;;  {:person 1, :gender :male, :preferences {1 0, 0 1}})
  )

(defn parse-data-file
  "Parses a data file"
  [filename]
  (parse-data-string (slurp filename)))

(comment
  (parse-data-file "resources/hw1-inst1.txt")
  (parse-data-file "resources/hw1-inst2.txt")
  (parse-data-file "resources/hw1-inst3.txt")
  )