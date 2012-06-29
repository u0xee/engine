(ns engine.server.command
  (:require [engine.data.rope :as rope]
            [engine.data.cursor :as cursor]))

(defn command
  ([fn] {:command fn})
  ([fn & args] {:command fn :args (apply hash-map args)}))

(defn position-map [pos]
  (zipmap [:row :column] pos))

(defn rope-delta [before after]
  (->> [before after] (sort-by second) (map #(apply rope/translate %)) (map position-map)))

(defn broadcasted [obj]
  (vary-meta obj assoc :broadcast true))

(defn command-insert [before after buffer]
  (let [[_ pos1] before, [root pos2] after]
    [(broadcasted (command "insert-text" :position (position-map (rope/translate root pos1))
                           :text (str (rope/report root pos1 pos2))
                           :buffer buffer))]))

(defn command-delete-forward [before after buffer]
  (let [delta (->> [before after] (map (comp count first)) (apply -)),
        [root pos] before]
    [(broadcasted (command "delete-range" :range (rope-delta before [root (+ pos delta)])
                           :buffer buffer))]))

(defn command-delete-backward [before after buffer]
  [(broadcasted (command "delete-range" :range (rope-delta before after)
                         :buffer buffer))])

(defn command-exit [& _]
  [(command "exit")])

(defn insertfn [s] #(cursor/insert % s))
