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

(ns carousel.test.module
  (:require [carousel.module :refer :all]
            [clojure.test :refer :all]
            [clojure.string :refer [trim]]
            [clojure.java.io :refer [reader]])
  (:import (java.io PrintStream)
           (java.net Socket)))

(def a (atom nil))

(definit autoexecdotbat []
  (reset! a :init))

(deftest test-init
  (reset! a nil)
  (init ['carousel.test.module])
  (is (= :init @a))
  (reset! a nil)
  (init)
  (is (= :init @a)))

(defadmin some-admin
  "just some docstring"
  []
  1)

(defadmin some-longer-admin
  "just some docstring too this requires a fair amount of documentation
  and stuff is not quite right. this is a complicated thing."
  []
  2)

(defadmin ping [pong]
  (println pong))

(deftest test-help
  (.contains (help) "some-admin        - just some docstring"))

(deftest test-admin-server
  (with-open [server (admin-server 17337)]
    (testing "admin server works in normal case"
      (let [msg "Howdy!"
            socket (Socket. "localhost" 17337)
            send (PrintStream. (.getOutputStream socket))
            receive (reader (.getInputStream socket))]
        (.println send (str "[\"ping\" \"" msg "\"]"))
        (is (= msg (.readLine receive)))))
    (testing "empty input is handled"
      (let [socket (Socket. "localhost" 17337)
            send (PrintStream. (.getOutputStream socket))
            receive (reader (.getInputStream socket))]
        (.println send "[]")
        (is (= "No function given" (trim (.readLine receive))))
        (.readLine receive)
        (is (= "Usage:" (trim (.readLine receive))))))
    (testing "non-existent functions are handled"
      (let [socket (Socket. "localhost" 17337)
            send (PrintStream. (.getOutputStream socket))
            receive (reader (.getInputStream socket))]
        (.println send "[\"foo\"]")
        (is (= "No such function foo registered for :admin"
               (trim (.readLine receive))))
        (.readLine receive)
        (is (= "Usage:" (trim (.readLine receive))))))))
