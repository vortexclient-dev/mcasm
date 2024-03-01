package dev.vortexclient.mcasm.java;

import dev.vortexclient.mcasm.assembler.Assembler;
import dev.vortexclient.mcasm.linker.ASMExecutionEvent;
import dev.vortexclient.mcasm.linker.ASMLinker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class JavaEntrypoint {
    public static void main(String[] args) {
        Logger LOGGER = LoggerFactory.getLogger("ASM");
        String assembled = "";
        final String URL = "https://seraph.nwlee.tech/sample.asm";

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL)).build();

            LOGGER.info("Fetching file from Seraph");
            String data = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

            assembled = Assembler.assemble(data);

        } catch (Exception e) {
            LOGGER.error("Failed to fetch", e);
        }
        LOGGER.info("Fetched file successfully");
        LOGGER.info("Assembled file");
        LOGGER.info("Linking file");
        ASMLinker linker = new ASMLinker();
        linker.link(assembled);
        linker.executeEvent(ASMExecutionEvent.INITIALIZE);
    }
}
