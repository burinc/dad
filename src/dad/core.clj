(ns dad.core
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [dad.config :as d.config]
            [dad.logger :as d.log]
            [dad.os :as d.os]
            [dad.reader :as d.reader]
            [dad.runner :as d.runner]))

(def ^:private cli-options
  [["-s" "--silent"]
   [nil "--debug"]
   ["-e" "--eval CODE"]
   ["-n" "--dry-run"]
   [nil "--no-color"]
   ["-h" "--help"]
   ["-v" "--version"]])

(defn- print-version []
  (let [ver (-> "version.txt" io/resource slurp str/trim)]
    (println (str "dad ver " ver))
    (println (str "* Detected OS: " (name (d.os/os-type))))))

(defn- usage [summary]
  (print-version)
  (println (str "* Usage:\n" summary)))

(defn- fetch-codes [arguments options]
  (let [codes (some->> (seq arguments)
                       (map slurp)
                       (str/join "\n"))
        codes (if-let [eval-code (:eval options)]
                (str eval-code " " codes)
                codes)]
    (if codes
      codes
      (->> *in*
           io/reader
           line-seq
           (str/join "\n")))))

(defn -main [& args]
  (let [{:keys [arguments options summary errors]} (cli/parse-opts args cli-options)
        {:keys [debug dry-run no-color help silent version]} options
        config (d.config/read-config)
        log-level (cond
                    silent :silent
                    debug :debug
                    :else :info)
        runner-fn (if dry-run
                    d.runner/dry-run-tasks
                    d.runner/run-tasks)]
    (cond
      errors (do (doseq [e errors] (println e))
                 (usage summary)
                 (System/exit 1))
      help (usage summary)
      version (print-version)
      :else
      (binding [d.log/*level* log-level
                d.log/*color* (not no-color)]
        (try
          (some->> (fetch-codes arguments options)
                   (d.reader/read-tasks config)
                   (runner-fn config))
          (catch Exception ex
            (d.log/error (.getMessage ex) (ex-data ex))
            (System/exit 1)))))
    (System/exit 0)))