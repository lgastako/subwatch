# subwatch

A Clojure library designed to make watching parts of a datastructure in an atom
easier.

I'm not sure if this whole nested atom thing is a good idea or not but I'm going
to find out.

## Usage

Require it in the REPL:

```clojure
(require '[subwatch.core :as subwatch])
```

Require it in your application:

```clojure
(ns my-app.core
  (:require [subwatch.core :as subwatch]))
```

Assuming the following atom:

```clojure
(def my-atom {:a {:b {:c {:d 4}
                      :e 5}}})
```

You can watch just the :d key for changes:

```clojure
(defn report-sum! [val]
    (println "Sum " (+ (get-in val [:a :b :c :d])
                       (get-in val [:a :b :c :e]))))

(add-sub-watch my-atom ::my-key [:a :b :c :d] report-sum!)

(swap! my-atom #(assoc-in % [:a :new-key] 5150))
;; Nothing is printed because it's outside the desired scope

(swap! my-atom #(assoc-in % [:a :b :c :d] 2112))
;; Prints "Sum 2117" because it is inside the desired scope.
```

And of course remove the sub-watch later:

```clojure
(remove-sub-watch my-atom ::my-key)
```

## License

This library is in the public domain.