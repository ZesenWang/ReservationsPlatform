package com.example.utils;

import android.graphics.Color;

import java.util.ArrayList;

/**
 * Created by wangz on 2016/11/19.
 */

public class GradientColorMaker {
    int startR,startG,startB,startA;
    int endR,endG,endB,endA;
    int repeatCount;
    ArrayList<Integer> gradients;

    public void setStartColor(int startColor){
        startA = Color.alpha(startColor);
        startB = Color.blue(startColor);
        startG = Color.green(startColor);
        startR = Color.red(startColor);
    }
    public void setEndColor(int endColor){
        endA = Color.alpha(endColor);
        endB = Color.blue(endColor);
        endG = Color.green(endColor);
        endR = Color.red(endColor);
    }
    public void setRepeatCount(int repeatCount){
        this.repeatCount = repeatCount;
    }
    public Integer get(int i ){
        if(gradients == null)
            generate();
        return gradients.get(i);
    }
    public void generate(){
        gradients = new ArrayList<Integer>();
        gradients.add(Color.argb(startA,startR,startG,startB));

        int deltaA = Math.round((endA - startA)/repeatCount);
        int deltaR = Math.round((endR - startR)/repeatCount);
        int deltaB = Math.round((endB - startB)/repeatCount);
        int deltaG = Math.round((endG - startG)/repeatCount);

        for(int i = 0; i < repeatCount; i++){
            startA += deltaA;
            startR += deltaR;
            startG += deltaG;
            startB += deltaB;
            gradients.add(Color.argb(startA, startR, startG, startB));
        }
    }
}
