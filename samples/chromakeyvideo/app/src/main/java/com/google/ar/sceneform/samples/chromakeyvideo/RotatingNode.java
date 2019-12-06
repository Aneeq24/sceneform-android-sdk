package com.google.ar.sceneform.samples.chromakeyvideo;

import android.animation.ObjectAnimator;
import android.view.animation.LinearInterpolator;

import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.QuaternionEvaluator;
import com.google.ar.sceneform.math.Vector3;

public class RotatingNode extends Node {

    private ObjectAnimator orbitAnimation = null;

    private boolean clockwise = false;
    private float speedMultiplier;
    private float lastSpeedMultiplier = 1.0f;

    RotatingNode() {
    }

    void setSpeedMultiplier(float speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        super.onUpdate(frameTime);

        // Animation hasn't been set up.
        if (orbitAnimation == null) {
            return;
        }

        // Check if we need to change the speed of rotation.

        // Nothing has changed. Continue rotating at the same speed.
        if (lastSpeedMultiplier == speedMultiplier) {
            return;
        } else clockwise = lastSpeedMultiplier < speedMultiplier;


        if (speedMultiplier == 0.0f) {
            orbitAnimation.pause();
        } else {
            orbitAnimation.resume();

            float animatedFraction = orbitAnimation.getAnimatedFraction();
            orbitAnimation.setDuration(getAnimationDuration());
            orbitAnimation.setCurrentFraction(animatedFraction);
        }
        lastSpeedMultiplier = speedMultiplier;
    }

    @Override
    public void onActivate() {
        startAnimation();
    }

    @Override
    public void onDeactivate() {
        stopAnimation();
    }

    private long getAnimationDuration() {
        if (speedMultiplier < 0)
            speedMultiplier = speedMultiplier * -1;
        float degreesPerSecond = 90.0f;
        return (long) (1000 * 3600 / (degreesPerSecond * speedMultiplier));
    }

    private void startAnimation() {
        if (orbitAnimation != null) {
            return;
        }

        float axisTiltDeg = 0.0f;
        orbitAnimation = createAnimator(clockwise, axisTiltDeg);
        orbitAnimation.setTarget(this);
        orbitAnimation.setDuration(getAnimationDuration());
        orbitAnimation.start();
    }

    private void stopAnimation() {
        if (orbitAnimation == null) {
            return;
        }
        orbitAnimation.cancel();
        orbitAnimation = null;
    }

    /**
     * Returns an ObjectAnimator that makes this node rotate.
     */
    private static ObjectAnimator createAnimator(boolean clockwise, float axisTiltDeg) {
        // Node's setLocalRotation method accepts Quaternions as parameters.
        // First, set up orientations that will animate a circle.
        Quaternion[] orientations = new Quaternion[4];
        // Rotation to apply first, to tilt its axis.
        Quaternion baseOrientation = Quaternion.axisAngle(new Vector3(1.0f, 0f, 0.0f), axisTiltDeg);
        for (int i = 0; i < orientations.length; i++) {
            float angle = i * 360 / (orientations.length - 1);
            if (clockwise) {
                angle = 360 - angle;
            }
            Quaternion orientation = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), angle);
            orientations[i] = Quaternion.multiply(baseOrientation, orientation);
        }

        ObjectAnimator orbitAnimation = new ObjectAnimator();
        // Cast to Object[] to make sure the varargs overload is called.
        orbitAnimation.setObjectValues((Object[]) orientations);

        // Next, give it the localRotation property.
        orbitAnimation.setPropertyName("localRotation");

        // Use Sceneform's QuaternionEvaluator.
        orbitAnimation.setEvaluator(new QuaternionEvaluator());

        //  Allow orbitAnimation to repeat forever
        orbitAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        orbitAnimation.setRepeatMode(ObjectAnimator.RESTART);
        orbitAnimation.setInterpolator(new LinearInterpolator());
        orbitAnimation.setAutoCancel(true);

        return orbitAnimation;
    }
}
