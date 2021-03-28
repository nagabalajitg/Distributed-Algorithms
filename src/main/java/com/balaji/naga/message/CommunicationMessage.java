package com.balaji.naga.message;

public interface CommunicationMessage<T> {
    public T getMessage();
    public void setMessage(T message);
}
