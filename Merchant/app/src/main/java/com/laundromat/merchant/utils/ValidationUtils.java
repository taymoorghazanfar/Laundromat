package com.laundromat.merchant.utils;

import java.util.regex.Pattern;

public class ValidationUtils {

    public static final String validInputs = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ";
    private static final String validNameInputs = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ ";
    private static final String validSpecialNumberInputs = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String validPlateNumberInputs = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String validUsernameInputs = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-";
    private static final String validPasswordInputs = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789~#^|$%*!@/()-'\\\":;,?{}=!$^';,?×÷<>";
    private static final String validPhoneInputs = "0123456789";

    public static boolean isEmailValid(String email) {

        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches();
    }

    public static boolean isInputValid(String data) {

        for (int x = 0; x < data.length(); x++) {

            boolean charValid = false;

            for (int y = 0; y < validInputs.length(); y++) {

                if (String.valueOf(data.charAt(x)).equals(String.valueOf(validInputs.charAt(y)))) {

                    charValid = true;
                    break;
                }
            }

            if (!charValid) {

                return false;
            }
        }

        return true;
    }

    public static boolean isNameValid(String data) {

        for (int x = 0; x < data.length(); x++) {

            boolean charValid = false;

            for (int y = 0; y < validNameInputs.length(); y++) {

                if (String.valueOf(data.charAt(x)).equals(String.valueOf(validNameInputs.charAt(y)))) {

                    charValid = true;
                    break;
                }
            }

            if (!charValid) {

                return false;
            }
        }

        return true;
    }

    public static boolean isPasswordValid(String data) {

        if (data.length() < 6) {

            return false;
        }

        for (int x = 0; x < data.length(); x++) {

            boolean charValid = false;

            for (int y = 0; y < validPasswordInputs.length(); y++) {

                if (String.valueOf(data.charAt(x)).equals(String.valueOf(validPasswordInputs.charAt(y)))) {

                    charValid = true;
                    break;
                }
            }

            if (!charValid) {

                return false;
            }
        }

        return true;
    }

    public static boolean isSpecialNumberValid(String data) {

        for (int x = 0; x < data.length(); x++) {

            boolean charValid = false;

            for (int y = 0; y < validSpecialNumberInputs.length(); y++) {

                if (String.valueOf(data.charAt(x)).equals(String.valueOf(validSpecialNumberInputs.charAt(y)))) {

                    charValid = true;
                    break;
                }
            }

            if (!charValid) {

                return false;
            }
        }

        return true;
    }

    public static boolean isPhoneValid(String data) {

        for (int x = 0; x < data.length(); x++) {

            boolean charValid = false;

            for (int y = 0; y < validPhoneInputs.length(); y++) {

                if (String.valueOf(data.charAt(x)).equals(String.valueOf(validPhoneInputs.charAt(y)))) {

                    charValid = true;
                    break;
                }
            }

            if (!charValid) {

                return false;
            }
        }

        return true;
    }
}
