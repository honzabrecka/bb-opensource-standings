(ns sandbox.fetch
  (:require [promesa.core :as p]
            [goog.object :as o]
            [isomorphic-fetch]
            [composable-fetch :as cf]))

(def pipe-p cf/pipeP)

(def composable-fetch cf/composableFetch)

(def json
  (pipe-p
    clj->js
    (.withHeader composable-fetch "Content-Type" "application/json")
    (.withEncodedBody composable-fetch #(-> % clj->js js/JSON.stringify))
    (->> js/fetch
         (.fetch1 composable-fetch)
         (.retryable composable-fetch))
    (.withRetry composable-fetch)
    (.withSafe204 composable-fetch)
    (.-decodeResponse composable-fetch)
    (.-checkStatus composable-fetch)
    (fn [res] {:body    (js->clj (o/get res "data"))
               :headers (o/get res "headers")})))

(defn debug-invalid-status-code-error
  [e]
  (println (.-message e)
           (.. e -res -data)))
