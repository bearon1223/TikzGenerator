package com.tikz.textinput;

import com.badlogic.gdx.Input;

public class InputListener implements Input.TextInputListener {
    @Override
    public void input(String text) {
        System.out.println(text);
    }

    @Override
    public void canceled() {

    }
}
