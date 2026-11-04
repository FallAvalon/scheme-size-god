import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.zip.ZipFile;

/** Standalone release integrity check; runtime implementation is in the shared source repository.
 * Run with JDK 17+: java VerifyRelease.java /path/to/scheme-size-god.jar
 * This real Java utility also makes Mindustry 160.7 detect the storefront as a Java release repository.
 * No game libraries, downloaded code, credentials or installed profile are required.
 */
public final class VerifyRelease {
    static final String expectedSha256 = "e201e2f8b524ad75c4ef67261b08a30d4c4c5b0e207ad3bedbd9a29ca9a19fe3";
    static final String expectedMain = "org.schemetools.SchemeSizeGodMod";
    static final String expectedVersion = "0.5.1";
    public static void main(String[] args) throws Exception {
        Path jar = Path.of(args.length == 0 ? "scheme-size-god.jar" : args[0]);
        byte[] bytes = Files.readAllBytes(jar);
        String actual = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        if (!expectedSha256.equals(actual)) throw new IllegalArgumentException("JAR SHA-256 mismatch: " + actual);
        try (var zip = new ZipFile(jar.toFile())) {
            if (zip.getEntry(expectedMain.replace('.', '/') + ".class") == null)
                throw new IllegalArgumentException("Compiled entry point missing");
            if (zip.getEntry("icon.png") == null || zip.getEntry("mod.hjson") == null)
                throw new IllegalArgumentException("Root icon/metadata missing");
            String metadata = new String(zip.getInputStream(zip.getEntry("mod.hjson")).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            if (!metadata.contains("version: " + expectedVersion)) throw new IllegalArgumentException("Version mismatch");
        }
        System.out.println("PASS " + jar.getFileName() + " " + expectedVersion + " SHA-256 " + actual);
    }
}
