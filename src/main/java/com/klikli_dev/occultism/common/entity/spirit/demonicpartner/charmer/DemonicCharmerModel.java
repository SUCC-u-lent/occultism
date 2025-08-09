/*
 * MIT License
 *
 * Copyright 2020 klikli-dev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.klikli_dev.occultism.common.entity.spirit.demonicpartner.charmer;

import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedGeoModel;

import java.util.List;


public class DemonicCharmerModel extends DefaultedGeoModel<DemonicCharmer> {

    public DemonicCharmerModel() {
        super(DemonicCharmer.ID);
    }

    @Override
    protected String subtype() {
        return "entity";
    }

    @Override
    public void setCustomAnimations(DemonicCharmer animatable, long instanceId, AnimationState<DemonicCharmer> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        AnimationProcessor<DemonicCharmer> processor = getAnimationProcessor();

        // List of all variant bones you want to hide
        List<String> hairVariantBones = List.of(
                "HairVariant1",
                "HairVariant2"
        );
        List<String> hornVariantBones = List.of(
                "HornVariant1",
                "HornVariant2",
                "HornVariant3"
        );
        int hornVariant = animatable.getEntityData().get(DemonicCharmer.HORN_VARIANT);
        int hairVariant = animatable.getEntityData().get(DemonicCharmer.HAIR_VARIANT);

        for (int i = 0; i < hairVariantBones.size(); i++) {
            String variantName = hairVariantBones.get(i);
            GeoBone bone = processor.getBone(variantName);
            bone.setHidden(i!=hairVariant);
        }
        for (int i = 0; i < hornVariantBones.size(); i++) {
            String variantName = hornVariantBones.get(i);
            GeoBone bone = processor.getBone(variantName);
            bone.setHidden(i!=hornVariant);
        }
        float powerLevel = animatable.getEntityData().get(DemonicCharmer.POWER_LEVEL);

        if (powerLevel > -2) {
            float maxHeadScale = 3f;
            float minHeadScale = 1f;
            float baseScale = 0.25f;
            float scaleFactor;
            if (powerLevel <= -1) {
                scaleFactor = baseScale;
            } else {
                float normalizedPower = (powerLevel - (-1)) / (animatable.getMaxHealth() - (-1));
                normalizedPower = Math.clamp(normalizedPower, 0f, 1f);
                scaleFactor = baseScale + normalizedPower * (1f - baseScale);
            }

            GeoBone root = processor.getBone("_");
            if (root != null) {
                root.setScaleX(scaleFactor);
                root.setScaleY(scaleFactor);
                root.setScaleZ(scaleFactor);
                root.setPivotY(12);
            }

            GeoBone headBone = processor.getBone("head");
            if (headBone != null) {
                float headScale = maxHeadScale - ((scaleFactor - baseScale) * (maxHeadScale - minHeadScale) / (1f - baseScale));
                headBone.setScaleX(headScale);
                headBone.setScaleY(headScale);
                headBone.setScaleZ(headScale);
            }
        }
    }
}
