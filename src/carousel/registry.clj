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

(ns carousel.registry)

(def registry (atom {}))

(defn register [type fn-var]
  (swap! registry update-in [type] (fnil conj #{}) fn-var))

(defn var-name [var]
  (let [m (meta var)]
    [(.name (:ns (meta var)))
     (:name (meta var))]))

(defn registered
  ([type]
     ;; return all the fns registered as a given type
     (sort-by var-name (@registry type)))
  ([type nses]
     ;; return all the fns registered as a given type, as long as
     ;; each fn's namespace is in nses, return them in the order given
     ;; by nses
     (let [fns-by-ns (group-by (comp :ns meta) (registered type))]
       (->> (for [ns nses]
              (sort-by var-name (fns-by-ns (the-ns ns))))
            (apply concat)))))

(defmacro defregistrar [name type]
  `(do
     (defmacro ~name
       [name# & fn-tail#]
       `(do
          (defn ~name#
            ~@fn-tail#)
          (register ~~type (var ~name#))))
     ;; jack the arglist for defn onto this, because that's what it
     ;; actually is
     (alter-meta! (var ~name) assoc :arglists (:arglists (meta #'defn)))
     (var ~name)))

(defn map-registered [f type & [nses]]
  (for [fn (apply registered type (when nses [nses]))]
    (f fn)))

(defn do-registered [f type & [nses]]
  (doseq [fn (apply registered type (when nses [nses]))]
    (f fn)))

(defn apply-registered
  "allows you to invoke a registered function by name, but (of course)
  requires that your registered functions have unique names"
  [type fn-name & args]
  (if fn-name
    (if-let [fn (first (filter #(= (symbol fn-name)
                                   (:name (meta %)))
                               (registered type)))]
      (apply apply fn args)
      (throw (IllegalArgumentException.
              (str "No such function " fn-name " registered for " type))))
    (throw (IllegalArgumentException. "No function given"))))
