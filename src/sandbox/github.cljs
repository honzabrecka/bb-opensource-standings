(ns sandbox.github
  (:require [sandbox.fetch :as fetch]
            [promesa.core :as p]))

(def token (.. js/process -env -TOKEN_GITHUB))

(def interesting-events
  #{["IssueCommentEvent"
     #(= (get % "action") "created")]
    ["IssuesEvent"
     #(= (get % "action") "opened")]
    ["PullRequestEvent"
     #(or (= (get % "action") "opened")
          (and (= (get % "action") "closed")
               (true? (get % "merged"))))]
    ["PullRequestReviewEvent"
     #(= (get % "action") "submitted")]
    ["PullRequestReviewCommentEvent"
     #(= (get % "action") "created")]
    ["PushEvent"
     (fn [_] true)]})

(def users
  #{"alcedoatthis"
    "atomn"
    "attilakiss323"
    "aurisak"
    "beretis"
    "Ceda"
    "danijelgrabez"
    "dnguyenba"
    "dualisimo"
    "geoRG77"
    "honzabrecka"
    "Hrachos"
    "imtoo"
    "incik"
    "jakubkottnauer"
    "Jamira40"
    "janmarsicek"
    "janprasil"
    "jchrtek"
    "kepi74"
    "lenskdav"
    "liskape"
    "lupuszr"
    "marinamarina"
    "matejlauko"
    "mbendik"
    "michalgritzbach"
    "michaljuris"
    "Mikosko"
    "milianriedel"
    "mmagnusek"
    "Nemanja3089"
    "omatejka"
    "ondrejbartas"
    "rodan-lewarx"
    "sljuka"
    "svains"
    "tomwagner"
    "TondaHack"
    "Xvie"
    "yavoo"})

(defn fetch-user-events
  [user]
  (letfn [($ [url events]
            (p/then
              (fetch/json {"url"     url
                           "headers" {"authorization" (str "token " token)}})
              (fn [{:keys [headers body]}]
                (let [events (concat events body)
                      [_ next] (re-find #"<(.+?)>; rel=\"next\"" (or (.get headers "link") ""))]
                  (if (nil? next)
                    events
                    ($ next events))))))]
    ($ (str "https://api.github.com/users/" user "/events/public") [])))

(defn fetch-users-events
  [users]
  (letfn [($ [[user & users] all-events]
            (p/then
              (fetch-user-events user)
              (fn [events]
                (let [all-events (concat all-events events)]
                  (if users
                    ($ users all-events)
                    all-events)))))]
    ($ users [])))

(defn filter-interesting-events
  [events]
  (into []
        (comp
          (filter (fn [item]
                    (= (subs (get item "created_at") 0 7)
                       "2017-09")))
          (filter (fn [item]
                    (some (fn [[type f]]
                            (and (= (get item "type") type)
                                 (f (get item "payload"))))
                          interesting-events)))
          (map (fn [item]
                 [(get-in item ["actor" "login"])
                  (get item "type")
                  (get-in item ["payload"])])))
        events))

(defn standings
  []
  (p/then
    (fetch-users-events users)
    (fn [events]
      (let [result (->> events
                        (filter-interesting-events)
                        (group-by first)
                        (map (fn [[user events]]
                               [user (count events)]))
                        (sort-by second)
                        (reverse))]
        (println result)))))
