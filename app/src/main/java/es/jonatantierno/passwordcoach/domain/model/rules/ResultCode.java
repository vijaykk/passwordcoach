package es.jonatantierno.passwordcoach.domain.model.rules;

public enum ResultCode {
    WEAK,
    TOO_SHORT,
    IN_DICTIONARY,
    WEAK_ACCORDING_TO_METER,
    CONTAINS_WORD_IN_DICTIONARY,
    STRONG,
    IN_PERSONAL_ATTACK_DICTIONARY,
    CONTAINS_DATE
}
