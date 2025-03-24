package com.example.personalexpensemanager.utility;

import android.widget.EditText;

public class InputHintRemover {
        /**
         * Removes hint on focus and restores it if input is empty.
         * @param editText The EditText to apply behavior to
         * @param hintText The hint to restore if empty
         */
        public static void setHintBehavior(EditText editText, String hintText) {
            editText.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    editText.setHint("");
                } else {
                    if (editText.getText().toString().isEmpty()) {
                        editText.setHint(hintText);
                    }
                }
            });
        }
}

