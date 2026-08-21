package br.com.freteflow.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidator implements ConstraintValidator<ValidCPF, String> {

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {
        if (cpf == null) {
            return false;
        }

        String digitsOnly = cpf.replaceAll("[^0-9]", "");

        if (digitsOnly.length() != 11) {
            return false;
        }

        if (digitsOnly.chars().distinct().count() == 1) {
            return false;
        }

        return hasValidCheckDigits(digitsOnly);
    }

    private boolean hasValidCheckDigits(String cpf) {
        int firstCheckDigit = calculateCheckDigit(cpf.substring(0, 9), 10);
        int secondCheckDigit = calculateCheckDigit(cpf.substring(0, 9) + firstCheckDigit, 11);

        String expected = String.valueOf(firstCheckDigit) + secondCheckDigit;
        return cpf.substring(9).equals(expected);
    }

    private int calculateCheckDigit(String base, int initialWeight) {
        int sum = 0;
        int weight = initialWeight;

        for (char digit : base.toCharArray()) {
            sum += Character.getNumericValue(digit) * weight;
            weight--;
        }

        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
