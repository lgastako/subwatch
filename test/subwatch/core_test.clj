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

(deftest test-removing-sub-section
  (testing "Removing watched section"
    (let [val {:a {:b {:c {:d 0}}}}
          test-atom (atom val)
          out-atom (atom nil)
          store-change (fn [k r o n]
                         (reset! out-atom [k r o n]))]
      (add-sub-watch test-atom ::test-removal [:a :b :c :d] store-change)
      (reset! test-atom {:a 5})
      (is (= @out-atom [::test-removal test-atom 0 nil])))))

(deftest test-readding
  (testing "Re-adding watched section"
    (let [val1 {:a {:b {:c {:d 0}}}}
          val2 {:a {:b {:c {:d 5}}}}
          test-atom (atom val1)
          out-atom (atom nil)
          store-change (fn [k r o n]
                         (reset! out-atom [k r o n]))]
      (add-sub-watch test-atom ::test-readd [:a :b :c :d] store-change)
      (reset! test-atom {:a 99})
      (reset! test-atom val2)
      (is (= @out-atom [::test-readd test-atom nil 5])))))
