package com.example.jeff.swap;

/**
 * Created by jeff on 15-04-06.
 */
import android.widget.EditText;

public class FormValidation {

    // Regular Expression
    // you can change the expression based on your need
    private static final String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    // Error Messages
    private static final String REQUIRED_MSG = "required";
    private static final String EMAIL_MSG = "invalid email";

    public FormValidation(){}

    // call this method when you need to check email validation
//    public boolean isEmailAddress(EditText editText, boolean required) {
//        return isValid(editText, EMAIL_REGEX, EMAIL_MSG, required);
//    }

    // return true if the input field is valid, based on the parameter passed
//    public boolean isValid(EditText editText, String regex, String errMsg, boolean required) {
//
//        String text = editText.getText().toString().trim();
//        // clearing the error, if it was previously set by some other values
//        editText.setError(null);
//
//        // text required and editText is blank, so return false
//        if ( required && !hasText(editText) ) return false;
//
//        // pattern doesn't match so returning false
//        if (required && !Pattern.matches(regex, text)) {
//            editText.setError(errMsg);
//            return false;
//        };
//
//        return true;
//    }

    // check the input field has any text or not
    // return true if it contains text otherwise false
    public void hasText(EditText editText) {

        String text = editText.getText().toString().trim();

        // length 0 means there is no text
        if (text.length() == 0) {
            editText.setError(REQUIRED_MSG);
        }else{
            editText.setError(null);
        }
    }
}
