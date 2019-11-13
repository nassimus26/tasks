package org.nassimus.auto;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class Main {
    public static void main(String[] args) {
        File tasksfile = readFileFromClasspath("tasks.txt");
        try {
            List<String> tasks = Files.readAllLines(tasksfile.toPath());
            for ( String task : tasks ) {
                String[] segments = replaceEnvVars(task).split(" ");
                if (segments.length==0)
                    continue;
                switch (segments[0]) {
                    case "replaceAll" : replaceAll(Paths.get(segments[1]), Arrays.copyOfRange(segments, 2, segments.length));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void replaceAll(Path path, String... args) {
        try {
            String content = Files.readString(path);
            String[] toReplace = new String[args.length/2];
            String[] replaceValues = new String[args.length/2];
            for ( int i=0; i<args.length/2; i++ ) {
                toReplace[i] = args[i*2];
                replaceValues[i] = args[i*2+1];
            }

            String ctn = StringUtils.replaceEach(content, toReplace, replaceValues);
            Files.write(path, ctn.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String replaceEnvVars(String text) {
        String pattern = "\\$\\{([A-Za-z0-9-]+)\\}";
        Pattern expr = Pattern.compile(pattern);
        Matcher matcher = expr.matcher(text);
        while (matcher.find()) {
            String key = matcher.group(1);
            String envValue = System.getProperty(key, System.getenv().getOrDefault(key, System.getenv().get(key.toUpperCase())));
            if (envValue==null)
                throw new RuntimeException("Missed env value "+key);
            if (envValue == null) {
                envValue = "";
            } else {
                envValue = envValue.replace("\\", "\\\\");
            }
            Pattern subexpr = Pattern.compile(Pattern.quote(matcher.group(0)));
            text = subexpr.matcher(text).replaceAll(envValue);
        }
        return text;
    }

    public static File readFileFromClasspath(String filename) {
        URL fileUrl = Main.class.getClassLoader().getResource(filename);
        return new File(fileUrl.getFile());
    }

}
