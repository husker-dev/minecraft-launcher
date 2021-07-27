package com.husker.minecraft.launcher.tools.fx;

import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Effect;

public class EffectBlender {

    private final Blend effect;

    public EffectBlender(Effect... effects){
        effect = new Blend();
        effect.setMode(BlendMode.SRC_OVER);

        Blend current = effect;
        for(int i = 0; i < effects.length - 1; i++){
            current.setTopInput(effects[i]);

            if(i == effects.length - 1){
                current.setBottomInput(effects[i + 1]);
                break;
            }
            Blend newEffect = new Blend();
            newEffect.setMode(BlendMode.SRC_OVER);
            current.setBottomInput(newEffect);
            current = newEffect;
        }
    }

    public Effect getEffect(){
        return effect;
    }
}
