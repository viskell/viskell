/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lwbdemo.ui;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

class Knot extends Circle {
        int ignoredTouchId = -1;
        
        private static final double RADIUS = 15;
        private double anchorX;
        
        private final Bowtie bowtie;
        private final TermBlade termBlade;
        private final TypeBlade typeBlade;
        
        public Knot(Bowtie bowtie) {
            super(RADIUS);
            setFill(Color.RED);

            this.bowtie = bowtie;
            this.termBlade = bowtie.termBlade;
            this.typeBlade = bowtie.typeBlade;
            
            setOnMousePressed(event -> {
                if (event.isSynthesized()) return;
                handlePressed(event.getX());
                event.consume();
            });

            setOnMouseDragged(event -> {
                if (event.isSynthesized()) return;
                handleMoved(event.getSceneX());
                event.consume();
            });

            setOnMouseReleased(event -> {
                if (event.isSynthesized()) return;
                handleReleased();
                event.consume();
            });
            
            setOnTouchPressed(event -> {
                handlePressed(event.getTouchPoint().getX());
                if (event.getTouchPoint().getId() == ignoredTouchId) return;
                event.consume();
            });
            
            setOnTouchMoved(event -> {
                if (event.getTouchPoint().getId() == ignoredTouchId) return;
                handleMoved(event.getTouchPoint().getSceneX());
                event.consume();
            });
            
            setOnTouchReleased(event -> {
                if (event.getTouchPoint().getId() == ignoredTouchId) return;
                handleReleased();
                event.consume();
            });
        }
        
        private void handlePressed(double localX) {
            // Record location of click event in Knot
            anchorX = localX;
        }
        
        private void handleMoved(double sceneX) {
            Bounds thisBounds = this.localToScene(getBoundsInLocal());
            double thisCenterX = thisBounds.getMinX() + thisBounds.getWidth() / 2;

            double x = sceneX - anchorX;

            if (x < thisCenterX && termBlade.getChildren().size() > 1) {
                Node left = termBlade.getChildren().get(termBlade.getChildren().size() - 1);
                Bounds leftBounds = left.localToScene(left.getBoundsInLocal());
                double leftCenterX = leftBounds.getMinX() + leftBounds.getWidth() / 2;

                double delta = thisCenterX - leftCenterX;
                double progress = 1 - (x - leftCenterX) / delta;
                if (progress < 0) progress = 0;
                if (progress > 1) progress = 1;

                setScaleX(1 + progress * 0.3);
                setScaleY(1 - progress * 0.3);
                setTranslateX(-RADIUS * progress);

                if (x < leftCenterX) {
                    bowtie.coverHole();
                }
            }

            if (x > thisCenterX && typeBlade.getChildren().size() > 1) {
                Node right = typeBlade.getChildren().get(0);
                Bounds rightBounds = right.localToScene(right.getBoundsInLocal());
                double rightCenterX = rightBounds.getMinX() + rightBounds.getWidth() / 2;

                double delta = rightCenterX - thisCenterX;
                double progress = 1 - (rightCenterX - x) / delta;
                if (progress < 0) progress = 0;
                if (progress > 1) progress = 1;

                setScaleX(1 + progress * 0.3);
                setScaleY(1 - progress * 0.3);
                setTranslateX(RADIUS * progress);

                if (x > rightCenterX) {
                    bowtie.exposeHole();
                }
            }
        }
        
        private void handleReleased() {
            setScaleX(1);
            setScaleY(1);
            setTranslateX(0);
        }
    }