package com.omkar.controller.ui.bluetooth;

public class ConnectionException extends Exception{
    public ConnectionException(String message) {
        super(message);
    }

    public enum ErrorMessage {
        BLUETOOTH_DISCONNECTED("Bluetooth is not connected"),
        UNEXPECTED_EXCEPTION("An unexpected exception has occurred");
        private String errorMessage;
        ErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        public String getErrorMessage() {
            return this.errorMessage;
        }
    }
}


