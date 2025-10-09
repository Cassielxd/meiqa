package io.renren.crmchat.exception;

/**
 * CRM Chat 业务异常
 *
 * @author CRMChat Team
 */
public class CrmChatException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String msg;
    private int code = 400;

    public CrmChatException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public CrmChatException(String msg, int code) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    public CrmChatException(String msg, Throwable cause) {
        super(msg, cause);
        this.msg = msg;
    }

    public CrmChatException(String msg, int code, Throwable cause) {
        super(msg, cause);
        this.msg = msg;
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
