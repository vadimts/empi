package eu.tsvetkov.empi.util;

import eu.tsvetkov.empi.mp3.Mp3File;
import eu.tsvetkov.empi.ops.MusicOps;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.nio.charset.Charset.defaultCharset;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class FileUtil {

    public static final String SEP = File.separator;
    public static final String SEP_RE = "\\" + Util.SEP;
    public static final Path TMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));
    private static SLogger log = new SLogger();

    public static void appendLines(Path textFilePath, List<String> lines) {
        try {
            Files.write(textFilePath, lines, CREATE, APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Path copy(Path src, Path dest) {
        try {
            return Files.copy(src, dest);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean dirContainsPath(Path dir, List<Path> paths) {
        try {
            return Files.walk(dir).anyMatch(paths::contains);
        } catch (IOException e) {
            return false;
        }
    }

    public static int fileCount(Path dir) {
        List<Path> list = list(dir);
        return (list != null ? list.size() : -1);
    }

    public static List<Path> getAudioFilePaths(Path dir) {
        return getAudioFilePathsMatching(dir);
    }

    public static List<Path> getAudioFilePathsMatching(Path dir, Predicate<? super Path>... matchers) {
        return getFilePathsMatching(dir, file -> isAudioFile(file) && pathMatchesOR(file, matchers));
    }

    public static List<Mp3File> getAudioFiles(Path dir) {
        return getAudioFilePaths(dir).stream().map(MusicOps::getMp3File).collect(toList());
    }

    public static String getFileExtension(String file) {
        return file.substring(file.lastIndexOf(".") + 1);
    }

    public static String getFileExtension(Path file) {
        return getFileExtension(file.toString());
    }

    public static List<List<Path>> getFilePathsLists(Path dir, Predicate<Path>... matchers) {
        List<List<Path>> pathsLists = Stream.generate((Supplier<ArrayList<Path>>) ArrayList::new).limit(matchers.length).collect(toList());
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    for (int i = 0; i < matchers.length; i++) {
                        if (matchers[i].test(file)) {
                            pathsLists.get(i).add(file);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        pathsLists.forEach(Collections::sort);
        return pathsLists;
    }

    public static List<Path> getFilePathsMatching(Path dir, Predicate<? super Path>... matchers) {
        try {
            return Files.walk(dir)
                .filter(filePath -> pathMatchesOR(filePath, matchers))
                .sorted()
                .collect(toList());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Path getParentSubdir(Path filePath, Path baseDir) {
        return !filePath.startsWith(baseDir) ? null : baseDir.resolve(filePath.relativize(baseDir).getRoot());
    }

    public static Path getPath(Class<?> baseClass, String path) {
        try {
            URL resource = baseClass.getResource(path);
            return (resource != null ? Paths.get(resource.toURI()) : null);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static Path getPath(Path basePath, String subPath) {
        return basePath.resolve(subPath);
    }

    public static int getPathDepth(Path path) {
        int depth = path.toString().split(SEP).length;
        log.debug(Str.of("Path depth: ${1}").with(depth));
        return depth;
    }

    public static void getPathsListsDir(Path dir, Map<Path, DirPaths> map) {
        DirPaths dirPaths = new DirPaths();
        map.put(dir, dirPaths);
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (attrs.isDirectory()) {
                        getPathsListsDir(file, map);
                    } else {
                        dirPaths.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isAudioFile(Path filePath) {
        String pathLow = filePath.toString().toLowerCase();
        return (/*Files.isRegularFile(filePath) &&*/ (pathLow.endsWith(".mp3") /*|| pathLow.endsWith("m4a") || pathLow.endsWith("ogg")*/));
    }

    public static boolean isAudioFile(String filePath) {
        return isAudioFile(Paths.get(filePath));
    }

    public static boolean isDir(Path path) {
        return Files.isDirectory(path);
//        return path.endsWith(File.separator); - Paths.get() strips the last /
    }

    public static boolean isDirEmpty(Path dir) {
        try {
            return Files.list(dir).count() == 0;
        } catch (IOException e) {
            return true;
        }
    }

    public static boolean isPictureFile(Path filePath) {
        String pathLow = filePath.toString().toLowerCase();
        return (/*Files.isRegularFile(filePath) &&*/ pathLow.endsWith(".jpg") || pathLow.endsWith(".jpeg") || pathLow.endsWith(".png") || pathLow.endsWith(".gif") || (pathLow.endsWith(".bmp") || pathLow.endsWith(".tiff")));
    }

    public static List<Path> list(Path dir) {
        try {
            return Files.list(dir).collect(toList());
        } catch (IOException e) {
            log.error(e);
            return null;
        }
    }

    public static Path mkdir(Path dir, String... subDir) {
        try {
            return Files.createDirectories(Paths.get(dir.toString(), subDir));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void mv(Path path, Path destination) {
        try {
            Files.move(path, destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void mvToDir(Path path, Path destination) {
        mv(path, destination.resolve(path.getFileName()));
    }

    public static boolean pathMatchesOR(Path file, Predicate<? super Path>[] matchers) {
        if (matchers.length == 0) {
            return true;
        }
        for (int i = 0; i < matchers.length; i++) {
            if (matchers[i].test(file)) {
                return true;
            }
        }
        return false;
    }

    public static List<Path> readFilePaths(Path file) {
        try {
            return Files.readAllLines(file, defaultCharset()).stream().filter(x -> !x.trim().equals("") && !x.startsWith("#")).map(Paths::get).collect(toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }
    }

    public static void rmDir(Path path) {
        try {
            if (path != null && Files.exists(path)) {
                Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeLines(Path textFilePath, List<String> lines) {
        try {
            Files.write(textFilePath, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeLines(Path textFilePath, String lines) {
        writeLines(textFilePath, asList(lines));
    }


    static class DirPaths {
        List<Path> audioFiles = new ArrayList<>();
        List<Path> pictureFiles = new ArrayList<>();

        public void add(Path file) {
            if (isAudioFile(file)) {
                audioFiles.add(file);
            } else if (isPictureFile(file)) {
                pictureFiles.add(file);
            }
        }
    }
}
