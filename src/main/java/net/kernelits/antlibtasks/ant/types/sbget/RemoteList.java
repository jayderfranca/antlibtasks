package net.kernelits.antlibtasks.ant.types.sbget;

import org.apache.tools.ant.types.DataType;

import java.io.File;

/**
 * Define uma lista WSDLs que serao processados pela task SBGet
 *
 * @author Jayder França <a href="mailto:jayderfranca@gmail.com">jayderfranca@gmail.com</a>
 */
public class RemoteList extends DataType {

    // variaveis
    File file = null;

    // sets
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
