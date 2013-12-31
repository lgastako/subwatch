(ns subwatch.core)

(def sub-watches (atom {}))

(defn- fire-sub-watches
  [_key ref old new]
  (when-let [cfg (@sub-watches ref)]
    (doall (for [[key [keys f]] cfg]
             (let [old-data (get-in old keys)
                   new-data (get-in new keys)]
               (when (not= old-data new-data)
                 ;; TODO: Do we need a try/catch here?
                 (f key ref old-data new-data)))))))

(defn add-sub-watch
  "Like clojure.core/add-watch except that you must also specify a list of keys
   similar to clojure.core/get-in that specifies which part of the structure to
   watch.  The watch fn will only be fired when that portion of the
   data-structure has changed."
  [ref key keys f]
  (if (contains? @sub-watches ref)
    (swap! sub-watches #(update-in % [ref] (fn [m] (conj m [key [keys f]]))))
    (do (swap! sub-watches #(assoc % ref {key [keys f]}))
        (add-watch ref ::sub-watcher fire-sub-watches))))

(defn remove-sub-watch
  "This function is to add-sub-watch as clojure.core/remove-watch is to
  clojure.core/add-watch."
  [ref key]
  (swap! sub-watches (fn [old-watches]
                       (let [cfg (old-watches ref)]
                         (if (> (count cfg) 1)
                           (assoc old-watches ref (dissoc cfg key))
                           (dissoc old-watches ref))))))
