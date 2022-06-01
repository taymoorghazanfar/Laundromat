package com.laundromat.delivery.utils;

import java.util.regex.Pattern;

public class ValidationUtils {

    public static final String validInputs = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ";
    private static final String validNameInputs = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ ";
    private static final String validSpecialNumberInputs = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String validPlateNumberInputs = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-";
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

    public static boolean isPlateNumberValid(String plateNumber) {

        // check if all characters are in valid range
        if (!checkPlateNumberData(plateNumber)) {

            return false;
        }

        // if user entered more than 1 '-'
        int count = 0;
        for (int x = 0; x < plateNumber.length(); x++) {

            if (String.valueOf(plateNumber.charAt(x)).equals("-")) {

                count++;

                if (count > 1) {

                    return false;
                }
            }
        }

        // if user entered '-' at the beginning or the end
        if (String.valueOf(plateNumber.charAt(0)).equals("-")
                || String.valueOf(plateNumber.charAt(plateNumber.length() - 1)).equals("-")) {

            return false;
        }

        String[] data = plateNumber.split("-");

        String alphabets = data[0];
        String digits = data[1];

        // validate alphabets
        if (alphabets.length() != 3) {

            return false;
        }

        // check is no digit is entered in place of alphabet
        for (int x = 0; x < alphabets.length(); x++) {

            if (!Character.isLetter(alphabets.charAt(x))) {

                return false;
            }
        }

        // validate digits
        if (digits.length() < 3 || digits.length() > 4) {

            return false;
        }

        // check is no alphabet is entered in place of digit
        for (int x = 0; x < digits.length(); x++) {

            if (!Character.isDigit(digits.charAt(x))) {

                return false;
            }
        }

        return true;
    }

    private static boolean checkPlateNumberData(String data) {

        for (int x = 0; x < data.length(); x++) {

            boolean charValid = false;

            for (int y = 0; y < validPlateNumberInputs.length(); y++) {

                if (String.valueOf(data.charAt(x)).equals(String.valueOf(validPlateNumberInputs.charAt(y)))) {

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

    public static boolean areAllCharacters(String data) {

        // check is no digit is entered in place of alphabet
        for (int x = 0; x < data.length(); x++) {

            if (!Character.isLetter(data.charAt(x))) {

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
