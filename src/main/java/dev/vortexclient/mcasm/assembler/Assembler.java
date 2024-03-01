package dev.vortexclient.mcasm.assembler;

public class Assembler {
    public static String assemble(String fileContent) {
        StringBuilder ret = new StringBuilder();

        String[] lines = fileContent.split("\n");
        boolean inSection = false;
        for (String line : lines) {
            if (line.startsWith(".section")) {
                if (inSection) ret.append("---\n");
                inSection = true;
            }
            boolean inComment = false;
            for (char c : line.toCharArray()) {
                if (c == ';') {
                    inComment = true;
                    break;
                }
                ret.append(c);
            }
            ret.append("\n");
        }
        // Remove empty lines
        String[] lines2 = ret.toString().split("\n");
        ret = new StringBuilder();
        for (String line : lines2) {
            if (!line.isEmpty()) ret.append(line).append("\n");
        }
        ret.append("---\n");
        return ret.toString();
    }
}
