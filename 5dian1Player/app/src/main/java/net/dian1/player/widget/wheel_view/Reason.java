package net.dian1.player.widget.wheel_view;


/**
 * Created by Desmond on 2015/8/31.
 */
public class Reason implements KeyValue{
    public final static int TYPE_REJECT = 1;
    public final static int TYPE_REDELIVERY = 2;
    private String reasonKey;//reasonKey	拒收原因键值	String	true	拒收原因键值
    private String reason;//reason	拒收原因	String	true	拒收原因
    private int type;

    public Reason() {
    }

    public Reason(String reasonKey, String reason) {
        this.reasonKey = reasonKey;
        this.reason = reason;
    }

    public String getReasonKey() {
        return reasonKey;
    }

    public void setReasonKey(String reasonKey) {
        this.reasonKey = reasonKey;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String getKey() {
        return reasonKey;
    }

    @Override
    public String getValue() {
        return reason;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
