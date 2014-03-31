;;   Copyright 2014 Sonian, Inc.
;;
;;   Licensed under the Apache License, Version 2.0 (the "License");
;;   you may not use this file except in compliance with the License.
;;   You may obtain a copy of the License at
;;
;;       http://www.apache.org/licenses/LICENSE-2.0
;;
;;   Unless required by applicable law or agreed to in writing, software
;;   distributed under the License is distributed on an "AS IS" BASIS,
;;   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;;   See the License for the specific language governing permissions and
;;   limitations under the License.

(ns carousel.test.registry
  (:require [carousel.registry :refer :all]
            [clojure.test :refer :all]))

(defregistrar defa :a)

(defregistrar defb :b)

(defregistrar defc :c)

(defa ^:hi a1 "docstring"
  ([] :a1)
  ([_] :a1))

(defa a2 []
  :a2)

(defb b1 []
  :b1)

(defc c1 [a b c]
  [a b c])

(defc c2 [a b c]
  [1 2 3 a b c])

(deftest test-fns-are-normal
  ;; we have all the power of defn
  (is (= :a1 (a1)))
  (is (= :a1 (a1 'foo)))
  (is (:hi (meta #'a1)))
  (is (:doc (meta #'a1))))

(deftest test-fns-are-registered
  (is (= #{#'a1 #'a2} (set (registered :a))))
  (is (= #{#'b1} (set (registered :b)))))

(deftest test-registered-allows-filtering
  (is (= #{} (set (registered :a ['clojure.core]))))
  (is (= #{#'b1} (set (registered :b ['carousel.test.registry])))))

(deftest test-map-registered
  (is (= #{#'a1 #'a2} (set (map-registered identity :a))))
  (is (= #{#'b1} (set (map-registered identity :b ['carousel.test.registry]))))
  (is (= #{} (set (map-registered identity :a ['clojure.core])))))

(deftest test-do-registered
  (let [a (atom #{})]
    (do-registered (partial swap! a conj) :a)
    (is (= #{#'a1 #'a2} @a))
    (reset! a #{})
    (do-registered (partial swap! a conj) :b ['carousel.test.registry])
    (is (= #{#'b1} @a))
    (reset! a #{})
    (do-registered (partial swap! a conj) :a ['clojure.core])
    (is (= #{} @a))))

(deftest test-apply-registered
  (is (= [1 2 3] (apply-registered :c 'c1 1 2 [3])))
  (is (= [1 2 3] (apply-registered :c 'c1 [1 2 3])))
  (is (= [1 2 3 1 2 3] (apply-registered :c "c2" [1 2 3])))
  (is (thrown? IllegalArgumentException
               (apply-registered :c nil nil)))
  (is (thrown? IllegalArgumentException
               (apply-registered :c 'c3 ['doesnt 'exist]))))
