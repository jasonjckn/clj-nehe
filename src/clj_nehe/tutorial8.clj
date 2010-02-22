(ns clj-nehe.tutorial8
  (:use [penumbra opengl geometry]
        [penumbra.opengl.texture :only [gl-tex-coord-2]]
        [penumbra.opengl.core :only [gl-import]])
  (:use [clojure.contrib.duck-streams :only [pwd]])
  (:require [penumbra.app :as app])
  (:import [javax.imageio ImageIO]
           [java.io File]))

;; -----------------------------------------------------------------------------
;; Vars

(def *image* (ImageIO/read (File. (pwd) "/src/clj_nehe/Glass.bmp")))
(def *width* 640)
(def *height* 480)
(def *light-ambient* [0.5 0.5 0.5 1])
(def *light-diffuse* [1 1 1 1])
(def *light-position* [0 0 2 1])

(def *cube*
     [
      ; front face
      [0 0 1]           ; normal pointing towards viewer
      [0 0] [-1 -1 1]   ; bottom left of the texture and quad
      [1 0] [1 -1 1]    ; bottom right of the texture and quad
      [1 1] [1 1 1]     ; top right of the texture and quad
      [0 1] [-1 1 1]    ; top left of the texture and quad

      ; back face
      [0 0 -1]          ; normal pointing away from viewer
      [1 0] [-1 -1 -1]  ; bottom right of the texture and quad
      [1 1] [-1 1 -1]   ; top right of the texture and quad
      [0 1] [1 1 -1]    ; top left of the texture and quad
      [0 0] [1 -1 -1]   ; bottom left of the texture and quad

      ; top face
      [0 1 0]           ; normal pointing up
      [0 1] [-1 1 -1]   ; top left of the texture and quad
      [0 0] [-1 1 1]    ; bottom left of the texture and quad
      [1 0] [1 1 1]     ; bottom right of the texture and quad
      [1 1] [1 1 -1]    ; top right of the texture and quad

      ; bottom face
      [0 -1 0]          ; normal pointing down
      [1 1] [-1 -1 -1]  ; top right of the texture and quad
      [0 1] [1 -1 -1]   ; top left of the texture and quad
      [0 0] [1 -1 1]    ; bottom left of the texture and quad
      [1 0] [-1 -1 1]   ; bottom right of the texture and quad

      ; right face
      [1 0 0]           ; normal pointing right
      [1 0] [1 -1 -1]   ; bottom right of the texture and quad
      [1 1] [1 1 -1]    ; top right of the texture and quad
      [0 1] [1 1 1]     ; top left of the texture and quad
      [0 0] [1 -1 1]    ; bottom left of the texture and quad

      ; left face
      [-1 0 0]          ; normal pointing left
      [0 0] [-1 -1 -1]  ; bottom left of the texture and quad
      [1 0] [-1 -1 1]   ; bottom right of the texture and quad
      [1 1] [-1 1 1]    ; top right of the texture and quad
      [0 1] [-1 1 -1]   ; top left of the texture and quad
      ])

;; -----------------------------------------------------------------------------
;; Helpers

(defmacro series [& args]
  (let [syms (take (count args) (repeatedly gensym))
        forms (map #(cons 'apply %) (partition 2 (interleave args syms)))]
   `(fn [[~@syms]]
      ~@forms)))

(defn tex-coord [x y]
  (gl-tex-coord-2 x y))

(def normal-and-4tex-coord-and-vertices
     (series normal
             tex-coord vertex
             tex-coord vertex
             tex-coord vertex
             tex-coord vertex))

;; -----------------------------------------------------------------------------
;; Import

(gl-import glClearDepth clear-depth)

;; -----------------------------------------------------------------------------
;; Fns

(defn init [state]
  (app/title! "Nehe Tutorial 8")
  (app/vsync! false)
  (app/display-mode! *width* *height*)
  (enable :texture-2d)
  (shade-model :smooth)
  (clear-color 0 0 0 0.5)
  (clear-depth 1)
  (enable :blend)
  (depth-test :lequal)
  (hint :perspective-correction-hint :nicest)
  (light 1
         :ambient *light-ambient*
         :diffuse *light-diffuse*
         :position *light-position*)
  (enable :light1)
  (color 1 1 1 5)
  (blend-func :src-alpha :one)
  (merge state
         {:light false
          :blend true
          :xrot 0
          :yrot 0
          :xspeed 0
          :yspeed 0
          :z -5
          :filter 0
          :texs [(load-texture-from-image *image* false :nearest)
                 (load-texture-from-image *image* false :linear)
                 (load-texture-from-image *image* true)]}))

(defn reshape [[x y width height] state]
  (viewport 0 0 *width* *height*)
  (frustum-view 45 (/ (double *width*) *height*) 0.1 100)
  (load-identity)
  state)

(defn update [[delta time] state]
   (let [state (if (state :zoom-in)
                 (update-in state [:z] #(+ % 0.002))
                 state)
         state (if (state :zoom-out)
                 (update-in state [:z] #(- % 0.002))
                 state)]
    (-> state
        (update-in [:xrot] #(+ % (:xspeed state)))
        (update-in [:yrot] #(+ % (:yspeed state))))))

(defn key-press [key state]
  (condp = key
    "l"    (if (:light state)
             (do
               (disable :lighting)
               (assoc state :light false))
             (do
               (enable :lighting)
               (assoc state :light true)))
    "b"    (if (:blend state)
             (do
               (disable :blend)
               (enable :depth-test)
               (assoc state :blend false))
             (do
               (enable :blend)
               (disable :depth-test)
               (assoc state :blend true)))
    "w"    (assoc state :zoom-in true)
    "s"    (assoc state :zoom-out true)
    "f"    (update-in state [:filter] #(mod (inc %) 3))
    :up    (update-in state [:xspeed] #(- % 0.1))
    :down  (update-in state [:xspeed] #(+ % 0.1))
    :left  (update-in state [:yspeed] #(- % 0.1))
    :right (update-in state [:yspeed] #(+ % 0.1))
    state))

(defn key-release [key state]
  (condp = key
    "w" (dissoc state :zoom-in)
    "s" (dissoc state :zoom-out)
    state))

(defn display [[delta time] state]
  (translate 0 0 (:z state))
  (rotate (:xrot state) 1 0 0)
  (rotate (:yrot state) 0 1 0)
  (with-texture ((:texs state) (:filter state))
    (draw-quads
     (doall
      (map normal-and-4tex-coord-and-vertices (partition 9 *cube*)))))
  (app/repaint!))

(defn display-proxy [& args]
  (apply display args))

(def options {:reshape reshape
              :update update
              :key-press key-press
              :key-release key-release
              :display display-proxy
              :init init})

(defn start []
  (app/start options {}))

(start)