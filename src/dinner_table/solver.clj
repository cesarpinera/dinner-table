(ns dinner-table.solver
  (:require [dinner-table.score :as score]
            [clojure.math.combinatorics :as combo]
            [clojure.pprint :refer [pprint]]))

(defn print-solution
  "Prints the solution in a human readable way"
  [solution]
  (let [s (score/score solution)
        c (count solution)
        c2 (/ c 2)]
    (println "Score:" s)
    (println (map #(:person %) (take c2 solution)))
    (println (map #(:person %) (drop c2 solution)))
    s))

(comment
  (require '[dinner-table.data :as data])
  (def d
    (data/parse-data-file "resources/hw1-inst1.txt"))
  (print-solution d)
)

(defn random-sitting
  "Generates a solution with random sitting assignment
   Returns an array with the sitting arrangements."
  [initial-configuration]
  (shuffle initial-configuration))

(comment

  (map #(select-keys % [:person]) (random-sitting d))
  ;; e.g.
  ;; ({:person 3}
  ;;  {:person 2}
  ;;  {:person 0}
  ;;  {:person 6}
  ;;  {:person 8}
  ;;  {:person 7}
  ;;  {:person 5}
  ;;  {:person 1}
  ;;  {:person 9}
  ;;  {:person 4})
  
  
  (score/score (random-sitting d))

)

(defn best-random
  "Selects the best random solution computed in a number of seconds"
  [initial-configuration seconds]
  (let [begin (System/currentTimeMillis)]
    ((fn [best-configuration begin miliseconds t]
       (if (> (- (System/currentTimeMillis) begin) miliseconds)
         best-configuration
         (let [new-configuration (random-sitting initial-configuration)
               new-score (score/score new-configuration)]
           (recur (if (> new-score (or (:score best-configuration) 0))
                    {:score new-score
                     :configuration new-configuration}
                    best-configuration)
                  begin miliseconds
                  (inc t)))))
     nil (System/currentTimeMillis) (* 1000 seconds) 0)))

(comment
  (:score (best-random d 1)) ; 82
  (:score (best-random d 5)) ; 85
  (:score (best-random d 15)) ; 86
  (:score (best-random d 30)) ; 86
  ;; Basically this means, if you can do better than 86 your algorithm is doing ok
  ;; But if you can't, then you're just wasting your time
  )

;; An empty seat
(def empty-seat {:person :empty
                 :preferences nil
                 :gender :none})

(defn- assign-first-available-seat
  "Returns a new configuration where the first empty seat will be assigned to person"
  [configuration person]
  ((fn [n]
     (if (>= n (count configuration))
       configuration
       (if (= (:person (nth configuration n)) :empty)
         (assoc configuration n person)
         (recur (inc n)))))
   0))

(comment
  (assign-first-available-seat [{:person 1}
                                {:person :empty}
                                {:person 3}]
                               {:person :new})
  ;; [{:person 1} {:person :new} {:person :2}]
  (assign-first-available-seat [{:person 1}
                                {:person 2}
                                {:person :empty}]
                               {:person :new})
  ;; [{:person 1} {:person :2} {:person :new}]
  (assign-first-available-seat [{:person 1}
                                {:person 2}
                                {:person 3}]
                               {:person :new})
  ;; [{:person 1} {:person 2} {:person 3}]
  )

(defn- complete-randomly
  "Assigns people in empty seats randomly"
  [unseated configuration]
  ((fn [unseated configuration]
     (if-not (seq unseated)
       configuration
       (let [[candidate & others] (shuffle unseated)]
         (recur others
                (assign-first-available-seat configuration candidate)))))
   unseated configuration))

(comment
  (complete-randomly [{:person :a} {:person :b}]
                     [{:person 1} empty-seat {:person 2}])
  ;; [{:person 1} {:person :b} {:person 2}]
  
  (complete-randomly [{:person :a} {:person :b}]
                     [{:person 1} empty-seat {:person 2} empty-seat])
  ;; [{:person 1} {:person :b} {:person 2} {:person :a}]
  )

(defn- empty-configuration
  [size]
  (into [] (repeat size empty-seat)))

(defn complete-search
  "Does a complete search of the search space, with a time limit"
  [initial-configuration]
  (let [all-combinations (combo/permutations initial-configuration)]
    (loop [remaining all-combinations
           next (first remaining)
           next-score (score/score (first remaining))
           best next
           best-score next-score]
      (if (nil? next)
        best
        (if (> next-score best-score)
          (recur (rest remaining)
                 (second remaining)
                 (score/score (second remaining))
                 next
                 next-score)
          (recur (rest remaining)
                 (second remaining)
                 (score/score (second remaining))
                 best
                 best-score))))))

(comment
  
  (let [d (data/parse-data-file "resources/hw1-inst1.txt")]
    (print-solution (complete-search d)))
  )


(defn haters
  "Sorts people by how much they hate (negative like) others. Haters go first"
  [configuration]
  (sort-by :hate
           (map (fn [person] (assoc person :hate (reduce + (vals (get person :preferences)))))
                configuration)))

(comment
  (haters d)
  ;; ({:person 1, :hate -33}
  ;;  {:person 4, :hate -30}
  ;;  {:person 5, :hate -19}
  ;;  {:person 7, :hate -10}
  ;;  {:person 2, :hate -1}
  ;;  {:person 8, :hate -1}
  ;;  {:person 3, :hate 0}
  ;;  {:person 0, :hate 4}
  ;;  {:person 6, :hate 11}
  ;;  {:person 9, :hate 25})
  )

(defn- haters-corners
  "Place the top four haters in the corners. Returns a map with :configuration and :unseated"
  [initial-configuration]
  (let [[h1 h2 h3 h4 & unseated] (haters initial-configuration)
        c (count initial-configuration)
        e (- (/ c 2) 2)]
    {:configuration (into [] (flatten [h1 (repeat e empty-seat) h2 h3 (repeat e empty-seat) h4]))
     :unseated unseated}))

(comment
  (haters-corners d))
  
(defn haters-first
  "Start by placing the top four haters in the corners.
   Then do a random assignment of whomever is left."
  [initial-configuration]
  (let [c (haters-corners initial-configuration)]
    ;; Complete the rest of the spaces in a random fashion
    (complete-randomly (:unseated c) (:configuration c))))

(comment

  (map #(:person %) (haters-first d))
  
  (score/score (haters-first d))
  (let [solution (haters-first d)]
    {:score (score/score solution)
     :seats (map #(:person %) solution)}
    )
  )


(defn best-haters-first
  "Selects the best haters-first solution computed in a number of seconds"
  [initial-configuration seconds]
  (let [begin (System/currentTimeMillis)]
    ((fn [best-configuration begin miliseconds t]
       (if (> (- (System/currentTimeMillis) begin) miliseconds)
         best-configuration
         (let [new-configuration (haters-first initial-configuration)
               new-score (score/score new-configuration)]
           (recur (if (> new-score (or (:score best-configuration) 0))
                    {:score new-score
                     :configuration new-configuration}
                    best-configuration)
                  begin miliseconds
                  (inc t)))))
     nil (System/currentTimeMillis) (* 1000 seconds) 0)))

(comment
  (:score (best-haters-first d 1)) ; 72
  (:score (best-haters-first d 5)) ; 72
  (:score (best-haters-first d 15)) ; 72
  ;; This algorithm sucks
  )

(defn greedy
  "Greedily fill the empty seats. Try all available candidates and choose the best"
  [initial-configuration]
  ((fn [unseated configuration]
     (if-not (seq unseated)
       configuration
       (let [branches (map #(let [solution (assign-first-available-seat configuration %)]
                                                  {:score (score/score solution)
                                                   :assigned %
                                                   :solution solution})
                                                unseated)
             best-solution (last (sort-by :score branches))]
         (recur (remove #(= (:assigned best-solution) %) unseated)
                (:solution best-solution)))))
   initial-configuration (into [] (repeat (count initial-configuration) empty-seat))))

(comment
  (score/score  (greedy d)) ; 59 yuck!
  )

(defn greedy-haters-first
  "Greedily fill the empty seats, but with the haters in the corners. Try all available candidates and choose the best"
  [initial-configuration]
  (let [h (haters-corners initial-configuration)]
    ((fn [unseated configuration]
       (if-not (seq unseated)
         configuration
         (let [branches (map #(let [solution (assign-first-available-seat configuration %)]
                                {:score (score/score solution)
                                 :assigned %
                                 :solution solution})
                             unseated)
               best-solution (last (sort-by :score branches))]
           ;; (println "Branches: " (map #(:score %) branches))
           ;; (println "Best solution: " (:score best-solution))

           (recur (remove #(= (:assigned best-solution) %) unseated)
                  (:solution best-solution)))))
     (:unseated h) (into [] (:configuration h)))))

(comment
  (score/score  (greedy-haters-first d)) ; 61
  (print-solution (greedy-haters-first d))

  ;; This algorithm is even worst and I think I hate everyone. 
  )

(defn- depth
  [unseated configuration]
  (if-not (seq unseated)
    (do (print-solution configuration)
      configuration)
    (map #(let [new-configuration (assign-first-available-seat configuration %)]
            {:configuration new-configuration
             :branches (depth (remove (fn [p] (= p %)) unseated) new-configuration)})
         unseated)))

(defn depth-first
  "The classic one"
  [initial-configuration]

  ;; Generate configurations for every permutation of unseated people in the empty spaces of the configuration
  (depth initial-configuration (into [] (repeat (count initial-configuration) empty-seat))))

(comment
  (count (depth-first d))
  (pprint
   (let [s (depth-first d)]
     ;; (-> s
     ;;     first
     ;;     :branches
     ;;     first
     ;;     :branches
     ;;     first
     ;;     :configuration)
     (take 1 s)
     ))

  ;; This being an exponential problem, it takes too much time just to compute the very first branch, and thus it is not viable.
  )

;; A*

(defn- is-goal
  "Determine if a given configuration can be considered a goal. Tests for :empty values in the :person key, or for an empty sequence"
  [configuration]
  (when-not (seq configuration) false)
  (loop [c configuration
         p (first c)]
    (if (nil? p)
      true
      (if (= :empty (:person p))
        false
        (recur (rest c)
               (second c))))))



(comment
  (is-goal [{:person 1} {:person :empty}]) ; false
  (is-goal [{:person 1} {:person 2}]) ; true
  (is-goal [{:person 1} {:person :empty} {:person 3}]) ; false
  (is-goal []) ; false
)

;; (defn- g-simple
;;   "The cost of the solution so far. Uses score for computation"
;;   [configuration]
;;   (score/score configuration))

;; (comment
;;   (g-simple []) ; 0
;; )

;; (defn- empty-count
;;   "How many of the seats are empty"
;;   [configuration]
;;   (loop [c configuration
;;          p (first c)
;;          result 0]
;;     (if (nil? p)
;;       result
;;       (recur (rest c) (second c) (if (= :empty (:person p)) (inc result) result)))))

;; (comment
;;   (empty-count nil) ; 0
;;   (empty-count [{:person 1}]) ; 0
;;   (empty-count [{:person :empty}]) ; 1
;;   (empty-count [{:person :empty} {:person 1} {:person :empty}]) ; 2
;;   )

;; (defn- h-simple
;;   "Assuming all the empty seats yield a +1"
;;   [configuration]
;;   (+ (empty-count configuration)
;;      (score/score configuration)))

;; (comment
;;   (g-simple d)
;;   (h-simple d)
;;   (is-goal d)
;;   )

;; (defn a*
;;   [initial-configuration]
  
;;   )


;; Simulated annealing
(defn- random-neighbour
  [configuration]
  ;; Swap a couple of people in a random fashion
  (let [c (count configuration)
        f (rand-int c)
        g (rand-int c)]
    (assoc configuration g (configuration f) f (configuration g))))

(comment
  (random-neighbour [1 2 3 4])
  )

(defn- temperature
  "Given the amount of miliseconds left, what should the temperature be?"
  [max begin ms]
  (let [elapsed (- ms begin)
        remaining (- max elapsed)]
    (if-not (zero? remaining)
      (/ 5.0 (Math/exp (/ max remaining)))
      0)))

(comment
  (let [max 1000
        begin (System/currentTimeMillis)
        r (rand-int max)]
    (Thread/sleep r)
    {:r r :temperature (temperature max begin (System/currentTimeMillis))}
    )
  (temperature 100 1381701455398 1381701455498) ;0

  )

(defn annealing-probability
  [current-energy new-energy t]
  (if (> new-energy current-energy)
    1.0
    (Math/exp (/ (- new-energy current-energy) t))))

(defn simulated-annealing
  [initial-configuration cooling max-seconds]
  (let [max (* 1000 max-seconds)
        begin (System/currentTimeMillis)
        start (random-sitting initial-configuration)]
    ((fn [t best-configuration best-score configuration configuration-score]
       (let [current-ms (System/currentTimeMillis)]
         (if (> (- current-ms begin) max)
           best-configuration
           ;; There is still some time left. Compute the remaining temperature 
           (let [neighbour (random-neighbour configuration)
                 neighbour-score (score/score neighbour)
                 r (rand)
                 ;t (temperature max begin current-ms)
                 t (* t (- 1 cooling))
                 p (annealing-probability configuration-score neighbour-score t)]
            ; (println "p" p "r" r "t" t "b" best-score "c" configuration-score "n" neighbour-score)
             (let [best-configuration (if (> p r) neighbour best-configuration)
                   best-score (if (> p r) neighbour-score best-score)
                   next-configuration (if (> p r) neighbour configuration)
                   next-score (if (> p r) neighbour-score configuration-score)]
               ;; (when-not (= configuration-score next-score)
               ;;   (println configuration-score next-score))
               (recur t best-configuration best-score next-configuration next-score))))))
     10000 start (score/score start) start (score/score start))))

(comment
  (score/score (simulated-annealing d 0.001 1000))
  (score/score (simulated-annealing (haters-first d) 0.001 1000))
  (score/score (simulated-annealing (haters-first d) 0.001 15000)) ;; 86, same as random

  (def d
    (data/parse-data-file "resources/hw1-inst2.txt"))
  (def d
    (data/parse-data-file "resources/hw1-inst3.txt"))
  
)

;; Test the different algorithms
(comment
  (let [max 15
        d (data/parse-data-file "resources/hw1-inst3.txt")]
    (println "Searching for solutions for a table with" (count d) "people, " max "seconds each")
    ;; (println "-----")
    ;; (println "Greedy, haters first")
    ;; (print-solution (greedy-haters-first d))
    ;; (println "-----")
    ;; (println "Random")
    ;; (print-solution (:configuration (best-random d max)))
    ;; (println "-----")
    ;; (println "Haters First + Random")
    ;; (print-solution (:configuration (best-haters-first d max)))
    ;; (println "-----")
    (println "Simulated Anneling")
    (print-solution (simulated-annealing d 0.001 max))
    (println "-----")
    (println "Simulated Anneling + Haters First")
    (print-solution (simulated-annealing (haters-first d) 0.001 max)))

  (let [max 15
        d (data/parse-data-file "resources/hw1-inst1.txt")]
    (println "Searching for solutions for a table with" (count d) "people, " max "seconds each")
    (println "-----")
    (println "Greedy, haters first")
    (print-solution (greedy-haters-first d))
    (println "-----")
    (println "Random")
    (print-solution (:configuration (best-random d max)))
    (println "-----")
    (println "Haters First + Random")
    (print-solution (:configuration (best-haters-first d max)))
    (println "-----")
    (println "Simulated Anneling")
    (print-solution (simulated-annealing d 0.001 max))
    (println "-----")
    (println "Simulated Anneling + Haters First")
    (print-solution (simulated-annealing (haters-first d) 0.001 max)))

  )
  


