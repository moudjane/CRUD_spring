package com.moudjane.crud_spring.Util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        // Normalize the prefix if necessary
        if (hashedPassword.startsWith("$2y$")) {
            hashedPassword = hashedPassword.replaceFirst("^\\$2y\\$", "\\$2a\\$");
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
