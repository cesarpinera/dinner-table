(ns dinner-table.score)

(comment
  (require '[dinner-table.data :as data])
  (def d
    (data/parse-data-file "resources/hw1-inst1.txt"))
  (require '[dinner-table.solver :as solver])
)

(defn- left
  "Returns the left adjacent person to the person in position n
   If there's no one on the left it returns nil"
  [partial-solution n]
  (when (> n 0) (nth partial-solution (dec n))))

(defn- right
  "Returns the right adjacent person to the person in position n
   If there's no one on the right it returns nil"
  [partial-solution n]
  (when (< n (dec (count partial-solution))) (nth partial-solution (inc n))))

(comment
  (left '[a b c] 0) ; nil
  (left '[a b c] 2) ; b
  (right '[a b c] 0) ; b
  (right '[a b c] 2) ; nil
)

(defn- h
  "The preference function h"
  [p1 p2]
  (or (get (:preferences p1) (:person p2)) 0)
  )

(comment
  (let [p1 (nth d (rand-int (count d)))
        p2 (nth d (rand-int (count d)))]
    {:p1 (:person p1)
     :p2 (:person p2)
     :preference (h p1 p2)})
  ;; e.g.
  ;; {:p1 5, :p2 8, :preference 8}

  (h solver/empty-seat solver/empty-seat) ; 0
  
  )

(defn- adjacent-score
  "The integer score value of a person sitting adjacent to another"
  [p1 p2]
  (let [g1 (:gender p1)
        g2 (:gender p2)]
    (if (or (nil? g1)
            (nil? g2)
            (= :none g1)
            (= :none g2))
      0
      (if (= g1 g2) 0 1))))

(comment
  (adjacent-score {:gender :male} {:gender :male}) ; 0
  (adjacent-score {:gender :male} {:gender :female}) ; 1
  (adjacent-score {:gender :female} nil) ; 0
  )

(defn- opposite-score
  "The integer score value of a person sitting opposite to another"
  [p1 p2]
  (let [g1 (:gender p1)
        g2 (:gender p2)]
    (if (or (nil? g1)
            (nil? g2)
            (= :none g1)
            (= :none g2))
      0
      (if (= g1 g2) 0 2))))

(comment
  (opposite-score {:gender :male} {:gender :male}) ; 0
  (opposite-score {:gender :male} {:gender :female}) ; 2
  (opposite-score {:gender :male} nil) ; 0
  (opposite-score nil nil) ; 0
  (opposite-score {:gender :none} nil)            ; 0
  (opposite-score {:gender :none} {:gender :none})          ; 0
  )

(defn- adjacent-line
  "The score for people sitting adjacent to each other (gender based)"
  [partial-solution]
  ((fn [persons points]
     (if-not (seq persons)
       points
       (recur (rest persons)
              (+ (adjacent-score (first persons) (second persons))
                 points))))
   partial-solution 0))

(comment
  (adjacent-line (take (/ (count d) 2) (solver/random-sitting d))) ; e.g. 4
  )

(defn- opposite
  "The score for people sitting in front of each other.
   r1 - the first row
   r2 - the second row"
  [r1 r2]
  ((fn [r1 r2 points]
     (if-not (or (seq r1) (seq r2))
       points
       (recur (rest r1) (rest r2)
              (+ (opposite-score (first r1) (first r2))
                 points))))
   r1 r2 0))

(comment
  (let [r (solver/random-sitting d)
        r1 (take (/ (count r) 2) r)
        r2 (drop (/ (count r) 2) r)]
    {:r1 (map #(get % :gender) r1)
     :r2 (map #(get % :gender) r2)
     :opposite (opposite r1 r2)})
  ;; e.g. 
  ;; {:r1 (:female :female :male :male :female),
  ;;  :r2 (:female :female :male :male :male),
  ;;  :opposite 2}
)

(defn- preference-line
  "The score for people sitting adjacent to each other (preference based)"
  [partial-solution]
  ((fn [persons points]
     (if-not (seq persons)
       points
       (let [p1 (first persons)
             p2 (second persons)]
         (recur (rest persons)
                (+ (h p1 p2) (h p2 p1)
                   points)))))
   partial-solution 0))

(comment
  (let [c [(nth d 1)
           (nth d 9)
           (nth d 0)
           (nth d 6)
           (nth d 4)
           ]]
    (preference-line c)) ; -30

  (let [c [(nth d 5)
           (nth d 3)
           (nth d 8)
           (nth d 2)
           (nth d 7)
           ]]
    (preference-line c)) ; -22
  
  (let [r (solver/random-sitting d)
        r1 (take (/ (count r) 2) r)
        r2 (drop (/ (count r) 2) r)]
    {:r1 (map #(get % :person) r1)
     :r2 (map #(get % :person) r2)
     :preference-r1 (preference-line r1)
     :preference-r2 (preference-line r2)})
)

(defn- preference-opposite
  "The score for people sitting opposite to each other (preference based)"
  [r1 r2]
  ((fn [r1 r2 points]
     (if-not (and (seq r1) (seq r2))
       points
       (let [p1 (first r1)
             p2 (first r2)]
         (recur (rest r1)
                (rest r2)
                (+ (h p1 p2) (h p2 p1)
                   points)))))
   r1 r2 0))


(defn score
  "Compute the score of a solution"
  [solution]
  (let [r1 (take (/ (count solution) 2) solution)
        r2 (drop (/ (count solution) 2) solution)]
    (reduce + [(adjacent-line r1)
               (adjacent-line r2)
               (opposite r1 r2)
               (preference-line r1)
               (preference-line r2)
               (preference-opposite r1 r2)])))

(comment
  (score d)
  (score (solver/random-sitting d))

  (score [{:person :empty}]) ; 0
  (score [{:person 1
           :gender :male
           :preferences [0 0]}
          {:person :empty
           :gender :none
           :preferences [0 0]}])           ; 0


  (let [d1 [(nth d 0)
            (nth d 3)
            (nth d 7)
            (nth d 6)
            (nth d 1)
            (nth d 4)
            (nth d 8)
            (nth d 5)
            (nth d 9)
            (nth d 2)]]
    (score d1)
    )
  
  )