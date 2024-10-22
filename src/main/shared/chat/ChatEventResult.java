package dev._2lstudios.chatsentinel.shared.chat;

public class ChatEventResult {
    private String message;
    private boolean cancelled;
    private boolean hide;

    public ChatEventResult(String message, boolean cancelled, boolean hide) {
        this.message = message;
        this.cancelled = cancelled;
        this.hide = hide;
    }

    public ChatEventResult(String message, boolean cancelled) {
        this(message, cancelled, false);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isHide() {
        return hide;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }
}
