package dev.vortexclient.mcasm.linker;

import dev.vortexclient.mcasm.assembler.ASMPointer;
import dev.vortexclient.mcasm.err.ASMLinkingException;
import dev.vortexclient.mcasm.executor.ASMRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class ASMLinker {
    static final Logger LOGGER = LoggerFactory.getLogger("ASM Linker");
    private final ASMRuntime runtime = new ASMRuntime();
    private final int[] stack = new int[256];
    private ArrayList<String> lines;
    private final ArrayList<ASMPointer> pointers = new ArrayList<>();
    public void link(String data) {
        lines = new ArrayList<>(Arrays.asList(data.split("\n")))
                .stream()
                .filter(s -> !s.isEmpty())
                .map(String::trim)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        int dataSection = lines.indexOf(".section data");

        int p = 0;
        // Start at data to find immutable data that can be immediately pushed onto the stack
        for (int i = dataSection + 1; !lines.get(i).equals("---"); i++) {
            String line = lines.get(i);
            String[] tokens = line.split(" ");
            // First token is the name
            String name = tokens[0];
            // Second token is the storing method: db or equ
            String method = tokens[1];
            if (method.equals("db")) {
                // Store data and push a new pointer with the name onto the stack
                int remainingTokens = tokens.length - 2;
                int origin = p;
                for (int j = 0; j < remainingTokens; j++) {
                    String token = tokens[j + 2];
                    int value;
                    if (token.startsWith("0x")) {
                        value = Integer.parseInt(token.substring(2), 16);
                    } else {
                        value = Integer.parseInt(token);
                    }
                    stack[p++] = value;
                }
                pointers.add(new ASMPointer(origin, name, remainingTokens));
                // Add a null terminator
                stack[p++] = -1;
            } else if (method.equals("equ")) {
                // Verify syntax
                if (!tokens[2].equals("$") || !tokens[3].equals("-")) {
                    throw new ASMLinkingException("Invalid syntax for equ: " + line);
                }
                // Name of data to search fore
                String search = tokens[4];
                // Find the pointer
                ASMPointer pointer = pointers.stream().filter(x -> x.name().equals(search)).findFirst().orElse(null);
                if (pointer == null) {
                    throw new ASMLinkingException("Pointer not found: " + search);
                }
                // Store the size
                int size = pointer.size();
                // Push the pointer onto the stack
                stack[++p] = size;
                pointers.add(new ASMPointer(p, name, 1));
            }
        }
    }

    public void executeEvent(ASMExecutionEvent event) {
        switch (event) {
            case INITIALIZE -> {
                int start = -1;
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).startsWith("event_init")) {
                        start = i;
                        break;
                    }
                }
                if (start == -1) {
                    LOGGER.warn("Failed to find initialization event to run");
                    return;
                }
                int i = start;
                while (!lines.get(++i).contains("---")) {
                    String line = lines.get(i);
                    String[] tokens = line.split(" ");
                    switch (tokens[0]) {
                        case "mov" -> {
                            String identifier = tokens[1];
                            int[] values = parse(tokens[2]);
                            for (int value : values) {
                                runtime.mov(identifier, value);
                            }
                        }
                        case "add" -> {
                            String identifier = tokens[1];
                            int[] values = parse(tokens[2]);
                            if (values.length != 1) {
                                throw new ASMLinkingException("Invalid number of arguments for add: Found " + values.length + ", expected 1");
                            }
                            for (int value : values) {
                                runtime.add(identifier, value);
                            }
                        }
                        case "mul" -> {
                            String identifier = tokens[1];
                            int[] values = parse(tokens[2]);
                            if (values.length != 1) {
                                throw new ASMLinkingException("Invalid number of arguments for mul: Found " + values.length + ", expected 1");
                            }
                            for (int value : values) {
                                runtime.mul(identifier, value);
                            }
                        }
                        case "int" -> {
                            int[] values = parse(tokens[1]);
                            if (values.length != 1) {
                                throw new ASMLinkingException("Invalid number of arguments for int: Found " + values.length + ", expected 1");
                            }
                            for (int value : values) {
                                runtime.interrupt(value);
                            }
                        }
                    }
                }
            }
        }
    }
    private int[] parse(String hexOrInt) {
        // If it isn't a hex number or an int, try to find the pointer
        if (hexOrInt.startsWith("0x")) {
            return new int[] {Integer.parseInt(hexOrInt.substring(2), 16)};
        } else if (isAllNumbers(hexOrInt)) {
            return new int[] {Integer.parseInt(hexOrInt)};
        } else {
            ASMPointer pointer = pointers.stream().filter(x -> x.name().equals(hexOrInt)).findFirst().orElse(null);
            if (pointer == null) {
                throw new ASMLinkingException("Pointer not found: " + hexOrInt);
            }
            ArrayList<Integer> values = new ArrayList<>();
            for (int i = pointer.location(); i < pointer.location() + pointer.size(); i++) {
                values.add(stack[i]);
            }
            int[] ret = new int[values.size()];
            for (int i = 0; i < values.size(); i++) {
                ret[i] = values.get(i);
            }
            return ret;
        }
    }

    private boolean isAllNumbers(String s) {
        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }
}
