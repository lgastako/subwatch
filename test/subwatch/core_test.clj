(ns subwatch.core-test
  (:require [clojure.test :refer :all]
            [subwatch.core :refer :all]))

(deftest test-add-watch
  (testing "Adding a watch"
    (let [val1 {:a {:b {:c {:d 0}}}}
          val2 {:a {:b {:c {:d 1}}}}
          test-atom (atom val1)
          out-atom (atom nil)
          store-change (fn [k r o n]
                         (reset! out-atom [k r o n]))]
      (add-sub-watch test-atom ::test-add-watch [:a :b :c :d] store-change)
      (reset! test-atom val2)
      (is (= @out-atom [::test-add-watch test-atom 0 1])))))

(deftest test-remove-watch
  (testing "Remove a watch"
    (let [val1 {:a {:b {:c {:d 0}}}}
          val2 {:a {:b {:c {:d 1}}}}
          test-atom (atom val1)
          out-atom (atom nil)
          store-change (fn [k r o n]
                         (reset! out-atom [k r o n]))]
      (add-sub-watch test-atom ::test-remove-watch [:a :b :c :d] store-change)
      (remove-sub-watch test-atom ::test-remove-watch)
      (reset! test-atom val2)
      (is (= @out-atom nil)))))

(deftest test-unrelated-changes
  (testing "Sub-watches ignore out of scope changes"
    (let [val1 {:a {:b {:c {:d 0}}}}
          val2 {:a {:b {:c {:d 0}
                        :q "extra"}}}
          test-atom (atom val1)
          out-atom (atom nil)
          store-change (fn [k r o n]
                         (reset! out-atom [k r o n]))]
      (add-sub-watch test-atom ::test-unrelated [:a :b :c :d] store-change)
      (reset! test-atom val2)
      (is (= @out-atom nil)))))

