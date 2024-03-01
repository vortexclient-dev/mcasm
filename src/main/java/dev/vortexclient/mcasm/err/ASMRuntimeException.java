package dev.vortexclient.mcasm.err;

public class ASMRuntimeException extends RuntimeException {
    public ASMRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
