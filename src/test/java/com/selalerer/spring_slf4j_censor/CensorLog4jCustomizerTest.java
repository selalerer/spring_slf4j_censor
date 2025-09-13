package com.selalerer.spring_slf4j_censor;

import com.selalerer.censor.Censor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
public class CensorLog4jCustomizerTest {

    @Configuration
    @ComponentScan("com.selalerer")
    public static class Conf {
    }

    @Autowired
    private Censor censor;

    @Test
    public void testUncensoredLog() {
        censor.uncensor("password1");
        var output = captureOutput(() -> log.info("User: user1, Password: password1"));
        assertTrue(output.contains("User: user1, Password: password1"));
    }

    @Test
    public void testCensoredLog() {
        censor.censor("password1");
        var output = captureOutput(() -> log.info("User: user1, Password: password1"));
        assertTrue(output.contains("User: user1, Password: ****"));
        assertFalse(output.contains("password1"));
    }

    private String captureOutput(Runnable r) {
        var output = new ByteArrayOutputStream();
        var regularOut = System.out;
        System.setOut(new PrintStream(output));
        try {
            r.run();
        } finally {
            System.setOut(regularOut);
        }
        var s = output.toString();
        System.out.print(s);
        return s;
    }
}