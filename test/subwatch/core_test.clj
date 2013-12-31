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

(deftest test-multple-same
  (testing "Multiple sub watches on same atom"
    (let [val {:a {:b {:c {:d 0}}
                   :e {:f {:g 100}}}}
          test-atom (atom val)
          out-atom1 (atom nil)
          out-atom2 (atom nil)
          store-change1 (fn [k r o n]
                          (reset! out-atom1 [k r o n]))
          store-change2 (fn [k r o n]
                          (reset! out-atom2 [k r o n]))]
      (add-sub-watch test-atom ::test-multiple-1 [:a :b :c :d] store-change1)
      (is (= 1 (count (@sub-watches test-atom))))
      (add-sub-watch test-atom ::test-multiple-2 [:a :e :f :g] store-change2)
      (is (= 2 (count (@sub-watches test-atom))))
      (swap! test-atom #(assoc-in % [:a :b :c :d] 1))
      (swap! test-atom #(assoc-in % [:a :e :f :g] 101))
      (is (= @out-atom1 [::test-multiple-1 test-atom 0 1]))
      (is (= @out-atom2 [::test-multiple-2 test-atom 100 101])))))

(deftest test-last-remove-last
  (testing "Removal of last sub watch removes last watch"
    (let [val {:a {:b 1
                   :c 2}}
          test-atom (atom val)
          out-atom (atom 0)
          store-change (fn [k r o n] (swap! out-atom inc))
          starting-count (count @subwatch.core/sub-watches)]
      (add-sub-watch test-atom ::test-lrl-1 [:a :b] store-change)
      (add-sub-watch test-atom ::test-lrl-2 [:a :c] store-change)
      (swap! test-atom #(update-in % [:a :b] inc))
      (is (= 1 @out-atom))
      (swap! test-atom #(update-in % [:a :c] inc))
      (is (= 2 @out-atom))
      (remove-sub-watch test-atom ::test-lrl-1)
      (swap! test-atom #(update-in % [:a :c] inc))
      (is (= 3 @out-atom))
      (remove-sub-watch test-atom ::test-lrl-2)
      (is (= starting-count (count @subwatch.core/sub-watches))))))
