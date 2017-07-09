package net.kernelits.antlibtasks.utils;

import junit.framework.TestCase;

import java.io.File;

/**
 * Funcoes utilitarias de arquivo
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class FileUtilsTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testDeleteWithNullParameter() throws Exception {
        assertEquals(false, FileUtils.delete(null));
    }

    public void testDeleteFileNotExists() throws Exception {
        assertEquals(false, FileUtils.delete(new File("/tmp/notexists")));
    }

    public void testDeleteFile() throws Exception {
        File file = File.createTempFile( "DeleteTestFile_", ".txt", new File("/tmp"));
        assertEquals(true, FileUtils.delete(file));
        assertEquals(false, file.exists());
    }

    public void testeDeleteEmptyDirectory() throws Exception {
        File topDir = new File("/tmp/deletetest");
        topDir.mkdirs();
        assertEquals(true, topDir.exists());
        assertEquals(true, FileUtils.delete(topDir));
        assertEquals(false, topDir.exists());
    }

    public void testDeleteDirectory() throws Exception {
        File topDir = new File("/tmp/deletetest");
        topDir.mkdirs();
        File subDir = new File(topDir, "subdir");
        subDir.mkdirs();
        File subdirFile = File.createTempFile("DeleteTestFileSubDir_", ".txt", subDir);
        File topFile = File.createTempFile("DeleteTestFileTopDir_", ".txt", topDir);
        assertEquals(true, topDir.exists());
        assertEquals(true, topFile.exists());
        assertEquals(true, subDir.exists());
        assertEquals(true, subdirFile.exists());
        assertEquals(true, FileUtils.delete(topDir));
        assertEquals(false, subdirFile.exists());
        assertEquals(false, topFile.exists());
        assertEquals(false, subDir.exists());
        assertEquals(false, topDir.exists());
    }

    public void testListFiles() throws Exception {
    }

    public void testUnzip() throws Exception {
    }

    public void testGetFileExtension() throws Exception {
    }

    public void testReadAll() throws Exception {
    }
}
