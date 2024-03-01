package dev.vortexclient.mcasm.executor;

import dev.vortexclient.mcasm.err.ASMRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class ASMRuntime {
    private static final Logger LOGGER = LogManager.getLogger("ASM Runtime");

    // Need stacks for accumulator, data, count
    private Stack<Integer> accumulator = new Stack<>();
    private Stack<Integer> data = new Stack<>();
    private Stack<Integer> count = new Stack<>();

    // Now stacks for output: console, render, and world
    private Stack<Integer> console = new Stack<>();
    private Stack<Integer> render = new Stack<>();
    private Stack<Integer> world = new Stack<>();

    // Now stacks for input: world, keyboard, and mouse
    private Stack<Integer> worldInput = new Stack<>();
    private Stack<Integer> keyboardInput = new Stack<>();
    private Stack<Integer> mouseInput = new Stack<>();

    private String getStackLocation(String identifier) {
        return switch (identifier) {
            case "eax" -> "0x0000";
            case "ebx" -> "0x1000";
            case "ecx" -> "0x2000";
            case "eco" -> "0x3000";
            case "ero" -> "0x4000";
            case "ewo" -> "0x5000";
            case "ewi" -> "0x6000";
            case "eki" -> "0x7000";
            case "emi" -> "0x8000";
            default -> "0xA000";
        };
    }

    public ASMRuntime() {
        // Initialize the stacks
        accumulator.push(0);
        data.push(0);
        count.push(0);
        console.push(0);
        render.push(0);
        world.push(0);
        worldInput.push(0);
        keyboardInput.push(0);
        mouseInput.push(0);
    }
    private int poll(String identifier) {
        // Poll the stack. Does not pop a value off the stack
        return switch (identifier) {
            case "eax" -> accumulator.peek();
            case "ebx" -> data.peek();
            case "ecx" -> count.peek();
            case "eco" -> console.peek();
            case "ero" -> render.peek();
            case "ewo" -> world.peek();
            case "ewi" -> worldInput.peek();
            case "eki" -> keyboardInput.peek();
            case "emi" -> mouseInput.peek();
            default -> throw new ASMRuntimeException("Failed to poll from stack " + identifier,
                    new NullPointerException(
                            "Segmentation fault: stack at "
                                    + getStackLocation(identifier)
                                    + " not found"
                    )
            );
        };
    }
    private int pop(String identifier) {
        // Pop the stack. Pops a value off the stack
        return switch (identifier) {
            case "eax" -> accumulator.pop();
            case "ebx" -> data.pop();
            case "ecx" -> count.pop();
            case "eco" -> console.pop();
            case "ero" -> render.pop();
            case "ewo" -> world.pop();
            case "ewi" -> worldInput.pop();
            case "eki" -> keyboardInput.pop();
            case "emi" -> mouseInput.pop();
            default -> throw new ASMRuntimeException("Failed to pop from stack " + identifier,
                    new NullPointerException(
                            "Segmentation fault: stack at "
                                    + getStackLocation(identifier)
                                    + " not found"
                    )
            );
        };
    }

    public void mov(String identifier, int value) {
        switch (identifier) {
            case "eax":
                accumulator.push(value);
                break;
            case "ebx":
                data.push(value);
                break;
            case "ecx":
                count.push(value);
                break;
            case "eco":
                console.push(value);
                break;
            case "ero":
                render.push(value);
                break;
            case "ewo":
                world.push(value);
                break;
            case "ewi":
                worldInput.push(value);
                break;
            case "eki":
                keyboardInput.push(value);
                break;
            case "emi":
                mouseInput.push(value);
                break;
            default:
                throw new ASMRuntimeException("Failed to move value to stack " + identifier,
                        new NullPointerException(
                                "Segmentation fault: stack at "
                                        + getStackLocation(identifier)
                                        + " not found"
                        )
                );
        }
    }

    public void mul(String identifier, int scalar) {
        mov(identifier, poll(identifier) * scalar);
    }
    public void add(String identifier, int scalar) {
        mov(identifier, poll(identifier) + scalar);
    }

    public void interrupt(int $int) {
        switch ($int) {
            case 0x00:
                endSysCall(pop("eax") - 1);
                break;
            case 0x01:
                int numBytes = pop("eax");
                for (int i = 0; i < numBytes; i++) {
                    mov("eco", pop("ebx"));
                }
                break;
            case 0x02:
                int numBytes2 = pop("eax");
                for (int i = 0; i < numBytes2; i++) {
                    mov("ero", pop("ebx"));
                }
                break;
            case 0x03:
                int numBytes3 = pop("eax");
                for (int i = 0; i < numBytes3; i++) {
                    mov("ewo", pop("ebx"));
                }
                break;
            case 0x04:
                int numBytes4 = pop("eax");
                for (int i = 0; i < numBytes4; i++) {
                    mov("ewi", pop("ebx"));
                }
                break;
            case 0x05:
                int numBytes5 = pop("eax");
                for (int i = 0; i < numBytes5; i++) {
                    mov("eki", pop("ebx"));
                }
                break;
            case 0x06:
                int numBytes6 = pop("eax");
                for (int i = 0; i < numBytes6; i++) {
                    mov("emi", pop("ebx"));
                }
                break;
            case 0x07:
                // Console output from eco
                StringBuilder consoleOutput = new StringBuilder();
                while(console.size() != 1) {
                    int value = pop("eco");
                    consoleOutput.append((char) value);
                }
                System.out.print(consoleOutput);
        }
    }

    private void endSysCall(int code) {
        if (code != 0) {
            LOGGER.error(
                    "System call failed",
                    new ASMRuntimeException(
                            "Failed to call interrupt 0x00 when reading memory from register " + getStackLocation("eax"),
                            new IllegalStateException("Expected exit code 0, found exit code " + code)
                    )
            );
        }
    }
}
