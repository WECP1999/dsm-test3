package com.example.dsm104_test3.util

object PasswordValidator {

    fun validate(password: String): ValidationResult {
        if (password.length < 12)
            return ValidationResult.Error("error_length")
        if (!password.any { it.isUpperCase() })
            return ValidationResult.Error("error_uppercase")
        if (!password.any { it.isLowerCase() })
            return ValidationResult.Error("error_lowercase")
        if (!password.any { it.isDigit() })
            return ValidationResult.Error("error_number")
        if (!password.any { it in "!@#\$%^&*" })
            return ValidationResult.Error("error_special")
        return ValidationResult.Success
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val code: String) : ValidationResult()
    }
}
