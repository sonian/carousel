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

(ns carousel.module
  (:require [clojure.edn]
            [clojure.string :refer [join]]
            [carousel.registry :refer :all]
            [server.socket :refer [create-server close-server]])
  (:import (java.io InputStreamReader OutputStream OutputStreamWriter
                    PrintWriter)
           (clojure.lang LineNumberingPushbackReader)))

(defregistrar definit :init)

(defregistrar defstart :start)

(defregistrar defstop :stop)

(defregistrar defstatus :status)

(defregistrar defadmin :admin)

(defn require-nses [nses]
  (when (seq nses)
    (apply require (for [ns nses]
                     (if (symbol? ns)
                       ns
                       (ns-name ns))))))

(defn init
  ([]
     (do-registered #(%) :init))
  ([nses]
     (require-nses nses)
     (do-registered #(%) :init nses)))

(defn start
  ([]
     (do-registered #(%) :start))
  ([nses]
     (do-registered #(%) :start nses)))

(defn stop
  ([]
     (do-registered #(%) :stop))
  ([nses]
     (do-registered #(%) :stop nses)))

(defn status
  ([]
     (do-registered #(%) :status))
  ([nses]
     (do-registered #(%) :status nses)))

(defn help [& [nses]]
  ;; use doric for this when it gets stinkin' multiline support
  (let [metas (map meta (apply registered :admin (when nses [nses])))
        names (map (comp str :name) metas)
        longest-name (last (sort-by count names))
        name-format (str "%-" (count longest-name) "s - ")
        line-space (apply str (repeat (inc (count longest-name)) " "))]
    (join "\n" (apply concat
                      (for [{:keys [name doc]} metas]
                        (let [[line & lines] (.split (or doc "") "\n")]
                          (cons (str (format name-format name) line)
                                (for [l lines]
                                  (str line-space l)))))))))

(defn admin-executor [strings]
  (let [[name & args] strings]
    (apply-registered :admin name args)))

(defn admin-handler [admin-executor]
  (fn [ins outs]
    (with-open [in (LineNumberingPushbackReader. (InputStreamReader. ins))
                out (OutputStreamWriter. outs)
                err (PrintWriter. ^OutputStream outs true)]
      (binding [*in* in
                *out* out
                *err* err]
        (try
          (admin-executor (clojure.edn/read))
          (catch IllegalArgumentException e
            (println (.getMessage e) "\n")
            (println "Usage:")
            (println (help)))
          (catch Throwable t
            (.printStackTrace t *err*)))))))

(defn admin-server
  ([port]
     (admin-server port (admin-handler admin-executor)))
  ([port handler]
     (let [server (create-server port handler)]
       (reify java.io.Closeable
         (close [this]
           (close-server server))))))
