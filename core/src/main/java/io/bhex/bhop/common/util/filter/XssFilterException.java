package io.bhex.bhop.common.util.filter;

public class XssFilterException extends RuntimeException {

    private String filed;

    public XssFilterException(String field) {
        super("");
        this.filed = field;
    }

    public String getField() {
        return this.filed;
    }
}
