(ns clj-nehe.tutorial4
  (:use [penumbra opengl geometry]
        [penumbra.opengl.core :only [gl-import]])
  (:require [penumbra.app :as app]))

;; -----------------------------------------------------------------------------
;; Vars

(def *width* 640)

(def *height* 480)

(def *tri* [[1 0 0]     ; color, red
            [0 1 0]     ; vertex
            [0 1 0]     ; color, green
            [-1 -1 0]   ; vertex
            [0 0 1]     ; color, blue
            [1 -1 0]]   ; vertex
     )

(def *quad* [[-1 1 0]
             [1 1 0]
             [1 -1 0]
             [-1 -1 0]])

;; -----------------------------------------------------------------------------
;; Helpers

(defn color-and-vertex [[a b]]
  (apply color a)
  (apply vertex b))

;; -----------------------------------------------------------------------------
;; Import

(gl-import glClearDepth clear-depth)

;; -----------------------------------------------------------------------------
;; Fns

(defn init [state]
  (app/title! "Nehe Tutorial 4")
  (app/vsync! false)
  (app/display-mode! *width* *height*)
  (shade-model :smooth)
  (clear-color 0 0 0 0.5)
  (clear-depth 1)
  (enable :depth-test)
  (depth-test :lequal)
  (hint :perspective-correction-hint :nicest)
  (assoc state
    :rtri 0
    :rquad 0))

(defn reshape [[x y width height] state]
  (viewport 0 0 *width* *height*)
  (frustum-view 45 (/ (double *width*) *height*) 0.1 100)
  (load-identity)
  state)

(defn update [[delta time] state]
   (-> state
       (update-in [:rtri] #(+ % 0.2))
       (update-in [:rquad] #(+ % 0.15))))

(defn key-press [key state]
  (condp = key
    :f1 (let [state (update-in state [:fullscreen] #(not %))]
          (app/fullscreen! (:fullscreen state))
          state)
    state))

(defn display [[delta time] state]
  (translate -1.5 0 -6)
  (rotate (:rtri state) 0 1 0)
  (draw-triangles
   (doall
    (map color-and-vertex (partition 2 *tri*))))
  (load-identity)
  (translate -1.5 0 -6)
  (translate 3 0 0)
  (rotate (:rquad state) 1 0 0)
  (color 0.5 0.5 1)
  (draw-quads
   (doall (map #(apply vertex %) *quad*)))
  (app/repaint!))

(defn display-proxy [& args]
  (apply display args))

(def options {:reshape reshape
              :update update
              :key-press key-press
              :display display-proxy
              :init init})

(defn start []
  (app/start options {}))

(start)