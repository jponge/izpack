package com.izforge.izpack.merge;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
public class MergeHelper {
    /**
     * Return the mergeable from the given path.
     *
     * @param path path of the mergeable
     * @return Mergeable if found. null else.
     */
    public static Mergeable getMergeableFromPath(String path) {
        TypeFile file = MergeManagerImpl.resolvePath(path);
        switch (file) {
            case DIRECTORY:
            case FILE:
                return new FileMerge(MergeManagerImpl.getFileFromPath(path), path);
            case JAR_CONTENT:
                return new JarMerge(path);
        }
        return null;
    }

    public static Mergeable getMergeableFromPath(String path, String destination) {
        TypeFile file = MergeManagerImpl.resolvePath(path);
        switch (file) {
            case DIRECTORY:
            case FILE:
                return new FileMerge(MergeManagerImpl.getFileFromPath(path), destination);
            case JAR_CONTENT:
                return new JarMerge(path, destination);
        }
        return null;
    }
}
