package com.m_w_k.synapse.api.connect;

public enum IDSetResult {
    SUCCESS, SUCCESS_UNCHANGED, FAIL_CHILD_CONFLICT, FAIL_SPECIAL_CODE, FAIL, NO_SET;

    public boolean success() {
        return this == SUCCESS || this == SUCCESS_UNCHANGED;
    }

    public boolean fail() {
        return this == FAIL || this == FAIL_CHILD_CONFLICT || this == FAIL_SPECIAL_CODE;
    }

    public String failTranslation() {
        return switch (this) {
            case FAIL_CHILD_CONFLICT -> "synapse.menu.id.fail_conflict";
            case FAIL_SPECIAL_CODE -> "synapse.menu.id.fail_code";
            case FAIL -> "synapse.menu.id.fail";
            default -> "";
        };
    }
}
