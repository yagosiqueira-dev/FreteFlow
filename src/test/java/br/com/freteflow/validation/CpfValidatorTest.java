package br.com.freteflow.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CpfValidatorTest {

    private final CpfValidator validator = new CpfValidator();

    @ParameterizedTest
    @ValueSource(strings = {"11144477735", "111.444.777-35"})
    void shouldAcceptValidCpf(String cpf) {
        assertThat(validator.isValid(cpf, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "11111111111",
            "12345678900",
            "00000000000",
            "123",
            "1114447773A"
    })
    void shouldRejectInvalidCpf(String cpf) {
        assertThat(validator.isValid(cpf, null)).isFalse();
    }

    @Test
    void shouldRejectNullCpf() {
        assertThat(validator.isValid(null, null)).isFalse();
    }
}
